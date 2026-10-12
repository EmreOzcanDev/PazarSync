package dev.emreozcan.pazarsync.importer;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryerTest {

    @Test
    void retriesUntilTheCallSucceeds() {
        AtomicInteger attempts = new AtomicInteger();
        Retryer retryer = new Retryer(duration -> { });

        String value = retryer.run(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("not yet");
            }
            return "ok";
        }, 4, Duration.ofMillis(10));

        assertEquals("ok", value);
        assertEquals(3, attempts.get());
    }

    @Test
    void givesUpAfterTheLastAttempt() {
        AtomicInteger attempts = new AtomicInteger();
        Retryer retryer = new Retryer(duration -> { });

        assertThrows(IllegalStateException.class, () -> retryer.run(() -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("down");
        }, 3, Duration.ofMillis(1)));

        assertEquals(3, attempts.get());
    }
}
