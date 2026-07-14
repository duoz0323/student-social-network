package com.stu.edu.vn.backend.admin.repository;

import java.time.LocalDateTime;

/**
 * Projection chỉ nhận các cột cần cho danh sách, tránh tải Entity và dữ liệu xác thực nhạy cảm.
 */
public interface AdminUserListProjection {

    Long getUserId();

    String getDisplayName();

    String getAvatarUrl();

    String getEmail();

    String getPhoneNumber();

    String getStatus();

    LocalDateTime getProfileCompletedAt();

    LocalDateTime getCreatedAt();
}
