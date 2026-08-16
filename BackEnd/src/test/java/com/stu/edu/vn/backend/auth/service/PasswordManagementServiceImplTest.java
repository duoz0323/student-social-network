package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.ChangePasswordRequest;
import com.stu.edu.vn.backend.auth.dto.SetPasswordRequest;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class PasswordManagementServiceImplTest {
    private final CurrentUserProvider currentUser = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
    private final ReauthenticationChallengeRepository challenges = org.mockito.Mockito.mock(ReauthenticationChallengeRepository.class);
    private final RefreshTokenRepository refreshTokens = org.mockito.Mockito.mock(RefreshTokenRepository.class);
    private final PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-16T03:00:00Z"), ZoneOffset.UTC);
    private AuthHmacService hmac;
    private PasswordManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        AuthRegistrationProperties properties = new AuthRegistrationProperties();
        properties.setOtpHmacSecret("otp-secret-for-tests-0123456789");
        properties.setFlowTokenHmacSecret("flow-secret-for-tests-0123456789");
        hmac = new AuthHmacService(properties);
        service = new PasswordManagementServiceImpl(currentUser, users, challenges, refreshTokens,
                new PasswordPolicyValidator(), encoder, hmac, clock);
        when(currentUser.getCurrentUserId()).thenReturn(7L);
    }

    @Test
    void setPasswordConsumesBoundReauthenticationAndRevokesAllRefreshTokens() {
        User user = verifiedUser(null);
        ReauthenticationChallenge challenge = ReauthenticationChallenge.start(user,
                hmac.hashFlowToken("reauth"), ReauthenticationProofMethod.GOOGLE,
                ReauthenticationScope.SET_PASSWORD, AuthMethod.EMAIL,
                LocalDateTime.now(clock).plusMinutes(5));
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
        when(challenges.findByTokenHashForUpdate(hmac.hashFlowToken("reauth"))).thenReturn(Optional.of(challenge));
        when(encoder.encode("NewPassword@1")).thenReturn("new-hash");

        var response = service.setPassword("reauth", new SetPasswordRequest("NewPassword@1", "NewPassword@1"));

        assertThat(response.sessionsRevoked()).isTrue();
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(challenge.getTokenHash()).isNull();
        verify(refreshTokens).revokeAllActiveByUserId(7L, LocalDateTime.now(clock));
    }

    @Test
    void setPasswordRejectsExistingPasswordBeforeConsumingChallenge() {
        User user = verifiedUser("old-hash");
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(user));

        assertError(() -> service.setPassword("reauth",
                new SetPasswordRequest("NewPassword@1", "NewPassword@1")),
                ErrorCode.AUTH_PASSWORD_ALREADY_CONFIGURED);
        verify(challenges, never()).findByTokenHashForUpdate(any());
        verify(refreshTokens, never()).revokeAllActiveByUserId(any(), any());
    }

    @Test
    void changePasswordVerifiesCurrentRejectsSameAndRevokesSessionsOnSuccess() {
        User user = verifiedUser("old-hash");
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
        when(encoder.matches("Current@1", "old-hash")).thenReturn(true);
        when(encoder.matches("NewPassword@1", "old-hash")).thenReturn(false);
        when(encoder.encode("NewPassword@1")).thenReturn("new-hash");

        service.changePassword(new ChangePasswordRequest("Current@1", "NewPassword@1", "NewPassword@1"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(refreshTokens).revokeAllActiveByUserId(7L, LocalDateTime.now(clock));
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPasswordWithoutMutation() {
        User user = verifiedUser("old-hash");
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
        when(encoder.matches("Wrong@1", "old-hash")).thenReturn(false);

        assertError(() -> service.changePassword(
                new ChangePasswordRequest("Wrong@1", "NewPassword@1", "NewPassword@1")),
                ErrorCode.AUTH_CURRENT_PASSWORD_INCORRECT);
        assertThat(user.getPasswordHash()).isEqualTo("old-hash");
        verify(refreshTokens, never()).revokeAllActiveByUserId(any(), any());
    }

    private User verifiedUser(String hash) {
        User user = new User("student@example.com", hash);
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        return user;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(code);
    }
}
