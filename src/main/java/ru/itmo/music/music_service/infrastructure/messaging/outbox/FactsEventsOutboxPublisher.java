package ru.itmo.music.music_service.infrastructure.messaging.outbox;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.messaging.FactsEventsPublisher;

import java.time.Instant;

@Component
@AllArgsConstructor
public class FactsEventsOutboxPublisher implements FactsEventsPublisher {

    private final OutboxService outboxService;

    @Override
    public void publishTrackCreated(String trackId) {
        enqueueFactsEvent(trackId, "created", null);
    }

    @Override
    public void publishTrackUpdated(String trackId) {
        enqueueFactsEvent(trackId, "updated", null);
    }

    @Override
    public void publishTrackDeleted(String trackId) {
        enqueueFactsEvent(trackId, "deleted", null);
    }

    @Override
    public void publishFactsRefresh(String trackId, int priority) {
        enqueueFactsEvent(trackId, "refresh", priority);
    }

    private void enqueueFactsEvent(String trackId, String eventType, Integer priority) {
        FactsOutboxPayload payload = new FactsOutboxPayload(
                "1",
                eventType,
                trackId,
                priority,
                Instant.now()
        );
        outboxService.enqueue("facts", trackId, eventType, payload, null);
    }
}
