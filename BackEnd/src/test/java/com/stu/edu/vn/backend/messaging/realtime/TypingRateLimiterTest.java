package com.stu.edu.vn.backend.messaging.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TypingRateLimiterTest {
    @Test
    void allowsFourFramesPerSecondAndCleansExpiredWindows() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-03T03:00:00Z"));
        TypingRateLimiter limiter = new TypingRateLimiter(clock);

        assertThat(limiter.tryAcquire(10L)).isTrue();
        assertThat(limiter.tryAcquire(10L)).isTrue();
        assertThat(limiter.tryAcquire(10L)).isTrue();
        assertThat(limiter.tryAcquire(10L)).isTrue();
        assertThat(limiter.tryAcquire(10L)).isFalse();

        for (long index = 0; index < 200; index++) limiter.tryAcquire(1_000L + index);
        clock.advanceSeconds(2);
        for (long index = 0; index < 51; index++) limiter.tryAcquire(2_000L + index);
        assertThat(limiter.tryAcquire(10L)).isTrue();
        assertThat(limiter.trackedUserCount()).isLessThanOrEqualTo(52);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        void advanceSeconds(long seconds) { instant = instant.plusSeconds(seconds); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
