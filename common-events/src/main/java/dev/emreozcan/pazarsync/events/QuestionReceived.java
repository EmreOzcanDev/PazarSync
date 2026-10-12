package dev.emreozcan.pazarsync.events;

import java.time.Instant;

public record QuestionReceived(
        String marketplace,
        String externalQuestionId,
        String productBarcode,
        String customerName,
        String text,
        Instant askedAt) {
}
