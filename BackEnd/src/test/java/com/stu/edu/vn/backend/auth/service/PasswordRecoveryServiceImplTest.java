package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.auth.config.PasswordRecoveryProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.*;
import com.stu.edu.vn.backend.auth.entity.PasswordRecoveryChallenge;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.PasswordRecoveryChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordRecoveryServiceImplTest {
    private final PasswordRecoveryChallengeRepository challenges = mock(PasswordRecoveryChallengeRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
    private final AuthHmacService hmac = mock(AuthHmacService.class);
    private final FlowTokenGenerator flowTokens = mock(FlowTokenGenerator.class);
    private final OtpGenerator otps = mock(OtpGenerator.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T00:00:00Z"), ZoneOffset.UTC);
    private PasswordRecoveryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordRecoveryServiceImpl(challenges, users, refreshTokens, hmac, flowTokens, otps,
                new PasswordRecoveryProperties(), new PasswordPolicyValidator(), encoder, events, clock);
        when(flowTokens.generate()).thenReturn("flow-token", "reset-token");
        when(otps.generate()).thenReturn("123456");
        when(hmac.hashFlowToken(anyString())).thenAnswer(invocation -> "h:" + invocation.getArgument(0));
        when(hmac.hashOtp(anyString())).thenAnswer(invocation -> "o:" + invocation.getArgument(0));
        when(challenges.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void startCreatesRealChallengeAndPublishesDeliveryOnlyForEligibleAccount() {
        User user = eligibleEmailUser();
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));

        PasswordRecoveryChallengeResponse response = service.start(
                new StartPasswordRecoveryRequest(" Student@Example.com ", "device"));

        assertThat(response.accepted()).isTrue();
        assertThat(response.flowType()).isEqualTo("PASSWORD_RECOVERY");
        verify(events).publishEvent(any(com.stu.edu.vn.backend.auth.delivery.PasswordRecoveryOtpRequested.class));
    }

    @Test
    void startCreatesLifecycleDecoyWithoutDeliveryForUnknownIdentifier() {
        PasswordRecoveryChallengeResponse response = service.start(
                new StartPasswordRecoveryRequest("missing@example.com", null));

        assertThat(response.accepted()).isTrue();
        verify(challenges).saveAndFlush(argThat(value -> value.getUser() == null));
        verifyNoInteractions(events);
    }

    @Test
    void unverifiedSocialOnlyAndBlockedAccountsAllReceiveIndistinguishableDecoys() {
        User unverified = new User("unverified@example.com", "hash");
        User socialOnly = new User("social@example.com", null);
        User blocked = eligibleEmailUser(); blocked.setEmail("blocked@example.com"); blocked.setStatus(UserStatus.BLOCKED);
        when(users.findByEmail("unverified@example.com")).thenReturn(Optional.of(unverified));
        when(users.findByEmail("social@example.com")).thenReturn(Optional.of(socialOnly));
        when(users.findByEmail("blocked@example.com")).thenReturn(Optional.of(blocked));

        for (String identifier : new String[] {"unverified@example.com", "social@example.com", "blocked@example.com"}) {
            PasswordRecoveryChallengeResponse response = service.start(new StartPasswordRecoveryRequest(identifier, null));
            assertThat(response.accepted()).isTrue();
            assertThat(response.flowType()).isEqualTo("PASSWORD_RECOVERY");
        }
        verify(challenges, times(3)).saveAndFlush(argThat(PasswordRecoveryChallenge::isDecoy));
        verifyNoInteractions(events);
    }

    @Test
    void decoyNeverIssuesResetTokenAndConsumesAttempt() {
        LocalDateTime now = LocalDateTime.now(clock);
        PasswordRecoveryChallenge decoy = PasswordRecoveryChallenge.start(null, "subject", RegistrationType.EMAIL,
                "h:flow-token", "o:123456", now.plusMinutes(10), now.plusMinutes(1), now.plusMinutes(15));
        when(challenges.findByFlowHashForUpdate("h:flow-token")).thenReturn(Optional.of(decoy));

        assertThatThrownBy(() -> service.verify("flow-token", new VerifyPasswordRecoveryRequest("123456")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_INVALID));
        assertThat(decoy.getFailedAttempts()).isEqualTo(1);
    }

    @Test
    void validOtpIssuesShortLivedResetAuthorizedTokenAndConsumesRecoveryFlow() {
        User user = eligibleEmailUser();
        LocalDateTime now = LocalDateTime.now(clock);
        PasswordRecoveryChallenge challenge = PasswordRecoveryChallenge.start(user, "subject", RegistrationType.EMAIL,
                "h:flow-token", "o:123456", now.plusMinutes(10), now.plusMinutes(1), now.plusMinutes(15));
        when(challenges.findByFlowHashForUpdate("h:flow-token")).thenReturn(Optional.of(challenge));
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(hmac.verifyOtp("123456", "o:123456")).thenReturn(true);

        VerifyPasswordRecoveryResponse response = service.verify("flow-token", new VerifyPasswordRecoveryRequest("123456"));

        assertThat(response.resetAuthorizedToken()).isEqualTo("flow-token");
        assertThat(response.resetTokenExpiresAt()).isEqualTo(now.plusMinutes(5));
        assertThat(challenge.getRecoveryFlowTokenHash()).isNull();
        assertThat(challenge.getResetTokenHash()).isEqualTo("h:flow-token");
    }

    @Test
    void expiredOtpAndFifthInvalidAttemptReturnProductionErrors() {
        LocalDateTime now = LocalDateTime.now(clock);
        PasswordRecoveryChallenge expired = PasswordRecoveryChallenge.start(eligibleEmailUser(), "expired", RegistrationType.EMAIL,
                "h:expired", "otp", now.minusSeconds(1), now.minusMinutes(1), now.plusMinutes(5));
        when(challenges.findByFlowHashForUpdate("h:expired")).thenReturn(Optional.of(expired));
        assertCode(() -> service.verify("expired", new VerifyPasswordRecoveryRequest("123456")),
                ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_EXPIRED);

        PasswordRecoveryChallenge attempts = PasswordRecoveryChallenge.start(eligibleEmailUser(), "attempts", RegistrationType.EMAIL,
                "h:attempts", "otp", now.plusMinutes(5), now.minusMinutes(1), now.plusMinutes(10));
        when(challenges.findByFlowHashForUpdate("h:attempts")).thenReturn(Optional.of(attempts));
        for (int index = 0; index < 4; index++)
            assertCode(() -> service.verify("attempts", new VerifyPasswordRecoveryRequest("000000")),
                    ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_INVALID);
        assertCode(() -> service.verify("attempts", new VerifyPasswordRecoveryRequest("000000")),
                ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_ATTEMPTS_EXCEEDED);
    }

    @Test
    void resendEnforcesCooldownAndRotatesFlowAndOtp() {
        LocalDateTime now = LocalDateTime.now(clock);
        PasswordRecoveryChallenge cooldown = PasswordRecoveryChallenge.start(null, "cooldown", RegistrationType.EMAIL,
                "h:cooldown", "old-otp", now.plusMinutes(5), now.plusSeconds(1), now.plusMinutes(15));
        when(challenges.findByFlowHashForUpdate("h:cooldown")).thenReturn(Optional.of(cooldown));
        assertCode(() -> service.resend("cooldown"), ErrorCode.AUTH_PASSWORD_RECOVERY_RESEND_TOO_SOON);

        PasswordRecoveryChallenge ready = PasswordRecoveryChallenge.start(null, "ready", RegistrationType.EMAIL,
                "h:ready", "old-otp", now.plusMinutes(5), now.minusSeconds(1), now.plusMinutes(15));
        when(challenges.findByFlowHashForUpdate("h:ready")).thenReturn(Optional.of(ready));
        PasswordRecoveryChallengeResponse response = service.resend("ready");
        assertThat(response.recoveryFlowToken()).isEqualTo("flow-token");
        assertThat(ready.getRecoveryFlowTokenHash()).isEqualTo("h:flow-token");
        assertThat(ready.getOtpHash()).isEqualTo("o:123456");
        assertThat(ready.getOtpVersion()).isEqualTo(2);
    }

    @Test
    void completeChangesPasswordAndRevokesAllRefreshTokens() {
        User user = eligibleEmailUser();
        LocalDateTime now = LocalDateTime.now(clock);
        PasswordRecoveryChallenge challenge = PasswordRecoveryChallenge.start(user, "subject", RegistrationType.EMAIL,
                "flow", "otp", now.plusMinutes(10), now.plusMinutes(1), now.plusMinutes(15));
        challenge.verify("h:reset-token", now.plusMinutes(5), now);
        when(challenges.findByResetHashForUpdate("h:reset-token")).thenReturn(Optional.of(challenge));
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("new-hash");

        CompletePasswordRecoveryResponse response = service.complete("reset-token",
                new CompletePasswordRecoveryRequest("NewStrong1!", "NewStrong1!"));

        assertThat(response.completed()).isTrue();
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(refreshTokens).revokeAllActiveByUserId(user.getId(), now);
    }

    @Test
    void completeRejectsExpiredUsedWeakAndMismatchedCredentials() {
        LocalDateTime now = LocalDateTime.now(clock);
        User user = eligibleEmailUser();

        PasswordRecoveryChallenge expired = verified(user, "expired-reset", now.minusSeconds(1));
        when(challenges.findByResetHashForUpdate("h:expired-reset")).thenReturn(Optional.of(expired));
        assertCode(() -> service.complete("expired-reset", request("NewStrong1!", "NewStrong1!")),
                ErrorCode.AUTH_PASSWORD_RESET_TOKEN_EXPIRED);

        PasswordRecoveryChallenge used = verified(user, "used-reset", now.plusMinutes(5)); used.complete(now);
        when(challenges.findByResetHashForUpdate("h:used-reset")).thenReturn(Optional.of(used));
        assertCode(() -> service.complete("used-reset", request("NewStrong1!", "NewStrong1!")),
                ErrorCode.AUTH_PASSWORD_RESET_TOKEN_USED);

        PasswordRecoveryChallenge mismatch = verified(user, "mismatch", now.plusMinutes(5));
        when(challenges.findByResetHashForUpdate("h:mismatch")).thenReturn(Optional.of(mismatch));
        assertCode(() -> service.complete("mismatch", request("NewStrong1!", "Different1!")),
                ErrorCode.AUTH_PASSWORD_CONFIRMATION_MISMATCH);

        PasswordRecoveryChallenge weak = verified(user, "weak", now.plusMinutes(5));
        when(challenges.findByResetHashForUpdate("h:weak")).thenReturn(Optional.of(weak));
        assertCode(() -> service.complete("weak", request("weak", "weak")), ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        verifyNoInteractions(refreshTokens);
    }

    private PasswordRecoveryChallenge verified(User user, String token, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now(clock);
        PasswordRecoveryChallenge value = PasswordRecoveryChallenge.start(user, token, RegistrationType.EMAIL,
                "flow", "otp", now.plusMinutes(5), now.plusMinutes(1), now.plusMinutes(15));
        value.verify("h:" + token, expiresAt, now);
        return value;
    }

    private CompletePasswordRecoveryRequest request(String password, String confirmation) {
        return new CompletePasswordRecoveryRequest(password, confirmation);
    }

    private void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode code) {
        assertThatThrownBy(action).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(code));
    }

    private User eligibleEmailUser() {
        User user = new User("student@example.com", "old-hash");
        user.setEmailVerifiedAt(LocalDateTime.now(clock).minusDays(1));
        return user;
    }
}

