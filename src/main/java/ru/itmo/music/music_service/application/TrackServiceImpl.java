package ru.itmo.music.music_service.application;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.music.music_service.api.dto.BatchTracksRequest;
import ru.itmo.music.music_service.api.dto.BatchTracksResponse;
import ru.itmo.music.music_service.api.dto.CreateTrackRequest;
import ru.itmo.music.music_service.api.dto.CreateTrackResponse;
import ru.itmo.music.music_service.api.dto.StreamUrlResponse;
import ru.itmo.music.music_service.api.dto.TrackDetailsResponse;
import ru.itmo.music.music_service.api.dto.TrackSummaryResponse;
import ru.itmo.music.music_service.api.dto.TracksSearchResponse;
import ru.itmo.music.music_service.api.dto.UpdateTrackRequest;
import ru.itmo.music.music_service.api.dto.UploadTrackRequest;
import ru.itmo.music.music_service.api.dto.UploadTrackResponse;
import ru.itmo.music.music_service.application.exception.ConflictException;
import ru.itmo.music.music_service.application.exception.DomainValidationException;
import ru.itmo.music.music_service.application.exception.NotFoundException;
import ru.itmo.music.music_service.application.mapper.TrackMapper;
import ru.itmo.music.music_service.application.IdempotencyService;
import ru.itmo.music.music_service.infrastructure.messaging.FactsEventsPublisher;
import ru.itmo.music.music_service.infrastructure.messaging.domain.TrackDomainEventEmitter;
import ru.itmo.music.music_service.infrastructure.storage.StorageService;
import ru.itmo.music.music_service.infrastructure.persistence.TrackJpaRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackEntity;
import ru.itmo.music.music_service.application.TrackCacheService;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default implementation of TrackService. Integrations (DB/Redis/S3/Kafka/Facts) to be wired here.
 */
@Service
@AllArgsConstructor
public class TrackServiceImpl implements TrackService {

    private static final Logger log = LoggerFactory.getLogger(TrackServiceImpl.class);

    private final TrackJpaRepository trackRepository;
    private final TrackMapper trackMapper;
    private final FactsEventsPublisher factsEventsPublisher;
    private final TrackCacheService trackCacheService;
    private final TrackDomainEventEmitter trackDomainEventEmitter;
    private final StorageService storageService;
    private final IdempotencyService idempotencyService;
    private final long streamUrlTtlSeconds;

