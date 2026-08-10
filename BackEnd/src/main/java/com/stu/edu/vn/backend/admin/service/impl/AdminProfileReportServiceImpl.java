package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.dto.request.AdminProfileReportResolutionRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportReporterResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportSnapshotResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportStatusResponse;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.service.AdminProfileReportService;
import com.stu.edu.vn.backend.admin.service.ModerationAccountBlockService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.entity.ProfileReport;
import com.stu.edu.vn.backend.report.entity.ProfileReportCase;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.repository.ProfileReportCaseRepository;
import com.stu.edu.vn.backend.report.repository.ProfileReportRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đọc và kết luận vụ việc hồ sơ đã gom nhiều người báo cáo, không tự động khóa USER. */
@Service
public class AdminProfileReportServiceImpl implements AdminProfileReportService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final ProfileReportCaseRepository caseRepository;
    private final ProfileReportRepository reportRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AdminActionRepository adminActionRepository;
    private final ModerationAccountBlockService accountBlockService;
    private final EntityManager entityManager;
    private final Clock clock;

    public AdminProfileReportServiceImpl(
            ProfileReportCaseRepository caseRepository,
            ProfileReportRepository reportRepository,
            CurrentUserProvider currentUserProvider,
            AdminActionRepository adminActionRepository,
            ModerationAccountBlockService accountBlockService,
            EntityManager entityManager,
            Clock clock
    ) {
        this.caseRepository = caseRepository;
        this.reportRepository = reportRepository;
        this.currentUserProvider = currentUserProvider;
        this.adminActionRepository = adminActionRepository;
        this.accountBlockService = accountBlockService;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminProfileReportListItemResponse> getReports(
            ReportStatus status, String keyword, int page, int size) {
        validatePagination(page, size);
        String normalizedKeyword = normalizeKeyword(keyword);
        Sort.Direction direction = status == ReportStatus.PENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(direction, "latestReportedAt").and(Sort.by(direction, "id")));

        Page<ProfileReportCase> cases;
        if (status != null && normalizedKeyword != null) {
            cases = caseRepository.findByStatusAndReportedDisplayNameSnapshotContainingIgnoreCase(
                    status, normalizedKeyword, pageable);
        } else if (status != null) {
            cases = caseRepository.findByStatus(status, pageable);
        } else if (normalizedKeyword != null) {
            cases = caseRepository.findByReportedDisplayNameSnapshotContainingIgnoreCase(
                    normalizedKeyword, pageable);
        } else {
            cases = caseRepository.findAll(pageable);
        }
        return PageResponse.from(cases.map(this::toListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProfileReportDetailResponse getDetail(Long caseId) {
        ProfileReportCase reportCase = requireCase(caseId);
        return toDetail(reportCase, reportRepository.findAllByReportCase_IdOrderByCreatedAtAscIdAsc(caseId));
    }

    @Override
    @Transactional
    public AdminProfileReportStatusResponse reject(
            Long caseId, AdminProfileReportResolutionRequest request) {
        return process(caseId, request, false);
    }

    @Override
    @Transactional
    public AdminProfileReportStatusResponse resolve(
            Long caseId, AdminProfileReportResolutionRequest request) {
        return process(caseId, request, true);
    }

    private AdminProfileReportStatusResponse process(
            Long caseId,
            AdminProfileReportResolutionRequest request,
            boolean violationConfirmed
    ) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String note = normalizeResolutionNote(request);
        if (!violationConfirmed && request.shouldBlockUser()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        ProfileReportCase reportCase = lockPendingCase(caseId);
        List<ProfileReport> pendingReports = reportRepository.findAllByCaseIdAndStatusForUpdate(
                reportCase.getId(), ReportStatus.PENDING);
        if (pendingReports.isEmpty()) {
            throw new BusinessException(ErrorCode.ADMIN_PROFILE_REPORT_ALREADY_PROCESSED);
        }

        User admin = entityManager.getReference(User.class, principal.getUserId());
        LocalDateTime now = LocalDateTime.now(clock);
        for (ProfileReport report : pendingReports) {
            if (violationConfirmed) report.resolve(admin, now, note);
            else report.reject(admin, now, note);
        }
        if (violationConfirmed) reportCase.resolve(admin, now, note);
        else reportCase.reject(admin, now, note);

        boolean accountBlocked = violationConfirmed && request.shouldBlockUser()
                && accountBlockService.blockIfActive(
                        reportCase.getReportedUser().getId(), admin, now, AdminBlockReason.PROFILE_VIOLATION);

        adminActionRepository.save(new AdminAction(
                admin,
                violationConfirmed ? AdminActionType.RESOLVE_PROFILE_REPORT : AdminActionType.REJECT_PROFILE_REPORT,
                AdminTargetType.PROFILE_REPORT,
                reportCase.getId(),
                note
        ));
        entityManager.flush();
        return new AdminProfileReportStatusResponse(
                reportCase.getId(), reportCase.getStatus(), principal.getUserId(),
                reportCase.getResolvedAt(), note, accountBlocked);
    }

    private ProfileReportCase lockPendingCase(Long caseId) {
        ProfileReportCase reportCase = caseRepository.findByIdForUpdate(validateId(caseId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_PROFILE_REPORT_NOT_FOUND));
        if (reportCase.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.ADMIN_PROFILE_REPORT_ALREADY_PROCESSED);
        }
        return reportCase;
    }

    private ProfileReportCase requireCase(Long caseId) {
        return caseRepository.findById(validateId(caseId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_PROFILE_REPORT_NOT_FOUND));
    }

    private Long validateId(Long id) {
        if (id == null || id <= 0) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        return id;
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        String normalized = keyword.trim();
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return normalized;
    }

    private String normalizeResolutionNote(AdminProfileReportResolutionRequest request) {
        if (request == null || request.resolutionNote() == null || request.resolutionNote().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_REQUIRED);
        }
        String note = request.resolutionNote().trim();
        if (note.length() > 500) {
            throw new BusinessException(ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_TOO_LONG);
        }
        return note;
    }

    private CustomUserPrincipal requireActiveAdmin() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.ADMIN) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (principal.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_BLOCKED);
        return principal;
    }

    private AdminProfileReportListItemResponse toListItem(ProfileReportCase reportCase) {
        return new AdminProfileReportListItemResponse(
                reportCase.getId(), reportCase.getStatus(), reportCase.getReportCount(),
                reportCase.getReportedUser().getId(), reportCase.getReportedDisplayNameSnapshot(),
                reportCase.getReportedAvatarUrlSnapshot(), reportCase.getCreatedAt(),
                reportCase.getLatestReportedAt());
    }

    private AdminProfileReportDetailResponse toDetail(
            ProfileReportCase reportCase, List<ProfileReport> reports) {
        List<AdminProfileReportReporterResponse> reporterResponses = reports.stream()
                .map(report -> new AdminProfileReportReporterResponse(
                        report.getId(), report.getReporter().getId(), report.getReporterDisplayNameSnapshot(),
                        report.getReason(), report.getStatus(), report.getCreatedAt()))
                .toList();
        return new AdminProfileReportDetailResponse(
                reportCase.getId(), reportCase.getStatus(), reportCase.getReportCount(),
                reportCase.getReportedUser().getId(),
                new AdminProfileReportSnapshotResponse(
                        reportCase.getReportedDisplayNameSnapshot(), reportCase.getReportedAvatarUrlSnapshot(),
                        reportCase.getReportedBioSnapshot(), reportCase.getReportedDateOfBirthSnapshot()),
                reporterResponses, reportCase.getCreatedAt(), reportCase.getLatestReportedAt(),
                reportCase.getResolvedBy() == null ? null : reportCase.getResolvedBy().getId(),
                reportCase.getResolvedAt(), reportCase.getResolutionNote());
    }
}
