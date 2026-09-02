package com.ruchi.order_update_service.throttle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void shouldRejectNonPositiveRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimiter(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimiter(-1)
        );
    }

    @Test
    void shouldThrottleEvents() {

        RateLimiter rateLimiter = new RateLimiter(10);

        long start = System.nanoTime();

        rateLimiter.acquire();
        rateLimiter.acquire();

        long elapsedMillis =
                (System.nanoTime() - start) / 1_000_000;

        assertTrue(
                elapsedMillis >= 80,
                "Expected throttling delay, but elapsed time was "
                        + elapsedMillis + " ms"
        );
    }
}