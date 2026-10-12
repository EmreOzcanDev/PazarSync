package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.EventKeys;
import dev.emreozcan.pazarsync.events.ImporterPoll;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.events.OrderReceived;
import dev.emreozcan.pazarsync.events.QuestionReceived;
import dev.emreozcan.pazarsync.events.SyncFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MarketplacePoller {

    private static final Logger log = LoggerFactory.getLogger(MarketplacePoller.class);
    private static final String ORDERS = "orders";
    private static final String QUESTIONS = "questions";

    private final MarketplaceClient client;
    private final CursorStore cursors;
    private final EventPublisher publisher;
    private final OrderMapper orderMapper;
    private final QuestionMapper questionMapper;
    private final ImporterProperties properties;

    public MarketplacePoller(
            MarketplaceClient client,
            CursorStore cursors,
            EventPublisher publisher,
            OrderMapper orderMapper,
            QuestionMapper questionMapper,
            ImporterProperties properties) {
        this.client = client;
        this.cursors = cursors;
        this.publisher = publisher;
        this.orderMapper = orderMapper;
        this.questionMapper = questionMapper;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString = "${pazarsync.importer.poll-delay-ms:5000}",
            initialDelayString = "${pazarsync.importer.poll-delay-ms:5000}")
    public void poll() {
        pollOrders();
        pollQuestions();
    }

    public synchronized void pollOrders() {
        Instant start = cursors.get(ORDERS);
        Instant end = nextEnd(start);
        try {
            RemoteOrderPage page = client.fetchOrders(start, end);
            if (page.content() != null) {
                for (RemoteOrder order : page.content()) {
                    OrderReceived event = orderMapper.toEvent(properties.getMarketplace(), order);
                    publisher.publish(
                            KafkaTopics.ORDERS_RECEIVED,
                            EventKeys.of(event.marketplace(), event.externalOrderId()),
                            event);
                }
            }
            cursors.advance(ORDERS, end);
            log.info("Polled orders from {} to {}, count {}", start, end, page.content() == null ? 0 : page.content().size());
        } catch (RuntimeException ex) {
            publishPollFailure("POLL_ORDERS", ORDERS, ex);
        }
    }

    public synchronized void pollQuestions() {
        Instant start = cursors.get(QUESTIONS);
        Instant end = nextEnd(start);
        try {
            RemoteQuestionPage page = client.fetchQuestions(start, end);
            if (page.content() != null) {
                for (RemoteQuestion question : page.content()) {
                    if ("ANSWERED".equals(question.status())) {
                        continue;
                    }
                    QuestionReceived event = questionMapper.toEvent(properties.getMarketplace(), question);
                    publisher.publish(
                            KafkaTopics.QUESTIONS_RECEIVED,
                            EventKeys.of(event.marketplace(), event.externalQuestionId()),
                            event);
                }
            }
            cursors.advance(QUESTIONS, end);
        } catch (RuntimeException ex) {
            publishPollFailure("POLL_QUESTIONS", QUESTIONS, ex);
        }
    }

    public void pollStream(String stream) {
        if (QUESTIONS.equals(stream)) {
            pollQuestions();
        } else {
            pollOrders();
        }
    }

    private void publishPollFailure(String operation, String stream, RuntimeException ex) {
        log.warn("{} failed: {}", operation, ex.getMessage());
        publisher.publishFailure(new SyncFailure(
                properties.getMarketplace(),
                operation,
                stream,
                KafkaTopics.IMPORTER_POLL,
                stream,
                EventCodec.write(new ImporterPoll(stream)),
                ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                Instant.now()));
    }

    private static Instant nextEnd(Instant start) {
        Instant end = Instant.now();
        return end.isAfter(start) ? end : start.plusMillis(1);
    }
}
