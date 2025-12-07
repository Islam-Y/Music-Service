package ru.itmo.music.music_service.config;

import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class MetricsConfig {

    @Bean
    @ConditionalOnBean(Producer.class)
    public KafkaClientMetrics kafkaClientMetrics(Producer<?, ?> producer) {
        return new KafkaClientMetrics(producer);
    }

    @Bean
    public WebClient instrumentedWebClient(WebClient.Builder builder) {
        ConnectionProvider provider = ConnectionProvider.builder("music-webclient")
                .maxConnections(100)
                .build();
        HttpClient httpClient = HttpClient.create(provider);
        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
