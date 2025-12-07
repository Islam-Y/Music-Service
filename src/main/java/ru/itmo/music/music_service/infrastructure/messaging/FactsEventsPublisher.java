package ru.itmo.music.music_service.infrastructure.messaging;

/**
 * Publishes events to Kafka (or another broker) to trigger facts generation.
 */
public interface FactsEventsPublisher {

    void publishTrackCreated(String trackId);
    void publishTrackUpdated(String trackId);
    void publishTrackDeleted(String trackId);
    void publishFactsRefresh(String trackId, int priority);
}
