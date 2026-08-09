package com.stu.edu.vn.backend.admin.dto.response;

import java.time.LocalDateTime;

/** Dữ liệu tối thiểu của một hashtag dùng trong danh sách quản trị. */
public record AdminHashtagListItemResponse(
        Long hashtagId,
        String name,
        int postCount,
        LocalDateTime createdAt,
        LocalDateTime latestUsedAt
) {
}
