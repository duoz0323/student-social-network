package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.user.enums.UserStatus;

/** Thông tin công khai tối thiểu của người dùng xuất hiện trong báo cáo quản trị. */
public record AdminReportUserResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        UserStatus accountStatus
) {
}
