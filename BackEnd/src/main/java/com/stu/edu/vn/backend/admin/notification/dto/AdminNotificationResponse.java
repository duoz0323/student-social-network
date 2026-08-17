package com.stu.edu.vn.backend.admin.notification.dto;

import java.time.LocalDateTime;

/** Response không lộ actor hoặc metadata RBAC nội bộ không cần thiết. */
public record AdminNotificationResponse(
        Long notificationId,
        String type,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}
