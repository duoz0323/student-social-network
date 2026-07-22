package com.stu.edu.vn.backend.notification.mapper;

import com.stu.edu.vn.backend.notification.dto.response.NotificationActorResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationResponse;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.repository.projection.NotificationListProjection;
import org.springframework.stereotype.Component;

/**
 * Chuyển projection danh sách sang DTO công khai mà không truy cập quan hệ lazy.
 */
@Component
public class NotificationMapper {

    public NotificationResponse toResponse(NotificationListProjection source) {
        NotificationActorResponse actor = source.getActorId() == null
                ? null
                : new NotificationActorResponse(
                        source.getActorId(),
                        source.getActorDisplayName(),
                        source.getActorAvatarUrl()
                );
        return new NotificationResponse(
                source.getNotificationId(),
                NotificationType.valueOf(source.getType()),
                actor,
                source.getPostId(),
                source.getCommentId(),
                source.getReportId(),
                source.getReadAt(),
                source.getCreatedAt()
        );
    }
}
