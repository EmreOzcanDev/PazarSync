package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.AnswerApproved;
import dev.emreozcan.pazarsync.events.StockChanged;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class MarketplaceClient {

    private final RestClient restClient;
    private final Retryer retryer;
    private final ImporterProperties properties;

    public MarketplaceClient(RestClient restClient, Retryer retryer, ImporterProperties properties) {
        this.restClient = restClient;
        this.retryer = retryer;
        this.properties = properties;
    }

    public RemoteOrderPage fetchOrders(Instant start, Instant end) {
        return retry("fetch orders", () -> restClient.get()
                .uri(uri -> uri.path("/suppliers/{id}/orders")
                        .queryParam("startDate", start.toEpochMilli())
                        .queryParam("endDate", end.toEpochMilli())
                        .build(properties.getSupplierId()))
                .retrieve()
                .body(RemoteOrderPage.class));
    }

    public RemoteQuestionPage fetchQuestions(Instant start, Instant end) {
        return retry("fetch questions", () -> restClient.get()
                .uri(uri -> uri.path("/suppliers/{id}/questions")
                        .queryParam("startDate", start.toEpochMilli())
                        .queryParam("endDate", end.toEpochMilli())
                        .build(properties.getSupplierId()))
                .retrieve()
                .body(RemoteQuestionPage.class));
    }

    public void updateStock(StockChanged event) {
        retry("update stock " + event.barcode(), () -> {
            restClient.put()
                    .uri("/suppliers/{id}/products/price-and-inventory", properties.getSupplierId())
                    .body(new PriceAndInventory(List.of(new StockItem(event.barcode(), event.quantity(), event.salePriceMinor()))))
                    .retrieve()
                    .toBodilessEntity();
            return Boolean.TRUE;
        });
    }

    public void postAnswer(AnswerApproved event) {
        retry("post answer " + event.externalQuestionId(), () -> {
            restClient.post()
                    .uri("/suppliers/{id}/questions/{questionId}/answers",
                            properties.getSupplierId(),
                            event.externalQuestionId())
                    .body(new AnswerBody(event.answerText()))
                    .retrieve()
                    .toBodilessEntity();
            return Boolean.TRUE;
        });
    }

    private <T> T retry(String operation, java.util.function.Supplier<T> call) {
        try {
            T body = retryer.run(
                    call,
                    properties.getRetry().getMaxAttempts(),
                    Duration.ofMillis(properties.getRetry().getInitialBackoffMs()));
            if (body == null) {
                throw new IllegalStateException(operation + " returned an empty body");
            }
            return body;
        } catch (RuntimeException ex) {
            throw new IllegalStateException(operation + " failed: " + rootMessage(ex), ex);
        }
    }

    private static String rootMessage(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    public record PriceAndInventory(List<StockItem> items) {
    }

    public record StockItem(String barcode, int quantity, long salePrice) {
    }

    public record AnswerBody(String text) {
    }
}
