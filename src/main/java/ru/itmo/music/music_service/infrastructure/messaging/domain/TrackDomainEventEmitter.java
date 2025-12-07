package ru.itmo.music.music_service.infrastructure.messaging.domain;

import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.messaging.outbox.OutboxService;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackEntity;

import java.time.Instant;

@Component
public class TrackDomainEventEmitter {

    private final OutboxService outboxService;

    public TrackDomainEventEmitter(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    public void emitCreated(TrackEntity entity) {
        outboxService.enqueue("track", entity.getId(), "created", toPayload("created", entity, null), null);
    }

    public void emitUpdated(TrackEntity entity, String changedFieldsCsv) {
        outboxService.enqueue("track", entity.getId(), "updated", toPayload("updated", entity, changedFieldsCsv), null);
    }

    public void emitDeleted(TrackEntity entity) {
        outboxService.enqueue("track", entity.getId(), "deleted", toPayload("deleted", entity, null), null);
    }

    private TrackDomainEventPayload toPayload(String eventType, TrackEntity entity, String changedFields) {
        return new TrackDomainEventPayload(
                "1",
                eventType,
                entity.getId(),
                entity.getTitle(),
                entity.getArtist(),
                entity.getDurationMs(),
                entity.getYear(),
                entity.isExplicit(),
                entity.getAudioStorageKey(),
                Instant.now(),
                changedFields
        );
    }
}
