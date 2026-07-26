package com.stu.edu.vn.backend.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.post.dto.response.PostSaveResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.SavedPost;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.mapper.PostMapper;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
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
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

class SavedPostServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final SavedPostRepository savedPostRepository = org.mockito.Mockito.mock(SavedPostRepository.class);
    private final PostMediaRepository postMediaRepository = org.mockito.Mockito.mock(PostMediaRepository.class);
    private final PostHashtagRepository postHashtagRepository = org.mockito.Mockito.mock(PostHashtagRepository.class);
    private final PostMapper postMapper = org.mockito.Mockito.mock(PostMapper.class);
    private final PlatformTransactionManager transactionManager = org.mockito.Mockito.mock(PlatformTransactionManager.class);
    private final FeedPostBatchLoader feedPostBatchLoader = org.mockito.Mockito.mock(FeedPostBatchLoader.class);
    private final CursorCodec cursorCodec = org.mockito.Mockito.mock(CursorCodec.class);

    private SavedPostServiceImpl savedPostService;

    @BeforeEach
    void setUp() {
        savedPostService = new SavedPostServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                postRepository,
                savedPostRepository,
                postMediaRepository,
                postHashtagRepository,
                postMapper,
                feedPostBatchLoader,
                cursorCodec,
                transactionManager
        );

        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(10L)));
        when(postRepository.findStatusById(15L)).thenReturn(Optional.of(PostStatus.PUBLISHED));
        when(postRepository.getReferenceById(15L)).thenReturn(post(15L, 20L));
    }

    @Test
    void savePublishedPostCreatesRelationForCurrentUser() {
        when(savedPostRepository.existsByIdUserIdAndIdPostId(10L, 15L)).thenReturn(false);

        PostSaveResponse response = savedPostService.savePost(15L);

        ArgumentCaptor<SavedPost> captor = ArgumentCaptor.forClass(SavedPost.class);
        verify(currentUserProvider).getCurrentUserId();
        verify(savedPostRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getId().getPostId()).isEqualTo(15L);
        assertThat(response).isEqualTo(new PostSaveResponse(15L, true));
    }

    @Test
    void saveOwnPublishedPostIsAllowed() {
        when(postRepository.getReferenceById(15L)).thenReturn(post(15L, 10L));
        when(savedPostRepository.existsByIdUserIdAndIdPostId(10L, 15L)).thenReturn(false);

        PostSaveResponse response = savedPostService.savePost(15L);

        assertThat(response.saved()).isTrue();
        verify(savedPostRepository).saveAndFlush(any(SavedPost.class));
    }

    @Test
    void saveAgainReturnsSuccessWithoutCreatingDuplicate() {
        when(savedPostRepository.existsByIdUserIdAndIdPostId(10L, 15L)).thenReturn(true);

        PostSaveResponse response = savedPostService.savePost(15L);

        assertThat(response).isEqualTo(new PostSaveResponse(15L, true));
        verify(savedPostRepository, never()).saveAndFlush(any());
        verify(postRepository, never()).getReferenceById(any());
    }

    @Test
    void duplicateConstraintRaceReturnsSavedTrue() {
        when(savedPostRepository.existsByIdUserIdAndIdPostId(10L, 15L)).thenReturn(false);
        when(savedPostRepository.saveAndFlush(any(SavedPost.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate composite primary key"));

        PostSaveResponse response = savedPostService.savePost(15L);

        assertThat(response).isEqualTo(new PostSaveResponse(15L, true));
        // Transaction INSERT cạnh tranh bị rollback riêng, không biến response idempotent thành lỗi commit muộn.
        verify(transactionManager).rollback(any());
    }

    @Test
    void saveMissingPostReturnsPostNotFound() {
        when(postRepository.findStatusById(15L)).thenReturn(Optional.empty());

        assertBusinessError(() -> savedPostService.savePost(15L), ErrorCode.POST_NOT_FOUND);
        verify(savedPostRepository, never()).existsByIdUserIdAndIdPostId(any(), any());
    }

    @Test
    void saveHiddenOrDeletedPostReturnsPostNotAvailable() {
        when(postRepository.findStatusById(15L)).thenReturn(Optional.of(PostStatus.HIDDEN));
        assertBusinessError(() -> savedPostService.savePost(15L), ErrorCode.POST_NOT_AVAILABLE);

        when(postRepository.findStatusById(15L)).thenReturn(Optional.of(PostStatus.DELETED));
        assertBusinessError(() -> savedPostService.savePost(15L), ErrorCode.POST_NOT_AVAILABLE);
        verify(savedPostRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveRejectsInactiveAccount() {
        User blockedUser = user(10L);
        blockedUser.setStatus(UserStatus.BLOCKED);
        when(userRepository.findById(10L)).thenReturn(Optional.of(blockedUser));

        assertBusinessError(() -> savedPostService.savePost(15L), ErrorCode.USER_BLOCKED);
        verify(userProfileRepository, never()).findById(any());
        verify(postRepository, never()).findStatusById(any());
    }

    @Test
    void saveRejectsIncompleteProfile() {
        UserProfile profile = completedProfile(10L);
        profile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        assertBusinessError(() -> savedPostService.savePost(15L), ErrorCode.PROFILE_NOT_COMPLETED);
        verify(postRepository, never()).findStatusById(any());
    }

    @Test
    void unsaveExistingRelationReturnsSavedFalse() {
        when(savedPostRepository.deleteByUserIdAndPostId(10L, 15L)).thenReturn(1);

        PostSaveResponse response = savedPostService.unsavePost(15L);

        assertThat(response).isEqualTo(new PostSaveResponse(15L, false));
        verify(savedPostRepository).deleteByUserIdAndPostId(10L, 15L);
        verify(savedPostRepository, never()).existsByIdUserIdAndIdPostId(any(), any());
    }

    @Test
    void unsaveMissingRelationStillReturnsSavedFalse() {
        when(savedPostRepository.deleteByUserIdAndPostId(10L, 15L)).thenReturn(0);

        PostSaveResponse response = savedPostService.unsavePost(15L);

        assertThat(response).isEqualTo(new PostSaveResponse(15L, false));
        verify(savedPostRepository).deleteByUserIdAndPostId(10L, 15L);
    }

    @Test
    void unsaveDeletesOnlyCurrentUserRelation() {
        savedPostService.unsavePost(15L);

        verify(currentUserProvider).getCurrentUserId();
        verify(savedPostRepository).deleteByUserIdAndPostId(10L, 15L);
        verify(savedPostRepository, never()).deleteByUserIdAndPostId(20L, 15L);
    }

    private void assertBusinessError(Runnable action, ErrorCode expectedError) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expectedError);
    }

    private User user(Long userId) {
        User user = new User("student" + userId + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private UserProfile completedProfile(Long userId) {
        UserProfile profile = new UserProfile(user(userId));
        ReflectionTestUtils.setField(profile, "userId", userId);
        profile.setDisplayName("Nguyen Van A");
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 12, 10, 0));
        return profile;
    }

    private Post post(Long postId, Long authorId) {
        Post post = new Post(user(authorId), "Noi dung");
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
