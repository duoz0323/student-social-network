package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.dto.request.AdminRejectReportRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminResolveReportRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminReportMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminPostRepository;
import com.stu.edu.vn.backend.admin.repository.AdminReportRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminReportDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminReportListProjection;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

class AdminReportServiceImplTest {
    private final AdminReportRepository repository = org.mockito.Mockito.mock(AdminReportRepository.class);
    private final AdminPostRepository postRepository = org.mockito.Mockito.mock(AdminPostRepository.class);
    private final AdminActionRepository actionRepository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private AdminReportServiceImpl service;
    private User admin;

    @BeforeEach
    void setUp() {
        admin = user(1L, UserRole.ADMIN);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
        when(postRepository.findAdminDisplayName(1L)).thenReturn(Optional.of("Admin"));
        service = new AdminReportServiceImpl(repository, new AdminReportMapper(new ObjectMapper()),
                postRepository, actionRepository, currentUserProvider, entityManager,
                Clock.fixed(Instant.parse("2026-07-15T01:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    @Test
    void listNormalizesKeywordMapsFiltersAndUsesPendingAscendingMode() {
        AdminReportListProjection projection = listProjection();
        when(repository.findAdminReports(any(), any(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 20), 1));

        var response = service.getReports(ReportStatus.PENDING, ReportReason.SPAM,
                11L, 12L, 13L, "  50%_off=now  ", 0, 20);

        verify(repository).findAdminReports("PENDING", "SPAM", 11L, 12L, 13L,
                "50=%=_off==now", 1, PageRequest.of(0, 20));
        assertThat(response.content().getFirst().status()).isEqualTo(ReportStatus.PENDING);
        assertThat(response.content().getFirst().reporter().accountStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(response.content().getFirst().post().currentStatus()).isEqualTo(PostStatus.DELETED);
        assertThat(response.content().getFirst().snapshotMediaCount()).isEqualTo(2);
    }

    @Test
    void listTreatsBlankAsNullAndUsesDescendingModeWithoutStatus() {
        when(repository.findAdminReports(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                anyInt(), any())).thenAnswer(invocation -> new PageImpl<AdminReportListProjection>(
                        List.of(), invocation.getArgument(7), 0));

        assertThat(service.getReports(null, null, null, null, null, "   ", 0, 1).content()).isEmpty();
        assertThat(service.getReports(null, null, null, null, null, null, 0, 100).size()).isEqualTo(100);
        verify(repository).findAdminReports(null, null, null, null, null, null, 0, PageRequest.of(0, 1));
    }

    @Test
    void listRejectsInvalidInputsBeforeQuery() {
        assertError(() -> service.getReports(null, null, null, null, null, null, -1, 20),
                ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getReports(null, null, null, null, null, null, 0, 0),
                ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getReports(null, null, null, null, null, null, 0, 101),
                ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getReports(null, null, 0L, null, null, null, 0, 20),
                ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getReports(null, null, null, -1L, null, null, 0, 20),
                ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getReports(null, null, null, null, 0L, null, 0, 20),
                ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getReports(null, null, null, null, null, "x".repeat(101), 0, 20),
                ErrorCode.ADMIN_REPORT_KEYWORD_TOO_LONG);
        verify(repository, never()).findAdminReports(any(), any(), any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void detailUsesSnapshotMediaAndMapsResolutionWithoutCurrentPostMediaQuery() {
        AdminReportDetailProjection projection = detailProjection();
        when(repository.findAdminReportDetail(31L)).thenReturn(Optional.of(projection));

        var response = service.getReportDetail(31L);

        assertThat(response.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(response.reportedPost().currentStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(response.reportedPost().author().accountStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(response.evidence().contentSnapshot()).isEqualTo("old content");
        assertThat(response.evidence().mediaSnapshot()).containsExactly("old-1.jpg", "old-2.jpg");
        assertThat(response.resolution().resolvedBy().adminId()).isEqualTo(1L);
        assertThat(response.resolution().resolutionNote()).isEqualTo("valid report");
        verify(repository).findAdminReportDetail(31L);
    }

    @Test
    void pendingDetailReturnsEmptySnapshotListAndNullResolver() {
        AdminReportDetailProjection projection = detailProjection();
        when(projection.getStatus()).thenReturn("PENDING");
        when(projection.getMediaSnapshot()).thenReturn(null);
        when(projection.getResolvedByAdminId()).thenReturn(null);
        when(repository.findAdminReportDetail(31L)).thenReturn(Optional.of(projection));

        var response = service.getReportDetail(31L);

        assertThat(response.evidence().mediaSnapshot()).isEmpty();
        assertThat(response.resolution().resolvedBy()).isNull();
    }

    @Test
    void detailMissingUsesAdminSpecificError() {
        when(repository.findAdminReportDetail(404L)).thenReturn(Optional.empty());
        assertError(() -> service.getReportDetail(404L), ErrorCode.ADMIN_REPORT_NOT_FOUND);
    }

    @Test
    void rejectPendingReportTrimsNoteKeepsPostAndCreatesRejectAction() {
        Post post = post(11L, PostStatus.PUBLISHED);
        Report report = report(31L, post);
        when(repository.findByIdForUpdate(31L)).thenReturn(Optional.of(report));

        var response = service.rejectReport(31L, new AdminRejectReportRequest("  Không vi phạm  "));

        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(report.getResolvedBy()).isSameAs(admin);
        assertThat(report.getResolvedAt()).isEqualTo(LocalDateTime.of(2026, 7, 15, 8, 0));
        assertThat(report.getResolutionNote()).isEqualTo("Không vi phạm");
        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.resolvedBy()).extracting("adminId", "displayName").containsExactly(1L, "Admin");
        assertActions(action(AdminActionType.REJECT_REPORT, AdminTargetType.REPORT, 31L, "Không vi phạm"));
        verify(postRepository, never()).findByIdForUpdate(anyLong());
        verify(entityManager).flush();
    }

    @Test
    void resolveWithoutHideLeavesPostAndCreatesOnlyResolveAction() {
        Post post = post(11L, PostStatus.PUBLISHED);
        Report report = report(31L, post);
        when(repository.findByIdForUpdate(31L)).thenReturn(Optional.of(report));

        var response = service.resolveReport(31L,
                new AdminResolveReportRequest("  Hợp lệ  ", null, null));

        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.post().status()).isEqualTo(PostStatus.PUBLISHED);
        assertActions(action(AdminActionType.RESOLVE_REPORT, AdminTargetType.REPORT, 31L, "Hợp lệ"));
        verify(postRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void resolveWithHideTransitionsPublishedPostAndCreatesExactlyTwoActions() {
        Post post = post(11L, PostStatus.PUBLISHED);
        Report report = report(31L, post);
        when(repository.findByIdForUpdate(31L)).thenReturn(Optional.of(report));
        when(postRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(post));

        var response = service.resolveReport(31L,
                new AdminResolveReportRequest("Hợp lệ", true, AdminPostHideReason.SPAM));

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(post.getHiddenBy()).isSameAs(admin);
        assertThat(post.getHiddenAt()).isEqualTo(LocalDateTime.of(2026, 7, 15, 8, 0));
        assertThat(post.getHiddenReason()).isEqualTo("SPAM");
        assertThat(response.post().hiddenReason()).isEqualTo("SPAM");
        assertActions(
                action(AdminActionType.RESOLVE_REPORT, AdminTargetType.REPORT, 31L, "Hợp lệ"),
                action(AdminActionType.HIDE_POST, AdminTargetType.POST, 11L, "SPAM"));
    }

    @Test
    void resolveHiddenOrDeletedPostIsNoOpAndDoesNotDuplicateHideAction() {
        for (PostStatus status : List.of(PostStatus.HIDDEN, PostStatus.DELETED)) {
            org.mockito.Mockito.clearInvocations(actionRepository, entityManager, postRepository, repository);
            Post post = post(status == PostStatus.HIDDEN ? 11L : 12L, status);
            Report report = report(status == PostStatus.HIDDEN ? 31L : 32L, post);
            when(repository.findByIdForUpdate(report.getId())).thenReturn(Optional.of(report));
            when(postRepository.findByIdForUpdate(post.getId())).thenReturn(Optional.of(post));

            service.resolveReport(report.getId(),
                    new AdminResolveReportRequest("Hợp lệ", true, AdminPostHideReason.SPAM));

            assertThat(post.getStatus()).isEqualTo(status);
            assertActions(action(AdminActionType.RESOLVE_REPORT, AdminTargetType.REPORT,
                    report.getId(), "Hợp lệ"));
        }
    }

    @Test
    void mutationRejectsProcessedReportAndInvalidRequestsBeforeWritingActions() {
        Post post = post(11L, PostStatus.PUBLISHED);
        Report processed = report(31L, post);
        processed.resolve(admin, LocalDateTime.now(), "done");
        when(repository.findByIdForUpdate(31L)).thenReturn(Optional.of(processed));

        assertError(() -> service.rejectReport(31L, new AdminRejectReportRequest("note")),
                ErrorCode.ADMIN_REPORT_ALREADY_PROCESSED);
        assertError(() -> service.rejectReport(31L, new AdminRejectReportRequest(null)),
                ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_REQUIRED);
        assertError(() -> service.rejectReport(31L, new AdminRejectReportRequest("   ")),
                ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_REQUIRED);
        assertError(() -> service.resolveReport(31L,
                        new AdminResolveReportRequest("x".repeat(501), false, null)),
                ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_TOO_LONG);
        assertError(() -> service.resolveReport(31L,
                        new AdminResolveReportRequest("note", true, null)),
                ErrorCode.ADMIN_REPORT_HIDE_REASON_REQUIRED);
        assertError(() -> service.resolveReport(31L,
                        new AdminResolveReportRequest("note", false, AdminPostHideReason.SPAM)),
                ErrorCode.ADMIN_REPORT_HIDE_REASON_NOT_ALLOWED);
        verifyNoInteractions(actionRepository);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(errorCode);
    }

    private AdminReportListProjection listProjection() {
        AdminReportListProjection p = org.mockito.Mockito.mock(AdminReportListProjection.class);
        when(p.getReportId()).thenReturn(31L);
        when(p.getStatus()).thenReturn("PENDING");
        when(p.getReason()).thenReturn("SPAM");
        when(p.getReporterId()).thenReturn(12L);
        when(p.getReporterDisplayName()).thenReturn("Reporter");
        when(p.getReporterAccountStatus()).thenReturn("BLOCKED");
        when(p.getPostId()).thenReturn(11L);
        when(p.getPostCurrentStatus()).thenReturn("DELETED");
        when(p.getContentPreview()).thenReturn("snapshot");
        when(p.getAuthorId()).thenReturn(13L);
        when(p.getAuthorDisplayName()).thenReturn("Author");
        when(p.getAuthorAccountStatus()).thenReturn("BLOCKED");
        when(p.getSnapshotMediaCount()).thenReturn(2L);
        return p;
    }

    private AdminReportDetailProjection detailProjection() {
        AdminReportDetailProjection p = org.mockito.Mockito.mock(AdminReportDetailProjection.class);
        when(p.getReportId()).thenReturn(31L);
        when(p.getStatus()).thenReturn("RESOLVED");
        when(p.getReason()).thenReturn("HARMFUL_CONTENT");
        when(p.getReporterId()).thenReturn(12L);
        when(p.getReporterDisplayName()).thenReturn("Reporter");
        when(p.getReporterAccountStatus()).thenReturn("ACTIVE");
        when(p.getPostId()).thenReturn(11L);
        when(p.getPostCurrentStatus()).thenReturn("HIDDEN");
        when(p.getPostCurrentContent()).thenReturn("edited content");
        when(p.getAuthorId()).thenReturn(13L);
        when(p.getAuthorDisplayName()).thenReturn("Author");
        when(p.getAuthorAccountStatus()).thenReturn("BLOCKED");
        when(p.getContentSnapshot()).thenReturn("old content");
        when(p.getMediaSnapshot()).thenReturn("[\"old-1.jpg\",\"old-2.jpg\"]");
        when(p.getResolvedByAdminId()).thenReturn(1L);
        when(p.getResolvedByDisplayName()).thenReturn("Admin");
        when(p.getResolvedAt()).thenReturn(LocalDateTime.of(2026, 7, 15, 8, 0));
        when(p.getResolutionNote()).thenReturn("valid report");
        return p;
    }

    private Report report(Long id, Post post) {
        Report report = new Report(user(9L, UserRole.USER), post, ReportReason.SPAM,
                null, "snapshot", "[]");
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }

    private Post post(Long id, PostStatus status) {
        Post post = new Post(user(8L, UserRole.USER), "content");
        ReflectionTestUtils.setField(post, "id", id);
        post.setStatus(status);
        if (status == PostStatus.HIDDEN) {
            post.setHiddenBy(admin);
            post.setHiddenAt(LocalDateTime.of(2026, 7, 15, 7, 0));
            post.setHiddenReason("SPAM");
        }
        if (status == PostStatus.DELETED) {
            post.setDeletedAt(LocalDateTime.of(2026, 7, 15, 7, 0));
        }
        return post;
    }

    private User user(Long id, UserRole role) {
        User user = new User("report-" + id + "@example.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        return user;
    }

    private ExpectedAction action(AdminActionType type, AdminTargetType targetType,
            Long targetId, String note) {
        return new ExpectedAction(type, targetType, targetId, note);
    }

    private void assertActions(ExpectedAction... expected) {
        ArgumentCaptor<AdminAction> captor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository, times(expected.length)).save(captor.capture());
        assertThat(captor.getAllValues()).hasSize(expected.length);
        for (int index = 0; index < expected.length; index++) {
            ExpectedAction item = expected[index];
            assertThat(captor.getAllValues().get(index))
                    .extracting(AdminAction::getAdmin, AdminAction::getActionType,
                            AdminAction::getTargetType, AdminAction::getTargetId,
                            AdminAction::getNote, AdminAction::getOldData, AdminAction::getNewData)
                    .containsExactly(admin, item.type(), item.targetType(), item.targetId(),
                            item.note(), null, null);
        }
    }

    private record ExpectedAction(AdminActionType type, AdminTargetType targetType,
            Long targetId, String note) {
    }
}
