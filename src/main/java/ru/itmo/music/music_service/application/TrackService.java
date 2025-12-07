package ru.itmo.music.music_service.application;

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

/**
 * Application service orchestrating track read/write operations and integrations.
 */
public interface TrackService {

    /**
     * Get full track details by id.
     */
    TrackDetailsResponse getTrack(String trackId);

    /**
     * Search/list tracks with pagination.
     */
    TracksSearchResponse searchTracks(String query, int limit, int offset);

    /**
     * Batch fetch tracks by ids.
     */
    BatchTracksResponse getTracksBatch(BatchTracksRequest request);

    /**
     * Build presigned stream/download URL for a track.
     */
    StreamUrlResponse getStreamUrl(String trackId);

    /**
     * Partially update track metadata.
     */
    TrackDetailsResponse updateTrack(String trackId, UpdateTrackRequest request);

    /**
     * Delete track and related resources.
     */
    void deleteTrack(String trackId);

    /**
     * Create a new track using uploaded file location.
     */
    CreateTrackResponse createTrack(CreateTrackRequest request, String idempotencyKey);

    /**
     * Create presigned upload URL for audio file.
     */
    UploadTrackResponse createUploadUrl(UploadTrackRequest request);
}
