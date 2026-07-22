package com.stu.edu.vn.backend.admin.dto.response;

/** Thông tin công khai tối thiểu của ADMIN đã thực hiện thao tác. */
public record AdminActionAdminResponse(
        Long adminId,
        String displayName,
        String avatarUrl
) {
}
