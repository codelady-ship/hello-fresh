package io.github.codeladyship.hellofresh.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    /** Clock that tests can move forward. */
    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-18T10:00:00Z");

        void advance(Duration d) { now = now.plus(d); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void allowsUpToTheLimitThenBlocksUntilTheWindowResets() {
        MutableClock clock = new MutableClock();
        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1), clock);

        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryAcquire("1.2.3.4")).isZero();
        }
        clock.advance(Duration.ofSeconds(20));
        assertThat(limiter.tryAcquire("1.2.3.4")).isEqualTo(40);
        assertThat(limiter.tryAcquire("5.6.7.8")).as("other IPs are not affected").isZero();

        clock.advance(Duration.ofSeconds(40));
        assertThat(limiter.tryAcquire("1.2.3.4")).as("new window").isZero();
    }

    @Test
    void evictsExpiredWindows() {
        MutableClock clock = new MutableClock();
        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1), clock);
        limiter.tryAcquire("a");
        limiter.tryAcquire("b");

        clock.advance(Duration.ofSeconds(61));
        limiter.tryAcquire("c");
        limiter.evictExpired();

        assertThat(limiter.trackedKeys()).isEqualTo(1);
    }

    @Test
    void clientIpPrefersFirstForwardedForEntry() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        assertThat(PlanRequestRateLimitInterceptor.clientIp(request)).isEqualTo("10.0.0.1");

        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.1.2.3");
        assertThat(PlanRequestRateLimitInterceptor.clientIp(request)).isEqualTo("203.0.113.9");
    }
}
