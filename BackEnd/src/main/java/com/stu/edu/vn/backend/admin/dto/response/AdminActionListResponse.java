package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import java.time.LocalDateTime;

/** Phần tử lịch sử dạng danh sách, chủ động không chứa oldData/newData. */
public record AdminActionListResponse(
        Long actionId,
        AdminActionType actionType,
        String actionLabel,
        AdminActionAdminResponse admin,
        AdminActionTargetResponse target,
        String note,
        LocalDateTime createdAt
) {
}
