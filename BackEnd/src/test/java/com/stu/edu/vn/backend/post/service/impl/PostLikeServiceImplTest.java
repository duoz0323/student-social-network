package com.stu.edu.vn.backend.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostLike;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.projection.PostInteractionTargetProjection;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class PostLikeServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = org.mockito.Mockito.mock(PostLikeRepository.class);
    private final NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);

    private PostLikeServiceImpl postLikeService;

    @BeforeEach
    void setUp() {
        postLikeService = new PostLikeServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                postRepository,
                postLikeRepository,
                notificationService
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(10L)));
        PostInteractionTargetProjection publishedTarget = target(PostStatus.PUBLISHED, 20L);
        when(postRepository.findInteractionTargetById(1L)).thenReturn(Optional.of(publishedTarget));
        when(postRepository.getReferenceById(1L)).thenReturn(post(1L));
        when(postRepository.findLikeCountById(1L)).thenReturn(Optional.of(6));
    }

    @Test
    void likePostUsesCurrentUserAndReturnsLatestLikeCountFromDatabase() {
        when(postLikeRepository.existsByIdUserIdAndIdPostId(10L, 1L)).thenReturn(false);

        PostLikeResponse response = postLikeService.likePost(1L);

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(currentUserProvider).getCurrentUserId();
        verify(postLikeRepository).saveAndFlush(captor.capture());
        verify(notificationService).createPostLikeNotification(10L, 20L, 1L);
        assertThat(captor.getValue().getId().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getId().getPostId()).isEqualTo(1L);
        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(response.likeCount()).isEqualTo(6);
    }

    @Test
    void likePostRejectsAlreadyLikedPost() {
        when(postLikeRepository.existsByIdUserIdAndIdPostId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> postLikeService.likePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_ALREADY_LIKED);

        verify(postLikeRepository, never()).saveAndFlush(any());
        verify(postRepository, never()).findLikeCountById(any());
    }

    @Test
    void likePostRejectsHiddenOrDeletedPostAsNotAvailable() {
        PostInteractionTargetProjection hiddenTarget = target(PostStatus.HIDDEN, 20L);
        when(postRepository.findInteractionTargetById(1L)).thenReturn(Optional.of(hiddenTarget));

        assertThatThrownBy(() -> postLikeService.likePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_AVAILABLE);

        verify(postLikeRepository, never()).existsByIdUserIdAndIdPostId(any(), any());
    }

    @Test
    void likePostReturnsNotFoundWhenPostDoesNotExist() {
        when(postRepository.findInteractionTargetById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.likePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void likePostRejectsUserWithoutCompletedProfile() {
        UserProfile profile = completedProfile(10L);
        profile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> postLikeService.likePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_COMPLETED);

        verify(postRepository, never()).findInteractionTargetById(any());
    }

    @Test
    void unlikePostDeletesLikeAndReturnsLatestLikeCountFromDatabase() {
        when(postLikeRepository.existsByIdUserIdAndIdPostId(10L, 1L)).thenReturn(true);
        when(postLikeRepository.deleteByUserIdAndPostId(10L, 1L)).thenReturn(1);
        when(postRepository.findLikeCountById(1L)).thenReturn(Optional.of(5));

        PostLikeResponse response = postLikeService.unlikePost(1L);

        verify(postLikeRepository).deleteByUserIdAndPostId(10L, 1L);
        verify(notificationService).deletePostLikeNotification(10L, 1L);
        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.likedByCurrentUser()).isFalse();
        assertThat(response.likeCount()).isEqualTo(5);
    }

    @Test
    void unlikePostRejectsPostThatWasNotLiked() {
        when(postLikeRepository.existsByIdUserIdAndIdPostId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> postLikeService.unlikePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_LIKED);

        verify(postLikeRepository, never()).deleteByUserIdAndPostId(any(), any());
        verify(notificationService, never()).deletePostLikeNotification(any(), any());
    }

    private User user(Long userId) {
        User user = new User("student@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private UserProfile completedProfile(Long userId) {
        UserProfile profile = new UserProfile(user(userId));
        ReflectionTestUtils.setField(profile, "userId", userId);
        profile.setDisplayName("Nguyen Van A");
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 3, 1, 0));
        return profile;
    }

    private Post post(Long postId) {
        Post post = new Post(user(20L), "Noi dung");
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private PostInteractionTargetProjection target(PostStatus status, Long authorId) {
        PostInteractionTargetProjection target = org.mockito.Mockito.mock(PostInteractionTargetProjection.class);
        when(target.getPostId()).thenReturn(1L);
        when(target.getAuthorId()).thenReturn(authorId);
        when(target.getStatus()).thenReturn(status);
        return target;
    }
}
