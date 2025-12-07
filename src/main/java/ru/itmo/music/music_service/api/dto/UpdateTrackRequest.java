package ru.itmo.music.music_service.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Year;

/**
 * Partial update payload. All fields optional; at least one should be provided (checked in service).
 */
public record UpdateTrackRequest(
        @Size(max = 255)
        String title,
        @Size(max = 255)
        String artist,
        @Min(1)
        Long durationMs,
        @Min(1900) @Max(Year.MAX_VALUE)
        Integer year,
        Boolean explicit,
        @Size(max = 1024)
        String coverUrl
) {
}
