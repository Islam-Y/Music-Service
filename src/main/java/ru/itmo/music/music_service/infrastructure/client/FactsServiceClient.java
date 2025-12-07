package ru.itmo.music.music_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.messaging.outbox.FactsOutboxPayload;

/**
 * Stub client for Facts Service. Replace with real HTTP/Kafka call when available.
 */
@Component
@RequiredArgsConstructor
public class FactsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FactsServiceClient.class);

    public void sendFactsEvent(FactsOutboxPayload payload) {
        // TODO: replace with real call to Facts Service (HTTP/Kafka)
        log.info("Stub: sending facts event to Facts Service eventType={} trackId={}", payload.eventType(), payload.trackId());
    }
}
