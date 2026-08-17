package com.stu.edu.vn.backend.follow.dto.response;

import java.time.LocalDateTime;
import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.List;

/**
 * Thông tin công khai của một người dùng trong danh sách Follow, không chứa dữ liệu xác thực nhạy cảm.
 */
public record FollowUserResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        String bio,
        LocalDateTime followedAt,
        boolean followedByCurrentUser,
        List<PublicUserBadge> badges
) {
    public FollowUserResponse(Long userId, String displayName, String avatarUrl, String bio,
                              LocalDateTime followedAt, boolean followedByCurrentUser) {
        this(userId, displayName, avatarUrl, bio, followedAt, followedByCurrentUser, List.of());
    }
}
