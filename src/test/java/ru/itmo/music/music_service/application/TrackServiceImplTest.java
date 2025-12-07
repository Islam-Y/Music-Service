package ru.itmo.music.music_service.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.music.music_service.api.dto.CreateTrackRequest;
import ru.itmo.music.music_service.application.exception.ConflictException;
import ru.itmo.music.music_service.application.mapper.TrackMapper;
import ru.itmo.music.music_service.config.S3Properties;
import ru.itmo.music.music_service.infrastructure.messaging.FactsEventsPublisher;
import ru.itmo.music.music_service.infrastructure.messaging.domain.TrackDomainEventEmitter;
import ru.itmo.music.music_service.infrastructure.persistence.TrackJpaRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackEntity;
import ru.itmo.music.music_service.infrastructure.storage.StorageService;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceImplTest {

    @Mock
    private TrackJpaRepository trackRepository;
    @Mock
    private TrackMapper trackMapper;
    @Mock
    private FactsEventsPublisher factsEventsPublisher;
    @Mock
    private TrackDomainEventEmitter trackDomainEventEmitter;
    @Mock
    private TrackCacheService trackCacheService;
    @Mock
    private StorageService storageService;
    @Mock
    private IdempotencyService idempotencyService;

    private TrackServiceImpl trackService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setPresignTtlSeconds(600);
        trackService = new TrackServiceImpl(
                trackRepository, trackMapper, factsEventsPublisher,
                trackCacheService, trackDomainEventEmitter, storageService,
                idempotencyService, 600, s3Properties
        );
    }

    @Test
    void createTrack_conflictOnDifferentPayloadForSameIdempotencyKey() {
        String key = "idem-1";
        CreateTrackRequest request = new CreateTrackRequest("t", "a", 1000L, 2024, false, "s3://key", null);

        TrackEntity existing = TrackEntity.builder()
                .id("track-1")
                .title("old")
                .artist("artist")
                .durationMs(1000L)
                .year(2024)
                .audioStorageKey("s3://key-old")
                .explicit(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(idempotencyService.getEntry(key))
                .thenReturn(Optional.of(new IdempotencyService.IdempotencyEntry(existing.getId(), "hash-old")));
        when(trackRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(idempotencyService.getHashPayload(request)).thenReturn("hash-new");

        assertThrows(ConflictException.class, () -> trackService.createTrack(request, key));

        verify(trackRepository, never()).save(any());
        verify(factsEventsPublisher, never()).publishTrackCreated(any());
    }

    @Test
    void createTrack_reusesExistingOnSameIdempotencyKeyAndPayload() {
        String key = "idem-2";
        CreateTrackRequest request = new CreateTrackRequest("t", "a", 1000L, 2024, false, "s3://key", null);

        TrackEntity existing = TrackEntity.builder()
                .id("track-2")
                .title("t")
                .artist("a")
                .durationMs(1000L)
                .year(2024)
                .audioStorageKey("s3://key")
                .explicit(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(idempotencyService.getEntry(key))
                .thenReturn(Optional.of(new IdempotencyService.IdempotencyEntry(existing.getId(), "hash")));
        when(idempotencyService.getHashPayload(request)).thenReturn("hash");
        when(trackRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        var response = trackService.createTrack(request, key);
        assertEquals(existing.getId(), response.id());
        verify(trackRepository, never()).save(any());
        verify(factsEventsPublisher, never()).publishTrackCreated(any());
    }
}
