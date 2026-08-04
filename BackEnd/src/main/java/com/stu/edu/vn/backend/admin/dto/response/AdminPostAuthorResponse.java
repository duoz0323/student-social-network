package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.user.enums.UserStatus;

/** Thông tin tác giả an toàn được phép hiển thị cho ADMIN. */
public record AdminPostAuthorResponse(Long userId, String displayName, String avatarUrl,
        String email, UserStatus accountStatus) {
}
