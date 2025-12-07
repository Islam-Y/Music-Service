package ru.itmo.music.music_service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackDetailsResponse(
        String id,
        String title,
        String artist,
        String coverUrl,
        Long durationMs,
        Integer year,
        boolean explicit,
        String audioStorageKey,
        Instant createdAt,
        Instant updatedAt
) {
}
