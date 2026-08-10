package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.dto.request.AdminProfileReportResolutionRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.service.ModerationAccountBlockService;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.entity.ProfileReport;
import com.stu.edu.vn.backend.report.entity.ProfileReportCase;
import com.stu.edu.vn.backend.report.enums.ProfileReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.repository.ProfileReportRepository;
import com.stu.edu.vn.backend.report.repository.ProfileReportCaseRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class AdminProfileReportServiceImplTest {

    private final ProfileReportRepository reportRepository = org.mockito.Mockito.mock(ProfileReportRepository.class);
    private final ProfileReportCaseRepository caseRepository = org.mockito.Mockito.mock(ProfileReportCaseRepository.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final AdminActionRepository actionRepository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final ModerationAccountBlockService accountBlockService =
            org.mockito.Mockito.mock(ModerationAccountBlockService.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private AdminProfileReportServiceImpl service;
    private ProfileReport report;
    private ProfileReportCase reportCase;

    @BeforeEach
    void setUp() {
        User admin = user(1L, UserRole.ADMIN);
        User reporter = user(10L, UserRole.USER);
        User target = user(20L, UserRole.USER);
        UserProfile targetProfile = profile(target, "Target");
        reportCase = new ProfileReportCase(target, targetProfile, LocalDateTime.of(2026, 8, 9, 9, 0));
        ReflectionTestUtils.setField(reportCase, "id", 50L);
        report = new ProfileReport(reportCase, reporter, target, ProfileReportReason.PROHIBITED_CONTENT,
                profile(reporter, "Reporter"), targetProfile);
        ReflectionTestUtils.setField(report, "id", 50L);
        ReflectionTestUtils.setField(report, "createdAt", LocalDateTime.of(2026, 8, 9, 9, 0));
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
        when(caseRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(reportCase));
        when(reportRepository.findAllByCaseIdAndStatusForUpdate(50L, ReportStatus.PENDING))
                .thenReturn(java.util.List.of(report));
        service = new AdminProfileReportServiceImpl(caseRepository, reportRepository, currentUserProvider,
                actionRepository, accountBlockService, entityManager,
                Clock.fixed(Instant.parse("2026-08-09T03:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void resolveConfirmsViolationForAllPendingReportsAndWritesOneAdminAudit() {
        User secondReporter = user(11L, UserRole.USER);
        ProfileReport secondReport = new ProfileReport(
                reportCase, secondReporter, report.getReportedUser(), ProfileReportReason.FALSE_INFORMATION,
                profile(secondReporter, "Second reporter"), profile(report.getReportedUser(), "Target"));
        when(reportRepository.findAllByCaseIdAndStatusForUpdate(50L, ReportStatus.PENDING))
                .thenReturn(java.util.List.of(report, secondReport));

        var response = service.resolve(50L, new AdminProfileReportResolutionRequest("  Vi phạm hồ sơ  "));

        assertThat(response.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(response.resolutionNote()).isEqualTo("Vi phạm hồ sơ");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(secondReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        ArgumentCaptor<AdminAction> captor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(captor.capture());
        assertThat(captor.getValue().getActionType()).isEqualTo(AdminActionType.RESOLVE_PROFILE_REPORT);
        assertThat(captor.getValue().getTargetType()).isEqualTo(AdminTargetType.PROFILE_REPORT);
    }

    @Test
    void cannotProcessProfileReportTwice() {
        reportCase.reject(user(1L, UserRole.ADMIN), LocalDateTime.now(), "Đã xử lý");
        assertThatThrownBy(() -> service.resolve(
                50L, new AdminProfileReportResolutionRequest("Xử lý lại")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADMIN_PROFILE_REPORT_ALREADY_PROCESSED));
    }

    @Test
    void adminCanResolveProfileViolationAndBlockTargetImmediately() {
        when(accountBlockService.blockIfActive(any(), any(), any(), any())).thenReturn(true);

        var response = service.resolve(
                50L, new AdminProfileReportResolutionRequest("Vi phạm nghiêm trọng", true));

        assertThat(response.accountBlocked()).isTrue();
        verify(accountBlockService).blockIfActive(
                org.mockito.Mockito.eq(20L), org.mockito.Mockito.any(User.class), org.mockito.Mockito.any(LocalDateTime.class),
                org.mockito.Mockito.eq(AdminBlockReason.PROFILE_VIOLATION));
    }

    private User user(Long id, UserRole role) {
        User user = new User("u" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private UserProfile profile(User user, String displayName) {
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(displayName);
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return profile;
    }
}
