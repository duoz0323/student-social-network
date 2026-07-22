package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.dto.request.AdminRejectReportRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminResolveReportRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusResponse;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminReportMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminPostRepository;
import com.stu.edu.vn.backend.admin.repository.AdminReportRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminReportDetailProjection;
import com.stu.edu.vn.backend.admin.service.AdminReportService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Triển khai truy vấn projection chỉ đọc cho màn hình quản trị báo cáo. */
@Service
public class AdminReportServiceImpl implements AdminReportService {
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_RESOLUTION_NOTE_LENGTH = 500;

    private final AdminReportRepository adminReportRepository;
    private final AdminReportMapper adminReportMapper;
    private final AdminPostRepository adminPostRepository;
    private final AdminActionRepository adminActionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EntityManager entityManager;
    private final Clock clock;
    private final NotificationService notificationService;

    public AdminReportServiceImpl(
            AdminReportRepository adminReportRepository,
            AdminReportMapper adminReportMapper,
            AdminPostRepository adminPostRepository,
            AdminActionRepository adminActionRepository,
            CurrentUserProvider currentUserProvider,
            EntityManager entityManager,
            Clock clock,
            NotificationService notificationService
    ) {
        this.adminReportRepository = adminReportRepository;
        this.adminReportMapper = adminReportMapper;
        this.adminPostRepository = adminPostRepository;
        this.adminActionRepository = adminActionRepository;
        this.currentUserProvider = currentUserProvider;
        this.entityManager = entityManager;
        this.clock = clock;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminReportListItemResponse> getReports(
            ReportStatus status,
            ReportReason reason,
            Long postId,
            Long reporterId,
            Long authorId,
            String keyword,
            int page,
            int size
    ) {
        validatePagination(page, size);
        validateOptionalId(postId);
        validateOptionalId(reporterId);
        validateOptionalId(authorId);
        String escapedKeyword = normalizeAndEscapeOptionalKeyword(keyword);

        // Chỉ PENDING dùng thứ tự hàng đợi tăng dần; status null và các trạng thái còn lại giảm dần.
        int pendingOrder = status == ReportStatus.PENDING ? 1 : 0;
        return PageResponse.from(adminReportRepository.findAdminReports(
                        status == null ? null : status.name(),
                        reason == null ? null : reason.name(),
                        postId, reporterId, authorId, escapedKeyword, pendingOrder,
                        PageRequest.of(page, size))
                .map(adminReportMapper::toListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReportDetailResponse getReportDetail(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        AdminReportDetailProjection projection = adminReportRepository.findAdminReportDetail(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_REPORT_NOT_FOUND));
        return adminReportMapper.toDetail(projection);
    }

    @Override
    @Transactional
    public AdminReportStatusResponse rejectReport(Long reportId, AdminRejectReportRequest request) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String resolutionNote = normalizeResolutionNote(request == null ? null : request.resolutionNote());
        Report report = lockPendingReport(reportId);
        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        LocalDateTime now = LocalDateTime.now(clock);

        report.reject(adminReference, now, resolutionNote);
        adminActionRepository.save(new AdminAction(adminReference, AdminActionType.REJECT_REPORT,
                AdminTargetType.REPORT, report.getId(), resolutionNote));
        notificationService.createReportRejectedNotification(report.getReporter().getId(), report.getId());

        // Flush chủ động để lỗi constraint hoặc audit rollback Report trước khi trả response.
        entityManager.flush();
        return toStatusResponse(report, report.getPost(), principal.getUserId());
    }

    @Override
    @Transactional
    public AdminReportStatusResponse resolveReport(Long reportId, AdminResolveReportRequest request) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String resolutionNote = normalizeResolutionNote(request == null ? null : request.resolutionNote());
        boolean hidePost = request != null && Boolean.TRUE.equals(request.hidePost());
        AdminPostHideReason hideReason = validateHideConfiguration(request, hidePost);
        Report report = lockPendingReport(reportId);
        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        LocalDateTime now = LocalDateTime.now(clock);
        Post post = report.getPost();
        boolean postHiddenNow = false;

        if (hidePost) {
            // Chỉ workflow có yêu cầu ẩn mới khóa Post; không tải media, hashtag hoặc snapshot.
            post = adminPostRepository.findByIdForUpdate(post.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_POST_NOT_FOUND));
            if (post.getStatus() == PostStatus.PUBLISHED) {
                AdminPostModerationHelper.hidePublishedPost(post, adminReference, now, hideReason);
                postHiddenNow = true;
            }
            // HIDDEN và DELETED là no-op có chủ đích, không tạo HIDE_POST giả.
        }

        report.resolve(adminReference, now, resolutionNote);
        adminActionRepository.save(new AdminAction(adminReference, AdminActionType.RESOLVE_REPORT,
                AdminTargetType.REPORT, report.getId(), resolutionNote));
        if (postHiddenNow) {
            adminActionRepository.save(new AdminAction(adminReference, AdminActionType.HIDE_POST,
                    AdminTargetType.POST, post.getId(), hideReason.name()));
            notificationService.createPostHiddenByAdminNotification(post.getAuthor().getId(), post.getId());
        }
        notificationService.createReportResolvedNotification(report.getReporter().getId(), report.getId());

        // Report, Post và các action cùng flush trong transaction; một lỗi sẽ rollback toàn bộ.
        entityManager.flush();
        return toStatusResponse(report, post, principal.getUserId());
    }

    private Report lockPendingReport(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Report report = adminReportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_REPORT_NOT_FOUND));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_ALREADY_PROCESSED);
        }
        return report;
    }

    private String normalizeResolutionNote(String rawNote) {
        String note = rawNote == null ? null : rawNote.trim();
        if (note == null || note.isEmpty()) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_REQUIRED);
        }
        if (note.length() > MAX_RESOLUTION_NOTE_LENGTH) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_TOO_LONG);
        }
        return note;
    }

    private AdminPostHideReason validateHideConfiguration(AdminResolveReportRequest request, boolean hidePost) {
        AdminPostHideReason reason = request == null ? null : request.hideReasonCode();
        if (hidePost && reason == null) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_HIDE_REASON_REQUIRED);
        }
        if (!hidePost && reason != null) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_HIDE_REASON_NOT_ALLOWED);
        }
        return reason;
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

    private AdminReportStatusResponse toStatusResponse(Report report, Post post, Long adminId) {
        String displayName = adminPostRepository.findAdminDisplayName(adminId).orElse(null);
        return adminReportMapper.toStatus(report, post, adminId, displayName);
    }

    private String normalizeAndEscapeOptionalKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_KEYWORD_TOO_LONG);
        }
        return LikePatternEscaper.escape(normalizedKeyword);
    }

    private void validatePagination(int page, int size) {
        // Service tự bảo vệ use case khi được gọi ngoài HTTP Controller.
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateOptionalId(Long id) {
        if (id != null && id <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
