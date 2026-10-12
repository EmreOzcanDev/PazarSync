package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.OrderLinePayload;
import dev.emreozcan.pazarsync.events.OrderReceived;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OrderMapper {

    public OrderReceived toEvent(String marketplace, RemoteOrder order) {
        List<OrderLinePayload> lines = order.lines() == null
                ? List.of()
                : order.lines().stream()
                        .map(line -> new OrderLinePayload(line.barcode(), line.productName(), line.quantity(), line.price()))
                        .toList();
        String customer = ((order.customerFirstName() == null ? "" : order.customerFirstName())
                + " "
                + (order.customerLastName() == null ? "" : order.customerLastName())).trim();
        return new OrderReceived(
                marketplace,
                order.orderNumber(),
                customer,
                Instant.ofEpochMilli(order.orderDate()),
                lines);
    }
}
