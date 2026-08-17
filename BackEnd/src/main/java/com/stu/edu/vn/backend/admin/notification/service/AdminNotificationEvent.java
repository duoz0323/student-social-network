package com.stu.edu.vn.backend.admin.notification.service;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;

/** Command nội bộ do Backend tạo; Client không được quyết định recipient, actor hoặc permission. */
public record AdminNotificationEvent(
        AdminNotificationType type,
        String title,
        String message,
        AdminNotificationReferenceType referenceType,
        Long referenceId,
        String eventKey
) {}
