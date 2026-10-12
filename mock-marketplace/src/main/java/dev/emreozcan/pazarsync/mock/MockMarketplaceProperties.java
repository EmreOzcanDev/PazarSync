package dev.emreozcan.pazarsync.mock;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pazarsync.mock")
public class MockMarketplaceProperties {

    private long supplierId = 1001;
    private final Faults faults = new Faults();
    private final Generator generator = new Generator();

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public Faults getFaults() {
        return faults;
    }

    public Generator getGenerator() {
        return generator;
    }

    public static class Faults {
        private boolean enabled = true;
        private double failureRate = 0.1;
        private double duplicateRate = 0.15;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getFailureRate() {
            return failureRate;
        }

        public void setFailureRate(double failureRate) {
            this.failureRate = failureRate;
        }

        public double getDuplicateRate() {
            return duplicateRate;
        }

        public void setDuplicateRate(double duplicateRate) {
            this.duplicateRate = duplicateRate;
        }
    }

    public static class Generator {
        private boolean enabled = true;
        private long delayMs = 4000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getDelayMs() {
            return delayMs;
        }

        public void setDelayMs(long delayMs) {
            this.delayMs = delayMs;
        }
    }
}
