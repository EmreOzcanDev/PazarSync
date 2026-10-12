package dev.emreozcan.pazarsync.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_failures")
public class StoredSyncFailure {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String marketplace;

    @Column(nullable = false)
    private String operation;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "retry_topic", nullable = false)
    private String retryTopic;

    @Column(name = "retry_key")
    private String retryKey;

    @Column(nullable = false)
    private String payload;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = marketplace;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getRetryTopic() {
        return retryTopic;
    }

    public void setRetryTopic(String retryTopic) {
        this.retryTopic = retryTopic;
    }

    public String getRetryKey() {
        return retryKey;
    }

    public void setRetryKey(String retryKey) {
        this.retryKey = retryKey;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
