package com.stu.edu.vn.backend.security;

/** Kết quả limiter không chứa key nội bộ hoặc định danh Client. */
public record RateLimitDecision(boolean allowed, long retryAfterSeconds) {
    static RateLimitDecision allow() {
        return new RateLimitDecision(true, 0);
    }

    static RateLimitDecision reject(long retryAfterSeconds) {
        return new RateLimitDecision(false, Math.max(1, retryAfterSeconds));
    }
}
