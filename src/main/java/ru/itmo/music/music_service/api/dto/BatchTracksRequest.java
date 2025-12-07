package ru.itmo.music.music_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchTracksRequest(
        @NotEmpty
        @Size(max = 500)
        List<@NotBlank String> ids
) {
}
