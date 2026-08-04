package com.stu.edu.vn.backend.messaging.realtime;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Fixed-window limiter 4 frame/giây, cục bộ trên một Backend instance. */
@Component
public class TypingRateLimiter {
    static final int MAX_FRAMES_PER_SECOND = 4;
    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong acquisitions = new AtomicLong();
    private final Clock clock;

    public TypingRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(Long userId) {
        if (userId == null) return false;
        Instant now = clock.instant();
        if ((acquisitions.incrementAndGet() & 255) == 0) {
            windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        }
        Window window = windows.compute(userId, (key, current) ->
                current == null || !current.expiresAt().isAfter(now)
                        ? new Window(now.plusSeconds(1), new AtomicInteger(1))
                        : increment(current));
        return window.count().get() <= MAX_FRAMES_PER_SECOND;
    }

    int trackedUserCount() {
        return windows.size();
    }

    private Window increment(Window window) {
        window.count().incrementAndGet();
        return window;
    }

    private record Window(Instant expiresAt, AtomicInteger count) { }
}
