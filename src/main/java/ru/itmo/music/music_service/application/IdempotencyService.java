package ru.itmo.music.music_service.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final long ttlSeconds;
    private final ObjectMapper objectMapper;

    public IdempotencyService(StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper,
                              @Value("${app.idempotency.track-ttl-seconds:86400}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<IdempotencyEntry> getEntry(String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(key(idempotencyKey));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, IdempotencyEntry.class));
        } catch (JacksonException e) {
            redisTemplate.delete(key(idempotencyKey));
            return Optional.empty();
        }
    }

    public void saveEntry(String idempotencyKey, String trackId, Object payload) {
        try {
            String payloadHash = hashPayload(payload);
            IdempotencyEntry entry = new IdempotencyEntry(trackId, payloadHash);
            redisTemplate.opsForValue().set(key(idempotencyKey),
                    objectMapper.writeValueAsString(entry),
                    Duration.ofSeconds(ttlSeconds));
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize idempotency entry", e);
        }
    }

    private String key(String idempotencyKey) {
        return "idemp:track:" + idempotencyKey;
    }

    public String hashPayload(Object payload) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (JacksonException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash idempotency payload", e);
        }
    }

    public record IdempotencyEntry(String trackId, String payloadHash) {}
}
