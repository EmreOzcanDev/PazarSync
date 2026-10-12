package dev.emreozcan.pazarsync.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventCodecTest {

    @Test
    void roundTripsAnOrder() {
        OrderReceived order = new OrderReceived(
                "trendyol",
                "TY-1",
                "Ada Lovelace",
                Instant.parse("2026-04-01T10:15:30Z"),
                List.of(new OrderLinePayload("BARCODE-MUG", "Stoneware mug", 2, 24900)));

        OrderReceived restored = EventCodec.read(EventCodec.write(order), OrderReceived.class);

        assertEquals(order, restored);
        assertEquals("trendyol:TY-1", EventKeys.of(order.marketplace(), order.externalOrderId()));
    }
}
