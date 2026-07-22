package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.entity.AccountStatusHistory;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminUserMapper;
import com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminUserRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class AdminUserStatusServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 14, 8, 0);

    private final AdminUserRepository adminUserRepository = org.mockito.Mockito.mock(AdminUserRepository.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final RefreshTokenRepository refreshTokenRepository = org.mockito.Mockito.mock(RefreshTokenRepository.class);
    private final AccountStatusHistoryRepository historyRepository =
            org.mockito.Mockito.mock(AccountStatusHistoryRepository.class);
    private final AdminActionRepository actionRepository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);
    private final User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
    private AdminUserServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-14T01:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        service = new AdminUserServiceImpl(
                adminUserRepository, new AdminUserMapper(), currentUserProvider, refreshTokenRepository,
                historyRepository, actionRepository, clock, entityManager, notificationService);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
    }

    @Test
    void blockChangesStateRevokesTokensAndCreatesMatchingAuditRecords() {
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));

        var response = service.blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.SPAM));

        assertThat(response)
                .extracting("userId", "status", "blockedAt", "blockedReason")
                .containsExactly(10L, UserStatus.BLOCKED, NOW, "SPAM");
        verify(refreshTokenRepository).revokeActiveTokensByUserId(10L, NOW);
        verify(notificationService).createAccountBlockedNotification(10L);

        ArgumentCaptor<AccountStatusHistory> historyCaptor = ArgumentCaptor.forClass(AccountStatusHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue())
                .extracting("user", "oldStatus", "newStatus", "changedBy", "reason")
                .containsExactly(target, UserStatus.ACTIVE, UserStatus.BLOCKED, admin, "SPAM");

        ArgumentCaptor<AdminAction> actionCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(actionCaptor.capture());
        assertThat(actionCaptor.getValue())
                .extracting("admin", "actionType", "targetType", "targetId", "note", "oldData", "newData")
                .containsExactly(
                        admin, AdminActionType.BLOCK_USER, AdminTargetType.USER, 10L, "SPAM", null, null);
        verify(entityManager).flush();
        verify(entityManager).refresh(target);
    }

    @Test
    void blockOtherStoresLiteralOtherInAllDestinations() {
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));

        service.blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.OTHER));

        ArgumentCaptor<AccountStatusHistory> historyCaptor = ArgumentCaptor.forClass(AccountStatusHistory.class);
        ArgumentCaptor<AdminAction> actionCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(historyRepository).save(historyCaptor.capture());
        verify(actionRepository).save(actionCaptor.capture());
        assertThat(target.getBlockedReason()).isEqualTo("OTHER");
        assertThat(historyCaptor.getValue().getReason()).isEqualTo("OTHER");
        assertThat(actionCaptor.getValue().getNote()).isEqualTo("OTHER");
    }

    @Test
    void blockRejectsMissingReasonBeforeLockingTarget() {
        assertError(() -> service.blockUser(10L, null), ErrorCode.ADMIN_BLOCK_REASON_REQUIRED);
        assertError(() -> service.blockUser(10L, new AdminBlockUserRequest(null)),
                ErrorCode.ADMIN_BLOCK_REASON_REQUIRED);
        verify(adminUserRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void blockRejectsSelfAdminTargetMissingAndRepeatedState() {
        assertError(() -> service.blockUser(1L, new AdminBlockUserRequest(AdminBlockReason.SPAM)),
                ErrorCode.ADMIN_SELF_ACTION_FORBIDDEN);

        User otherAdmin = user(2L, UserRole.ADMIN, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(otherAdmin));
        assertError(() -> service.blockUser(2L, new AdminBlockUserRequest(AdminBlockReason.SPAM)),
                ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);

        when(adminUserRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertError(() -> service.blockUser(404L, new AdminBlockUserRequest(AdminBlockReason.SPAM)),
                ErrorCode.ADMIN_USER_NOT_FOUND);

        User blocked = user(11L, UserRole.USER, UserStatus.BLOCKED);
        when(adminUserRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(blocked));
        assertError(() -> service.blockUser(11L, new AdminBlockUserRequest(AdminBlockReason.SPAM)),
                ErrorCode.ADMIN_USER_ALREADY_BLOCKED);
    }

    @Test
    void unblockClearsBlockDataCreatesAuditAndNeverTouchesRefreshTokens() {
        User target = blockedUser(10L);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));

        var response = service.unblockUser(10L);

        assertThat(response)
                .extracting("status", "blockedAt", "blockedReason")
                .containsExactly(UserStatus.ACTIVE, null, null);
        verify(refreshTokenRepository, never()).revokeActiveTokensByUserId(any(), any());

        ArgumentCaptor<AccountStatusHistory> historyCaptor = ArgumentCaptor.forClass(AccountStatusHistory.class);
        ArgumentCaptor<AdminAction> actionCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(historyRepository).save(historyCaptor.capture());
        verify(actionRepository).save(actionCaptor.capture());
        assertThat(historyCaptor.getValue())
                .extracting("oldStatus", "newStatus", "changedBy", "reason")
                .containsExactly(UserStatus.BLOCKED, UserStatus.ACTIVE, admin, "ADMIN_UNBLOCK");
        assertThat(actionCaptor.getValue())
                .extracting("actionType", "targetType", "targetId", "note")
                .containsExactly(AdminActionType.UNBLOCK_USER, AdminTargetType.USER, 10L, "ADMIN_UNBLOCK");
        verify(notificationService).createAccountUnblockedNotification(10L);
    }

    @Test
    void unblockRejectsSelfAdminTargetAndAlreadyActiveUser() {
        assertError(() -> service.unblockUser(1L), ErrorCode.ADMIN_SELF_ACTION_FORBIDDEN);

        User otherAdmin = user(2L, UserRole.ADMIN, UserStatus.BLOCKED);
        when(adminUserRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(otherAdmin));
        assertError(() -> service.unblockUser(2L), ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);

        User active = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(active));
        assertError(() -> service.unblockUser(10L), ErrorCode.ADMIN_USER_ALREADY_ACTIVE);
    }

    @Test
    void revokeFailureStopsBeforeCreatingAudit() {
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));
        doThrow(new IllegalStateException("revoke failed"))
                .when(refreshTokenRepository).revokeActiveTokensByUserId(10L, NOW);

        assertThatThrownBy(() -> service.blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);
        verify(historyRepository, never()).save(any());
        verify(actionRepository, never()).save(any());
    }

    @Test
    void historyFailureStopsBeforeCreatingAdminAction() {
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));
        doThrow(new IllegalStateException("history failed")).when(historyRepository).save(any());

        assertThatThrownBy(() -> service.blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);
        verify(refreshTokenRepository).revokeActiveTokensByUserId(10L, NOW);
        verify(actionRepository, never()).save(any());
    }

    @Test
    void adminActionFailurePropagatesBeforeFinalFlush() {
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));
        doThrow(new IllegalStateException("action failed")).when(actionRepository).save(any());

        assertThatThrownBy(() -> service.blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);
        verify(historyRepository).save(any());
        verify(entityManager, never()).flush();
    }

    @Test
    void serviceRejectsNonAdminOrBlockedPrincipalBeforeLockingTarget() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(20L, UserRole.USER, UserStatus.ACTIVE));
        assertError(() -> service.unblockUser(10L), ErrorCode.FORBIDDEN);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.BLOCKED));
        assertError(() -> service.unblockUser(10L), ErrorCode.USER_BLOCKED);
        verify(adminUserRepository, never()).findByIdForUpdate(any());
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private User blockedUser(Long id) {
        User user = user(id, UserRole.USER, UserStatus.BLOCKED);
        user.setBlockedAt(LocalDateTime.of(2026, 7, 13, 8, 0));
        user.setBlockedReason("SPAM");
        return user;
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("user" + id + "@example.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "updatedAt", LocalDateTime.of(2026, 7, 14, 7, 0));
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