    @Override
    @Transactional(readOnly = true)
    public TrackDetailsResponse getTrack(String trackId) {
        return trackCacheService.getTrack(trackId)
                .orElseGet(() -> {
                    TrackEntity entity = trackRepository.findById(trackId)
                            .orElseThrow(() -> new NotFoundException("Track not found"));
                    TrackDetailsResponse response = trackMapper.toDetails(entity);
                    trackCacheService.putTrack(trackId, response);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TracksSearchResponse searchTracks(String query, int limit, int offset) {
        int page = offset / Math.max(limit, 1);
        PageRequest pageable = PageRequest.of(page, limit);
        Page<TrackEntity> result;
        if (StringUtils.hasText(query)) {
            result = trackRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(query, query, pageable);
        } else {
            result = trackRepository.findAll(pageable);
        }
        List<TrackSummaryResponse> items = result.getContent().stream()
                .map(trackMapper::toSummary)
                .toList();
        return new TracksSearchResponse(items, (int) result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public BatchTracksResponse getTracksBatch(BatchTracksRequest request) {
        List<TrackEntity> found = trackRepository.findAllById(request.ids());
        Set<String> foundIds = found.stream().map(TrackEntity::getId).collect(Collectors.toSet());
        List<String> notFound = request.ids().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        return new BatchTracksResponse(
                found.stream().map(trackMapper::toSummary).toList(),
                notFound.isEmpty() ? null : notFound
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StreamUrlResponse getStreamUrl(String trackId) {
        return trackCacheService.getStreamUrl(trackId)
                .orElseGet(() -> {
                    TrackEntity entity = trackRepository.findById(trackId)
                            .orElseThrow(() -> new NotFoundException("Track not found"));
                    Instant expiresAt = Instant.now().plusSeconds(streamUrlTtlSeconds);
                    String signedUrl = storageService.generatePresignedGetUrl(entity.getAudioStorageKey());
                    StreamUrlResponse url = new StreamUrlResponse(signedUrl, expiresAt);
                    trackCacheService.putStreamUrl(trackId, url);
                    log.info("event=stream_url status=success trackId={} expiresAt={}", trackId, expiresAt);
                    return url;
                });
    }

    @Override
    @Transactional
    public TrackDetailsResponse updateTrack(String trackId, UpdateTrackRequest request) {
        if (request.title() == null && request.artist() == null
                && request.durationMs() == null && request.year() == null
                && request.explicit() == null && request.coverUrl() == null) {
            throw new DomainValidationException("At least one field must be provided for update");
        }

        TrackEntity entity = trackRepository.findById(trackId)
                .orElseThrow(() -> new NotFoundException("Track not found"));
        validateYear(request.year());

        StringBuilder changed = new StringBuilder();
        if (request.title() != null && !Objects.equals(entity.getTitle(), request.title())) {
            entity.setTitle(request.title());
            changed.append("title,");
        }
        if (request.artist() != null && !Objects.equals(entity.getArtist(), request.artist())) {
            entity.setArtist(request.artist());
            changed.append("artist,");
        }
        if (request.durationMs() != null && !Objects.equals(entity.getDurationMs(), request.durationMs())) {
            entity.setDurationMs(request.durationMs());
            changed.append("durationMs,");
        }
        if (request.year() != null && !Objects.equals(entity.getYear(), request.year())) {
            entity.setYear(request.year());
            changed.append("year,");
        }
        if (request.explicit() != null && !Objects.equals(entity.isExplicit(), request.explicit())) {
            entity.setExplicit(request.explicit());
            changed.append("explicit,");
        }
        if (request.coverUrl() != null && !Objects.equals(entity.getCoverUrl(), request.coverUrl())) {
            entity.setCoverUrl(request.coverUrl());
            changed.append("coverUrl,");
        }

        if (changed.length() == 0) {
            throw new DomainValidationException("No changes detected");
        }

        TrackEntity saved = trackRepository.save(entity);
        factsEventsPublisher.publishTrackUpdated(trackId);
        String changedFields = changed.length() > 0 ? changed.substring(0, changed.length() - 1) : null;
        trackDomainEventEmitter.emitUpdated(saved, changedFields);
        TrackDetailsResponse response = trackMapper.toDetails(saved);
        trackCacheService.putTrack(trackId, response);
        trackCacheService.evictStreamUrl(trackId);
        trackCacheService.evictTrackFacts(trackId);
        log.info("event=track_update status=success trackId={} changedFields={}", trackId, changedFields);
        return response;
    }

    @Override
    @Transactional
    public void deleteTrack(String trackId) {
        TrackEntity entity = trackRepository.findById(trackId)
                .orElseThrow(() -> new NotFoundException("Track not found"));
        trackRepository.delete(entity);
        factsEventsPublisher.publishTrackDeleted(trackId);
        trackDomainEventEmitter.emitDeleted(entity);
        storageService.deleteObject(entity.getAudioStorageKey());
        trackCacheService.evictTrack(trackId);
        trackCacheService.evictStreamUrl(trackId);
        trackCacheService.evictTrackFacts(trackId);
        log.info("event=track_delete status=success trackId={}", trackId);
    }

    @Override
    @Transactional
    public CreateTrackResponse createTrack(CreateTrackRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var entry = idempotencyService.getEntry(idempotencyKey);
            if (entry.isPresent()) {
                TrackEntity existing = trackRepository.findById(entry.get().trackId())
                        .orElseThrow(() -> new NotFoundException("Track not found for idempotency key"));
                String payloadHash = idempotencyService.hashPayload(request);
                if (!payloadHash.equals(entry.get().payloadHash())) {
                    throw new ConflictException("Idempotency-Key already used with different payload");
                }
                return new CreateTrackResponse(existing.getId(), existing.getCreatedAt());
            }
        }

        validateYear(request.year());
        trackRepository.findByAudioStorageKey(request.fileLocation())
                .ifPresent(t -> {
                    throw new DomainValidationException("Track with the provided fileLocation already exists");
                });

        if (!storageService.objectExists(request.fileLocation())) {
            throw new DomainValidationException("File not found in storage at provided fileLocation");
        }

        TrackEntity entity = TrackEntity.builder()
                .id(UUID.randomUUID().toString())
                .title(request.title())
                .artist(request.artist())
                .coverUrl(request.coverUrl())
                .durationMs(request.durationMs())
                .year(request.year())
                .audioStorageKey(request.fileLocation())
                .explicit(request.explicit())
                .build();

        TrackEntity saved = trackRepository.save(entity);
        factsEventsPublisher.publishTrackCreated(saved.getId());
        trackDomainEventEmitter.emitCreated(saved);
        TrackDetailsResponse response = trackMapper.toDetails(saved);
        trackCacheService.putTrack(saved.getId(), response);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.saveEntry(idempotencyKey, saved.getId(), request);
        }
        log.info("event=track_create status=success trackId={} storageKey={}", saved.getId(), saved.getAudioStorageKey());
        return new CreateTrackResponse(saved.getId(), saved.getCreatedAt());
    }

    private void validateYear(Integer year) {
        if (year == null) {
            return;
        }
        int maxYear = Year.now().getValue() + 1;
        if (year < 1900 || year > maxYear) {
            throw new DomainValidationException("Year must be between 1900 and " + maxYear);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UploadTrackResponse createUploadUrl(UploadTrackRequest request) {
        String key = "audio/" + UUID.randomUUID() + "/" + request.fileName();
        String uploadUrl = storageService.generatePresignedPutUrl(key, request.contentType());
        Instant expiresAt = Instant.now().plusSeconds(streamUrlTtlSeconds);
        return new UploadTrackResponse(key, uploadUrl, expiresAt);
    }
}
