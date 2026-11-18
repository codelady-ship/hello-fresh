package io.github.codeladyship.hellofresh.config;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory fixed-window rate limiter keyed by client IP. Good enough for one free
 * instance; counters reset on restart and are not shared between instances.
 */
public class RateLimiter {

    private final int limit;
    private final long windowMillis;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(int limit, Duration window, Clock clock) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
        this.limit = limit;
        this.windowMillis = window.toMillis();
        this.clock = clock;
    }

    /** Counts one request for {@code key}; returns 0 if allowed, otherwise seconds until the window resets. */
    public long tryAcquire(String key) {
        long now = clock.millis();
        Window w = windows.compute(key, (k, current) ->
                current == null || now - current.start >= windowMillis ? new Window(now, 1) : current.increment());
        if (w.count <= limit) {
            return 0;
        }
        return Math.max(1, (w.start + windowMillis - now + 999) / 1000);
    }

    /** Drops expired windows so the map does not grow without bound. */
    public void evictExpired() {
        long now = clock.millis();
        windows.values().removeIf(w -> now - w.start >= windowMillis);
    }

    int trackedKeys() {
        return windows.size();
    }

    public int getLimit() {
        return limit;
    }

    private record Window(long start, int count) {
        Window increment() {
            return new Window(start, count + 1);
        }
    }
}
