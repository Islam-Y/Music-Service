package ru.itmo.music.music_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.util.backoff.FixedBackOff;
import ru.itmo.music.music_service.infrastructure.messaging.kafka.FactsTopicsProperties;
import ru.itmo.music.music_service.infrastructure.messaging.kafka.MdcRecordInterceptor;

@Configuration
public class KafkaConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic trackCreatedTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getTrackCreated()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic trackUpdatedTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getTrackUpdated()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic trackDeletedTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getTrackDeleted()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic trackDomainTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getTrackDomain()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic factsRefreshTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getFactsRefresh()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic factsGeneratedTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getFactsGenerated()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic factsGeneratedDlq(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getFactsGenerated() + ".dlq").partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic trackDomainDlq(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getTrackDomainDlq()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic factsEventsOutboxTopic(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getFactsEventsOutbox()).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic factsEventsOutboxDlq(FactsTopicsProperties topics) {
        return TopicBuilder.name(topics.getFactsEventsOutbox() + ".dlq").partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public NewTopic outboxGenericDlq(@Value("${app.outbox.dlq-topic:music.outbox.dlq}") String outboxDlq) {
        return TopicBuilder.name(outboxDlq).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<Object, Object> kafkaTemplate,
                                                                       FactsTopicsProperties topics) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> {
            String topic = record.topic();
            if (topic.equals(topics.getFactsGenerated())) {
                return new org.apache.kafka.common.TopicPartition(topics.getFactsGenerated() + ".dlq", record.partition());
            }
            if (topic.equals(topics.getFactsEventsOutbox())) {
                return new org.apache.kafka.common.TopicPartition(topics.getFactsEventsOutbox() + ".dlq", record.partition());
            }
            if (topic.equals(topics.getTrackDomain())) {
                return new org.apache.kafka.common.TopicPartition(topics.getTrackDomainDlq(), record.partition());
            }
            return new org.apache.kafka.common.TopicPartition(topic + ".dlq", record.partition());
        });
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        FixedBackOff backOff = new FixedBackOff(1000L, 3); // 3 ретрая с паузой 1с
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        return errorHandler;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler,
            RecordInterceptor<String, Object> mdcRecordInterceptor) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.setRecordInterceptor(mdcRecordInterceptor);
        return factory;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
