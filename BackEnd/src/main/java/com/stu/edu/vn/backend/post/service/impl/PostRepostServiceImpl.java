package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.cursor.FollowingActivityCursor;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.feed.service.FeedActivityAssembler;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.dto.response.PostRepostResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.post.repository.projection.FeedActivityProjection;
import com.stu.edu.vn.backend.post.service.PostRepostService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Nghiệp vụ Repost idempotent, khóa bài gốc để tuần tự hóa request concurrent cùng Post. */
@Service
@RequiredArgsConstructor
public class PostRepostServiceImpl implements PostRepostService {
    private static final int MAX_LIMIT = 20;
    private static final LocalDateTime FIRST_PAGE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostRepostRepository postRepostRepository;
    private final NotificationService notificationService;
    private final FeedActivityAssembler feedActivityAssembler;
    private final CursorCodec cursorCodec;

    @Override
    @Transactional
    public PostRepostResponse repost(Long postId) {
        return repostAs(currentUserProvider.getCurrentUserId(), postId);
    }

    @Override
    @Transactional
    public PostRepostResponse repostAs(Long userId, Long postId) {
        requireActiveCompletedUser(userId);
        Post post = lockPost(postId);
        requirePublishedRepostTarget(post, userId);

        if (postRepostRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
            return new PostRepostResponse(postId, true, latestRepostCount(postId));
        }

        // Câu lệnh atomic hấp thụ duplicate key; chỉ request thật sự INSERT mới được tạo side effect.
        int inserted = postRepostRepository.insertIfAbsent(userId, postId);
        if (inserted == 0) {
            return new PostRepostResponse(postId, true, latestRepostCount(postId));
        }
        notificationService.createPostRepostNotification(userId, post.getAuthor().getId(), postId);
        return new PostRepostResponse(postId, true, latestRepostCount(postId));
    }

    @Override
    @Transactional
    public PostRepostResponse unrepost(Long postId) {
        return unrepostAs(currentUserProvider.getCurrentUserId(), postId);
    }

    @Override
    @Transactional
    public PostRepostResponse unrepostAs(Long userId, Long postId) {
        requireActiveCompletedUser(userId);
        lockPost(postId);

        // DELETE điều kiện trả 0 dòng vẫn là kết quả hợp lệ của Unrepost idempotent.
        int deleted = postRepostRepository.deleteByUserIdAndPostId(userId, postId);
        if (deleted > 0) {
            notificationService.deletePostRepostNotification(userId, postId);
        }
        return new PostRepostResponse(postId, false, latestRepostCount(postId));
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedItemResponse> getProfileReposts(
            Long userId, String encodedCursor, int limit) {
        if (userId == null || userId <= 0 || limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long viewerId = currentUserProvider.getCurrentUserId();
        requireActiveCompletedUser(viewerId);
        requireActiveCompletedUser(userId);

        FollowingActivityCursor cursor = cursorCodec.decode(encodedCursor, FollowingActivityCursor.class);
        if (cursor != null && (!cursor.isValid() || cursor.itemRank() != 1 || !cursor.actorId().equals(userId))) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.activityAt();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<FeedActivityProjection> fetched = postRepostRepository.findProfileRepostActivities(
                userId, time, postId, PageRequest.of(0, limit + 1));
        boolean hasNext = fetched.size() > limit;
        List<FeedActivityProjection> activities = fetched.stream().limit(limit).toList();
        List<FeedItemResponse> content = feedActivityAssembler.assemble(activities, viewerId);
        String nextCursor = hasNext && !activities.isEmpty()
                ? cursorCodec.encode(toCursor(activities.get(activities.size() - 1)))
                : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private FollowingActivityCursor toCursor(FeedActivityProjection activity) {
        return new FollowingActivityCursor(activity.getActivityAt(), activity.getItemRank(),
                activity.getActorId(), activity.getPostId());
    }

    private Post lockPost(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return postRepository.findRepostTargetByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private void requirePublishedRepostTarget(Post post, Long userId) {
        if (post.getStatus() != PostStatus.PUBLISHED
                || post.getAuthor().getStatus() != UserStatus.ACTIVE
                || post.getAuthorProfile() == null
                || post.getAuthorProfile().getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
        if (post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_REPOST_SELF_FORBIDDEN);
        }
    }

    private User requireActiveCompletedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return user;
    }

    private int latestRepostCount(Long postId) {
        return postRepository.findRepostCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
