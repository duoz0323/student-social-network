package com.stu.edu.vn.backend.admin.rbac.repository;

import java.time.LocalDateTime;

/** Projection danh sách admin, không đọc password_hash. */
public interface AdminAccountListProjection {
    Long getAdminId();
    String getEmail();
    String getUsername();
    String getDisplayName();
    String getStatus();
    String getRoleCodes();
    LocalDateTime getCreatedAt();
}
