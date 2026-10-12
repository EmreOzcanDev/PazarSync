package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event instanceof String payload ? payload : EventCodec.write(event));
    }

    public void publishFailure(dev.emreozcan.pazarsync.events.SyncFailure failure) {
        publish(KafkaTopics.SYNC_FAILURES, failure.retryKey(), failure);
    }
}
