package com.stu.edu.vn.backend.admin.dto.request;

import java.time.LocalDate;

/**
 * Dữ liệu hồ sơ công khai mà ADMIN được phép chỉnh sửa cho tài khoản USER.
 */
public record AdminUpdateUserProfileRequest(
        String displayName,
        LocalDate dateOfBirth,
        String bio
) {
}
