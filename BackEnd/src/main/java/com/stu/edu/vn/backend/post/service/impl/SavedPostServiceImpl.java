package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.dto.response.PostSaveResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.SavedPost;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.service.SavedPostService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Triển khai Save/Unsave idempotent cho tài khoản ACTIVE, hồ sơ hoàn tất và bài PUBLISHED.
 */
@Service
public class SavedPostServiceImpl implements SavedPostService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final SavedPostRepository savedPostRepository;
    private final TransactionTemplate saveTransactionTemplate;

    public SavedPostServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository,
            SavedPostRepository savedPostRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.savedPostRepository = savedPostRepository;
        this.saveTransactionTemplate = new TransactionTemplate(transactionManager);
        // Tách INSERT sang transaction riêng để lỗi khóa trùng chỉ rollback INSERT cạnh tranh, không làm hỏng response idempotent bên ngoài.
        this.saveTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional
    public PostSaveResponse savePost(Long postId) {
        Long userId = currentUserProvider.getCurrentUserId();
        User currentUser = ensureCurrentUserCanSave(userId);
        ensurePostIsPublished(postId);

        if (savedPostRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
            // Save đã tồn tại vẫn thành công và không tạo thêm dữ liệu theo nguyên tắc idempotent.
            return new PostSaveResponse(postId, true);
        }

        try {
            saveTransactionTemplate.executeWithoutResult(status -> {
                Post postReference = postRepository.getReferenceById(postId);
                // Flush ngay để composite primary key phát hiện request Save đồng thời trong transaction độc lập này.
                savedPostRepository.saveAndFlush(new SavedPost(currentUser, postReference));
            });
        } catch (DataIntegrityViolationException exception) {
            // Transaction INSERT đã rollback độc lập; request cạnh tranh vẫn được xem là kết quả thành công saved=true.
            return new PostSaveResponse(postId, true);
        }

        return new PostSaveResponse(postId, true);
    }

    @Override
    @Transactional
    public PostSaveResponse unsavePost(Long postId) {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanSave(userId);
        ensurePostIsPublished(postId);

        // Không gọi exists trước; một câu DELETE vừa tránh race condition vừa bảo đảm Unsave idempotent.
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
        return new PostSaveResponse(postId, false);
    }

    private User ensureCurrentUserCanSave(Long userId) {
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

    private void ensurePostIsPublished(Long postId) {
        PostStatus status = postRepository.findStatusById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (status != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
    }
}
