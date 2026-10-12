package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.events.OrderLinePayload;
import dev.emreozcan.pazarsync.events.OrderReceived;
import dev.emreozcan.pazarsync.events.StockChanged;
import dev.emreozcan.pazarsync.orderservice.domain.Product;
import dev.emreozcan.pazarsync.orderservice.domain.ProductRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderIngestionService {

    private final JdbcTemplate jdbc;
    private final ProductRepository products;
    private final StockEventPublisher stockEvents;

    public OrderIngestionService(JdbcTemplate jdbc, ProductRepository products, StockEventPublisher stockEvents) {
        this.jdbc = jdbc;
        this.products = products;
        this.stockEvents = stockEvents;
    }

    @Transactional
    public IngestResult ingest(OrderReceived event) {
        UUID orderId = UUID.randomUUID();
        long total = total(event);
        int inserted = jdbc.update("""
                INSERT INTO orders (id, marketplace, external_order_id, customer_name, ordered_at, total_minor)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (marketplace, external_order_id) DO NOTHING
                """,
                orderId,
                event.marketplace(),
                event.externalOrderId(),
                event.customerName(),
                Timestamp.from(event.orderedAt()),
                total);
        if (inserted == 0) {
            return IngestResult.DUPLICATE;
        }
        List<StockChanged> changes = new ArrayList<>();
        List<OrderLinePayload> lines = event.lines() == null ? List.of() : event.lines();
        for (OrderLinePayload line : lines) {
            Product product = products.lockByBarcode(line.barcode())
                    .orElseThrow(() -> new UnknownProductException(line.barcode()));
            if (product.getQuantity() < line.quantity()) {
                throw new InsufficientStockException(line.barcode());
            }
            product.setQuantity(product.getQuantity() - line.quantity());
            boolean low = product.getQuantity() <= product.getLowStockThreshold();
            product.setLowStock(low);
            jdbc.update("""
                    INSERT INTO order_lines (id, order_id, barcode, product_name, quantity, unit_price_minor)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    UUID.randomUUID(),
                    orderId,
                    line.barcode(),
                    line.productName(),
                    line.quantity(),
                    line.unitPriceMinor());
            jdbc.update("""
                    INSERT INTO stock_movements (id, product_id, order_id, delta, reason, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    UUID.randomUUID(),
                    product.getId(),
                    orderId,
                    -line.quantity(),
                    "ORDER",
                    Timestamp.from(event.orderedAt()));
            changes.add(new StockChanged(
                    event.marketplace(),
                    product.getBarcode(),
                    product.getQuantity(),
                    product.getSalePriceMinor(),
                    low));
        }
        publishAfterCommit(changes);
        return IngestResult.CREATED;
    }

    private void publishAfterCommit(List<StockChanged> changes) {
        if (changes.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            changes.forEach(stockEvents::publish);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                changes.forEach(stockEvents::publish);
            }
        });
    }

    private static long total(OrderReceived event) {
        if (event.lines() == null) {
            return 0;
        }
        return event.lines().stream().mapToLong(line -> line.unitPriceMinor() * line.quantity()).sum();
    }
}
