package com.stu.edu.vn.backend.search.dto.response;

import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.List;

/**
 * Thông tin hồ sơ công khai trong kết quả tìm kiếm, không chứa dữ liệu xác thực nhạy cảm.
 */
public record SearchUserResponse(
        Long userId,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        boolean followedByCurrentUser,
        List<PublicUserBadge> badges
) {
    public SearchUserResponse(Long userId, String displayName, String avatarUrl, String bio,
                              boolean followedByCurrentUser) {
        this(userId, null, displayName, avatarUrl, bio, followedByCurrentUser, List.of());
    }
}
