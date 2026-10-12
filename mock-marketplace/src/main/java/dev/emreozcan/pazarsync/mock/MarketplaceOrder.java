package dev.emreozcan.pazarsync.mock;

public record MarketplaceOrder(
        String orderNumber,
        long orderDate,
        String customerFirstName,
        String customerLastName,
        java.util.List<MarketplaceLine> lines) {
}
