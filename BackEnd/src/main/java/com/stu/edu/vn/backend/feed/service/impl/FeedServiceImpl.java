package com.stu.edu.vn.backend.feed.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.cursor.ForYouCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.feed.cursor.FollowingActivityCursor;
import com.stu.edu.vn.backend.feed.repository.PersonalizedFeedRepository;
import com.stu.edu.vn.backend.feed.repository.projection.PersonalizedPostRankProjection;
import com.stu.edu.vn.backend.feed.service.FeedService;
import com.stu.edu.vn.backend.feed.service.FeedActivityAssembler;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đọc Feed bằng keyset cursor và batch-load dữ liệu PostCard để tránh N+1. */
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private static final int MAX_LIMIT = 20;
    private static final LocalDateTime FIRST_PAGE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PersonalizedFeedRepository personalizedFeedRepository;
    private final PostRepostRepository postRepostRepository;
    private final CursorCodec cursorCodec;
    private final FeedActivityAssembler feedActivityAssembler;
    private final FeedPostBatchLoader feedPostBatchLoader;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getForYou(String encodedCursor, int limit) {
        return getForYouAs(currentUserProvider.getCurrentUserId(), encodedCursor, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getForYouAs(Long viewerId, String encodedCursor, int limit) {
        requireEligibleViewer(viewerId, limit);
        ForYouCursor cursor = cursorCodec.decode(encodedCursor, ForYouCursor.class);
        if (cursor != null && !cursor.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        LocalDateTime rankingAt = cursor == null
                ? LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC)
                : cursor.rankingAt();
        int score = cursor == null ? Integer.MAX_VALUE : cursor.score();
        LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.publishedAt();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<PersonalizedPostRankProjection> fetched = personalizedFeedRepository.findRankedPosts(
                viewerId, rankingAt, score, time, postId, PageRequest.of(0, limit + 1));
        return loadPersonalizedFeed(fetched, viewerId, rankingAt, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedItemResponse> getFollowing(String encodedCursor, int limit) {
        Long viewerId = currentUserProvider.getCurrentUserId();
        requireEligibleViewer(viewerId, limit);
        FollowingActivityCursor cursor = cursorCodec.decode(encodedCursor, FollowingActivityCursor.class);
        if (cursor != null && !cursor.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.activityAt();
        int itemRank = cursor == null ? 1 : cursor.itemRank();
        long actorId = cursor == null ? Long.MAX_VALUE : cursor.actorId();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<FeedActivityProjection> fetched = postRepostRepository.findFollowingActivities(
                viewerId, time, itemRank, actorId, postId, PageRequest.of(0, limit + 1));
        boolean hasNext = fetched.size() > limit;
        List<FeedActivityProjection> activities = fetched.stream().limit(limit).toList();
        List<FeedItemResponse> content = feedActivityAssembler.assemble(activities, viewerId);
        String nextCursor = hasNext && !activities.isEmpty()
                ? cursorCodec.encode(toFollowingCursor(activities.get(activities.size() - 1)))
                : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private FollowingActivityCursor toFollowingCursor(FeedActivityProjection activity) {
        // Cursor chứa nguyên khóa ORDER BY nên các activity trùng thời điểm vẫn phân trang ổn định.
        return new FollowingActivityCursor(activity.getActivityAt(), activity.getItemRank(),
                activity.getActorId(), activity.getPostId());
    }

    private CursorPageResponse<FeedPostResponse> loadPersonalizedFeed(
            List<PersonalizedPostRankProjection> fetched,
            Long viewerId,
            LocalDateTime rankingAt,
            int limit
    ) {
        boolean hasMoreRanks = fetched.size() > limit;
        List<PersonalizedPostRankProjection> pageRanks = fetched.stream().limit(limit).toList();
        if (pageRanks.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null, false);
        }

        List<Long> rankedPostIds = pageRanks.stream().map(PersonalizedPostRankProjection::getPostId).toList();
        Map<Long, Post> accessibleById = postRepository.findAccessibleFeedHeadersByIds(viewerId, rankedPostIds)
                .stream().collect(Collectors.toMap(Post::getId, post -> post));
        List<Post> orderedPosts = rankedPostIds.stream()
                .map(accessibleById::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        List<FeedPostResponse> content = feedPostBatchLoader.map(orderedPosts, viewerId);

        PersonalizedPostRankProjection lastRank = orderedPosts.isEmpty() ? null
                : pageRanks.stream()
                .filter(rank -> rank.getPostId().equals(orderedPosts.get(orderedPosts.size() - 1).getId()))
                .findFirst()
                .orElse(null);
        String nextCursor = hasMoreRanks && lastRank != null
                ? cursorCodec.encode(new ForYouCursor(
                ForYouCursor.CURRENT_VERSION,
                rankingAt,
                lastRank.getScore(),
                lastRank.getPublishedAt(),
                lastRank.getPostId()))
                : null;
        return new CursorPageResponse<>(content, nextCursor, nextCursor != null);
    }

    private Long requireEligibleViewer(Long viewerId, int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (viewer.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        UserProfile profile = userProfileRepository.findById(viewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return viewerId;
    }
}
