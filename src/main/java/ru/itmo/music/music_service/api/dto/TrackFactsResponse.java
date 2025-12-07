package ru.itmo.music.music_service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackFactsResponse(
        String trackId,
        String factsJson,
        Instant generatedAt,
        Instant updatedAt
) {
}
