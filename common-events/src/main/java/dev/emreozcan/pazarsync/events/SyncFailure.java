package dev.emreozcan.pazarsync.events;

import java.time.Instant;

public record SyncFailure(
        String marketplace,
        String operation,
        String externalId,
        String retryTopic,
        String retryKey,
        String payload,
        String errorMessage,
        Instant failedAt) {
}
