package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class RegistrationVerificationTransactionServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 19, 3, 0);

    private final PendingRegistrationRepository pendingRepository =
            org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository profileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final RefreshTokenIssuer refreshTokenIssuer = org.mockito.Mockito.mock(RefreshTokenIssuer.class);
    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-19T03:00:00Z"), ZoneOffset.UTC);

    private AuthHmacService hmacService;
    private RegistrationVerificationTransactionService service;

    @BeforeEach
    void setUp() {
        AuthRegistrationProperties registrationProperties = new AuthRegistrationProperties();
        registrationProperties.setOtpHmacSecret("otp-secret-for-test-only");
        registrationProperties.setFlowTokenHmacSecret("flow-secret-for-test-only");
        hmacService = new AuthHmacService(registrationProperties);

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenExpirationMillis(900_000);
        jwtProperties.setRefreshTokenExpirationMillis(2_592_000_000L);
        service = new RegistrationVerificationTransactionService(
                pendingRepository,
                userRepository,
                profileRepository,
                hmacService,
                registrationProperties,
                refreshTokenIssuer,
                jwtService,
                jwtProperties,
                clock
        );
    }

    @Test
    void validEmailOtpCreatesVerifiedUserProfileTokensAndCompletesPending() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        arrangePending(pending);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 10L);
            return user;
        });
        when(refreshTokenIssuer.issue(any(), any(), any(), any()))
                .thenReturn(new IssuedRefreshToken("raw-refresh-token", 2_592_000));
        when(jwtService.generateAccessToken(10L, UserRole.USER.name())).thenReturn("access-token");

        RegistrationVerificationResult result = service.verify(request("123456"), "203.0.113.10");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(profileRepository).saveAndFlush(profileCaptor.capture());
        User user = userCaptor.getValue();
        assertThat(user.getEmail()).isEqualTo("student@example.com");
        assertThat(user.getEmailVerifiedAt()).isEqualTo(NOW);
        assertThat(user.getPasswordHash()).isEqualTo("bcrypt-password-hash");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(profileCaptor.getValue().getUser()).isSameAs(user);
        assertThat(profileCaptor.getValue().getProfileCompletedAt()).isNull();
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.COMPLETED);
        assertThat(pending.getCompletedUser()).isSameAs(user);
        assertThat(pending.getActiveIdentifierKey()).isNull();
        assertThat(pending.getPasswordHash()).isNull();
        assertThat(pending.getOtpHash()).isNull();
        assertThat(pending.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken("raw-flow-token"));
        assertThat(pending.getTerminalAt()).isEqualTo(NOW);
        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.response().refreshToken()).isEqualTo("raw-refresh-token");
        assertThat(result.response().profileCompleted()).isFalse();
        assertThat(result.response().nextStep()).isEqualTo("ONBOARDING");
    }


    @Test
    void wrongOtpIncrementsAttemptsAndReturnsInvalidBeforeLimit() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        arrangePending(pending);

        RegistrationVerificationResult result = service.verify(request("654321"), null);

        assertThat(result.errorCode()).isEqualTo(ErrorCode.AUTH_OTP_INVALID);
        assertThat(pending.getFailedAttempts()).isEqualTo(1);
        verify(pendingRepository).saveAndFlush(pending);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void fifthWrongOtpReturnsAttemptsExceededAndPendingRemainsPending() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        ReflectionTestUtils.setField(pending, "failedAttempts", 4);
        arrangePending(pending);

        RegistrationVerificationResult result = service.verify(request("654321"), null);

        assertThat(result.errorCode()).isEqualTo(ErrorCode.AUTH_OTP_ATTEMPTS_EXCEEDED);
        assertThat(pending.getFailedAttempts()).isEqualTo(5);
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
    }

    @Test
    void reachedAttemptLimitDoesNotCompareOrIncrementAgain() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        ReflectionTestUtils.setField(pending, "failedAttempts", 5);
        arrangePending(pending);

        RegistrationVerificationResult result = service.verify(request("123456"), null);

        assertThat(result.errorCode()).isEqualTo(ErrorCode.AUTH_OTP_ATTEMPTS_EXCEEDED);
        assertThat(pending.getFailedAttempts()).isEqualTo(5);
        verify(pendingRepository, never()).saveAndFlush(any());
    }

    @Test
    void expiredOtpDoesNotIncrementAttempts() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        ReflectionTestUtils.setField(pending, "otpExpiresAt", NOW);
        arrangePending(pending);

        RegistrationVerificationResult result = service.verify(request("123456"), null);

        assertThat(result.errorCode()).isEqualTo(ErrorCode.AUTH_OTP_EXPIRED);
        assertThat(pending.getFailedAttempts()).isZero();
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void expiredPendingIsTerminalizedWithoutCreatingUser() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        ReflectionTestUtils.setField(pending, "expiresAt", NOW);
        arrangePending(pending);

        RegistrationVerificationResult result = service.verify(request("123456"), null);

        assertThat(result.errorCode()).isEqualTo(ErrorCode.AUTH_REGISTRATION_EXPIRED);
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.EXPIRED);
        assertThat(pending.getIdentifierNormalized()).isNull();
        assertThat(pending.getTerminalAt()).isEqualTo(NOW);
        verify(pendingRepository).saveAndFlush(pending);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void terminalFlowTokenCannotVerifyOtpCreateUserOrIssueJwt() {
        PendingRegistration cancelled = pending(RegistrationType.EMAIL, "cancelled@example.com");
        cancelled.cancel(NOW.minusMinutes(1));
        when(pendingRepository.findByFlowTokenHashForUpdate(any())).thenReturn(Optional.of(cancelled));
        assertThat(service.verify(request("123456"), null).errorCode())
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_CANCELLED);

        PendingRegistration expired = pending(RegistrationType.EMAIL, "expired@example.com");
        expired.expire(NOW.minusMinutes(1));
        when(pendingRepository.findByFlowTokenHashForUpdate(any())).thenReturn(Optional.of(expired));
        assertThat(service.verify(request("123456"), null).errorCode())
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_EXPIRED);

        PendingRegistration completed = pending(RegistrationType.EMAIL, "completed@example.com");
        completed.complete(new User("completed@example.com", "hash"), NOW.minusMinutes(1));
        when(pendingRepository.findByFlowTokenHashForUpdate(any())).thenReturn(Optional.of(completed));
        assertThat(service.verify(request("123456"), null).errorCode())
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_ALREADY_COMPLETED);

        verify(userRepository, never()).saveAndFlush(any());
        verify(profileRepository, never()).saveAndFlush(any());
        verify(refreshTokenIssuer, never()).issue(any(), any(), any(), any());
        verify(jwtService, never()).generateAccessToken(any(), any());
    }

    @Test
    void invalidFlowTokenDoesNotCreateAnything() {
        RegistrationVerificationResult result = service.verify(request("123456"), null);

        assertThat(result.errorCode()).isEqualTo(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
        verify(userRepository, never()).saveAndFlush(any());
        verify(profileRepository, never()).saveAndFlush(any());
    }

    @Test
    void identifierClaimedBeforeVerificationIsRejected() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        arrangePending(pending);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.verify(request("123456"), null))
                .isInstanceOf(RegistrationIdentifierConflictException.class);
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void profileTokenOrPendingFailurePropagatesForTransactionRollback() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "student@example.com");
        arrangePending(pending);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 12L);
            return user;
        });
        when(profileRepository.saveAndFlush(any(UserProfile.class)))
                .thenThrow(new DataIntegrityViolationException("profile constraint"));

        assertThatThrownBy(() -> service.verify(request("123456"), null))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(refreshTokenIssuer, never()).issue(any(), any(), any(), any());
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
    }

    private void arrangePending(PendingRegistration pending) {
        when(pendingRepository.findByFlowTokenHashForUpdate(hmacService.hashFlowToken("raw-flow-token")))
                .thenReturn(Optional.of(pending));
    }

    private VerifyRegistrationRequest request(String code) {
        return new VerifyRegistrationRequest("raw-flow-token", code, "device-1", "Chrome");
    }

    private PendingRegistration pending(RegistrationType type, String identifier) {
        return PendingRegistration.start(
                type,
                identifier,
                "bcrypt-password-hash",
                hmacService.hashFlowToken("raw-flow-token"),
                hmacService.hashOtp("123456"),
                NOW.plusMinutes(10),
                NOW.plusSeconds(60),
                NOW.plusHours(24)
        );
    }
}

