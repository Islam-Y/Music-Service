package ru.itmo.music.music_service.application;

import ru.itmo.music.music_service.api.dto.TrackFactsRefreshResponse;
import ru.itmo.music.music_service.api.dto.TrackFactsResponse;

/**
 * Handles retrieval and regeneration triggers for track facts.
 */
public interface TrackFactsService {

    TrackFactsResponse getFacts(String trackId);

    TrackFactsRefreshResponse refreshFacts(String trackId, int priority);

    void saveFacts(String trackId, String factsJson);
}
