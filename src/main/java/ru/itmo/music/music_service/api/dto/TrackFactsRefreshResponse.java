package ru.itmo.music.music_service.api.dto;

import java.time.Instant;

public record TrackFactsRefreshResponse(
        String trackId,
        String status,
        Instant requestedAt
) {
}
