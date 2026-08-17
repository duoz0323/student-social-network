package com.stu.edu.vn.backend.admin.notification.dto;

import java.time.LocalDateTime;

public record AdminNotificationMutationResponse(Long notificationId, LocalDateTime readAt, boolean deleted) {}
