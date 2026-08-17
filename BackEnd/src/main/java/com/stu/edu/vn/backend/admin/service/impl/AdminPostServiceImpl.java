package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationEvent;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationRouter;
import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostStatusResponse;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminPostMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminPostRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostHashtagProjection;
import com.stu.edu.vn.backend.admin.service.AdminPostService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/** Triển khai truy vấn và transaction kiểm duyệt bài viết dành cho ADMIN. */
@Service
public class AdminPostServiceImpl implements AdminPostService {
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String RESTORE_NOTE = "ADMIN_RESTORE";

    private final AdminPostRepository adminPostRepository;
    private final AdminPostMapper adminPostMapper;
    private final AdminActionRepository adminActionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EntityManager entityManager;
    private final Clock clock;
    private final NotificationService notificationService;
    private AdminNotificationRouter adminNotificationRouter;

    public AdminPostServiceImpl(
            AdminPostRepository adminPostRepository,
            AdminPostMapper adminPostMapper,
            AdminActionRepository adminActionRepository,
            CurrentUserProvider currentUserProvider,
            EntityManager entityManager,
            Clock clock,
            NotificationService notificationService
    ) {
        this.adminPostRepository = adminPostRepository;
        this.adminPostMapper = adminPostMapper;
        this.adminActionRepository = adminActionRepository;
        this.currentUserProvider = currentUserProvider;
        this.entityManager = entityManager;
        this.clock = clock;
        this.notificationService = notificationService;
    }

    @Autowired
    void setAdminNotificationRouter(AdminNotificationRouter adminNotificationRouter) {
        this.adminNotificationRouter = adminNotificationRouter;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPostListItemResponse> getPosts(String keyword, PostStatus status,
            Long authorId, boolean reportedOnly, int page, int size) {
        validatePagination(page, size);
        validateAuthorId(authorId);
        String escapedKeyword = normalizeAndEscapeOptionalKeyword(keyword);
        String statusValue = status == null ? null : status.name();

        // Projection đã chứa thumbnail và các bộ đếm nên Page.map không phát sinh query bổ sung.
        return PageResponse.from(adminPostRepository.findAdminPosts(escapedKeyword, statusValue, authorId,
                        reportedOnly ? 1 : 0, PageRequest.of(page, size))
                .map(adminPostMapper::toListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPostDetailResponse getPostDetail(Long postId) {
        AdminPostDetailProjection detail = adminPostRepository.findAdminPostDetail(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_POST_NOT_FOUND));
        // Hai query con có số lượng cố định và chỉ chọn trường an toàn, không phụ thuộc số phần tử.
        return adminPostMapper.toDetail(detail, adminPostRepository.findAdminPostMedia(postId),
                readSingleHashtag(postId));
    }

    @Override
    @Transactional
    public AdminPostStatusResponse hidePost(Long postId, AdminHidePostRequest request) {
        CustomUserPrincipal principal = requireActiveAdmin();
        if (request == null || request.reasonCode() == null) {
            throw new BusinessException(ErrorCode.ADMIN_POST_HIDE_REASON_REQUIRED);
        }
        Post post = lockPost(postId);
        if (post.getStatus() == PostStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.ADMIN_POST_ALREADY_HIDDEN);
        }
        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException(ErrorCode.ADMIN_POST_DELETED_ACTION_FORBIDDEN);
        }

        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        String reason = request.reasonCode().name();
        AdminPostModerationHelper.hidePublishedPost(
                post, adminReference, LocalDateTime.now(clock), request.reasonCode());

        // Audit và trạng thái Post cùng thuộc transaction; lỗi lưu hoặc flush sẽ rollback cả hai.
        AdminAction action = adminActionRepository.save(new AdminAction(adminReference, AdminActionType.HIDE_POST,
                AdminTargetType.POST, post.getId(), reason));
        notificationService.createPostHiddenByAdminNotification(post.getAuthor().getId(), post.getId());
        notifyPostAction(principal.getUserId(), post.getId(), action == null ? null : action.getId(), true);
        return flushRefreshAndMap(post, principal.getUserId());
    }

    @Override
    @Transactional
    public AdminPostStatusResponse restorePost(Long postId) {
        CustomUserPrincipal principal = requireActiveAdmin();
        Post post = lockPost(postId);
        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.ADMIN_POST_ALREADY_PUBLISHED);
        }
        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException(ErrorCode.ADMIN_POST_DELETED_ACTION_FORBIDDEN);
        }

        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        post.setStatus(PostStatus.PUBLISHED);
        post.setHiddenBy(null);
        post.setHiddenAt(null);
        post.setHiddenReason(null);

        // Không sửa action HIDE_POST cũ; mỗi lần khôi phục ghi một action độc lập.
        AdminAction action = adminActionRepository.save(new AdminAction(adminReference, AdminActionType.RESTORE_POST,
                AdminTargetType.POST, post.getId(), RESTORE_NOTE));
        notificationService.createPostRestoredByAdminNotification(post.getAuthor().getId(), post.getId());
        notifyPostAction(principal.getUserId(), post.getId(), action == null ? null : action.getId(), false);
        return flushRefreshAndMap(post, principal.getUserId());
    }

    private void notifyPostAction(Long actorId, Long postId, Long actionId, boolean hidden) {
        if (adminNotificationRouter == null) return;
        adminNotificationRouter.notifyByPermission("POST_VIEW", actorId, new AdminNotificationEvent(
                hidden ? AdminNotificationType.POST_HIDDEN_BY_ADMIN : AdminNotificationType.POST_RESTORED_BY_ADMIN,
                hidden ? "Bài viết đã bị ẩn" : "Bài viết đã được khôi phục",
                hidden ? "Một bài viết vừa bị quản trị viên ẩn." : "Một bài viết vừa được quản trị viên khôi phục.",
                AdminNotificationReferenceType.POST,
                postId,
                "ADMIN_ACTION:" + actionId + ":" + (hidden ? "POST_HIDDEN" : "POST_RESTORED")));
    }

    private CustomUserPrincipal requireActiveAdmin() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        return principal;
    }

    private Post lockPost(Long postId) {
        // PESSIMISTIC_WRITE khiến request thứ hai chờ và đọc trạng thái mới sau khi request đầu commit.
        return adminPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_POST_NOT_FOUND));
    }

    private AdminPostStatusResponse flushRefreshAndMap(Post post, Long adminId) {
        // Flush chủ động để mọi lỗi constraint/audit xảy ra trước khi tạo response và vẫn nằm trong transaction.
        entityManager.flush();
        entityManager.refresh(post);
        String displayName = post.getStatus() == PostStatus.HIDDEN
                ? adminPostRepository.findAdminDisplayName(adminId).orElse(null)
                : null;
        return adminPostMapper.toStatus(post, adminId, displayName);
    }

    private String normalizeAndEscapeOptionalKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.ADMIN_POST_KEYWORD_TOO_LONG);
        }
        return LikePatternEscaper.escape(normalizedKeyword);
    }

    private void validatePagination(int page, int size) {
        // Kiểm tra ở Service để use case vẫn an toàn khi được gọi ngoài HTTP Controller.
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateAuthorId(Long authorId) {
        if (authorId != null && authorId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private String readSingleHashtag(Long postId) {
        List<AdminPostHashtagProjection> hashtags = adminPostRepository.findAdminPostHashtags(postId);
        if (hashtags.size() > 1) {
            // Dữ liệu vi phạm invariant phải được phát hiện rõ thay vì âm thầm chọn một hashtag.
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return hashtags.isEmpty() ? null : hashtags.get(0).getName();
    }
}
