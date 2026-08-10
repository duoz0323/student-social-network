package com.stu.edu.vn.backend.analytics.dto;

/**
 * Người dùng nổi bật trong ngày, chỉ trả dữ liệu hồ sơ công khai cần cho bảng Dashboard.
 */
public record FeaturedUserResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        long postCount,
        long interactionCount
) {
}
