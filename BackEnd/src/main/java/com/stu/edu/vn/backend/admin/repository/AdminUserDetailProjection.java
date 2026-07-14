package com.stu.edu.vn.backend.admin.repository;

import java.time.LocalDateTime;

/**
 * Projection một dòng cho chi tiết tài khoản, có role nội bộ để Service chặn target ADMIN.
 */
public interface AdminUserDetailProjection {

    Long getUserId();

    String getDisplayName();

    String getAvatarUrl();

    String getBio();

    String getEmail();

    String getPhoneNumber();

    String getRole();

    String getStatus();

    LocalDateTime getProfileCompletedAt();

    LocalDateTime getBlockedAt();

    String getBlockedReason();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
