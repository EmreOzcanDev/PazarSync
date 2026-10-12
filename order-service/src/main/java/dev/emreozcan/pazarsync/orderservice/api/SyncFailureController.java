package dev.emreozcan.pazarsync.orderservice.api;

import dev.emreozcan.pazarsync.orderservice.domain.StoredSyncFailure;
import dev.emreozcan.pazarsync.orderservice.domain.StoredSyncFailureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
public class SyncFailureController {

    private final StoredSyncFailureRepository failures;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SyncFailureController(StoredSyncFailureRepository failures, KafkaTemplate<String, String> kafkaTemplate) {
        this.failures = failures;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/api/sync-failures")
    public List<FailureResponse> list() {
        return failures.findByResolvedAtIsNullOrderByCreatedAtDesc().stream()
                .map(failure -> new FailureResponse(
                        failure.getId(),
                        failure.getMarketplace(),
                        failure.getOperation(),
                        failure.getExternalId(),
                        failure.getErrorMessage(),
                        failure.getCreatedAt(),
                        failure.getRetryTopic()))
                .toList();
    }

    @PostMapping("/api/sync-failures/{id}/retry")
    @Transactional
    public void retry(@PathVariable UUID id) {
        StoredSyncFailure failure = failures.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Failure not found"));
        kafkaTemplate.send(failure.getRetryTopic(), failure.getRetryKey(), failure.getPayload());
        failure.setResolvedAt(Instant.now());
    }

    public record FailureResponse(
            UUID id,
            String marketplace,
            String operation,
            String externalId,
            String errorMessage,
            Instant createdAt,
            String retryTopic) {
    }
}
