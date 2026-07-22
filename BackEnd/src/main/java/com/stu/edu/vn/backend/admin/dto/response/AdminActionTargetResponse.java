package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.admin.enums.AdminTargetType;

/** Mô tả target đa hình và trạng thái tồn tại hiện tại của target. */
public record AdminActionTargetResponse(
        AdminTargetType targetType,
        Long targetId,
        String displayText,
        boolean targetAvailable
) {
}
