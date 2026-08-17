package com.stu.edu.vn.backend.admin.notification.repository.projection;

import java.time.LocalDateTime;

/** Projection nhỏ dùng chung cho REST list và payload realtime. */
public interface AdminNotificationProjection {
    Long getNotificationId();
    String getType();
    String getTitle();
    String getMessage();
    String getRequiredPermissionCode();
    String getReferenceType();
    Long getReferenceId();
    LocalDateTime getReadAt();
    LocalDateTime getCreatedAt();
}
