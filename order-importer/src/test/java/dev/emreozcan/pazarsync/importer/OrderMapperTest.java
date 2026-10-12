package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.OrderReceived;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderMapperTest {

    @Test
    void mapsAMarketplaceOrderOntoTheKafkaEvent() {
        RemoteOrder remote = new RemoteOrder(
                "TY42",
                Instant.parse("2026-05-01T08:00:00Z").toEpochMilli(),
                "Ada",
                "Lovelace",
                List.of(new RemoteLine("BARCODE-MUG", "Stoneware mug", 2, 24900)));

        OrderReceived event = new OrderMapper().toEvent("trendyol", remote);

        assertEquals("trendyol", event.marketplace());
        assertEquals("TY42", event.externalOrderId());
        assertEquals("Ada Lovelace", event.customerName());
        assertEquals(Instant.parse("2026-05-01T08:00:00Z"), event.orderedAt());
        assertEquals(1, event.lines().size());
        assertEquals(24900, event.lines().getFirst().unitPriceMinor());
        assertEquals(2, event.lines().getFirst().quantity());
    }
}
