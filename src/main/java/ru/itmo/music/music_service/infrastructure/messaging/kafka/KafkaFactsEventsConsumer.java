package ru.itmo.music.music_service.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.messaging.FactsEventsConsumer;
import ru.itmo.music.music_service.application.TrackCacheService;

@Component
@RequiredArgsConstructor
public class KafkaFactsEventsConsumer implements FactsEventsConsumer {

    private final FactsTopicsProperties topics;
    private final FactsGeneratedHandler handler;
    private final TrackCacheService trackCacheService;

    @Override
    public void onFactsGenerated(String trackId, String factsJson) {
        handler.handle(trackId, factsJson);
    }

    @KafkaListener(topics = "#{factsTopicsProperties.factsGenerated}", groupId = "music-service")
    public void listenFactsGenerated(FactsGeneratedEvent event) {
        onFactsGenerated(event.trackId(), event.factsJson());
        trackCacheService.evictTrackFacts(event.trackId());
    }

    public record FactsGeneratedEvent(String trackId, String factsJson) {}

    @Component
    @RequiredArgsConstructor
    public static class FactsGeneratedHandler {
        private final ru.itmo.music.music_service.application.TrackFactsService trackFactsService;

        public void handle(String trackId, String factsJson) {
            trackFactsService.saveFacts(trackId, factsJson);
        }
    }
}
