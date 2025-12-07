package ru.itmo.music.music_service.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Logs messages that land in outbox DLQ for manual inspection or alerting.
 */
@Component
@RequiredArgsConstructor
public class OutboxDlqListener {

    private static final Logger log = LoggerFactory.getLogger(OutboxDlqListener.class);

    @KafkaListener(topics = "${app.outbox.dlq-topic:music.outbox.dlq}", groupId = "music-service-outbox-dlq")
    public void onOutboxDlq(String payload) {
        log.error("Received outbox DLQ message payload={}", payload);
    }
}
