package ru.itmo.music.music_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadTrackRequest(
        @NotBlank @Size(max = 255)
        String fileName,
        @NotBlank @Size(max = 64)
        String contentType
) {
}
