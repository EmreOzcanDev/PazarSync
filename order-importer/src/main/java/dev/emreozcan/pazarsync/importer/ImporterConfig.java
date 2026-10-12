package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.client.RestClient;

@Configuration
public class ImporterConfig {

    @Bean
    RestClient marketplaceRestClient(ImporterProperties properties, RestClient.Builder builder) {
        return builder.baseUrl(properties.getMarketplaceBaseUrl()).build();
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
