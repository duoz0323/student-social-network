package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import java.time.LocalDateTime;

/** Chi tiết lịch sử có snapshot JSON đã được lọc dữ liệu nhạy cảm. */
public record AdminActionDetailResponse(
        Long actionId,
        AdminActionType actionType,
        String actionLabel,
        AdminActionAdminResponse admin,
        AdminActionTargetResponse target,
        String note,
        LocalDateTime createdAt,
        Object oldData,
        Object newData
) {
}
