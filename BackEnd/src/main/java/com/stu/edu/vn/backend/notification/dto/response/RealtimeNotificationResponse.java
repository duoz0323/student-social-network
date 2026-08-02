package com.stu.edu.vn.backend.notification.dto.response;

import com.stu.edu.vn.backend.notification.enums.NotificationType;

/** Payload realtime tối thiểu để Frontend biết cần đồng bộ lại notification qua REST. */
public record RealtimeNotificationResponse(
        NotificationType type,
        Long actorId,
        Long postId
) {
}
