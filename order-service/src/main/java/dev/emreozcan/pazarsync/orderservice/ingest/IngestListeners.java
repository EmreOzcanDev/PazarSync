package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.events.OrderReceived;
import dev.emreozcan.pazarsync.events.QuestionReceived;
import dev.emreozcan.pazarsync.events.SyncFailure;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IngestListeners {

    private static final Logger log = LoggerFactory.getLogger(IngestListeners.class);

    private final OrderIngestionService orders;
    private final QuestionIngestionService questions;
    private final SyncFailureStore failures;

    public IngestListeners(
            OrderIngestionService orders,
            QuestionIngestionService questions,
            SyncFailureStore failures) {
        this.orders = orders;
        this.questions = questions;
        this.failures = failures;
    }

    @KafkaListener(topics = KafkaTopics.ORDERS_RECEIVED, groupId = "order-service")
    public void onOrder(String payload) {
        try {
            orders.ingest(read(payload, OrderReceived.class));
        } catch (EventReadException ex) {
            failures.recordPoison(KafkaTopics.ORDERS_RECEIVED, null, payload, ex.getMessage());
            throw ex;
        }
    }

    @KafkaListener(topics = KafkaTopics.QUESTIONS_RECEIVED, groupId = "order-service")
    public void onQuestion(String payload) {
        questions.ingest(read(payload, QuestionReceived.class));
    }

    @KafkaListener(topics = KafkaTopics.SYNC_FAILURES, groupId = "order-service-failures")
    public void onSyncFailure(String payload) {
        try {
            failures.record(EventCodec.read(payload, SyncFailure.class));
        } catch (RuntimeException ex) {
            log.error("Could not store sync failure", ex);
        }
    }

    @KafkaListener(topics = KafkaTopics.ORDERS_DLT, groupId = "order-service-dlt")
    public void onOrdersDlt(ConsumerRecord<String, String> record) {
        try {
            failures.recordPoison(KafkaTopics.ORDERS_RECEIVED, record.key(), record.value(), "Poison order message");
        } catch (RuntimeException ex) {
            log.error("Could not store dead-letter order", ex);
        }
    }

    private static <T> T read(String payload, Class<T> type) {
        try {
            return EventCodec.read(payload, type);
        } catch (RuntimeException ex) {
            throw new EventReadException(ex);
        }
    }
}
