package dev.emreozcan.pazarsync.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class Retryer {

    private final Sleeper sleeper;

    @Autowired
    public Retryer() {
        this(Retryer::sleep);
    }

    Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    public <T> T run(Supplier<T> action, int maxAttempts, Duration initialBackoff) {
        RuntimeException last = null;
        Duration wait = initialBackoff;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException ex) {
                last = ex;
                if (attempt == maxAttempts) {
                    break;
                }
                sleeper.sleep(wait);
                wait = wait.multipliedBy(2);
            }
        }
        if (last == null) {
            throw new IllegalStateException("Retry produced no result");
        }
        throw last;
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", ex);
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration);
    }
}
