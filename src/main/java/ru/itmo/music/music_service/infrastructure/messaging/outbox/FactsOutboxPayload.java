package ru.itmo.music.music_service.infrastructure.messaging.outbox;

import java.time.Instant;

public record FactsOutboxPayload(
        String version,
        String eventType, // created/updated/deleted/refresh
        String trackId,
        Integer priority,
        Instant timestamp
) {
}
