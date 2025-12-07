package ru.itmo.music.music_service.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.messaging.outbox.FactsOutboxPayload;
import ru.itmo.music.music_service.infrastructure.client.FactsServiceClient;

/**
 * Stub consumer for facts outbox events. In real deployment, Facts Service should consume this topic.
 */
@Component
@RequiredArgsConstructor
public class FactsEventsOutboxConsumer {

    private static final Logger log = LoggerFactory.getLogger(FactsEventsOutboxConsumer.class);
    private final FactsServiceClient factsServiceClient;

    @KafkaListener(
            topics = "#{factsTopicsProperties.factsEventsOutbox}",
            groupId = "music-service-facts-outbox"
    )
    public void onFactsOutboxEvent(FactsOutboxPayload payload) {
        // Placeholder: forward to Facts Service via client
        log.info("Received facts outbox event: type={} trackId={} priority={} version={}",
                payload.eventType(), payload.trackId(), payload.priority(), payload.version());
        factsServiceClient.sendFactsEvent(payload);
    }
}
