package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.response.AdminActionDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionListResponse;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.common.api.PageResponse;
import java.time.LocalDateTime;

/** Contract chỉ đọc lịch sử thao tác quản trị. */
public interface AdminActionService {
    PageResponse<AdminActionListResponse> getActions(
            AdminActionType actionType,
            AdminTargetType targetType,
            Long adminId,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );

    AdminActionDetailResponse getActionDetail(Long actionId);
}
