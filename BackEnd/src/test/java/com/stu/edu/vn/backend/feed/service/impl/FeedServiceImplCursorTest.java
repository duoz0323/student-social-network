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
import com.stu.edu.vn.backend.feed.cursor.FollowingActivityCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.feed.service.FeedActivityAssembler;
import com.stu.edu.vn.backend.feed.repository.PersonalizedFeedRepository;
import com.stu.edu.vn.backend.feed.repository.projection.PersonalizedPostRankProjection;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.post.repository.projection.FeedActivityProjection;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    @Mock private PersonalizedFeedRepository personalizedFeedRepository;
    @Mock private PostRepostRepository postRepostRepository;
    @Mock private CursorCodec cursorCodec;
    @Mock private FeedActivityAssembler feedActivityAssembler;
    @Mock private FeedPostBatchLoader feedPostBatchLoader;

    private FeedServiceImpl service;
    private User author;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        service = new FeedServiceImpl(
                currentUserProvider, userRepository, userProfileRepository, postRepository,
                personalizedFeedRepository, postRepostRepository, cursorCodec, feedActivityAssembler,
                feedPostBatchLoader,
                Clock.fixed(Instant.parse("2026-08-11T03:00:00Z"), ZoneOffset.UTC));
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
        List<FeedActivityProjection> fetched = activities(11);
        when(postRepostRepository.findFollowingActivities(eq(7L), any(), eq(1), eq(Long.MAX_VALUE),
                eq(Long.MAX_VALUE), any())).thenReturn(fetched);
        when(feedActivityAssembler.assemble(anyList(), eq(7L))).thenReturn(java.util.Collections.nCopies(10,
                org.mockito.Mockito.mock(FeedItemResponse.class)));
        when(cursorCodec.encode(any(FollowingActivityCursor.class))).thenReturn("next");

        var result = service.getFollowing(null, 10);

        assertThat(result.content()).hasSize(10);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("next");
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepostRepository).findFollowingActivities(eq(7L), any(), eq(1), eq(Long.MAX_VALUE),
                eq(Long.MAX_VALUE), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(11);
        ArgumentCaptor<FollowingActivityCursor> cursor = ArgumentCaptor.forClass(FollowingActivityCursor.class);
        verify(cursorCodec).encode(cursor.capture());
        assertThat(cursor.getValue().postId()).isEqualTo(10L);
    }

    @Test
    void validCursorShouldBePassedToKeysetQueryAndLastPageHasNoNextCursor() {
        LocalDateTime cursorTime = LocalDateTime.of(2026, 7, 24, 10, 0);
        when(cursorCodec.decode("valid", FollowingActivityCursor.class))
                .thenReturn(new FollowingActivityCursor(cursorTime, 1, 20L, 50L));
        List<FeedActivityProjection> fetched = activities(2);
        when(postRepostRepository.findFollowingActivities(eq(7L), eq(cursorTime), eq(1), eq(20L),
                eq(50L), any())).thenReturn(fetched);
        when(feedActivityAssembler.assemble(fetched, 7L)).thenReturn(java.util.Collections.nCopies(2,
                org.mockito.Mockito.mock(FeedItemResponse.class)));

        var result = service.getFollowing("valid", 10);

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        verify(cursorCodec, never()).encode(any());
    }

    @Test
    void forYouShouldFreezeRankingTimeAndEncodeTheCompleteRankKey() {
        LocalDateTime rankingAt = LocalDateTime.of(2026, 8, 11, 3, 0);
        List<PersonalizedPostRankProjection> ranks = ranks(11, rankingAt.minusHours(1));
        List<Post> posts = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            Post post = org.mockito.Mockito.mock(Post.class);
            when(post.getId()).thenReturn((long) index + 1);
            posts.add(post);
        }
        when(personalizedFeedRepository.findRankedPosts(eq(7L), eq(rankingAt), eq(Integer.MAX_VALUE),
                any(), eq(Long.MAX_VALUE), any())).thenReturn(ranks);
        when(postRepository.findAccessibleFeedHeadersByIds(eq(7L), anyList())).thenReturn(posts);
        when(feedPostBatchLoader.map(posts, 7L)).thenReturn(java.util.Collections.nCopies(10,
                org.mockito.Mockito.mock(FeedPostResponse.class)));
        when(cursorCodec.encode(any(ForYouCursor.class))).thenReturn("next");

        var result = service.getForYou(null, 10);

        assertThat(result.content()).hasSize(10);
        assertThat(result.hasNext()).isTrue();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(personalizedFeedRepository).findRankedPosts(eq(7L), eq(rankingAt), eq(Integer.MAX_VALUE),
                any(), eq(Long.MAX_VALUE), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(11);
        ArgumentCaptor<ForYouCursor> cursor = ArgumentCaptor.forClass(ForYouCursor.class);
        verify(cursorCodec).encode(cursor.capture());
        assertThat(cursor.getValue().version()).isEqualTo(ForYouCursor.CURRENT_VERSION);
        assertThat(cursor.getValue().rankingAt()).isEqualTo(rankingAt);
        assertThat(cursor.getValue().score()).isEqualTo(91);
        assertThat(cursor.getValue().postId()).isEqualTo(10L);
    }

    @Test
    void forYouNextPageShouldReuseRankingTimeFromCursor() {
        LocalDateTime rankingAt = LocalDateTime.of(2026, 8, 10, 8, 30);
        LocalDateTime publishedAt = rankingAt.minusDays(1);
        ForYouCursor cursor = new ForYouCursor(
                ForYouCursor.CURRENT_VERSION, rankingAt, 80, publishedAt, 50L);
        when(cursorCodec.decode("valid", ForYouCursor.class)).thenReturn(cursor);
        when(personalizedFeedRepository.findRankedPosts(eq(7L), eq(rankingAt), eq(80),
                eq(publishedAt), eq(50L), any())).thenReturn(List.of());

        var result = service.getForYou("valid", 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        verify(personalizedFeedRepository).findRankedPosts(eq(7L), eq(rankingAt), eq(80),
                eq(publishedAt), eq(50L), any());
    }

    private List<FeedActivityProjection> activities(int count) {
        List<FeedActivityProjection> result = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            FeedActivityProjection activity = org.mockito.Mockito.mock(FeedActivityProjection.class);
            lenient().when(activity.getPostId()).thenReturn((long) index);
            lenient().when(activity.getActorId()).thenReturn(20L);
            lenient().when(activity.getItemRank()).thenReturn(1);
            lenient().when(activity.getActivityAt())
                    .thenReturn(LocalDateTime.of(2026, 7, 24, 12, 0).minusMinutes(index));
            result.add(activity);
        }
        return result;
    }

    private List<PersonalizedPostRankProjection> ranks(int count, LocalDateTime firstPublishedAt) {
        List<PersonalizedPostRankProjection> result = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            PersonalizedPostRankProjection rank = org.mockito.Mockito.mock(PersonalizedPostRankProjection.class);
            lenient().when(rank.getPostId()).thenReturn((long) index);
            lenient().when(rank.getScore()).thenReturn(101 - index);
            lenient().when(rank.getPublishedAt()).thenReturn(firstPublishedAt.minusMinutes(index));
            result.add(rank);
        }
        return result;
    }

    @Test
    void invalidCursorFieldsAndOutOfRangeLimitsShouldFailBeforeRepositoryQuery() {
        when(cursorCodec.decode("missing", ForYouCursor.class))
                .thenReturn(new ForYouCursor(ForYouCursor.CURRENT_VERSION, null, 10,
                        LocalDateTime.now(), 1L));

        assertBusinessError(() -> service.getForYou("missing", 10), ErrorCode.INVALID_CURSOR);
        assertBusinessError(() -> service.getForYou(null, 0), ErrorCode.VALIDATION_ERROR);
        assertBusinessError(() -> service.getForYou(null, 21), ErrorCode.VALIDATION_ERROR);
        verify(personalizedFeedRepository, never()).findRankedPosts(any(), any(), any(Integer.class), any(), any(), any());
    }

    @Test
    void blockedOrIncompleteViewerShouldBeRejectedBeforeRankingQuery() {
        when(author.getStatus()).thenReturn(UserStatus.BLOCKED);
        assertBusinessError(() -> service.getForYou(null, 10), ErrorCode.USER_BLOCKED);

        when(author.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(profile.getProfileCompletedAt()).thenReturn(null);
        assertBusinessError(() -> service.getForYou(null, 10), ErrorCode.PROFILE_NOT_COMPLETED);

        verify(personalizedFeedRepository, never()).findRankedPosts(any(), any(), any(Integer.class), any(), any(), any());
    }

    private void assertBusinessError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
