package com.stu.edu.vn.backend.post.service.impl;

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
import com.stu.edu.vn.backend.post.service.PostLikeService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai Like/Unlike bài viết theo rule: chỉ user đã đăng nhập, hồ sơ hoàn tất và post PUBLISHED mới được tương tác.
 */
@Service
public class PostLikeServiceImpl implements PostLikeService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationService notificationService;

    public PostLikeServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            NotificationService notificationService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.notificationService = notificationService;
    }

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
}
