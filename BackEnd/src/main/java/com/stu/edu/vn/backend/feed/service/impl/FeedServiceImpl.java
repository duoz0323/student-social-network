package com.stu.edu.vn.backend.feed.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.cursor.ForYouCursor;
import com.stu.edu.vn.backend.common.cursor.TimeCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.feed.cursor.FollowingActivityCursor;
import com.stu.edu.vn.backend.feed.mapper.FeedPostMapper;
import com.stu.edu.vn.backend.feed.service.FeedService;
import com.stu.edu.vn.backend.feed.service.FeedActivityAssembler;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.post.repository.projection.FeedActivityProjection;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.service.PostLocationBatchLoader;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
    private final PostRepostRepository postRepostRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;
    private final FeedPostMapper feedPostMapper;
    private final CursorCodec cursorCodec;
    private final PostLocationBatchLoader postLocationBatchLoader;
    private final FeedActivityAssembler feedActivityAssembler;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getForYou(String encodedCursor, int limit) {
        Long viewerId = requireEligibleViewer(limit);
        ForYouCursor cursor = cursorCodec.decode(encodedCursor, ForYouCursor.class);
        if (cursor != null && !cursor.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        int score = cursor == null ? Integer.MAX_VALUE : cursor.score();
        LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.createdAt();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<Post> posts = postRepository.findForYouFeed(
                score, time, postId, PageRequest.of(0, limit + 1));
        return loadFeed(posts, viewerId, limit, true);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedItemResponse> getFollowing(String encodedCursor, int limit) {
        Long viewerId = requireEligibleViewer(limit);
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

    private CursorPageResponse<FeedPostResponse> loadFeed(
            List<Post> fetchedPosts, Long viewerId, int limit, boolean ranked) {
        boolean hasNext = fetchedPosts.size() > limit;
        List<Post> posts = fetchedPosts.stream().distinct().limit(limit).toList();
        if (posts.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null, false);
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> authorIds = posts.stream().map(post -> post.getAuthor().getId()).distinct().toList();
        Map<Long, UserProfile> profiles = userProfileRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Map<Long, List<PostMedia>> media = loadMedia(postIds);
        Map<Long, String> hashtags = loadHashtags(postIds);
        Set<Long> liked = new HashSet<>(postLikeRepository.findLikedPostIds(viewerId, postIds));
        Set<Long> saved = new HashSet<>(savedPostRepository.findSavedPostIds(viewerId, postIds));
        Set<Long> reposted = new HashSet<>(postRepostRepository.findRepostedPostIds(viewerId, postIds));
        Map<Long, Location> locations = postLocationBatchLoader.loadByPostId(posts);

        List<FeedPostResponse> content = posts.stream().map(post -> feedPostMapper.toResponse(
                post,
                profiles.get(post.getAuthor().getId()),
                media.getOrDefault(post.getId(), List.of()),
                hashtags.get(post.getId()),
                liked.contains(post.getId()),
                saved.contains(post.getId()),
                reposted.contains(post.getId()),
                locations.get(post.getId())
        )).toList();

        Post last = posts.get(posts.size() - 1);
        String nextCursor = hasNext
                ? cursorCodec.encode(ranked
                ? new ForYouCursor(last.getLikeCount() + last.getCommentCount(), last.getPublishedAt(), last.getId())
                : new TimeCursor(last.getPublishedAt(), last.getId()))
                : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private Map<Long, List<PostMedia>> loadMedia(List<Long> postIds) {
        Map<Long, List<PostMedia>> result = new HashMap<>();
        for (PostMedia item : postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(postIds)) {
            result.computeIfAbsent(item.getPost().getId(), ignored -> new ArrayList<>()).add(item);
        }
        return result;
    }

    private Map<Long, String> loadHashtags(List<Long> postIds) {
        Map<Long, String> result = new HashMap<>();
        for (PostHashtag relation : postHashtagRepository.findWithHashtagByPostIds(postIds)) {
            if (result.put(relation.getPost().getId(), relation.getHashtag().getNormalizedName()) != null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
        }
        return result;
    }

    private Long requireEligibleViewer(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long viewerId = currentUserProvider.getCurrentUserId();
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
