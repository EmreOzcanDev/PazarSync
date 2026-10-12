package dev.emreozcan.pazarsync.mock;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketplaceStoreTest {

    @Test
    void repeatsTheSameOrderWhenDuplicateFaultsAreOn() {
        MockMarketplaceProperties properties = new MockMarketplaceProperties();
        properties.getFaults().setEnabled(true);
        properties.getFaults().setDuplicateRate(1);
        MarketplaceStore store = new MarketplaceStore(properties, () -> 0.0);
        store.addOrder(new MarketplaceOrder(
                "TY1",
                1_700_000_000_000L,
                "Ada",
                "Lovelace",
                List.of(new MarketplaceLine("BARCODE-MUG", "Stoneware mug", 1, 24900))));

        List<MarketplaceOrder> page = store.ordersBetween(0, Long.MAX_VALUE);

        assertEquals(2, page.size());
        assertEquals("TY1", page.get(0).orderNumber());
        assertEquals(page.get(0), page.get(1));
    }
}
