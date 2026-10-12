package dev.emreozcan.pazarsync.events;

public final class KafkaTopics {

    public static final String ORDERS_RECEIVED = "orders.received";
    public static final String QUESTIONS_RECEIVED = "questions.received";
    public static final String SYNC_FAILURES = "sync.failures";
    public static final String STOCK_CHANGED = "stock.changed";
    public static final String ANSWERS_APPROVED = "answers.approved";
    public static final String IMPORTER_POLL = "importer.poll";
    public static final String ORDERS_DLT = "orders.received.DLT";

    private KafkaTopics() {
    }
}
