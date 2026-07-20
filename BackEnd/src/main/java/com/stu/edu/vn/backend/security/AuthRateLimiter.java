package com.stu.edu.vn.backend.security;

/** Abstraction để có thể thay in-memory limiter bằng shared store khi triển khai nhiều instance. */
public interface AuthRateLimiter {
    RateLimitDecision acquire(String opaqueKey);
    void reset();
}
