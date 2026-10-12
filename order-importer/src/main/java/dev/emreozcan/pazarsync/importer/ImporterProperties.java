package dev.emreozcan.pazarsync.importer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pazarsync.importer")
public class ImporterProperties {

    private String marketplaceBaseUrl = "http://localhost:8082";
    private long supplierId = 1001;
    private String marketplace = "trendyol";
    private long pollDelayMs = 5000;
    private final Retry retry = new Retry();

    public String getMarketplaceBaseUrl() {
        return marketplaceBaseUrl;
    }

    public void setMarketplaceBaseUrl(String marketplaceBaseUrl) {
        this.marketplaceBaseUrl = marketplaceBaseUrl;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = marketplace;
    }

    public long getPollDelayMs() {
        return pollDelayMs;
    }

    public void setPollDelayMs(long pollDelayMs) {
        this.pollDelayMs = pollDelayMs;
    }

    public Retry getRetry() {
        return retry;
    }

    public static class Retry {
        private int maxAttempts = 4;
        private long initialBackoffMs = 200;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialBackoffMs() {
            return initialBackoffMs;
        }

        public void setInitialBackoffMs(long initialBackoffMs) {
            this.initialBackoffMs = initialBackoffMs;
        }
    }
}
