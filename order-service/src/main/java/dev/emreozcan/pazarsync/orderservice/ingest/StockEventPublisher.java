package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.EventKeys;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.events.StockChanged;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public StockEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(StockChanged event) {
        kafkaTemplate.send(
                KafkaTopics.STOCK_CHANGED,
                EventKeys.of(event.marketplace(), event.barcode()),
                EventCodec.write(event));
    }
}
