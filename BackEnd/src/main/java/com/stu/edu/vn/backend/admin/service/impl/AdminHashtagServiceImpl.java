package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationEvent;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationRouter;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagDeleteResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagUpdateResponse;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminHashtagRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminHashtagListProjection;
import com.stu.edu.vn.backend.admin.service.AdminHashtagService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/** Triển khai truy vấn danh sách hashtag mà không phát sinh N+1. */
@Service
public class AdminHashtagServiceImpl implements AdminHashtagService {
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminHashtagRepository adminHashtagRepository;
    private final AdminActionRepository adminActionRepository;
    private final HashtagNormalizer hashtagNormalizer;
    private final CurrentUserProvider currentUserProvider;
    private final EntityManager entityManager;
    private AdminNotificationRouter adminNotificationRouter;

    public AdminHashtagServiceImpl(
            AdminHashtagRepository adminHashtagRepository,
            AdminActionRepository adminActionRepository,
            HashtagNormalizer hashtagNormalizer,
            CurrentUserProvider currentUserProvider,
            EntityManager entityManager
    ) {
        this.adminHashtagRepository = adminHashtagRepository;
        this.adminActionRepository = adminActionRepository;
        this.hashtagNormalizer = hashtagNormalizer;
        this.currentUserProvider = currentUserProvider;
        this.entityManager = entityManager;
    }

    @Autowired
    void setAdminNotificationRouter(AdminNotificationRouter adminNotificationRouter) {
        this.adminNotificationRouter = adminNotificationRouter;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminHashtagListItemResponse> getHashtags(String keyword, int page, int size) {
        validatePagination(page, size);
        String escapedKeyword = normalizeAndEscapeOptionalKeyword(keyword);
        // Một truy vấn tổng hợp trả cả bộ đếm và lần sử dụng mới nhất, không tải danh sách bài viết.
        return PageResponse.from(adminHashtagRepository
                .findAdminHashtags(escapedKeyword, PageRequest.of(page, size))
                .map(this::toResponse));
    }

    @Override
    @Transactional
    public AdminHashtagListItemResponse createHashtag(String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String normalizedName = normalizeRequiredName(name);
        if (adminHashtagRepository.existsByNormalizedName(normalizedName)) {
            throw new BusinessException(ErrorCode.ADMIN_HASHTAG_ALREADY_EXISTS);
        }

        Hashtag hashtag;
        try {
            // Unique normalized_name là lớp bảo vệ cuối cùng khi hai ADMIN tạo cùng tên đồng thời.
            hashtag = adminHashtagRepository.saveAndFlush(new Hashtag(normalizedName, normalizedName));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ADMIN_HASHTAG_ALREADY_EXISTS);
        }
        entityManager.refresh(hashtag);
        AdminAction action = saveAudit(principal.getUserId(), AdminActionType.CREATE_HASHTAG, hashtag.getId(),
                "#" + normalizedName);
        notifyHashtag(principal.getUserId(), hashtag.getId(), action == null ? null : action.getId(),
                AdminNotificationType.HASHTAG_CREATED, "Hashtag mới đã được tạo");
        return toResponse(hashtag);
    }

    @Override
    @Transactional
    public AdminHashtagDeleteResponse deleteHashtag(Long hashtagId) {
        CustomUserPrincipal principal = requireActiveAdmin();
        if (hashtagId == null || hashtagId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Hashtag hashtag = adminHashtagRepository.findByIdForUpdate(hashtagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_HASHTAG_NOT_FOUND));
        String name = hashtag.getDisplayName();

        // FK RESTRICT yêu cầu gỡ quan hệ trước; trigger hiện tại tự giảm post_count cho từng quan hệ.
        int detachedPostCount = adminHashtagRepository.deletePostRelations(hashtagId);
        adminHashtagRepository.delete(hashtag);
        adminHashtagRepository.flush();
        AdminAction action = saveAudit(principal.getUserId(), AdminActionType.DELETE_HASHTAG, hashtagId,
                "#" + name + "; detachedPosts=" + detachedPostCount);
        notifyHashtag(principal.getUserId(), hashtagId, action == null ? null : action.getId(),
                AdminNotificationType.HASHTAG_DELETED, "Hashtag đã bị xóa");
        return new AdminHashtagDeleteResponse(hashtagId, name, detachedPostCount);
    }

