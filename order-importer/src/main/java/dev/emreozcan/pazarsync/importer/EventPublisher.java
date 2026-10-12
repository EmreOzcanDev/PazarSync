package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, Object event) {
        String payload = event instanceof String text ? text : EventCodec.write(event);
        try {
            kafkaTemplate.send(topic, key, payload).get(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not publish to " + topic, ex);
        }
    }

    public void publishFailure(dev.emreozcan.pazarsync.events.SyncFailure failure) {
        publish(KafkaTopics.SYNC_FAILURES, failure.retryKey(), failure);
    }
}
