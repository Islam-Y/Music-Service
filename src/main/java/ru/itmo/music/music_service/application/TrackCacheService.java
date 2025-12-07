package ru.itmo.music.music_service.application;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.itmo.music.music_service.api.dto.StreamUrlResponse;
import ru.itmo.music.music_service.api.dto.TrackDetailsResponse;
import ru.itmo.music.music_service.api.dto.TrackFactsResponse;

import java.time.Duration;
import java.util.Optional;

/**
 * Simple Redis cache for track metadata and stream URLs.
 */
@Service
@AllArgsConstructor
public class TrackCacheService {

    private static final Logger log = LoggerFactory.getLogger(TrackCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.track-ttl-seconds:600}")
    private long trackTtlSeconds;

    @Value("${app.cache.stream-url-ttl-seconds:300}")
    private long streamUrlTtlSeconds;

    @Value("${app.cache.facts-ttl-seconds:1800}")
    private long factsTtlSeconds;

    public Optional<TrackDetailsResponse> getTrack(String trackId) {
        String key = getTrackKey(trackId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, TrackDetailsResponse.class));
        } catch (JacksonException e) {
            log.warn("Failed to deserialize track cache for {}", trackId, e);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void putTrack(String trackId, TrackDetailsResponse track) {
        try {
            redisTemplate.opsForValue().set(getTrackKey(trackId),
                    objectMapper.writeValueAsString(track),
                    Duration.ofSeconds(trackTtlSeconds));
        } catch (JacksonException e) {
            log.warn("Failed to serialize track cache for {}", trackId, e);
        }
    }

    public void evictTrack(String trackId) {
        redisTemplate.delete(getTrackKey(trackId));
    }

    public Optional<StreamUrlResponse> getStreamUrl(String trackId) {
        String key = getStreamUrlKey(trackId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, StreamUrlResponse.class));
        } catch (JacksonException e) {
            log.warn("Failed to deserialize stream url cache for {}", trackId, e);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void putStreamUrl(String trackId, StreamUrlResponse streamUrl) {
        try {
            redisTemplate.opsForValue().set(getStreamUrlKey(trackId),
                    objectMapper.writeValueAsString(streamUrl),
                    Duration.ofSeconds(streamUrlTtlSeconds));
        } catch (JacksonException e) {
            log.warn("Failed to serialize stream url cache for {}", trackId, e);
        }
    }

    public void evictStreamUrl(String trackId) {
        redisTemplate.delete(getStreamUrlKey(trackId));
    }

    public Optional<TrackFactsResponse> getTrackFacts(String trackId) {
        String key = getFactsKey(trackId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, TrackFactsResponse.class));
        } catch (JacksonException e) {
            log.warn("Failed to deserialize track facts cache for {}", trackId, e);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void putTrackFacts(String trackId, TrackFactsResponse facts) {
        try {
            redisTemplate.opsForValue().set(getFactsKey(trackId),
                    objectMapper.writeValueAsString(facts),
                    Duration.ofSeconds(factsTtlSeconds));
        } catch (JacksonException e) {
            log.warn("Failed to serialize track facts cache for {}", trackId, e);
        }
    }

    public void evictTrackFacts(String trackId) {
        redisTemplate.delete(getFactsKey(trackId));
    }

    private String getTrackKey(String trackId) {
        return "track:" + trackId;
    }

    private String getStreamUrlKey(String trackId) {
        return "track:" + trackId + ":streamUrl";
    }

    private String getFactsKey(String trackId) {
        return "track:" + trackId + ":facts";
    }
}
