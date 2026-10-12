package dev.emreozcan.pazarsync.events;

public record OrderLinePayload(String barcode, String productName, int quantity, long unitPriceMinor) {
}
