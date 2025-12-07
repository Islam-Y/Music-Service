package ru.itmo.music.music_service.api.internal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.music.music_service.api.dto.BatchTracksRequest;
import ru.itmo.music.music_service.api.dto.BatchTracksResponse;
import ru.itmo.music.music_service.api.dto.CreateTrackRequest;
import ru.itmo.music.music_service.api.dto.CreateTrackResponse;
import ru.itmo.music.music_service.api.dto.StreamUrlResponse;
import ru.itmo.music.music_service.api.dto.TrackDetailsResponse;
import ru.itmo.music.music_service.api.dto.UpdateTrackRequest;
import ru.itmo.music.music_service.api.dto.TracksSearchResponse;
import ru.itmo.music.music_service.api.dto.UploadTrackRequest;
import ru.itmo.music.music_service.api.dto.UploadTrackResponse;
import ru.itmo.music.music_service.application.TrackService;

/**
 * Internal endpoints for track metadata, batch fetch, search, presigned URLs, and creation.
 */
@RestController
@RequestMapping("/internal/tracks")
@Validated
@AllArgsConstructor
public class InternalTrackController {

    private final TrackService trackService;

    @GetMapping("/{trackId}")
    /**
     * Returns full track details by id.
     */
    public ResponseEntity<TrackDetailsResponse> getTrack(@PathVariable String trackId) {
        return ResponseEntity.ok(trackService.getTrack(trackId));
    }

    @GetMapping
    /**
     * Search/list tracks for internal callers with pagination.
     */
    //GET /internal/tracks?query=beatles&limit=20&offset=40
    public ResponseEntity<TracksSearchResponse> getTracks(
            @RequestParam(value = "query", required = false) String query, //поиск/фильтрация треков, например
            @RequestParam(value = "limit", defaultValue = "30") @Min(1) @Max(100) int limit,
            @RequestParam(value = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        return ResponseEntity.ok(trackService.searchTracks(query, limit, offset));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchTracksResponse> getTracksBatch(@Valid @RequestBody BatchTracksRequest request) {
        return ResponseEntity.ok(trackService.getTracksBatch(request));
    }

    @GetMapping("/{trackId}/stream-url")
    public ResponseEntity<StreamUrlResponse> getStreamUrl(@PathVariable String trackId) {
        return ResponseEntity.ok(trackService.getStreamUrl(trackId));
    }

    @PostMapping
    public ResponseEntity<CreateTrackResponse> createTrack(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateTrackRequest request
    ) {
        CreateTrackResponse response = trackService.createTrack(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/upload-url")
    public ResponseEntity<UploadTrackResponse> createUploadUrl(
            @Valid @RequestBody UploadTrackRequest request
    ) {
        UploadTrackResponse response = trackService.createUploadUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{trackId}")
    public ResponseEntity<TrackDetailsResponse> updateTrack(
            @PathVariable String trackId,
            @Valid @RequestBody UpdateTrackRequest request
    ) {
        return ResponseEntity.ok(trackService.updateTrack(trackId, request));
    }

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable String trackId) {
        trackService.deleteTrack(trackId);
        return ResponseEntity.noContent().build();
    }
}
