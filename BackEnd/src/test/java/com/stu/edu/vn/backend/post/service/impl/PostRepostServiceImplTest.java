package com.stu.edu.vn.backend.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.service.FeedActivityAssembler;
import com.stu.edu.vn.backend.notification.event.PostRepostNotificationEvent;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

/** Kiểm chứng idempotence, validation và side effect duy nhất của nghiệp vụ Repost. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostRepostServiceImplTest {
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PostRepository postRepository;
    @Mock private PostRepostRepository postRepostRepository;
    @Mock private NotificationService notificationService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private FeedActivityAssembler feedActivityAssembler;
    @Mock private CursorCodec cursorCodec;

    private PostRepostServiceImpl service;
    private User actor;
    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        service = new PostRepostServiceImpl(currentUserProvider, userRepository, userProfileRepository,
                postRepository, postRepostRepository, notificationService, eventPublisher,
                feedActivityAssembler, cursorCodec);
        actor = org.mockito.Mockito.mock(User.class);
        author = org.mockito.Mockito.mock(User.class);
        post = org.mockito.Mockito.mock(Post.class);
        UserProfile actorProfile = org.mockito.Mockito.mock(UserProfile.class);
        UserProfile authorProfile = org.mockito.Mockito.mock(UserProfile.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(actor));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(actorProfile));
        when(actor.getId()).thenReturn(10L);
        when(actor.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(actorProfile.getProfileCompletedAt()).thenReturn(LocalDateTime.now());
        when(postRepository.findRepostTargetByIdForUpdate(100L)).thenReturn(Optional.of(post));
        when(post.getId()).thenReturn(100L);
        when(post.getStatus()).thenReturn(PostStatus.PUBLISHED);
        when(post.getAuthor()).thenReturn(author);
        when(post.getAuthorProfile()).thenReturn(authorProfile);
        when(author.getId()).thenReturn(20L);
        when(author.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(authorProfile.getProfileCompletedAt()).thenReturn(LocalDateTime.now());
        when(postRepository.findRepostCountById(100L)).thenReturn(Optional.of(1));
        org.mockito.Mockito.lenient().when(postRepostRepository.insertIfAbsent(10L, 100L)).thenReturn(1);
    }

    @Test
    void repostShouldCreateRelationNotificationAndAfterCommitEventSourceOnce() {
        when(postRepostRepository.existsByIdUserIdAndIdPostId(10L, 100L)).thenReturn(false);

        var response = service.repost(100L);

        assertThat(response.repostedByCurrentUser()).isTrue();
        assertThat(response.repostCount()).isEqualTo(1);
        verify(postRepostRepository).insertIfAbsent(10L, 100L);
        verify(notificationService).createPostRepostNotification(10L, 20L, 100L);
        ArgumentCaptor<PostRepostNotificationEvent> event = ArgumentCaptor.forClass(PostRepostNotificationEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue()).isEqualTo(new PostRepostNotificationEvent(10L, 20L, 100L));
    }

    @Test
    void repeatedOrSerializedConcurrentRepostShouldNotCreateDuplicateSideEffects() {
        when(postRepostRepository.existsByIdUserIdAndIdPostId(10L, 100L)).thenReturn(false, true);

        service.repost(100L);
        service.repost(100L);

        // Pessimistic lock tuần tự hóa hai request; composite PK là invariant cuối cùng tại database.
        verify(postRepostRepository, times(1)).insertIfAbsent(10L, 100L);
        verify(notificationService, times(1)).createPostRepostNotification(10L, 20L, 100L);
        verify(eventPublisher, times(1)).publishEvent(any(PostRepostNotificationEvent.class));
    }

    @Test
    void repeatedUnrepostShouldRemainIdempotentAndCounterCannotBecomeNegative() {
        when(postRepostRepository.deleteByUserIdAndPostId(10L, 100L)).thenReturn(1, 0);
        when(postRepository.findRepostCountById(100L)).thenReturn(Optional.of(0));

        var first = service.unrepost(100L);
        var second = service.unrepost(100L);

        assertThat(first.repostCount()).isZero();
        assertThat(second.repostCount()).isZero();
        verify(notificationService, times(1)).deletePostRepostNotification(10L, 100L);
    }

    @Test
    void ownHiddenAndDeletedPostsShouldBeRejectedWithoutSideEffects() {
        when(author.getId()).thenReturn(10L);
        assertBusinessError(() -> service.repost(100L), ErrorCode.POST_REPOST_SELF_FORBIDDEN);

        when(author.getId()).thenReturn(20L);
        when(post.getStatus()).thenReturn(PostStatus.HIDDEN, PostStatus.DELETED);
        assertBusinessError(() -> service.repost(100L), ErrorCode.POST_NOT_AVAILABLE);
        assertBusinessError(() -> service.repost(100L), ErrorCode.POST_NOT_AVAILABLE);
        verify(postRepostRepository, never()).insertIfAbsent(any(), any());
        verify(notificationService, never()).createPostRepostNotification(any(), any(), any());
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(expected));
    }
}
