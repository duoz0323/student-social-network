package com.stu.edu.vn.backend.notification.service;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.notification.dto.response.DeleteNotificationResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationReadAllResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationReadResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationUnreadCountResponse;

/**
 * Contract truy vấn hộp thông báo và các lệnh nội bộ được gọi từ nghiệp vụ nguồn.
 */
public interface NotificationService {

    PageResponse<NotificationResponse> getNotifications(int page, int size);

    NotificationUnreadCountResponse getUnreadCount();

    NotificationReadResponse markAsRead(Long notificationId);

    NotificationReadAllResponse markAllAsRead();

    DeleteNotificationResponse deleteNotification(Long notificationId);

    void createFollowNotification(Long actorId, Long recipientId);

    void deleteFollowNotification(Long actorId, Long recipientId);

    void createPostLikeNotification(Long actorId, Long recipientId, Long postId);

    void deletePostLikeNotification(Long actorId, Long postId);

    void createPostRepostNotification(Long actorId, Long recipientId, Long postId);

    void deletePostRepostNotification(Long actorId, Long postId);

    void createPostCommentNotification(Long actorId, Long recipientId, Long postId, Long commentId);

    void createCommentReplyNotification(Long actorId, Long recipientId, Long postId, Long commentId);

    void deleteCommentNotification(Long commentId);

    void createReportResolvedNotification(Long recipientId, Long reportId);

    void createReportRejectedNotification(Long recipientId, Long reportId);

    void createPostHiddenByAdminNotification(Long recipientId, Long postId);

    void createPostRestoredByAdminNotification(Long recipientId, Long postId);

    void createUserProfileUpdatedByAdminNotification(Long recipientId);

    void createAccountBlockedNotification(Long recipientId);

    void createAccountUnblockedNotification(Long recipientId);
}
