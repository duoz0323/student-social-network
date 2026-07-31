package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseActionRequest;
import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseNoViolationRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.enums.ModerationCaseAction;
import com.stu.edu.vn.backend.admin.mapper.AdminModerationCaseMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminModerationCaseRepository;
import com.stu.edu.vn.backend.admin.repository.AdminPostRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.report.entity.ModerationCase;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.repository.ModerationCaseRepository;
import com.stu.edu.vn.backend.report.repository.ReportRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class AdminModerationCaseServiceImplTest {
    private final AdminModerationCaseRepository adminCaseRepository = mock(AdminModerationCaseRepository.class);
    private final ModerationCaseRepository caseRepository = mock(ModerationCaseRepository.class);
    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final AdminPostRepository adminPostRepository = mock(AdminPostRepository.class);
    private final AdminActionRepository adminActionRepository = mock(AdminActionRepository.class);
    private final AdminModerationCaseMapper mapper = mock(AdminModerationCaseMapper.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-31T03:00:00Z"), ZoneOffset.UTC);

    private AdminModerationCaseServiceImpl service;
    private User admin;
    private User author;
    private User reporterOne;
    private User reporterTwo;
    private Post post;
    private ModerationCase moderationCase;
    private List<Report> reports;

    @BeforeEach
    void setUp() {
        service = new AdminModerationCaseServiceImpl(
                adminCaseRepository, caseRepository, reportRepository, adminPostRepository,
                adminActionRepository, mapper, notificationService, currentUserProvider, entityManager, clock);
        admin = user(1L, UserRole.ADMIN);
        author = user(2L, UserRole.USER);
        reporterOne = user(3L, UserRole.USER);
        reporterTwo = user(4L, UserRole.USER);
        post = new Post(author, "Bài viết cần kiểm duyệt");
        ReflectionTestUtils.setField(post, "id", 10L);
        post.setStatus(PostStatus.PUBLISHED);
        moderationCase = new ModerationCase(post, LocalDateTime.of(2026, 7, 31, 8, 0));
        ReflectionTestUtils.setField(moderationCase, "id", 20L);
        moderationCase.registerReport(LocalDateTime.of(2026, 7, 31, 8, 0));
        moderationCase.registerReport(LocalDateTime.of(2026, 7, 31, 9, 0));
        reports = List.of(
                new Report(reporterOne, post, moderationCase, ReportReason.SPAM, "Một", "Nội dung", "[]"),
                new Report(reporterTwo, post, moderationCase, ReportReason.HARASSMENT, "Hai", "Nội dung", "[]"));
        ReflectionTestUtils.setField(reports.get(0), "id", 31L);
        ReflectionTestUtils.setField(reports.get(1), "id", 32L);

        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        when(principal.getUserId()).thenReturn(1L);
        when(principal.getRole()).thenReturn(UserRole.ADMIN);
        when(principal.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(currentUserProvider.getCurrentUser()).thenReturn(principal);
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
        when(caseRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(moderationCase));
        when(reportRepository.findByModerationCase_IdOrderByCreatedAtDescIdDesc(20L)).thenReturn(reports);
        when(adminPostRepository.findAdminDisplayName(1L)).thenReturn(Optional.of("Admin"));
    }

    @Test
    void resolveNoViolationRejectsEveryReportAndCreatesOneCaseAction() {
        var response = service.resolveNoViolation(
                20L, new ResolveModerationCaseNoViolationRequest(null));

        assertThat(response.status()).isEqualTo(ModerationCaseStatus.RESOLVED_NO_VIOLATION);
        assertThat(response.resolutionNote()).isNull();
        assertThat(reports).extracting(Report::getStatus).containsOnly(ReportStatus.REJECTED);
        verify(notificationService).createReportRejectedNotification(3L, 31L);
        verify(notificationService).createReportRejectedNotification(4L, 32L);
        verify(adminActionRepository).save(any(AdminAction.class));
        verify(adminPostRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void resolveActionHidesPostResolvesEveryReportAndCreatesCaseAndPostActions() {
        when(caseRepository.findById(20L)).thenReturn(Optional.of(moderationCase));
        when(adminPostRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(post));

        var response = service.resolveAction(20L, new ResolveModerationCaseActionRequest(
                ModerationCaseAction.HIDE_POST, AdminPostHideReason.SPAM, null));

        assertThat(response.status()).isEqualTo(ModerationCaseStatus.RESOLVED_ACTION_TAKEN);
        assertThat(response.resolutionNote()).isNull();
        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(reports).extracting(Report::getStatus).containsOnly(ReportStatus.RESOLVED);
        verify(notificationService).createReportResolvedNotification(3L, 31L);
        verify(notificationService).createReportResolvedNotification(4L, 32L);
        verify(notificationService).createPostHiddenByAdminNotification(2L, 10L);
        verify(adminActionRepository, times(2)).save(any(AdminAction.class));
    }

    @Test
    void resolvedCaseCannotBeProcessedAgain() {
        moderationCase.resolveNoViolation(admin, "Đã xử lý", LocalDateTime.of(2026, 7, 31, 10, 0));

        assertThatThrownBy(() -> service.resolveNoViolation(
                20L, new ResolveModerationCaseNoViolationRequest("Xử lý lại")))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_MODERATION_CASE_ALREADY_RESOLVED);
        verify(adminActionRepository, never()).save(any());
    }

    private User user(Long id, UserRole role) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "role", role);
        ReflectionTestUtils.setField(user, "status", UserStatus.ACTIVE);
        return user;
    }
}
