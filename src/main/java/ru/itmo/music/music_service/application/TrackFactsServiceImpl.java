package ru.itmo.music.music_service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.music.music_service.api.dto.TrackFactsRefreshResponse;
import ru.itmo.music.music_service.api.dto.TrackFactsResponse;
import ru.itmo.music.music_service.application.exception.NotFoundException;
import ru.itmo.music.music_service.application.mapper.TrackFactsMapper;
import ru.itmo.music.music_service.infrastructure.messaging.FactsEventsPublisher;
import ru.itmo.music.music_service.infrastructure.persistence.TrackFactsJpaRepository;
import ru.itmo.music.music_service.infrastructure.persistence.TrackJpaRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackFactsEntity;

import java.time.Instant;

/**
 * Stub implementation: persistence + trigger publishing to Facts Service will be added later.
 */
@Service
@RequiredArgsConstructor
public class TrackFactsServiceImpl implements TrackFactsService {

    private final TrackJpaRepository trackRepository;
    private final TrackFactsJpaRepository trackFactsRepository;
    private final TrackFactsMapper trackFactsMapper;
    private final FactsEventsPublisher factsEventsPublisher;
    private final TrackCacheService trackCacheService;

    @Override
    public TrackFactsResponse getFacts(String trackId) {
        return trackCacheService.getTrackFacts(trackId)
                .orElseGet(() -> loadAndCacheFacts(trackId));
    }

    private TrackFactsResponse loadAndCacheFacts(String trackId) {
        ensureTrackExists(trackId);
        TrackFactsEntity entity = trackFactsRepository.findById(trackId)
                .orElseThrow(() -> new NotFoundException("Track facts not found"));
        TrackFactsResponse response = trackFactsMapper.toResponse(entity);
        trackCacheService.putTrackFacts(trackId, response);
        return response;
    }

    @Override
    public TrackFactsRefreshResponse refreshFacts(String trackId, int priority) {
        ensureTrackExists(trackId);
        factsEventsPublisher.publishFactsRefresh(trackId, priority);
        return new TrackFactsRefreshResponse(trackId, "queued", Instant.now());
    }

    @Override
    public void saveFacts(String trackId, String factsJson) {
        TrackFactsEntity entity = trackFactsRepository.findById(trackId)
                .orElse(TrackFactsEntity.builder().trackId(trackId).build());
        entity.setFactsJson(factsJson);
        entity.setGeneratedAt(entity.getGeneratedAt() == null ? Instant.now() : entity.getGeneratedAt());
        entity.setUpdatedAt(Instant.now());
        trackFactsRepository.save(entity);
        TrackFactsResponse response = trackFactsMapper.toResponse(entity);
        trackCacheService.putTrackFacts(trackId, response);
    }

    private void ensureTrackExists(String trackId) {
        boolean exists = trackRepository.existsById(trackId);
        if (!exists) {
            throw new NotFoundException("Track not found");
        }
    }
}
