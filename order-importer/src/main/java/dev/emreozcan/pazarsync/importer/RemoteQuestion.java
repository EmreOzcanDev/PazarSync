package dev.emreozcan.pazarsync.importer;

public record RemoteQuestion(
        long id,
        String productBarcode,
        String productName,
        String userName,
        String text,
        long creationDate,
        String status,
        String answerText) {
}
