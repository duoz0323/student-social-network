package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminActionMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionPostTargetProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionReportTargetProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionUserTargetProjection;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.ObjectMapper;

class AdminActionServiceImplTest {
    private final AdminActionRepository repository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private AdminActionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminActionServiceImpl(repository, new AdminActionMapper(new ObjectMapper()));
    }

    @Test
    void listPassesAllFiltersKeepsStableRepositoryOrderAndResolvesEachTargetTypeOnce() {
        LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 16, 23, 59);
        List<AdminActionListProjection> projections = List.of(
                action(6L, "BLOCK_USER", "USER", 10L),
                action(5L, "UNBLOCK_USER", "USER", 11L),
                action(4L, "HIDE_POST", "POST", 20L),
                action(3L, "RESTORE_POST", "POST", 21L),
                action(2L, "RESOLVE_REPORT", "REPORT", 30L),
                action(1L, "REJECT_REPORT", "REPORT", 31L)
        );
        AdminActionUserTargetProjection user10 = userTarget(10L, "Minh");
        AdminActionUserTargetProjection user11 = userTarget(11L, null);
        AdminActionPostTargetProjection post20 = postTarget(20L);
        AdminActionPostTargetProjection post21 = postTarget(21L);
        AdminActionReportTargetProjection report30 = reportTarget(30L);
        AdminActionReportTargetProjection report31 = reportTarget(31L);
        when(repository.findAdminActions("BLOCK_USER", "USER", 1L, from, to, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(projections, PageRequest.of(0, 20), projections.size()));
        when(repository.findUserTargets(any())).thenReturn(List.of(user10, user11));
        when(repository.findPostTargets(any())).thenReturn(List.of(post20, post21));
        when(repository.findReportTargets(any())).thenReturn(List.of(report30, report31));

        var response = service.getActions(AdminActionType.BLOCK_USER, AdminTargetType.USER,
                1L, from, to, 0, 20);

        assertThat(response.content()).extracting("actionId").containsExactly(6L, 5L, 4L, 3L, 2L, 1L);
        assertThat(response.content().get(1).target().displayText()).isEqualTo("Người dùng #11");
        assertThat(response.content().get(2).target().displayText()).isEqualTo("Bài viết #20");
        assertThat(response.content().get(4).target().displayText()).isEqualTo("Báo cáo #30");
        verify(repository, times(1)).findUserTargets(any());
        verify(repository, times(1)).findPostTargets(any());
        verify(repository, times(1)).findReportTargets(any());
    }

    @Test
    void missingTargetKeepsHistoryAndMarksTargetUnavailableWithoutPerRowLookup() {
        List<AdminActionListProjection> projections = List.of(
                action(2L, "BLOCK_USER", "USER", 404L),
                action(1L, "UNBLOCK_USER", "USER", 405L)
        );
        when(repository.findAdminActions(null, null, null, null, null, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(projections, PageRequest.of(0, 20), 2));
        when(repository.findUserTargets(any())).thenReturn(List.of());

        var response = service.getActions(null, null, null, null, null, 0, 20);

        assertThat(response.content()).hasSize(2).allSatisfy(item -> {
            assertThat(item.target().targetAvailable()).isFalse();
            assertThat(item.target().displayText()).startsWith("Người dùng #");
        });
        verify(repository, times(1)).findUserTargets(any());
        verify(repository, never()).findPostTargets(any());
        verify(repository, never()).findReportTargets(any());
    }

    @Test
    void detailParsesJsonAndRemovesSensitiveKeysRecursively() {
        AdminActionDetailProjection detail = detailAction(9L, "BLOCK_USER", "USER", 10L,
                "{\"status\":\"ACTIVE\",\"passwordHash\":\"secret\",\"nested\":{\"refresh_token\":\"jwt\",\"safe\":1}}",
                "{\"status\":\"BLOCKED\",\"accessToken\":\"jwt\"}");
        AdminActionUserTargetProjection userTarget = userTarget(10L, "Minh");
        when(repository.findAdminActionDetail(9L)).thenReturn(Optional.of(detail));
        when(repository.findUserTargets(any())).thenReturn(List.of(userTarget));

        var response = service.getActionDetail(9L);

        assertThat(response.oldData().toString()).contains("status=ACTIVE", "safe=1")
                .doesNotContain("password", "refresh", "secret", "jwt");
        assertThat(response.newData().toString()).contains("status=BLOCKED").doesNotContain("accessToken", "jwt");
    }

    @Test
    void detailMissingUsesAdminActionNotFound() {
        when(repository.findAdminActionDetail(404L)).thenReturn(Optional.empty());

        assertError(() -> service.getActionDetail(404L), ErrorCode.ADMIN_ACTION_NOT_FOUND);
        verify(repository, never()).findUserTargets(any());
    }

    @Test
    void listRejectsInvalidPaginationAdminIdAndReversedRangeBeforeQuery() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 16, 8, 0);
        assertError(() -> service.getActions(null, null, null, null, null, -1, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getActions(null, null, null, null, null, 0, 0), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getActions(null, null, null, null, null, 0, 101), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getActions(null, null, 0L, null, null, 0, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getActions(null, null, null, now.plusMinutes(1), now, 0, 20),
                ErrorCode.VALIDATION_ERROR);
        verify(repository, never()).findAdminActions(any(), any(), any(), any(), any(), any());
    }

    private AdminActionListProjection action(Long id, String actionType, String targetType, Long targetId) {
        AdminActionListProjection projection = org.mockito.Mockito.mock(AdminActionListProjection.class);
        when(projection.getActionId()).thenReturn(id);
        when(projection.getActionType()).thenReturn(actionType);
        when(projection.getAdminId()).thenReturn(1L);
        when(projection.getAdminDisplayName()).thenReturn("Quản trị viên");
        when(projection.getAdminAvatarUrl()).thenReturn("admin.jpg");
        when(projection.getTargetType()).thenReturn(targetType);
        when(projection.getTargetId()).thenReturn(targetId);
        when(projection.getNote()).thenReturn("note");
        when(projection.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 16, 8, 0));
        return projection;
    }

    private AdminActionDetailProjection detailAction(
            Long id, String actionType, String targetType, Long targetId, String oldData, String newData
    ) {
        AdminActionDetailProjection projection = org.mockito.Mockito.mock(AdminActionDetailProjection.class);
        when(projection.getActionId()).thenReturn(id);
        when(projection.getActionType()).thenReturn(actionType);
        when(projection.getAdminId()).thenReturn(1L);
        when(projection.getAdminDisplayName()).thenReturn("Quản trị viên");
        when(projection.getTargetType()).thenReturn(targetType);
        when(projection.getTargetId()).thenReturn(targetId);
        when(projection.getOldData()).thenReturn(oldData);
        when(projection.getNewData()).thenReturn(newData);
        return projection;
    }

    private AdminActionUserTargetProjection userTarget(Long id, String displayName) {
        AdminActionUserTargetProjection projection = org.mockito.Mockito.mock(AdminActionUserTargetProjection.class);
        when(projection.getTargetId()).thenReturn(id);
        when(projection.getDisplayName()).thenReturn(displayName);
        return projection;
    }

    private AdminActionPostTargetProjection postTarget(Long id) {
        AdminActionPostTargetProjection projection = org.mockito.Mockito.mock(AdminActionPostTargetProjection.class);
        when(projection.getTargetId()).thenReturn(id);
        return projection;
    }

    private AdminActionReportTargetProjection reportTarget(Long id) {
        AdminActionReportTargetProjection projection = org.mockito.Mockito.mock(AdminActionReportTargetProjection.class);
        when(projection.getTargetId()).thenReturn(id);
        return projection;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode code) {
        assertThatThrownBy(callable).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(code));
    }
}
