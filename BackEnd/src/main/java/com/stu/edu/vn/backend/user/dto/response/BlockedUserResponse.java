package com.stu.edu.vn.backend.user.dto.response;

import java.time.LocalDateTime;

/** Thông tin công khai tối thiểu để chủ tài khoản quản lý danh sách chặn. */
public record BlockedUserResponse(Long userId, String displayName, String avatarUrl, LocalDateTime blockedAt) {
}
