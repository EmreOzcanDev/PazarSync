package dev.emreozcan.pazarsync.mock;

public record MarketplaceQuestion(
        long id,
        String productBarcode,
        String productName,
        String userName,
        String text,
        long creationDate,
        String status,
        String answerText) {
}
