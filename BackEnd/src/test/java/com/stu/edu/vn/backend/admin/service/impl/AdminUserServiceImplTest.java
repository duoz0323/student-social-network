package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.mapper.AdminUserMapper;
import com.stu.edu.vn.backend.admin.repository.AdminUserDetailProjection;
import com.stu.edu.vn.backend.admin.repository.AdminUserListProjection;
import com.stu.edu.vn.backend.admin.repository.AdminUserRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AdminUserServiceImplTest {

    private final AdminUserRepository adminUserRepository = org.mockito.Mockito.mock(AdminUserRepository.class);
    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(
                adminUserRepository,
                new AdminUserMapper(),
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.security.CurrentUserProvider.class),
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository.class),
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository.class),
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.admin.repository.AdminActionRepository.class),
                java.time.Clock.systemUTC(),
                org.mockito.Mockito.mock(jakarta.persistence.EntityManager.class),
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.notification.service.NotificationService.class)
        );
    }

    @Test
    void listTrimsEscapesKeywordFiltersStatusAndMapsProjection() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 14, 8, 0);
        AdminUserListProjection projection = listProjection(10L, "Minh", "ACTIVE", createdAt, true);
        when(adminUserRepository.findManagedUsers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 20), 1));

        var response = adminUserService.getUsers("  50%_off=now  ", UserStatus.ACTIVE, 0, 20);

        verify(adminUserRepository).findManagedUsers("50=%=_off==now", "ACTIVE", PageRequest.of(0, 20));
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst())
                .extracting("userId", "displayName", "status", "profileCompleted", "createdAt")
                .containsExactly(10L, "Minh", UserStatus.ACTIVE, true, createdAt);
    }

    @Test
    void listTreatsMissingAndBlankKeywordAsNull() {
        when(adminUserRepository.findManagedUsers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        adminUserService.getUsers(null, null, 0, 20);
        adminUserService.getUsers("   ", null, 0, 20);

        verify(adminUserRepository, org.mockito.Mockito.times(2))
                .findManagedUsers(null, null, PageRequest.of(0, 20));
    }

    @Test
    void listAllowsBoundarySizesAndEmptyPage() {
        when(adminUserRepository.findManagedUsers(any(), any(), any()))
                .thenAnswer(invocation -> {
                    PageRequest request = invocation.getArgument(2);
                    return new PageImpl<AdminUserListProjection>(List.of(), request, 0);
                });

        var sizeOne = adminUserService.getUsers(null, null, 0, 1);
        var sizeOneHundred = adminUserService.getUsers(null, null, 2, 100);

        assertThat(sizeOne.content()).isEmpty();
        assertThat(sizeOne.size()).isEqualTo(1);
        assertThat(sizeOneHundred.content()).isEmpty();
        assertThat(sizeOneHundred.size()).isEqualTo(100);
    }

    @Test
    void listRejectsInvalidPaginationAndOverlongKeywordBeforeQuery() {
        assertError(() -> adminUserService.getUsers(null, null, -1, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> adminUserService.getUsers(null, null, 0, 0), ErrorCode.VALIDATION_ERROR);
        assertError(() -> adminUserService.getUsers(null, null, 0, 101), ErrorCode.VALIDATION_ERROR);
        assertError(() -> adminUserService.getUsers("a".repeat(101), null, 0, 20), ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        verify(adminUserRepository, never()).findManagedUsers(any(), any(), any());
    }

    @Test
    void detailReturnsUserWithCompletedProfileAndSafeAccountFields() {
        AdminUserDetailProjection projection = detailProjection("USER", "BLOCKED", true);
        when(adminUserRepository.findManagedUserDetail(10L)).thenReturn(Optional.of(projection));

        var response = adminUserService.getUserDetail(10L);

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(UserStatus.BLOCKED);
        assertThat(response.profileCompleted()).isTrue();
        assertThat(response.blockedReason()).isEqualTo("SPAM");
    }

    @Test
    void detailAllowsIncompleteProfile() {
        AdminUserDetailProjection projection = detailProjection("USER", "ACTIVE", false);
        when(adminUserRepository.findManagedUserDetail(10L))
                .thenReturn(Optional.of(projection));

        var response = adminUserService.getUserDetail(10L);

        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.profileCompletedAt()).isNull();
    }

    @Test
    void detailRejectsMissingAndAdminTargetWithApprovedErrors() {
        AdminUserDetailProjection adminProjection = detailProjection("ADMIN", "ACTIVE", true);
        when(adminUserRepository.findManagedUserDetail(404L)).thenReturn(Optional.empty());
        when(adminUserRepository.findManagedUserDetail(1L))
                .thenReturn(Optional.of(adminProjection));

        assertError(() -> adminUserService.getUserDetail(404L), ErrorCode.ADMIN_USER_NOT_FOUND);
        assertError(() -> adminUserService.getUserDetail(1L), ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private AdminUserListProjection listProjection(
            Long userId,
            String displayName,
            String status,
            LocalDateTime createdAt,
            boolean completed
    ) {
        AdminUserListProjection projection = org.mockito.Mockito.mock(AdminUserListProjection.class);
        when(projection.getUserId()).thenReturn(userId);
        when(projection.getDisplayName()).thenReturn(displayName);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getCreatedAt()).thenReturn(createdAt);
        when(projection.getProfileCompletedAt()).thenReturn(completed ? createdAt : null);
        return projection;
    }

    private AdminUserDetailProjection detailProjection(String role, String status, boolean completed) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 14, 8, 0);
        AdminUserDetailProjection projection = org.mockito.Mockito.mock(AdminUserDetailProjection.class);
        when(projection.getUserId()).thenReturn(10L);
        when(projection.getRole()).thenReturn(role);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getDisplayName()).thenReturn("Minh");
        when(projection.getEmail()).thenReturn("minh@example.com");
        when(projection.getProfileCompletedAt()).thenReturn(completed ? timestamp : null);
        when(projection.getBlockedAt()).thenReturn("BLOCKED".equals(status) ? timestamp : null);
        when(projection.getBlockedReason()).thenReturn("BLOCKED".equals(status) ? "SPAM" : null);
        when(projection.getCreatedAt()).thenReturn(timestamp);
        when(projection.getUpdatedAt()).thenReturn(timestamp);
        return projection;
    }
}
