package com.stu.edu.vn.backend.notification.event;

/** Sự kiện realtime tối thiểu được phát trong transaction và chỉ xử lý sau commit. */
public record PostRepostNotificationEvent(
        Long actorId,
        Long recipientId,
        Long postId
) {
}
