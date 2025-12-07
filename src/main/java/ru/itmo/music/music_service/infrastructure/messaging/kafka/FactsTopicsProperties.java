package ru.itmo.music.music_service.infrastructure.messaging.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka.topics")
public class FactsTopicsProperties {

    private String trackCreated = "music.track.created";
    private String trackUpdated = "music.track.updated";
    private String trackDeleted = "music.track.deleted";
    private String trackDomain = "music.track.domain";
    private String trackDomainDlq = "music.track.domain.dlq";
    private String factsRefresh = "music.track.facts.refresh";
    private String factsGenerated = "music.track.facts.generated";
    private String factsEventsOutbox = "music.facts.events";
}
