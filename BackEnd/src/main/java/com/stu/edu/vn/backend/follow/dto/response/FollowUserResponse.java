package com.stu.edu.vn.backend.follow.dto.response;

import java.time.LocalDateTime;

/**
 * Thông tin công khai của một người dùng trong danh sách Follow, không chứa dữ liệu xác thực nhạy cảm.
 */
public record FollowUserResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        String bio,
        LocalDateTime followedAt,
        boolean followedByCurrentUser
) {
}
