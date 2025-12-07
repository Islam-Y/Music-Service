package ru.itmo.music.music_service.api.publicapi;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.music.music_service.api.dto.StreamUrlResponse;
import ru.itmo.music.music_service.api.dto.TrackDetailsResponse;
import ru.itmo.music.music_service.api.dto.TrackFactsResponse;
import ru.itmo.music.music_service.api.dto.TracksSearchResponse;
import ru.itmo.music.music_service.application.TrackFactsService;
import ru.itmo.music.music_service.application.TrackService;

/**
 * Public read-only endpoints for tracks, stream URL, and facts (for Gateway/BFF).
 */
@RestController
@RequestMapping("/tracks")
@Validated
@RequiredArgsConstructor
public class PublicTrackController {

    private final TrackService trackService;
    private final TrackFactsService trackFactsService;

    @GetMapping("/{trackId}")
    public ResponseEntity<TrackDetailsResponse> getTrack(@PathVariable String trackId) {
        return ResponseEntity.ok(trackService.getTrack(trackId));
    }

    @GetMapping
    public ResponseEntity<TracksSearchResponse> listTracks(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(100) int limit,
            @RequestParam(value = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        return ResponseEntity.ok(trackService.searchTracks(query, limit, offset));
    }

    @GetMapping("/{trackId}/stream-url")
    public ResponseEntity<StreamUrlResponse> getStreamUrl(@PathVariable String trackId) {
        return ResponseEntity.ok(trackService.getStreamUrl(trackId));
    }

    @GetMapping("/{trackId}/facts")
    public ResponseEntity<TrackFactsResponse> getTrackFacts(@PathVariable String trackId) {
        return ResponseEntity.ok(trackFactsService.getFacts(trackId));
    }
}
