package com.stu.edu.vn.backend.user.repository;

import java.time.LocalDateTime;

/** Projection tối thiểu cho trang quản lý tài khoản đã chặn. */
public interface BlockedUserProjection {
    Long getUserId();
    String getDisplayName();
    String getAvatarUrl();
    LocalDateTime getBlockedAt();
}
