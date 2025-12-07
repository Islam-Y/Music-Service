package ru.itmo.music.music_service.infrastructure.messaging;

/**
 * Consumer stub for facts generated events from Facts Service.
 */
public interface FactsEventsConsumer {

    void onFactsGenerated(String trackId, String factsJson);
}
