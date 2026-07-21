package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.RegistrationStatusResponse;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.support.EmailMasker;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RegistrationLifecycleTransactionServiceTest {

    private static final String RAW_FLOW_TOKEN = "raw-flow-token";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 19, 3, 0);

    private final PendingRegistrationRepository repository =
            org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final OtpGenerator otpGenerator = org.mockito.Mockito.mock(OtpGenerator.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-19T03:00:00Z"), ZoneOffset.UTC);

    private AuthRegistrationProperties properties;
    private AuthHmacService hmacService;
    private RegistrationLifecycleTransactionService service;

    @BeforeEach
    void setUp() {
        properties = new AuthRegistrationProperties();
        properties.setOtpHmacSecret("otp-secret-for-lifecycle-test");
        properties.setFlowTokenHmacSecret("flow-secret-for-lifecycle-test");
        hmacService = new AuthHmacService(properties);
        service = new RegistrationLifecycleTransactionService(
                repository,
                hmacService,
                otpGenerator,
                properties,
                new EmailMasker(),
                clock
        );
    }

    @Test
    void statusReturnsSafePendingMetadataAndCooldownUsingFixedClock() {
        PendingRegistration pending = pending(NOW.plusSeconds(60), NOW.plusHours(24));
        arrange(pending);

        RegistrationStatusResponse response = service.status(RAW_FLOW_TOKEN);

        assertThat(response.status()).isEqualTo(OtpChallengeStatus.PENDING);
        assertThat(response.maskedIdentifier()).isEqualTo("s***@example.com");
        assertThat(response.canResend()).isFalse();
        assertThat(response.remainingOtpAttempts()).isEqualTo(5);
        assertThat(response.nextStep()).isEqualTo(RegistrationStatusResponse.NEXT_STEP_VERIFY_OTP);
    }

    @Test
    void statusReturnsEveryTerminalStateWithoutIssuingOtpOrJwt() {
        PendingRegistration completed = pending(NOW.minusMinutes(1), NOW.plusHours(24));
        completed.complete(new User("student@example.com", "hash"), NOW.minusSeconds(1));
        arrange(completed);
        RegistrationStatusResponse completedResponse = service.status(RAW_FLOW_TOKEN);
        assertThat(completedResponse.status()).isEqualTo(OtpChallengeStatus.COMPLETED);
        assertThat(completedResponse.nextStep())
                .isEqualTo(RegistrationStatusResponse.NEXT_STEP_REGISTRATION_COMPLETED);

        PendingRegistration cancelled = pending(NOW.minusMinutes(1), NOW.plusHours(24));
        cancelled.cancel(NOW.minusSeconds(1));
        arrange(cancelled);
        assertThat(service.status(RAW_FLOW_TOKEN).status()).isEqualTo(OtpChallengeStatus.CANCELLED);

        PendingRegistration expired = pending(NOW.minusMinutes(1), NOW.plusHours(24));
        expired.expire(NOW.minusSeconds(1));
        arrange(expired);
        assertThat(service.status(RAW_FLOW_TOKEN).status()).isEqualTo(OtpChallengeStatus.EXPIRED);

        verify(otpGenerator, never()).generate();
        assertThat(completed.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken(RAW_FLOW_TOKEN));
        assertThat(cancelled.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken(RAW_FLOW_TOKEN));
        assertThat(expired.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken(RAW_FLOW_TOKEN));
    }

    @Test
    void statusTerminalizesExpiredPendingAndKeepsLookupHash() {
        PendingRegistration pending = pending(NOW.minusMinutes(1), NOW);
        arrange(pending);

        RegistrationStatusResponse response = service.status(RAW_FLOW_TOKEN);

        assertThat(response.status()).isEqualTo(OtpChallengeStatus.EXPIRED);
        assertThat(pending.getOtpHash()).isNull();
        assertThat(pending.getPasswordHash()).isNull();
        assertThat(pending.getActiveIdentifierKey()).isNull();
        assertThat(pending.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken(RAW_FLOW_TOKEN));
        verify(repository).saveAndFlush(pending);
    }

    @Test
    void fakeFlowTokenIsRejected() {
        assertThatThrownBy(() -> service.status("fake-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
    }

    @Test
    void cancelIsIdempotentAndReleasesIdentifierWithoutDeletingLookupHash() {
        PendingRegistration pending = pending(NOW.minusMinutes(1), NOW.plusHours(24));
        arrange(pending);

        CancelRegistrationResponse first = service.cancel(RAW_FLOW_TOKEN);
        CancelRegistrationResponse second = service.cancel(RAW_FLOW_TOKEN);

        assertThat(first.status()).isEqualTo(OtpChallengeStatus.CANCELLED);
        assertThat(second).isEqualTo(first);
        assertThat(pending.getActiveIdentifierKey()).isNull();
        assertThat(pending.getIdentifierNormalized()).isNull();
        assertThat(pending.getPasswordHash()).isNull();
        assertThat(pending.getOtpHash()).isNull();
        assertThat(pending.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken(RAW_FLOW_TOKEN));
    }

    @Test
    void completedRegistrationCannotBeCancelled() {
        PendingRegistration completed = pending(NOW.minusMinutes(1), NOW.plusHours(24));
        completed.complete(new User("student@example.com", "hash"), NOW.minusSeconds(1));
        arrange(completed);

        assertThatThrownBy(() -> service.cancel(RAW_FLOW_TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_ALREADY_COMPLETED);
    }

    @Test
    void resendCreatesNewOtpVersionResetsAttemptsAndDoesNotExtendPending() {
        PendingRegistration pending = pending(NOW, NOW.plusHours(24));
        String originalFlowHash = pending.getFlowTokenHash();
        LocalDateTime originalPendingExpiry = pending.getExpiresAt();
        pending.recordFailedAttempt();
        arrange(pending);
        when(otpGenerator.generate()).thenReturn("654321");

        RegistrationResendResult result = service.issueNewOtp(RAW_FLOW_TOKEN);

        assertThat(result.successful()).isTrue();
        assertThat(result.issuance().otpVersion()).isEqualTo(2);
        assertThat(result.issuance().otpExpiresAt()).isEqualTo(NOW.plusMinutes(10));
        assertThat(result.issuance().resendAvailableAt()).isEqualTo(NOW.plusSeconds(60));
        assertThat(pending.getResendCount()).isEqualTo(1);
        assertThat(pending.getFailedAttempts()).isZero();
        assertThat(pending.getFlowTokenHash()).isEqualTo(originalFlowHash);
        assertThat(pending.getExpiresAt()).isEqualTo(originalPendingExpiry);
        assertThat(hmacService.verifyOtp("123456", pending.getOtpHash())).isFalse();
        assertThat(hmacService.verifyOtp("654321", pending.getOtpHash())).isTrue();
    }

    @Test
    void resendBeforeCooldownIsRejectedButExactCooldownIsAllowed() {
        PendingRegistration tooSoon = pending(NOW.plusNanos(1), NOW.plusHours(24));
        arrange(tooSoon);
        assertThat(service.issueNewOtp(RAW_FLOW_TOKEN).errorCode())
                .isEqualTo(ErrorCode.AUTH_OTP_RESEND_TOO_SOON);
        verify(otpGenerator, never()).generate();

        PendingRegistration exact = pending(NOW, NOW.plusHours(24));
        arrange(exact);
        when(otpGenerator.generate()).thenReturn("654321");
        assertThat(service.issueNewOtp(RAW_FLOW_TOKEN).successful()).isTrue();
    }

    @Test
    void resendTerminalReturnsStateSpecificErrorInsteadOfFlowInvalid() {
        PendingRegistration completed = pending(NOW, NOW.plusHours(24));
        completed.complete(new User("student@example.com", "hash"), NOW.minusSeconds(1));
        arrange(completed);
        assertThat(service.issueNewOtp(RAW_FLOW_TOKEN).errorCode())
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_ALREADY_COMPLETED);

        PendingRegistration cancelled = pending(NOW, NOW.plusHours(24));
        cancelled.cancel(NOW.minusSeconds(1));
        arrange(cancelled);
        assertThat(service.issueNewOtp(RAW_FLOW_TOKEN).errorCode())
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_CANCELLED);

        PendingRegistration expired = pending(NOW, NOW.plusHours(24));
        expired.expire(NOW.minusSeconds(1));
        arrange(expired);
        assertThat(service.issueNewOtp(RAW_FLOW_TOKEN).errorCode())
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_EXPIRED);
    }

    @Test
    void resendNearPendingExpiryCapsOtpAndCooldownAtPendingExpiry() {
        PendingRegistration pending = pending(NOW, NOW.plusSeconds(30));
        arrange(pending);
        when(otpGenerator.generate()).thenReturn("654321");

        RegistrationOtpIssuance issuance = service.issueNewOtp(RAW_FLOW_TOKEN).issuance();

        assertThat(issuance.otpExpiresAt()).isEqualTo(NOW.plusSeconds(30));
        assertThat(issuance.resendAvailableAt()).isEqualTo(NOW.plusSeconds(30));
    }

    private void arrange(PendingRegistration pending) {
        when(repository.findByFlowTokenHashForUpdate(hmacService.hashFlowToken(RAW_FLOW_TOKEN)))
                .thenReturn(Optional.of(pending));
    }

    private PendingRegistration pending(LocalDateTime resendAvailableAt, LocalDateTime expiresAt) {
        PendingRegistration pending = PendingRegistration.start(
                RegistrationType.EMAIL,
                "student@example.com",
                "bcrypt-password-hash",
                hmacService.hashFlowToken(RAW_FLOW_TOKEN),
                hmacService.hashOtp("123456"),
                min(NOW.plusMinutes(10), expiresAt),
                min(resendAvailableAt, expiresAt),
                expiresAt
        );
        ReflectionTestUtils.setField(pending, "id", 10L);
        return pending;
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }
}
