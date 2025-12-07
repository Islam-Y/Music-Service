package ru.itmo.music.music_service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TracksSearchResponse(
        List<TrackSummaryResponse> tracks,
        Integer total
) {
}
