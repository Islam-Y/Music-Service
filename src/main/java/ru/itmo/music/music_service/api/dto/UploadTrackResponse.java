package ru.itmo.music.music_service.api.dto;

import java.time.Instant;

public record UploadTrackResponse(
        String storageKey,
        String uploadUrl,
        Instant expiresAt
) {
}
