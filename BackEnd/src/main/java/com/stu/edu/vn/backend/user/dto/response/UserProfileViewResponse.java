package com.stu.edu.vn.backend.user.dto.response;

import java.time.LocalDate;

/**
 * Dữ liệu hiển thị trang hồ sơ; không chứa email hoặc dữ liệu xác thực nhạy cảm.
 */
public record UserProfileViewResponse(
        Long userId,
        String username,
        String displayName,
        String avatarUrl,
        LocalDate dateOfBirth,
        String bio,
        long followerCount,
        long followingCount,
        boolean followedByCurrentUser,
        boolean blockedByMe,
        boolean restrictedByMe
) {
}
