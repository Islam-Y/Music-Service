package ru.itmo.music.music_service.infrastructure.messaging.domain;

import java.time.Instant;

public record TrackDomainEventPayload(
        String version,
        String eventType, // created/updated/deleted
        String trackId,
        String title,
        String artist,
        Long durationMs,
        Integer year,
        Boolean explicit,
        String audioStorageKey,
        Instant timestamp,
        String changedFields
) {
}
