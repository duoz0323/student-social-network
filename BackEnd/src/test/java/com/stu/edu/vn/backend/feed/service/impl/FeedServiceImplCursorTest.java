package com.stu.edu.vn.backend.feed.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.cursor.ForYouCursor;
import com.stu.edu.vn.backend.common.cursor.TimeCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.mapper.FeedPostMapper;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplCursorTest {

    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PostRepository postRepository;
    @Mock private PostMediaRepository postMediaRepository;
    @Mock private PostHashtagRepository postHashtagRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private SavedPostRepository savedPostRepository;
    @Mock private FeedPostMapper feedPostMapper;
    @Mock private CursorCodec cursorCodec;

    private FeedServiceImpl service;
    private User author;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        service = new FeedServiceImpl(
                currentUserProvider, userRepository, userProfileRepository, postRepository,
                postMediaRepository, postHashtagRepository, postLikeRepository,
                savedPostRepository, feedPostMapper, cursorCodec);
        author = org.mockito.Mockito.mock(User.class);
        profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(author));
        when(author.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userProfileRepository.findById(7L)).thenReturn(Optional.of(profile));
        when(profile.getProfileCompletedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    void firstPageShouldFetchLimitPlusOneAndReturnCursorFromLastReturnedPost() {
        List<Post> fetched = posts(11);
        when(postRepository.findFollowingFeed(eq(7L), any(), eq(Long.MAX_VALUE), any()))
                .thenReturn(fetched);
        stubBatchMapping(fetched.subList(0, 10));
        when(cursorCodec.encode(any(TimeCursor.class))).thenReturn("next");

        var result = service.getFollowing(null, 10);

        assertThat(result.content()).hasSize(10);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("next");
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findFollowingFeed(eq(7L), any(), eq(Long.MAX_VALUE), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(11);
        ArgumentCaptor<TimeCursor> cursor = ArgumentCaptor.forClass(TimeCursor.class);
        verify(cursorCodec).encode(cursor.capture());
        assertThat(cursor.getValue().postId()).isEqualTo(10L);
    }

    @Test
    void validCursorShouldBePassedToKeysetQueryAndLastPageHasNoNextCursor() {
        LocalDateTime cursorTime = LocalDateTime.of(2026, 7, 24, 10, 0);
        when(cursorCodec.decode("valid", TimeCursor.class)).thenReturn(new TimeCursor(cursorTime, 50L));
        List<Post> fetched = posts(2);
        when(postRepository.findFollowingFeed(eq(7L), eq(cursorTime), eq(50L), any()))
                .thenReturn(fetched);
        stubBatchMapping(fetched);

        var result = service.getFollowing("valid", 10);

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        verify(cursorCodec, never()).encode(any());
    }

    @Test
    void invalidCursorFieldsAndOutOfRangeLimitsShouldFailBeforeRepositoryQuery() {
        when(cursorCodec.decode("missing", ForYouCursor.class))
                .thenReturn(new ForYouCursor(null, LocalDateTime.now(), 1L));

        assertBusinessError(() -> service.getForYou("missing", 10), ErrorCode.INVALID_CURSOR);
        assertBusinessError(() -> service.getForYou(null, 0), ErrorCode.VALIDATION_ERROR);
        assertBusinessError(() -> service.getForYou(null, 21), ErrorCode.VALIDATION_ERROR);
        verify(postRepository, never()).findForYouFeed(any(Integer.class), any(), any(), any());
    }

    private List<Post> posts(int count) {
        List<Post> result = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            Post post = org.mockito.Mockito.mock(Post.class);
            lenient().when(post.getId()).thenReturn((long) index);
            lenient().when(post.getAuthor()).thenReturn(author);
            lenient().when(post.getPublishedAt())
                    .thenReturn(LocalDateTime.of(2026, 7, 24, 12, 0).minusMinutes(index));
            result.add(post);
        }
        return result;
    }

    private void stubBatchMapping(List<Post> returnedPosts) {
        when(author.getId()).thenReturn(7L);
        when(userProfileRepository.findAllById(anyList())).thenReturn(List.of(profile));
        when(profile.getUserId()).thenReturn(7L);
        when(postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(anyList()))
                .thenReturn(List.of());
        when(postHashtagRepository.findWithHashtagByPostIds(anyList())).thenReturn(List.of());
        when(postLikeRepository.findLikedPostIds(eq(7L), anyList())).thenReturn(List.of());
        when(savedPostRepository.findSavedPostIds(eq(7L), anyList())).thenReturn(List.of());
        for (Post post : returnedPosts) {
            Long postId = post.getId();
            LocalDateTime publishedAt = post.getPublishedAt();
            when(feedPostMapper.toResponse(eq(post), eq(profile), anyList(), eq(null), eq(false), eq(false)))
                    .thenReturn(new FeedPostResponse(
                            postId, null, false, 0, 0, publishedAt,
                            null, List.of(), null, false, false));
        }
    }

    private void assertBusinessError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
