package dev.emreozcan.pazarsync.events;

import java.time.Instant;
import java.util.List;

public record OrderReceived(
        String marketplace,
        String externalOrderId,
        String customerName,
        Instant orderedAt,
        List<OrderLinePayload> lines) {
}
