package ru.itmo.music.music_service.api.internal;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.music.music_service.api.dto.TrackFactsRefreshResponse;
import ru.itmo.music.music_service.api.dto.TrackFactsResponse;
import ru.itmo.music.music_service.application.TrackFactsService;

/**
 * Internal endpoints for track facts retrieval and regeneration trigger.
 */
@RestController
@RequestMapping("/internal/tracks")
@Validated
@AllArgsConstructor
public class InternalTrackFactsController {

    private final TrackFactsService trackFactsService;

    @GetMapping("/{trackId}/facts")
    public ResponseEntity<TrackFactsResponse> getTrackFacts(@PathVariable String trackId) {
        return ResponseEntity.ok(trackFactsService.getFacts(trackId));
    }

    @PostMapping("/{trackId}/facts/refresh")
    public ResponseEntity<TrackFactsRefreshResponse> refreshTrackFacts(
            @PathVariable String trackId,
            @RequestParam(value = "priority", required = false, defaultValue = "0") @Min(0) int priority
    ) {
        return ResponseEntity.ok(trackFactsService.refreshFacts(trackId, priority));
    }
}
