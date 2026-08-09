package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseActionRequest;
import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseNoViolationRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseStatusResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportResolvedByResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusPostResponse;
import com.stu.edu.vn.backend.admin.dto.response.ModerationReasonCountResponse;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.enums.ModerationCaseAction;
import com.stu.edu.vn.backend.admin.mapper.AdminModerationCaseMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminModerationCaseRepository;
import com.stu.edu.vn.backend.admin.repository.AdminPostRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.ModerationReasonCountProjection;
import com.stu.edu.vn.backend.admin.service.AdminModerationCaseService;
import com.stu.edu.vn.backend.admin.service.ModerationAccountBlockService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.report.entity.ModerationCase;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.repository.ModerationCaseRepository;
import com.stu.edu.vn.backend.report.repository.ReportRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Triển khai aggregation và state machine ba trạng thái của Moderation Case. */
@Service
public class AdminModerationCaseServiceImpl implements AdminModerationCaseService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_RESOLUTION_NOTE_LENGTH = 1000;
    private static final long POST_VIOLATION_BLOCK_THRESHOLD = 3L;

    private final AdminModerationCaseRepository adminCaseRepository;
    private final ModerationCaseRepository caseRepository;
    private final ReportRepository reportRepository;
    private final AdminPostRepository adminPostRepository;
    private final AdminActionRepository adminActionRepository;
    private final AdminModerationCaseMapper mapper;
    private final NotificationService notificationService;
    private final ModerationAccountBlockService accountBlockService;
    private final CurrentUserProvider currentUserProvider;
    private final EntityManager entityManager;
    private final Clock clock;

    public AdminModerationCaseServiceImpl(
            AdminModerationCaseRepository adminCaseRepository,
            ModerationCaseRepository caseRepository,
            ReportRepository reportRepository,
            AdminPostRepository adminPostRepository,
            AdminActionRepository adminActionRepository,
            AdminModerationCaseMapper mapper,
            NotificationService notificationService,
            ModerationAccountBlockService accountBlockService,
            CurrentUserProvider currentUserProvider,
            EntityManager entityManager,
            Clock clock
    ) {
        this.adminCaseRepository = adminCaseRepository;
        this.caseRepository = caseRepository;
        this.reportRepository = reportRepository;
        this.adminPostRepository = adminPostRepository;
        this.adminActionRepository = adminActionRepository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.accountBlockService = accountBlockService;
        this.currentUserProvider = currentUserProvider;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminModerationCaseListItemResponse> getCases(
            ModerationCaseStatus status,
            ReportReason reason,
            Long postId,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        validatePagination(page, size);
        validateOptionalId(postId);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String normalizedKeyword = normalizeKeyword(keyword);
        LocalDateTime fromTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toTime = toDate == null ? null : toDate.atTime(LocalTime.MAX);
        Page<AdminModerationCaseListProjection> source = adminCaseRepository.findCases(
                status == null ? null : status.name(), reason == null ? null : reason.name(), postId,
                normalizedKeyword, fromTime, toTime, PageRequest.of(page, size));
        List<Long> caseIds = source.getContent().stream().map(AdminModerationCaseListProjection::getCaseId).toList();
        Map<Long, List<ModerationReasonCountResponse>> reasonsByCase = loadReasonCounts(caseIds);
        List<AdminModerationCaseListItemResponse> content = source.getContent().stream()
                .map(item -> mapper.toListItem(item, reasonsByCase))
                .toList();
        return PageResponse.from(new PageImpl<>(content, source.getPageable(), source.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminModerationCaseDetailResponse getCaseDetail(Long caseId) {
        validateCaseId(caseId);
        var detail = adminCaseRepository.findCaseDetail(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_MODERATION_CASE_NOT_FOUND));
        List<ModerationReasonCountResponse> reasons = loadReasonCounts(List.of(caseId))
                .getOrDefault(caseId, List.of());
        return mapper.toDetail(detail, reasons, adminCaseRepository.findCaseReports(caseId),
                adminCaseRepository.findCaseActions(caseId));
    }

    @Override
    @Transactional
    public AdminModerationCaseStatusResponse resolveNoViolation(
            Long caseId,
            ResolveModerationCaseNoViolationRequest request
    ) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String note = normalizeOptionalResolutionNote(request == null ? null : request.resolutionNote());
        ModerationCase moderationCase = lockOpenCase(caseId);
        User admin = entityManager.getReference(User.class, principal.getUserId());
        LocalDateTime now = LocalDateTime.now(clock);
        List<Report> reports = reportRepository.findByModerationCase_IdOrderByCreatedAtDescIdDesc(caseId);

        moderationCase.resolveNoViolation(admin, note, now);
        reports.forEach(report -> {
            report.reject(admin, now, note);
            notificationService.createReportRejectedNotification(report.getReporter().getId(), report.getId());
        });
        adminActionRepository.save(new AdminAction(admin, AdminActionType.REJECT_MODERATION_CASE,
                AdminTargetType.MODERATION_CASE, caseId, note));
        entityManager.flush();
        return toStatusResponse(moderationCase, moderationCase.getPost(), principal.getUserId(), 0L, false);
    }

    @Override
    @Transactional
    public AdminModerationCaseStatusResponse resolveAction(
            Long caseId,
            ResolveModerationCaseActionRequest request
    ) {
        CustomUserPrincipal principal = requireActiveAdmin();
        if (request == null || request.action() != ModerationCaseAction.HIDE_POST || request.reasonCode() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String note = normalizeOptionalResolutionNote(request.resolutionNote());
        validateCaseId(caseId);
        ModerationCase caseSnapshot = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_MODERATION_CASE_NOT_FOUND));
        // Khóa Post trước Moderation Case để cùng thứ tự với luồng tạo Report, tránh deadlock chéo.
        Post post = adminPostRepository.findByIdForUpdate(caseSnapshot.getPost().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_POST_NOT_FOUND));
        ModerationCase moderationCase = lockOpenCase(caseId);
        User admin = entityManager.getReference(User.class, principal.getUserId());
        LocalDateTime now = LocalDateTime.now(clock);
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(post.getStatus() == PostStatus.DELETED
                    ? ErrorCode.ADMIN_POST_DELETED_ACTION_FORBIDDEN
                    : ErrorCode.ADMIN_POST_ALREADY_HIDDEN);
        }

        AdminPostModerationHelper.hidePublishedPost(post, admin, now, request.reasonCode());
        moderationCase.resolveActionTaken(admin, note, now);
        List<Report> reports = reportRepository.findByModerationCase_IdOrderByCreatedAtDescIdDesc(caseId);
        reports.forEach(report -> {
            report.resolve(admin, now, note);
            notificationService.createReportResolvedNotification(report.getReporter().getId(), report.getId());
        });
        adminActionRepository.save(new AdminAction(admin, AdminActionType.RESOLVE_MODERATION_CASE,
                AdminTargetType.MODERATION_CASE, caseId, note));
        adminActionRepository.save(new AdminAction(admin, AdminActionType.HIDE_POST,
                AdminTargetType.POST, post.getId(), request.reasonCode().name()));
        notificationService.createPostHiddenByAdminNotification(post.getAuthor().getId(), post.getId());
        // Flush case hiện tại trước khi đếm để lần vi phạm vừa kết luận được tính đúng là một strike.
        entityManager.flush();
        long violationCount = caseRepository.countByPost_Author_IdAndStatus(
                post.getAuthor().getId(), ModerationCaseStatus.RESOLVED_ACTION_TAKEN);
        boolean accountBlocked = false;
        if (violationCount >= POST_VIOLATION_BLOCK_THRESHOLD) {
            accountBlocked = accountBlockService.blockIfActive(
                    post.getAuthor().getId(), admin, now, AdminBlockReason.REPEATED_VIOLATION);
        }
        entityManager.flush();
        return toStatusResponse(moderationCase, post, principal.getUserId(), violationCount, accountBlocked);
    }

    private ModerationCase lockOpenCase(Long caseId) {
        validateCaseId(caseId);
        ModerationCase moderationCase = caseRepository.findByIdForUpdate(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_MODERATION_CASE_NOT_FOUND));
        if (moderationCase.getStatus() != ModerationCaseStatus.OPEN) {
            throw new BusinessException(ErrorCode.ADMIN_MODERATION_CASE_ALREADY_RESOLVED);
        }
        return moderationCase;
    }

    private Map<Long, List<ModerationReasonCountResponse>> loadReasonCounts(List<Long> caseIds) {
        Map<Long, List<ModerationReasonCountResponse>> result = new LinkedHashMap<>();
        if (caseIds.isEmpty()) return result;
        for (ModerationReasonCountProjection row : adminCaseRepository.findReasonCounts(caseIds)) {
            result.computeIfAbsent(row.getCaseId(), ignored -> new ArrayList<>())
                    .add(new ModerationReasonCountResponse(
                            ReportReason.valueOf(row.getReason()), row.getReasonCount()));
        }
        result.replaceAll((ignored, values) -> List.copyOf(values));
        return result;
    }

    private AdminModerationCaseStatusResponse toStatusResponse(
            ModerationCase moderationCase,
            Post post,
            Long adminId,
            long authorViolationCount,
            boolean accountBlocked
    ) {
        String displayName = adminPostRepository.findAdminDisplayName(adminId).orElse(null);
        return new AdminModerationCaseStatusResponse(
                moderationCase.getId(), moderationCase.getStatus(), moderationCase.getResolvedAt(),
                moderationCase.getResolutionNote(), new AdminReportResolvedByResponse(adminId, displayName),
                new AdminReportStatusPostResponse(
                        post.getId(), post.getStatus(), post.getHiddenAt(), post.getHiddenReason()),
                authorViolationCount, accountBlocked);
    }

    private CustomUserPrincipal requireActiveAdmin() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.ADMIN) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (principal.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_BLOCKED);
        return principal;
    }

    private String normalizeOptionalResolutionNote(String rawNote) {
        String note = rawNote == null ? null : rawNote.trim();
        if (note == null || note.isEmpty()) return null;
        if (note.length() > MAX_RESOLUTION_NOTE_LENGTH) {
            throw new BusinessException(ErrorCode.ADMIN_MODERATION_CASE_RESOLUTION_NOTE_TOO_LONG);
        }
        return note;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        String normalized = keyword.trim();
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.ADMIN_MODERATION_CASE_KEYWORD_TOO_LONG);
        }
        return LikePatternEscaper.escape(normalized);
    }

    private void validateCaseId(Long caseId) {
        if (caseId == null || caseId <= 0) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    private void validateOptionalId(Long id) {
        if (id != null && id <= 0) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