    @Override
    @Transactional
    public AdminHashtagUpdateResponse updateHashtag(Long hashtagId, String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        if (hashtagId == null || hashtagId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Hashtag hashtag = adminHashtagRepository.findByIdForUpdate(hashtagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_HASHTAG_NOT_FOUND));
        String normalizedName = normalizeRequiredName(name);
        if (normalizedName.equals(hashtag.getNormalizedName())) {
            // Không tạo audit khi giá trị nghiệp vụ không đổi sau chuẩn hóa.
            return new AdminHashtagUpdateResponse(hashtagId, hashtag.getDisplayName());
        }
        if (adminHashtagRepository.existsByNormalizedNameAndIdNot(normalizedName, hashtagId)) {
            throw new BusinessException(ErrorCode.ADMIN_HASHTAG_ALREADY_EXISTS);
        }

        String oldName = hashtag.getDisplayName();
        hashtag.setNormalizedName(normalizedName);
        hashtag.setDisplayName(normalizedName);
        try {
            adminHashtagRepository.saveAndFlush(hashtag);
        } catch (DataIntegrityViolationException exception) {
            // Unique constraint xử lý race khi hai ADMIN đổi hai hashtag về cùng tên.
            throw new BusinessException(ErrorCode.ADMIN_HASHTAG_ALREADY_EXISTS);
        }
        AdminAction action = saveAudit(principal.getUserId(), AdminActionType.UPDATE_HASHTAG, hashtagId,
                "old=#" + oldName + "; new=#" + normalizedName);
        notifyHashtag(principal.getUserId(), hashtagId, action == null ? null : action.getId(),
                AdminNotificationType.HASHTAG_UPDATED, "Hashtag đã được cập nhật");
        return new AdminHashtagUpdateResponse(hashtagId, normalizedName);
    }

    private AdminHashtagListItemResponse toResponse(AdminHashtagListProjection source) {
        return new AdminHashtagListItemResponse(
                source.getHashtagId(), source.getName(), source.getPostCount(),
                source.getCreatedAt(), source.getLatestUsedAt());
    }

    private AdminHashtagListItemResponse toResponse(Hashtag source) {
        return new AdminHashtagListItemResponse(
                source.getId(), source.getDisplayName(), source.getPostCount(),
                source.getCreatedAt(), null);
    }

    private String normalizeRequiredName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.ADMIN_HASHTAG_NAME_REQUIRED);
        }
        try {
            String normalizedName = hashtagNormalizer.normalizeOptional(name);
            if (normalizedName == null) {
                throw new BusinessException(ErrorCode.ADMIN_HASHTAG_NAME_REQUIRED);
            }
            return normalizedName;
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.POST_HASHTAG_TOO_LONG) {
                throw new BusinessException(ErrorCode.ADMIN_HASHTAG_NAME_TOO_LONG);
            }
            if (exception.getErrorCode() == ErrorCode.POST_HASHTAG_INVALID) {
                throw new BusinessException(ErrorCode.ADMIN_HASHTAG_NAME_INVALID);
            }
            throw exception;
        }
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

    private AdminAction saveAudit(Long adminId, AdminActionType actionType, Long hashtagId, String note) {
        User adminReference = entityManager.getReference(User.class, adminId);
        return adminActionRepository.save(new AdminAction(
                adminReference, actionType, AdminTargetType.HASHTAG, hashtagId, note));
    }

    private void notifyHashtag(Long actorId, Long hashtagId, Long actionId,
            AdminNotificationType type, String title) {
        if (adminNotificationRouter == null) return;
        adminNotificationRouter.notifyByPermission("HASHTAG_VIEW", actorId, new AdminNotificationEvent(
                type, title, title + ".", AdminNotificationReferenceType.HASHTAG, hashtagId,
                "ADMIN_ACTION:" + actionId + ":" + type.name()));
    }

    private String normalizeAndEscapeOptionalKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return LikePatternEscaper.escape(normalizedKeyword);
    }

    private void validatePagination(int page, int size) {
        // Service vẫn tự bảo vệ khi use case được gọi ngoài HTTP Controller.
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
