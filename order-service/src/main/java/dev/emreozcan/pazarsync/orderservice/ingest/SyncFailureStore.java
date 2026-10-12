package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.events.SyncFailure;
import dev.emreozcan.pazarsync.orderservice.domain.StoredSyncFailure;
import dev.emreozcan.pazarsync.orderservice.domain.StoredSyncFailureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SyncFailureStore {

    private final StoredSyncFailureRepository repository;

    public SyncFailureStore(StoredSyncFailureRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(SyncFailure event) {
        save(event.marketplace(), event.operation(), event.externalId(), event.retryTopic(), event.retryKey(),
                event.payload(), event.errorMessage(), event.failedAt());
    }

    @Transactional
    public void recordPoison(String topic, String key, String payload, String message) {
        save("trendyol", "POISON", key, topic, key, payload == null ? "" : payload, message, Instant.now());
    }

    private void save(
            String marketplace,
            String operation,
            String externalId,
            String retryTopic,
            String retryKey,
            String payload,
            String errorMessage,
            Instant createdAt) {
        StoredSyncFailure failure = new StoredSyncFailure();
        failure.setId(UUID.randomUUID());
        failure.setMarketplace(marketplace);
        failure.setOperation(operation);
        failure.setExternalId(externalId);
        failure.setRetryTopic(retryTopic);
        failure.setRetryKey(retryKey);
        failure.setPayload(payload == null ? "" : payload);
        failure.setErrorMessage(errorMessage == null ? "unknown" : errorMessage);
        failure.setCreatedAt(createdAt == null ? Instant.now() : createdAt);
        repository.save(failure);
    }
}
