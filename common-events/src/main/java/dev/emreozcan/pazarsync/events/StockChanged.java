package dev.emreozcan.pazarsync.events;

public record StockChanged(
        String marketplace,
        String barcode,
        int quantity,
        long salePriceMinor,
        boolean lowStock) {
}
