package ru.itmo.music.music_service.infrastructure.messaging.outbox;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.music.music_service.infrastructure.persistence.OutboxRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.OutboxMessageEntity;
import ru.itmo.music.music_service.infrastructure.persistence.entity.enums.OutboxStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Persists domain events in the outbox table with trace metadata for later asynchronous delivery.
 */
@Service
@AllArgsConstructor
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final TraceContextProvider traceContextProvider;

    @Transactional
    public void enqueue(String aggregateType, String aggregateId, String eventType, Object payload, String traceId) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String effectiveTraceId = traceId != null ? traceId : traceContextProvider.currentTraceId();
            String correlationId = traceContextProvider.currentCorrelationId();
            OutboxMessageEntity entity = OutboxMessageEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .status(OutboxStatus.READY)
                    .attempts(0)
                    .availableAt(Instant.now())
                    .traceId(effectiveTraceId)
                    .correlationId(correlationId)
                    .build();
            outboxRepository.save(entity);
        } catch (JacksonException e) {
            log.error("Failed to serialize outbox payload for aggregate {}", aggregateId, e);
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
