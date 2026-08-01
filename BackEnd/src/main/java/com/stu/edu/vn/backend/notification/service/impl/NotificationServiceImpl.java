package com.stu.edu.vn.backend.notification.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.notification.dto.response.DeleteNotificationResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationReadAllResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationReadResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationUnreadCountResponse;
import com.stu.edu.vn.backend.notification.entity.Notification;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.event.NotificationCreatedEvent;
import com.stu.edu.vn.backend.notification.mapper.NotificationMapper;
import com.stu.edu.vn.backend.notification.repository.NotificationRepository;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai thông báo đồng bộ trong cùng transaction với Follow, Like và Comment.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EntityManager entityManager;
    private final Clock clock;
    private final UserRelationshipPolicyService relationshipPolicyService;
    private final UserRestrictionRepository userRestrictionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            EntityManager entityManager,
            Clock clock,
            UserRelationshipPolicyService relationshipPolicyService,
            UserRestrictionRepository userRestrictionRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.entityManager = entityManager;
        this.clock = clock;
        this.relationshipPolicyService = relationshipPolicyService;
        this.userRestrictionRepository = userRestrictionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(int page, int size) {
        Long currentUserId = getEligibleCurrentUserId();
        return PageResponse.from(notificationRepository
                .findVisibleNotifications(currentUserId, PageRequest.of(page, size))
                .map(notificationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse getUnreadCount() {
        Long currentUserId = getEligibleCurrentUserId();
        long unreadCount = notificationRepository
                .countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(currentUserId);
        return new NotificationUnreadCountResponse(unreadCount);
    }

    @Override
    @Transactional
    public NotificationReadResponse markAsRead(Long notificationId) {
        Long currentUserId = getEligibleCurrentUserId();
        Notification notification = findOwnedVisibleNotification(notificationId, currentUserId);
        notification.markRead(LocalDateTime.now(clock));
        return new NotificationReadResponse(notification.getId(), notification.getReadAt());
    }

    @Override
    @Transactional
    public NotificationReadAllResponse markAllAsRead() {
        Long currentUserId = getEligibleCurrentUserId();
        int updatedCount = notificationRepository.markAllRead(currentUserId, LocalDateTime.now(clock));
        return new NotificationReadAllResponse(updatedCount);
    }

    @Override
    @Transactional
    public DeleteNotificationResponse deleteNotification(Long notificationId) {
        Long currentUserId = getEligibleCurrentUserId();
        Notification notification = findOwnedVisibleNotification(notificationId, currentUserId);
        notification.hide(LocalDateTime.now(clock));
        return new DeleteNotificationResponse(notificationId, true);
    }

    @Override
    @Transactional
    public void createFollowNotification(Long actorId, Long recipientId) {
        createNotification(actorId, recipientId, NotificationType.FOLLOW, null, null, null);
    }

    @Override
    @Transactional
    public void deleteFollowNotification(Long actorId, Long recipientId) {
        // Dữ liệu cũ trước migration có thể không có notification nên không coi 0 dòng là lỗi.
        notificationRepository.deleteFollowNotification(NotificationType.FOLLOW, actorId, recipientId);
    }

    @Override
    @Transactional
    public void createPostLikeNotification(Long actorId, Long recipientId, Long postId) {
        createNotification(actorId, recipientId, NotificationType.POST_LIKE, postId, null, null);
    }

    @Override
    @Transactional
    public void deletePostLikeNotification(Long actorId, Long postId) {
        notificationRepository.deletePostLikeNotification(NotificationType.POST_LIKE, actorId, postId);
    }

    @Override
    @Transactional
    public void createPostCommentNotification(Long actorId, Long recipientId, Long postId, Long commentId) {
        createNotification(actorId, recipientId, NotificationType.POST_COMMENT, postId, commentId, null);
    }

    @Override
    @Transactional
    public void createCommentReplyNotification(Long actorId, Long recipientId, Long postId, Long commentId) {
        createNotification(actorId, recipientId, NotificationType.COMMENT_REPLY, postId, commentId, null);
    }

    @Override
    @Transactional
    public void deleteCommentNotification(Long commentId) {
        // comment_id chỉ thuộc POST_COMMENT hoặc COMMENT_REPLY theo constraint database.
        notificationRepository.deleteCommentNotification(commentId);
    }

    @Override
    @Transactional
    public void createReportResolvedNotification(Long recipientId, Long reportId) {
        createNotification(null, recipientId, NotificationType.REPORT_RESOLVED, null, null, reportId);
    }

    @Override
    @Transactional
    public void createReportRejectedNotification(Long recipientId, Long reportId) {
        createNotification(null, recipientId, NotificationType.REPORT_REJECTED, null, null, reportId);
    }

    @Override
    @Transactional
    public void createPostHiddenByAdminNotification(Long recipientId, Long postId) {
        createNotification(null, recipientId, NotificationType.POST_HIDDEN_BY_ADMIN, postId, null, null);
    }

    @Override
    @Transactional
    public void createPostRestoredByAdminNotification(Long recipientId, Long postId) {
        createNotification(null, recipientId, NotificationType.POST_RESTORED_BY_ADMIN, postId, null, null);
    }

    @Override
    @Transactional
    public void createAccountBlockedNotification(Long recipientId) {
        createNotification(null, recipientId, NotificationType.ACCOUNT_BLOCKED, null, null, null);
    }

    @Override
    @Transactional
    public void createAccountUnblockedNotification(Long recipientId) {
        createNotification(null, recipientId, NotificationType.ACCOUNT_UNBLOCKED, null, null, null);
    }

    private void createNotification(
            Long actorId,
            Long recipientId,
            NotificationType type,
            Long postId,
            Long commentId,
            Long reportId
    ) {
        if (actorId != null && actorId.equals(recipientId)) {
            // Người thực hiện đã biết hành động của mình nên không tạo tự thông báo.
            return;
        }
        if (actorId != null && relationshipPolicyService.existsBlockEitherDirection(actorId, recipientId)) {
            // Block không phát sinh thông báo mới và không tiết lộ hành động chặn cho đối phương.
            return;
        }
        if (actorId != null && isRestrictedInteractionNotification(type)
                && userRestrictionRepository.existsByIdRestrictorIdAndIdRestrictedId(recipientId, actorId)) {
            // Suppress trước khi lưu để unread count, badge và event không bao giờ được phát sinh.
            return;
        }
        User actor = actorId == null ? null : entityManager.getReference(User.class, actorId);
        User recipient = entityManager.getReference(User.class, recipientId);
        Post post = postId == null ? null : entityManager.getReference(Post.class, postId);
        Comment comment = commentId == null ? null : entityManager.getReference(Comment.class, commentId);
        Report report = reportId == null ? null : entityManager.getReference(Report.class, reportId);
        Notification notification = notificationRepository.saveAndFlush(
                new Notification(recipient, actor, type, post, comment, report));
        // Event chỉ mang định danh; listener sẽ đọc lại dữ liệu đã commit và áp dụng lại visibility.
        eventPublisher.publishEvent(new NotificationCreatedEvent(notification.getId(), recipientId));
    }

    private boolean isRestrictedInteractionNotification(NotificationType type) {
        return type == NotificationType.POST_LIKE
                || type == NotificationType.POST_COMMENT
                || type == NotificationType.COMMENT_REPLY;
    }

    private Long getEligibleCurrentUserId() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        UserProfile profile = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return currentUserId;
    }

    private Notification findOwnedVisibleNotification(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository
                .findByIdAndRecipient_IdAndDeletedAtIsNull(notificationId, currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.getActor() != null
                && relationshipPolicyService.existsBlockEitherDirection(
                currentUserId, notification.getActor().getId())) {
            // Notification đã bị ẩn bởi Block cũng không được thao tác qua endpoint trực tiếp.
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        return notification;
    }
}
