package com.stu.edu.vn.backend.search.dto.response;

/**
 * Thông tin hồ sơ công khai trong kết quả tìm kiếm, không chứa dữ liệu xác thực nhạy cảm.
 */
public record SearchUserResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        String bio,
        boolean followedByCurrentUser
) {
}
