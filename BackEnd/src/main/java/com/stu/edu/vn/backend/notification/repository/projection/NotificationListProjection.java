package com.stu.edu.vn.backend.notification.repository.projection;

import java.time.LocalDateTime;

/**
 * Projection danh sách chỉ lấy dữ liệu hiển thị cần thiết để tránh N+1 và dữ liệu nhạy cảm.
 */
public interface NotificationListProjection {

    Long getNotificationId();

    String getType();

    Long getActorId();

    String getActorDisplayName();

    String getActorAvatarUrl();

    Long getPostId();

    Long getCommentId();

    Long getReportId();

    LocalDateTime getReadAt();

    LocalDateTime getCreatedAt();
}
