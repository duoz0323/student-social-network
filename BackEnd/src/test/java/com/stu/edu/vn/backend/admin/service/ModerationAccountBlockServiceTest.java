package com.stu.edu.vn.backend.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ModerationAccountBlockServiceTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final RefreshTokenRepository tokenRepository = org.mockito.Mockito.mock(RefreshTokenRepository.class);
    private final AccountStatusHistoryRepository historyRepository =
            org.mockito.Mockito.mock(AccountStatusHistoryRepository.class);
    private final AdminActionRepository actionRepository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);
    private final ModerationAccountBlockService service = new ModerationAccountBlockService(
            userRepository, tokenRepository, historyRepository, actionRepository, notificationService);

    @Test
    void blocksActiveUserAndRevokesAllSessions() {
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 15, 0);
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));

        boolean blocked = service.blockIfActive(
                10L, admin, now, AdminBlockReason.REPEATED_VIOLATION);

        assertThat(blocked).isTrue();
        assertThat(target.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(target.getBlockedReason()).isEqualTo("REPEATED_VIOLATION");
        verify(tokenRepository).revokeAllActiveByUserId(10L, now);
        verify(historyRepository).save(any());
        verify(actionRepository).save(any());
        verify(notificationService).createAccountBlockedNotification(10L);
    }

    @Test
    void alreadyBlockedUserIsIdempotent() {
        User target = user(10L, UserRole.USER, UserStatus.BLOCKED);
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));

        boolean blocked = service.blockIfActive(
                10L, user(1L, UserRole.ADMIN, UserStatus.ACTIVE),
                LocalDateTime.of(2026, 8, 9, 15, 0), AdminBlockReason.PROFILE_VIOLATION);

        assertThat(blocked).isFalse();
        verify(tokenRepository, never()).revokeAllActiveByUserId(any(), any());
        verify(historyRepository, never()).save(any());
        verify(actionRepository, never()).save(any());
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
