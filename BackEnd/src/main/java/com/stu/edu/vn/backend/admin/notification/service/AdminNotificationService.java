package com.stu.edu.vn.backend.admin.notification.service;

import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationMutationResponse;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationResponse;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationUnreadCountResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;

public interface AdminNotificationService {
    CursorPageResponse<AdminNotificationResponse> getNotifications(int limit, String cursor);
    AdminNotificationUnreadCountResponse getUnreadCount();
    AdminNotificationMutationResponse markRead(Long notificationId);
    int markAllRead();
    AdminNotificationMutationResponse delete(Long notificationId);
}
