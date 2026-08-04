package com.stu.edu.vn.backend.messaging.dto.response;

/** Thông tin profile công khai tối thiểu, không chứa email hoặc dữ liệu xác thực. */
public record MessagingUserResponse(Long userId, String displayName, String avatarUrl) { }
