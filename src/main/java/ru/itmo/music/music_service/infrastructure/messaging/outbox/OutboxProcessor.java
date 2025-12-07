package ru.itmo.music.music_service.infrastructure.messaging.outbox;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.messaging.kafka.FactsTopicsProperties;
import ru.itmo.music.music_service.infrastructure.persistence.OutboxRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.OutboxMessageEntity;
import ru.itmo.music.music_service.infrastructure.persistence.entity.enums.OutboxStatus;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FactsTopicsProperties topics;

    @Value("${app.outbox.max-attempts:5}")
    private int maxAttempts;
    @Value("${app.outbox.dlq-topic:music.outbox.dlq}")
    private String outboxDlqTopic;
    @Value("${app.outbox.retry-failed-interval-ms:300000}")
    private long retryFailedIntervalMs;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void processOutbox() {
        List<OutboxMessageEntity> messages = outboxRepository.findReady(OutboxStatus.READY, Instant.now());
        for (OutboxMessageEntity message : messages) {
            kafkaTemplate.executeInTransaction(ops -> {
                sendMessage(message);
                return null;
            });
        }
        outboxRepository.saveAll(messages);
    }

    @Scheduled(fixedDelayString = "${app.outbox.retry-failed-interval-ms:300000}")
    public void retryFailed() {
        List<OutboxMessageEntity> failed = outboxRepository.findTop100ByStatusOrderByUpdatedAtAsc(OutboxStatus.FAILED);
        if (failed.isEmpty()) {
            return;
        }
        log.info("Retrying {} failed outbox messages", failed.size());
        for (OutboxMessageEntity message : failed) {
            message.setStatus(OutboxStatus.READY);
            message.setAttempts(0);
            message.setAvailableAt(Instant.now());
        }
        outboxRepository.saveAll(failed);
    }

    private long backoffSeconds(int attempts) {
        return Math.min(60, attempts * 5L);
    }

    private String resolveTopic(OutboxMessageEntity message) {
        if ("track".equals(message.getAggregateType())) {
            return topics.getTrackDomain();
        }
        if ("facts".equals(message.getAggregateType())) {
            return topics.getFactsEventsOutbox();
        }
        return topics.getTrackDomain();
    }

    private void sendMessage(OutboxMessageEntity message) {
        try {
            String topic = resolveTopic(message);
            kafkaTemplate.executeInTransaction(ops -> {
                org.apache.kafka.clients.producer.ProducerRecord<String, Object> record =
                        new org.apache.kafka.clients.producer.ProducerRecord<>(topic, message.getAggregateId(), message.getPayload());
                if (message.getTraceId() != null) {
                    record.headers().add("traceId", message.getTraceId().getBytes());
                }
                if (message.getCorrelationId() != null) {
                    record.headers().add("correlationId", message.getCorrelationId().getBytes());
                }
                ops.send(record);
                return null;
            });
            message.setStatus(OutboxStatus.SENT);
        } catch (Exception ex) {
            log.warn("Failed to send outbox message id={} aggregateId={}, attempt={}", message.getId(), message.getAggregateId(), message.getAttempts(), ex);
            int attempts = message.getAttempts() + 1;
            message.setAttempts(attempts);
            if (attempts >= maxAttempts) {
                message.setStatus(OutboxStatus.FAILED);
                try {
                    kafkaTemplate.send(outboxDlqTopic, message.getAggregateId(), message.getPayload());
                } catch (Exception dlqEx) {
                    log.error("Failed to send outbox message to DLQ id={} aggregateId={}", message.getId(), message.getAggregateId(), dlqEx);
                }
            } else {
                message.setAvailableAt(Instant.now().plusSeconds(backoffSeconds(attempts)));
            }
        }
    }
}
