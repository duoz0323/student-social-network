package com.stu.edu.vn.backend.interaction.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;
import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import com.stu.edu.vn.backend.interaction.mapper.CommentMapper;
import com.stu.edu.vn.backend.interaction.repository.CommentRepository;
import com.stu.edu.vn.backend.interaction.service.CommentService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai bình luận và reply một cấp, không tự cập nhật posts.comment_count vì database trigger đã xử lý.
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;
    private final Clock clock;

    public CommentServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            CommentMapper commentMapper,
            EntityManager entityManager,
            Clock clock
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User currentUser = ensureCurrentUserCanInteract(userId);
        String content = validateCommentContent(request);
        ensurePostIsPublished(postId);

        // Dùng reference để tạo khóa ngoại post_id mà không cần tải toàn bộ entity bài viết.
        Post postReference = postRepository.getReferenceById(postId);
        Comment comment = commentRepository.saveAndFlush(new Comment(postReference, currentUser, content));

        // Refresh để lấy created_at do MySQL tự sinh và authorProfile phục vụ response.
        entityManager.refresh(comment);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse createReply(Long parentCommentId, CreateCommentRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User currentUser = ensureCurrentUserCanInteract(userId);
        String content = validateCommentContent(request);

        Comment parentComment = commentRepository.findForReplyCreationById(parentCommentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_PARENT_NOT_FOUND));
        if (parentComment.getStatus() != CommentStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.COMMENT_PARENT_NOT_AVAILABLE);
        }
        if (parentComment.getParentComment() != null) {
            throw new BusinessException(ErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED);
        }

        Long postId = parentComment.getPost().getId();
        ensurePostIsPublished(postId);

        // post_id luôn lấy từ bình luận cha để Client không thể gắn reply sang bài viết khác.
        Comment reply = commentRepository.saveAndFlush(
                new Comment(parentComment.getPost(), currentUser, parentComment, content)
        );
        entityManager.refresh(reply);
        return commentMapper.toResponse(reply);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getPublishedComments(Long postId, int page, int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanInteract(userId);
        ensurePostIsPublished(postId);

        Page<Comment> comments = commentRepository.findVisibleRootComments(
                postId,
                CommentStatus.PUBLISHED,
                PageRequest.of(page, size)
        );
        List<Long> commentIds = comments.getContent().stream().map(Comment::getId).toList();
        Map<Long, Long> replyCounts = getPublishedReplyCounts(commentIds);

        return PageResponse.from(comments.map(comment -> commentMapper.toResponse(
                comment,
                replyCounts.getOrDefault(comment.getId(), 0L)
        )));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getPublishedReplies(Long parentCommentId, int page, int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanInteract(userId);

        Comment parentComment = commentRepository.findWithPostAndParentById(parentCommentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (parentComment.getParentComment() != null) {
            throw new BusinessException(ErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED);
        }
        ensurePostIsPublished(parentComment.getPost().getId());

        Page<CommentResponse> replies = commentRepository
                .findByParentComment_IdAndStatusOrderByCreatedAtAscIdAsc(
                        parentCommentId,
                        CommentStatus.PUBLISHED,
                        PageRequest.of(page, size)
                )
                .map(commentMapper::toResponse);
        return PageResponse.from(replies);
    }

    @Override
    @Transactional
    public DeleteCommentResponse deleteComment(Long commentId) {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanInteract(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        // Chỉ chuyển trạng thái sang DELETED; trigger database tự giảm posts.comment_count.
        int updatedRows = commentRepository.softDeletePublishedComment(commentId, LocalDateTime.now(clock));
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return new DeleteCommentResponse(commentId, true);
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

    private void ensurePostIsPublished(Long postId) {
        PostStatus status = postRepository.findStatusById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (status != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
    }

    private String validateCommentContent(CreateCommentRequest request) {
        String content = request == null ? null : request.content();
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_REQUIRED);
        }
        String normalizedContent = content.trim();
        if (normalizedContent.length() > 1000) {
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
        }
        return normalizedContent;
    }

    private Map<Long, Long> getPublishedReplyCounts(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }
        return commentRepository.countRepliesByParentIdsAndStatus(commentIds, CommentStatus.PUBLISHED)
                .stream()
                .collect(Collectors.toMap(
                        projection -> projection.getCommentId(),
                        projection -> projection.getReplyCount()
                ));
    }
}
