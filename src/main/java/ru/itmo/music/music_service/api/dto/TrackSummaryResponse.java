package ru.itmo.music.music_service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackSummaryResponse(
        String id,
        String title,
        String artist,
        String coverUrl,
        Long durationMs,
        Integer year,
        boolean explicit
) {
}
