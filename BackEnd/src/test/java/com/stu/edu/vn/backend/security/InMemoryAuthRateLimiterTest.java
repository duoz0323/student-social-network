package com.stu.edu.vn.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class InMemoryAuthRateLimiterTest {
    @Test
    void allowsWithinQuotaRejectsExcessAndCanReset() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(2);
        properties.setWindow(Duration.ofMinutes(1));
        InMemoryAuthRateLimiter limiter = new InMemoryAuthRateLimiter(properties,
                Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.UTC));

        assertThat(limiter.acquire("opaque-key").allowed()).isTrue();
        assertThat(limiter.acquire("opaque-key").allowed()).isTrue();
        RateLimitDecision rejected = limiter.acquire("opaque-key");
        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.retryAfterSeconds()).isEqualTo(60);
        assertThat(limiter.acquire("other-user-key").allowed()).isTrue();

        limiter.reset();
        assertThat(limiter.acquire("opaque-key").allowed()).isTrue();
    }

    @Test
    void disabledLimiterNeverConsumesQuota() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties();
        properties.setEnabled(false);
        properties.setMaxRequests(1);
        InMemoryAuthRateLimiter limiter = new InMemoryAuthRateLimiter(properties, Clock.systemUTC());

        assertThat(limiter.acquire("key").allowed()).isTrue();
        assertThat(limiter.acquire("key").allowed()).isTrue();
    }
}
