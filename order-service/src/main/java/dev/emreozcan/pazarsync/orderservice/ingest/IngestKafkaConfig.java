package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.events.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class IngestKafkaConfig {

    @Bean
    CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                new FixedBackOff(200L, 2L));
        handler.addNotRetryableExceptions(
                InsufficientStockException.class,
                UnknownProductException.class,
                EventReadException.class);
        return handler;
    }

    @Bean
    NewTopic ordersReceivedTopic() {
        return topic(KafkaTopics.ORDERS_RECEIVED);
    }

    @Bean
    NewTopic questionsReceivedTopic() {
        return topic(KafkaTopics.QUESTIONS_RECEIVED);
    }

    @Bean
    NewTopic syncFailuresTopic() {
        return topic(KafkaTopics.SYNC_FAILURES);
    }

    @Bean
    NewTopic stockChangedTopic() {
        return topic(KafkaTopics.STOCK_CHANGED);
    }

    @Bean
    NewTopic answersApprovedTopic() {
        return topic(KafkaTopics.ANSWERS_APPROVED);
    }

    @Bean
    NewTopic importerPollTopic() {
        return topic(KafkaTopics.IMPORTER_POLL);
    }

    private static NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
