package com.stu.edu.vn.backend.notification.dto.response;

import com.stu.edu.vn.backend.notification.enums.NotificationType;
import java.time.LocalDateTime;

/** Snapshot quyết định kiểm duyệt chỉ trả cho chính người nhận notification. */
public record ModerationNotificationDetailResponse(
        Long notificationId,
        NotificationType type,
        Long postId,
        String postSummary,
        String reasonCode,
        Integer violationCount,
        int violationThreshold,
        LocalDateTime processedAt
) {
}
