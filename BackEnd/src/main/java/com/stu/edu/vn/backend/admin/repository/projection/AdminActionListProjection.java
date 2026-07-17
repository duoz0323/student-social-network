package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection scalar của danh sách lịch sử, không đọc JSON chi tiết. */
public interface AdminActionListProjection {
    Long getActionId();

    String getActionType();

    Long getAdminId();

    String getAdminDisplayName();

    String getAdminAvatarUrl();

    String getTargetType();

    Long getTargetId();

    String getNote();

    LocalDateTime getCreatedAt();
}
