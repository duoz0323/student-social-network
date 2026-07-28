package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.cursor.TimeCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostSaveResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.entity.SavedPost;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.mapper.PostMapper;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.service.SavedPostService;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private static final int MAX_PAGE_SIZE = 20;
    private static final java.time.LocalDateTime FIRST_PAGE_TIME =
            java.time.LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final SavedPostRepository savedPostRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostMapper postMapper;
    private final FeedPostBatchLoader feedPostBatchLoader;
    private final CursorCodec cursorCodec;
    private final TransactionTemplate saveTransactionTemplate;
    private final UserRelationshipPolicyService relationshipPolicyService;

    public SavedPostServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository,
            SavedPostRepository savedPostRepository,
            PostMediaRepository postMediaRepository,
            PostHashtagRepository postHashtagRepository,
            PostMapper postMapper,
            FeedPostBatchLoader feedPostBatchLoader,
            CursorCodec cursorCodec,
            PlatformTransactionManager transactionManager,
            UserRelationshipPolicyService relationshipPolicyService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.savedPostRepository = savedPostRepository;
        this.postMediaRepository = postMediaRepository;
        this.postHashtagRepository = postHashtagRepository;
        this.postMapper = postMapper;
        this.feedPostBatchLoader = feedPostBatchLoader;
        this.cursorCodec = cursorCodec;
        this.relationshipPolicyService = relationshipPolicyService;
        this.saveTransactionTemplate = new TransactionTemplate(transactionManager);
        // Tách INSERT sang transaction riêng để lỗi khóa trùng chỉ rollback INSERT cạnh tranh, không làm hỏng response idempotent bên ngoài.
        this.saveTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional
    public PostSaveResponse savePost(Long postId) {
        Long userId = currentUserProvider.getCurrentUserId();
        User currentUser = ensureCurrentUserCanSave(userId);
        assertPostCanBeAccessed(userId, postId);

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
        assertPostCanBeAccessed(userId, postId);

        // Không gọi exists trước; một câu DELETE vừa tránh race condition vừa bảo đảm Unsave idempotent.
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
        return new PostSaveResponse(postId, false);
    }

    private void assertPostCanBeAccessed(Long userId, Long postId) {
        var target = postRepository.findInteractionTargetById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        // Kiểm tra Block trước trạng thái để không tiết lộ bài viết của tài khoản đã chặn.
        relationshipPolicyService.assertNoBlock(userId, target.getAuthorId());
        if (target.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getSavedPosts(String encodedCursor, int limit) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long viewerId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanSave(viewerId);
        TimeCursor cursor = cursorCodec.decode(encodedCursor, TimeCursor.class);
        if (cursor != null && !cursor.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        java.time.LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.createdAt();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<Post> fetched = postRepository.findSavedPosts(
                viewerId, time, postId, PageRequest.of(0, limit + 1));
        boolean hasNext = fetched.size() > limit;
        List<Post> posts = fetched.stream().distinct().limit(limit).toList();
        List<FeedPostResponse> content = feedPostBatchLoader.map(posts, viewerId);
        String nextCursor = null;
        if (hasNext && !posts.isEmpty()) {
            Post last = posts.get(posts.size() - 1);
            java.time.LocalDateTime savedAt = savedPostRepository.findCreatedAt(viewerId, last.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
            nextCursor = cursorCodec.encode(new TimeCursor(savedAt, last.getId()));
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
