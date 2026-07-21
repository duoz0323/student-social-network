package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RegistrationDeliveryStatusServiceTest {

    private final PendingRegistrationRepository repository =
            org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-19T03:00:00Z"), ZoneOffset.UTC);
    private final RegistrationDeliveryStatusService service = new RegistrationDeliveryStatusService(repository, clock);

    @Test
    void successfulDeliveryUpdatesAuditFields() {
        PendingRegistration pending = pending();
        when(repository.findByFlowTokenHashForUpdate("a".repeat(64))).thenReturn(Optional.of(pending));

        service.record("a".repeat(64), OtpDeliveryResult.sent());

        assertThat(pending.getDeliveryStatus()).isEqualTo(OtpDeliveryStatus.SENT);
        assertThat(pending.getDeliveryAttemptCount()).isEqualTo(1);
        assertThat(pending.getLastDeliveryAttemptAt()).isEqualTo(LocalDateTime.of(2026, 7, 19, 3, 0));
        assertThat(pending.getLastDeliverySucceededAt()).isEqualTo(LocalDateTime.of(2026, 7, 19, 3, 0));
        assertThat(pending.getDeliveryFailureCode()).isNull();
    }

    @Test
    void failedDeliveryStoresOnlyNormalizedFailureCode() {
        PendingRegistration pending = pending();
        when(repository.findByFlowTokenHashForUpdate("a".repeat(64))).thenReturn(Optional.of(pending));

        service.record("a".repeat(64), OtpDeliveryResult.failed("raw provider exception: recipient@example.com"));

        assertThat(pending.getDeliveryStatus()).isEqualTo(OtpDeliveryStatus.FAILED);
        assertThat(pending.getDeliveryFailureCode()).isEqualTo("DELIVERY_FAILED");
    }

    @Test
    void versionedDeliveryUpdatesOnlyMatchingPendingOtpVersion() {
        PendingRegistration pending = pending();
        ReflectionTestUtils.setField(pending, "id", 10L);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(pending));

        service.record(10L, 1, OtpDeliveryResult.sent());

        assertThat(pending.getDeliveryStatus()).isEqualTo(OtpDeliveryStatus.SENT);
        verify(repository).saveAndFlush(pending);
    }

    @Test
    void staleDeliveryCannotOverwriteNewerOtpVersion() {
        PendingRegistration pending = pending();
        ReflectionTestUtils.setField(pending, "id", 10L);
        pending.resend(
                "new-otp-hash",
                LocalDateTime.of(2026, 7, 19, 3, 10),
                LocalDateTime.of(2026, 7, 19, 3, 1)
        );
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(pending));

        service.record(10L, 1, OtpDeliveryResult.failed("SMTP_REJECTED"));

        assertThat(pending.getDeliveryStatus()).isEqualTo(OtpDeliveryStatus.PENDING);
        verify(repository, never()).saveAndFlush(pending);
    }

    private PendingRegistration pending() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 3, 0);
        return PendingRegistration.start(
                RegistrationType.EMAIL,
                "student@example.com",
                "password-hash",
                "a".repeat(64),
                "b".repeat(64),
                now.plusMinutes(10),
                now.plusSeconds(60),
                now.plusHours(24)
        );
    }
}
