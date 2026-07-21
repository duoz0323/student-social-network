package com.stu.edu.vn.backend.security;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Fixed-window limiter đơn giản cho một application instance. */
@Component
public class InMemoryAuthRateLimiter implements AuthRateLimiter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AuthRateLimitProperties properties;
    private final Clock clock;
    private final AtomicLong acquisitions = new AtomicLong();

    public InMemoryAuthRateLimiter(AuthRateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public RateLimitDecision acquire(String opaqueKey) {
        if (!properties.isEnabled()) return RateLimitDecision.allow();
        Instant now = clock.instant();
        if ((acquisitions.incrementAndGet() & 1023) == 0) {
            windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        }
        Window window = windows.compute(opaqueKey, (key, current) ->
                current == null || !current.expiresAt().isAfter(now)
                        ? new Window(now.plus(properties.getWindow()), new AtomicInteger(1))
                        : increment(current));
        if (window.count().get() <= Math.max(1, properties.getMaxRequests())) {
            return RateLimitDecision.allow();
        }
        long retry = java.time.Duration.between(now, window.expiresAt()).toSeconds();
        return RateLimitDecision.reject(retry);
    }

    @Override
    public void reset() {
        windows.clear();
    }

    private Window increment(Window window) {
        window.count().incrementAndGet();
        return window;
    }

    private record Window(Instant expiresAt, AtomicInteger count) {
    }
}
