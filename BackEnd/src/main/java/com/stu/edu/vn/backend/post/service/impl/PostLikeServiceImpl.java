package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.cursor.TimeCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostLike;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.mapper.PostMapper;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.projection.PostInteractionTargetProjection;
import com.stu.edu.vn.backend.post.service.PostLikeService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Triển khai Like/Unlike bài viết theo rule: chỉ user đã đăng nhập, hồ sơ hoàn tất và post PUBLISHED mới được tương tác.
 */
@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {
    private static final int MAX_PAGE_SIZE = 20;
    private static final java.time.LocalDateTime FIRST_PAGE_TIME =
            java.time.LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostMapper postMapper;
    private final NotificationService notificationService;
    private final FeedPostBatchLoader feedPostBatchLoader;
    private final CursorCodec cursorCodec;

    @Override
    @Transactional
    public PostLikeResponse likePost(Long postId) {
        Long userId = currentUserProvider.getCurrentUserId();
        User currentUser = ensureCurrentUserCanInteract(userId);
        PostInteractionTargetProjection target = findPublishedInteractionTarget(postId);

        if (postLikeRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
            throw new BusinessException(ErrorCode.POST_ALREADY_LIKED);
        }

        try {
            // Lưu quan hệ Like, sau đó flush để trigger MySQL cập nhật posts.like_count ngay trong transaction hiện tại.
            Post postReference = postRepository.getReferenceById(postId);
            postLikeRepository.saveAndFlush(new PostLike(currentUser, postReference));
        } catch (DataIntegrityViolationException exception) {
            // Trường hợp hai request Like đồng thời, khóa chính kép vẫn chặn trùng và Service trả đúng lỗi nghiệp vụ.
            throw new BusinessException(ErrorCode.POST_ALREADY_LIKED);
        }

        notificationService.createPostLikeNotification(userId, target.getAuthorId(), postId);

        int likeCount = getLatestLikeCount(postId);
        return new PostLikeResponse(postId, true, likeCount);
    }

    @Override
    @Transactional
    public PostLikeResponse unlikePost(Long postId) {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanInteract(userId);
        findPublishedInteractionTarget(postId);

        if (!postLikeRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_LIKED);
        }

        // Xóa quan hệ Like để trigger MySQL tự giảm posts.like_count; Service không tự cộng/trừ bộ đếm.
        int deletedRows = postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_LIKED);
        }

        notificationService.deletePostLikeNotification(userId, postId);

        int likeCount = getLatestLikeCount(postId);
        return new PostLikeResponse(postId, false, likeCount);
    }

    private User ensureCurrentUserCanInteract(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return currentUser;
    }

    private PostInteractionTargetProjection findPublishedInteractionTarget(Long postId) {
        PostInteractionTargetProjection target = postRepository.findInteractionTargetById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (target.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
        return target;
    }

    private int getLatestLikeCount(Long postId) {
        // Query lại like_count từ database sau insert/delete để nhận giá trị mới do trigger post_likes cập nhật.
        return postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getLikedPosts(String encodedCursor, int limit) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long viewerId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanInteract(viewerId);
        TimeCursor cursor = cursorCodec.decode(encodedCursor, TimeCursor.class);
        if (cursor != null && !cursor.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        java.time.LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.createdAt();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<Post> fetched = postRepository.findLikedPosts(
                viewerId, time, postId, PageRequest.of(0, limit + 1));
        boolean hasNext = fetched.size() > limit;
        List<Post> posts = fetched.stream().distinct().limit(limit).toList();
        List<FeedPostResponse> content = feedPostBatchLoader.map(posts, viewerId);
        String nextCursor = null;
        if (hasNext && !posts.isEmpty()) {
            Post last = posts.get(posts.size() - 1);
            java.time.LocalDateTime likedAt = postLikeRepository.findCreatedAt(viewerId, last.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
            nextCursor = cursorCodec.encode(new TimeCursor(likedAt, last.getId()));
        }
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
}
