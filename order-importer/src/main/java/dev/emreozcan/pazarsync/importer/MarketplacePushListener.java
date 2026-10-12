package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.AnswerApproved;
import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.EventKeys;
import dev.emreozcan.pazarsync.events.ImporterPoll;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.events.StockChanged;
import dev.emreozcan.pazarsync.events.SyncFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MarketplacePushListener {

    private static final Logger log = LoggerFactory.getLogger(MarketplacePushListener.class);

    private final MarketplaceClient client;
    private final EventPublisher publisher;
    private final MarketplacePoller poller;
    private final ImporterProperties properties;

    public MarketplacePushListener(
            MarketplaceClient client,
            EventPublisher publisher,
            MarketplacePoller poller,
            ImporterProperties properties) {
        this.client = client;
        this.publisher = publisher;
        this.poller = poller;
        this.properties = properties;
    }

    @KafkaListener(topics = KafkaTopics.STOCK_CHANGED, groupId = "order-importer")
    public void onStock(String payload) {
        StockChanged event = EventCodec.read(payload, StockChanged.class);
        try {
            client.updateStock(event);
        } catch (RuntimeException ex) {
            log.warn("Stock push failed for {}: {}", event.barcode(), ex.getMessage());
            publisher.publishFailure(new SyncFailure(
                    event.marketplace(),
                    "PUSH_STOCK",
                    event.barcode(),
                    KafkaTopics.STOCK_CHANGED,
                    EventKeys.of(event.marketplace(), event.barcode()),
                    payload,
                    ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                    Instant.now()));
        }
    }

    @KafkaListener(topics = KafkaTopics.ANSWERS_APPROVED, groupId = "order-importer")
    public void onAnswer(String payload) {
        AnswerApproved event = EventCodec.read(payload, AnswerApproved.class);
        try {
            client.postAnswer(event);
        } catch (RuntimeException ex) {
            log.warn("Answer push failed for {}: {}", event.externalQuestionId(), ex.getMessage());
            publisher.publishFailure(new SyncFailure(
                    event.marketplace(),
                    "PUSH_ANSWER",
                    event.externalQuestionId(),
                    KafkaTopics.ANSWERS_APPROVED,
                    EventKeys.of(event.marketplace(), event.externalQuestionId()),
                    payload,
                    ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                    Instant.now()));
        }
    }

    @KafkaListener(topics = KafkaTopics.IMPORTER_POLL, groupId = "order-importer-poll")
    public void onPoll(String payload) {
        ImporterPoll poll = EventCodec.read(payload, ImporterPoll.class);
        log.info("Retrying {} poll for {}", poll.stream(), properties.getMarketplace());
        poller.pollStream(poll.stream());
    }
}
