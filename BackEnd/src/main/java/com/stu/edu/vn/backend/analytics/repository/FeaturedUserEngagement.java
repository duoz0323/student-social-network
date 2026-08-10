package com.stu.edu.vn.backend.analytics.repository;

/**
 * Projection nội bộ kết hợp bài viết PUBLISHED và activity hợp lệ của một USER trong ngày.
 */
public record FeaturedUserEngagement(
        Long userId,
        String displayName,
        String avatarUrl,
        long postCount,
        long interactionCount
) {
}
