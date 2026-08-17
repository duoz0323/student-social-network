package com.stu.edu.vn.backend.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.entity.Notification;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.event.NotificationCreatedEvent;
import com.stu.edu.vn.backend.notification.mapper.NotificationMapper;
import com.stu.edu.vn.backend.notification.repository.NotificationRepository;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final NotificationRepository notificationRepository = org.mockito.Mockito.mock(NotificationRepository.class);
    private final NotificationMapper notificationMapper = org.mockito.Mockito.mock(NotificationMapper.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-07-18T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final UserRelationshipPolicyService relationshipPolicyService =
            org.mockito.Mockito.mock(UserRelationshipPolicyService.class);
    private final UserRestrictionRepository userRestrictionRepository =
            org.mockito.Mockito.mock(UserRestrictionRepository.class);
    private final ApplicationEventPublisher eventPublisher =
            org.mockito.Mockito.mock(ApplicationEventPublisher.class);

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                notificationRepository,
                notificationMapper,
                entityManager,
                clock,
                relationshipPolicyService,
                userRestrictionRepository,
                eventPublisher
        );
        when(notificationRepository.saveAndFlush(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", 99L);
            return notification;
        });
        when(currentUserProvider.getCurrentUserId()).thenReturn(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L)));
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(completedProfile(20L)));
    }

    @Test
    void selfInteractionDoesNotCreateNotification() {
        service.createPostLikeNotification(20L, 20L, 100L);

        verify(notificationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(entityManager, never()).getReference(any(), any());
    }

    @Test
    void followCreatesNotificationWithReferencedActorAndRecipient() {
        User actor = user(10L);
        User recipient = user(20L);
        when(entityManager.getReference(User.class, 10L)).thenReturn(actor);
        when(entityManager.getReference(User.class, 20L)).thenReturn(recipient);

        service.createFollowNotification(10L, 20L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.FOLLOW);
        assertThat(captor.getValue().getActor().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getRecipient().getId()).isEqualTo(20L);
        verify(eventPublisher).publishEvent(new NotificationCreatedEvent(99L, 20L));
    }

    @Test
    void restrictedLikeIsSuppressedBeforeNotificationIsSaved() {
        when(userRestrictionRepository
                .existsByIdRestrictorIdAndIdRestrictedId(20L, 10L)).thenReturn(true);

        service.createPostLikeNotification(10L, 20L, 100L);

        verify(notificationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(entityManager, never()).getReference(any(), any());
    }

    @Test
    void blockInEitherDirectionPreventsActorNotification() {
        when(relationshipPolicyService.existsBlockEitherDirection(10L, 20L)).thenReturn(true);

        service.createFollowNotification(10L, 20L);
        service.createPostLikeNotification(10L, 20L, 100L);
        service.createPostCommentNotification(10L, 20L, 100L, 200L);
        service.createCommentReplyNotification(10L, 20L, 100L, 200L);

        verify(notificationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(entityManager, never()).getReference(any(), any());
    }

    @Test
    void accountNotificationUsesNullActorToProtectAdminIdentity() {
        User recipient = user(20L);
        when(entityManager.getReference(User.class, 20L)).thenReturn(recipient);

        service.createAccountBlockedNotification(20L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.ACCOUNT_BLOCKED);
        assertThat(captor.getValue().getActor()).isNull();
        assertThat(captor.getValue().getRecipient().getId()).isEqualTo(20L);
        verify(eventPublisher).publishEvent(new NotificationCreatedEvent(99L, 20L));
    }

    @Test
    void violationWarningsUseImmutableTypesAndHideAdminIdentity() {
        User recipient = user(20L);
        Post post = new Post(recipient, "Nội dung vi phạm cần lưu snapshot");
        ReflectionTestUtils.setField(post, "id", 100L);
        post.setHiddenReason("HARMFUL_CONTENT");
        when(entityManager.getReference(User.class, 20L)).thenReturn(recipient);
        when(entityManager.getReference(Post.class, 100L)).thenReturn(post);

        service.createContentViolationWarningNotification(20L, 100L, true);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.CONTENT_VIOLATION_FINAL_WARNING);
        assertThat(captor.getValue().getActor()).isNull();
        assertThat(captor.getValue().getRecipient().getId()).isEqualTo(20L);
        assertThat(captor.getValue().getPost().getId()).isEqualTo(100L);
        assertThat(captor.getValue().getModerationReason()).isEqualTo("HARMFUL_CONTENT");
        assertThat(captor.getValue().getModerationPostSummary()).isEqualTo("Nội dung vi phạm cần lưu snapshot");
        verify(eventPublisher).publishEvent(new NotificationCreatedEvent(99L, 20L));
    }

    @Test
    void adminProfileUpdateCreatesSystemNotificationWithoutAdminActor() {
        User recipient = user(20L);
        when(entityManager.getReference(User.class, 20L)).thenReturn(recipient);

        service.createUserProfileUpdatedByAdminNotification(20L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PROFILE_UPDATED_BY_ADMIN);
        assertThat(captor.getValue().getActor()).isNull();
        assertThat(captor.getValue().getRecipient().getId()).isEqualTo(20L);
        verify(eventPublisher).publishEvent(new NotificationCreatedEvent(99L, 20L));
    }

    @Test
    void markAsReadOnlyLoadsNotificationOwnedByCurrentUser() {
        Notification notification = new Notification(
                user(20L), user(10L), NotificationType.FOLLOW, null, null, null);
        ReflectionTestUtils.setField(notification, "id", 30L);
        when(notificationRepository.findByIdAndRecipient_IdAndDeletedAtIsNull(30L, 20L))
                .thenReturn(Optional.of(notification));

        var response = service.markAsRead(30L);

        assertThat(response.notificationId()).isEqualTo(30L);
        assertThat(response.readAt()).isEqualTo(LocalDateTime.of(2026, 7, 18, 10, 0));
    }

    @Test
    void moderationDetailReturnsSnapshotOnlyToNotificationOwner() {
        Post post = new Post(user(20L), "Nội dung hiện tại");
        ReflectionTestUtils.setField(post, "id", 100L);
        Notification notification = new Notification(
                user(20L), null, NotificationType.CONTENT_VIOLATION_FINAL_WARNING,
                post, null, null, "MISINFORMATION", "Snapshot tại thời điểm xử lý");
        ReflectionTestUtils.setField(notification, "id", 30L);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.of(2026, 7, 18, 9, 30));
        when(notificationRepository.findByIdAndRecipient_IdAndDeletedAtIsNull(30L, 20L))
                .thenReturn(Optional.of(notification));

        var response = service.getModerationDetail(30L);

        assertThat(response.postId()).isEqualTo(100L);
        assertThat(response.reasonCode()).isEqualTo("MISINFORMATION");
        assertThat(response.postSummary()).isEqualTo("Snapshot tại thời điểm xử lý");
        assertThat(response.violationCount()).isEqualTo(2);
        assertThat(response.violationThreshold()).isEqualTo(3);
    }

    @Test
    void hiddenPostModerationDetailKeepsViolationCountNullWithoutUnboxingFailure() {
        Post post = new Post(user(20L), "Nội dung hiện tại");
        ReflectionTestUtils.setField(post, "id", 100L);
        Notification notification = new Notification(
                user(20L), null, NotificationType.POST_HIDDEN_BY_ADMIN,
                post, null, null, "HARMFUL_CONTENT", "Snapshot bài viết bị ẩn");
        ReflectionTestUtils.setField(notification, "id", 31L);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.of(2026, 7, 18, 9, 35));
        when(notificationRepository.findByIdAndRecipient_IdAndDeletedAtIsNull(31L, 20L))
                .thenReturn(Optional.of(notification));

        var response = service.getModerationDetail(31L);

        assertThat(response.type()).isEqualTo(NotificationType.POST_HIDDEN_BY_ADMIN);
        assertThat(response.postId()).isEqualTo(100L);
        assertThat(response.reasonCode()).isEqualTo("HARMFUL_CONTENT");
        assertThat(response.postSummary()).isEqualTo("Snapshot bài viết bị ẩn");
        assertThat(response.violationCount()).isNull();
        assertThat(response.violationThreshold()).isEqualTo(3);
    }

    @Test
    void markAsReadHidesExistenceOfAnotherUsersNotification() {
        when(notificationRepository.findByIdAndRecipient_IdAndDeletedAtIsNull(30L, 20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(30L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void markAsReadTreatsBlockedActorNotificationAsNotFound() {
        Notification notification = new Notification(
                user(20L), user(10L), NotificationType.FOLLOW, null, null, null);
        ReflectionTestUtils.setField(notification, "id", 30L);
        when(notificationRepository.findByIdAndRecipient_IdAndDeletedAtIsNull(30L, 20L))
                .thenReturn(Optional.of(notification));
        when(relationshipPolicyService.existsBlockEitherDirection(20L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.markAsRead(30L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    private User user(Long id) {
        User user = new User("student" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserProfile completedProfile(Long id) {
        UserProfile profile = new UserProfile(user(id));
        ReflectionTestUtils.setField(profile, "userId", id);
        profile.setDisplayName("Student " + id);
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 18, 9, 0));
        return profile;
    }
}
