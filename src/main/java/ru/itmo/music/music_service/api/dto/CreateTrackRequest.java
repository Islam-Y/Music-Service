package ru.itmo.music.music_service.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Year;

public record CreateTrackRequest(
        @NotBlank @Size(max = 255)
        String title,
        @NotBlank @Size(max = 255)
        String artist,
        @NotNull @Min(1)
        Long durationMs,
        @Min(1900) @Max(Year.MAX_VALUE) // actual upper bound enforced in service
        Integer year,
        boolean explicit,
        @NotBlank @Size(max = 1024)
        String fileLocation,
        @Size(max = 1024)
        String coverUrl
) {
}
