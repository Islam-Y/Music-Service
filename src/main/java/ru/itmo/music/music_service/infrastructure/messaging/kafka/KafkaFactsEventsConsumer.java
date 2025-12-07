package ru.itmo.music.music_service.infrastructure.messaging.kafka;

import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.application.TrackFactsService;
import ru.itmo.music.music_service.infrastructure.messaging.FactsEventsConsumer;
import ru.itmo.music.music_service.application.TrackCacheService;

@Component
@AllArgsConstructor
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
    @AllArgsConstructor
    public static class FactsGeneratedHandler {
        private final TrackFactsService trackFactsService;

        public void handle(String trackId, String factsJson) {
            trackFactsService.saveFacts(trackId, factsJson);
        }
    }
}
