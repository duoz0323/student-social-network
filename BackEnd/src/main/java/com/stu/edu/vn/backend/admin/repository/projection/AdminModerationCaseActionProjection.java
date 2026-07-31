package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection lịch sử Admin Action của một case. */
public interface AdminModerationCaseActionProjection {
    Long getActionId();
    String getActionType();
    Long getAdminId();
    String getAdminDisplayName();
    String getNote();
    LocalDateTime getCreatedAt();
}
