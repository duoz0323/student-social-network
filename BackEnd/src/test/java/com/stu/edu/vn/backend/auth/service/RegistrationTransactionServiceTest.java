package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class RegistrationTransactionServiceTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final PendingRegistrationRepository pendingRepository =
            org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final OtpGenerator otpGenerator = org.mockito.Mockito.mock(OtpGenerator.class);
    private final FlowTokenGenerator flowTokenGenerator = org.mockito.Mockito.mock(FlowTokenGenerator.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-19T03:00:00Z"), ZoneOffset.UTC);

    private AuthHmacService hmacService;
    private RegistrationTransactionService service;

    @BeforeEach
    void setUp() {
        AuthRegistrationProperties properties = properties();
        hmacService = new AuthHmacService(properties);
        service = new RegistrationTransactionService(
                userRepository,
                pendingRepository,
                passwordEncoder,
                otpGenerator,
                flowTokenGenerator,
                hmacService,
                properties,
                clock
        );
        when(passwordEncoder.encode("Password@123")).thenReturn("bcrypt-password-hash");
        when(otpGenerator.generate()).thenReturn("123456");
        when(flowTokenGenerator.generate()).thenReturn("raw-flow-token");
    }

    @Test
    void createsEmailPendingWithHashesAndConfiguredTimes() {
        service.create(new NormalizedEmail("student@example.com"), "Password@123");

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRepository).saveAndFlush(captor.capture());
        PendingRegistration pending = captor.getValue();
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 3, 0);

        assertThat(pending.getRegistrationType().name()).isEqualTo("EMAIL");
        assertThat(pending.getIdentifierNormalized()).isEqualTo("student@example.com");
        assertThat(pending.getActiveIdentifierKey()).isEqualTo("EMAIL:student@example.com");
        assertThat(pending.getPasswordHash()).isEqualTo("bcrypt-password-hash").isNotEqualTo("Password@123");
        assertThat(pending.getOtpHash()).isEqualTo(hmacService.hashOtp("123456")).doesNotContain("123456");
        assertThat(pending.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken("raw-flow-token"));
        assertThat(pending.getOtpVersion()).isEqualTo(1);
        assertThat(pending.getFailedAttempts()).isZero();
        assertThat(pending.getResendCount()).isZero();
        assertThat(pending.getDeliveryStatus()).isEqualTo(OtpDeliveryStatus.PENDING);
        assertThat(pending.getDeliveryAttemptCount()).isZero();
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
        assertThat(pending.getOtpExpiresAt()).isEqualTo(now.plusMinutes(10));
        assertThat(pending.getResendAvailableAt()).isEqualTo(now.plusSeconds(60));
        assertThat(pending.getExpiresAt()).isEqualTo(now.plusHours(24));
        assertThat(pending.getCompletedUser()).isNull();
        assertThat(pending.getTerminalAt()).isNull();
    }


    @Test
    void rejectsIdentifierAlreadyOwnedByUser() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new NormalizedEmail("student@example.com"),
                "Password@123"
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);

        verify(pendingRepository, never()).saveAndFlush(any());
    }


    @Test
    void resumesActivePendingWithRotatedFlowTokenWithoutChangingPasswordOrOtp() {
        PendingRegistration existing = pendingExpiringAt(LocalDateTime.of(2026, 7, 20, 3, 0));
        String originalPasswordHash = existing.getPasswordHash();
        String originalOtpHash = existing.getOtpHash();
        when(pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:student@example.com"))
                .thenReturn(Optional.of(existing));

        RegistrationCreation creation = service.create(
                new NormalizedEmail("student@example.com"),
                "Password@123"
        );

        assertThat(creation.resumed()).isTrue();
        assertThat(creation.rawFlowToken()).isEqualTo("raw-flow-token");
        assertThat(creation.rawOtp()).isNull();
        assertThat(existing.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken("raw-flow-token"));
        assertThat(existing.getPasswordHash()).isEqualTo(originalPasswordHash);
        assertThat(existing.getOtpHash()).isEqualTo(originalOtpHash);
        verify(otpGenerator, never()).generate();
        verify(passwordEncoder, never()).encode(any());
        verify(pendingRepository).saveAndFlush(existing);
    }

    @Test
    void expiresOldPendingBeforeCreatingReplacement() {
        PendingRegistration existing = pendingExpiringAt(LocalDateTime.of(2026, 7, 19, 2, 59));
        when(pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:student@example.com"))
                .thenReturn(Optional.of(existing));

        service.create(new NormalizedEmail("student@example.com"), "Password@123");

        assertThat(existing.getStatus()).isEqualTo(OtpChallengeStatus.EXPIRED);
        assertThat(existing.getActiveIdentifierKey()).isNull();
        assertThat(existing.getIdentifierNormalized()).isNull();
        assertThat(existing.getPasswordHash()).isNull();
        assertThat(existing.getOtpHash()).isNull();
        assertThat(existing.getFlowTokenHash()).isEqualTo("a".repeat(64));
        assertThat(existing.getTerminalAt()).isEqualTo(LocalDateTime.of(2026, 7, 19, 3, 0));
        verify(pendingRepository).saveAndFlush(existing);
    }

    private PendingRegistration pendingExpiringAt(LocalDateTime expiresAt) {
        return PendingRegistration.start(
                com.stu.edu.vn.backend.auth.enums.RegistrationType.EMAIL,
                "student@example.com",
                "old-password-hash",
                "a".repeat(64),
                "b".repeat(64),
                expiresAt.minusMinutes(10),
                expiresAt.minusHours(1),
                expiresAt
        );
    }

    private AuthRegistrationProperties properties() {
        AuthRegistrationProperties properties = new AuthRegistrationProperties();
        properties.setOtpHmacSecret("otp-secret-for-test-only");
        properties.setFlowTokenHmacSecret("flow-secret-for-test-only");
        return properties;
    }
}

