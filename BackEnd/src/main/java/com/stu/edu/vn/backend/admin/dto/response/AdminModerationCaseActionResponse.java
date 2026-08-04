package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import java.time.LocalDateTime;

/** Lịch sử quyết định quản trị gắn trực tiếp với Moderation Case. */
public record AdminModerationCaseActionResponse(
        Long actionId,
        AdminActionType actionType,
        Long adminId,
        String adminDisplayName,
        String note,
        LocalDateTime createdAt
) {
}
