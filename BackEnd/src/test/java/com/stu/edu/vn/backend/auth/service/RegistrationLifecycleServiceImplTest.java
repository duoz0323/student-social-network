package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationResponse;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class RegistrationLifecycleServiceImplTest {

    private final RegistrationLifecycleTransactionService transactionService =
            org.mockito.Mockito.mock(RegistrationLifecycleTransactionService.class);
    private final RegistrationDeliveryStatusService deliveryStatusService =
            org.mockito.Mockito.mock(RegistrationDeliveryStatusService.class);
    private final RegistrationOtpSender otpSender = org.mockito.Mockito.mock(RegistrationOtpSender.class);
    private RegistrationLifecycleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistrationLifecycleServiceImpl(transactionService, deliveryStatusService, otpSender);
    }

    @Test
    void resendCommitsBeforeProviderAndTracksExactOtpVersion() {
        RegistrationOtpIssuance issuance = issuance();
        when(transactionService.issueNewOtp("raw-flow-token"))
                .thenReturn(RegistrationResendResult.success(issuance));
        when(otpSender.send(RegistrationType.EMAIL, "student@example.com", "654321"))
                .thenReturn(OtpDeliveryResult.sent());

        ResendRegistrationResponse response = service.resend(new ResendRegistrationRequest("raw-flow-token"));

        InOrder order = inOrder(transactionService, otpSender, deliveryStatusService);
        order.verify(transactionService).issueNewOtp("raw-flow-token");
        order.verify(otpSender).send(RegistrationType.EMAIL, "student@example.com", "654321");
        order.verify(deliveryStatusService).record(10L, 2, OtpDeliveryResult.sent());
        assertThat(response.maskedIdentifier()).isEqualTo("s***@example.com");
        assertThat(response.otpExpiresAt()).isEqualTo(LocalDateTime.of(2026, 7, 19, 3, 10));
    }

    @Test
    void transactionFailureNeverCallsProvider() {
        when(transactionService.issueNewOtp("raw-flow-token"))
                .thenReturn(RegistrationResendResult.failure(ErrorCode.AUTH_OTP_RESEND_TOO_SOON));

        assertThatThrownBy(() -> service.resend(new ResendRegistrationRequest("raw-flow-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_RESEND_TOO_SOON);

        verify(otpSender, never()).send(any(), any(), any());
        verify(deliveryStatusService, never()).record(any(), any(Integer.class), any());
    }

    @Test
    void providerAcceptedOtpStillReturnsSuccessWhenDeliveryAuditFails() {
        RegistrationOtpIssuance issuance = issuance();
        when(transactionService.issueNewOtp("raw-flow-token"))
                .thenReturn(RegistrationResendResult.success(issuance));
        when(otpSender.send(any(), any(), any())).thenReturn(OtpDeliveryResult.sent());
        doThrow(new RuntimeException("database unavailable"))
                .when(deliveryStatusService).record(10L, 2, OtpDeliveryResult.sent());

        ResendRegistrationResponse response = service.resend(new ResendRegistrationRequest("raw-flow-token"));

        assertThat(response.status().name()).isEqualTo("PENDING");
    }

    @Test
    void confirmedProviderFailureDoesNotRestoreOldOtp() {
        RegistrationOtpIssuance issuance = issuance();
        OtpDeliveryResult failed = OtpDeliveryResult.failed("SMTP_REJECTED");
        when(transactionService.issueNewOtp("raw-flow-token"))
                .thenReturn(RegistrationResendResult.success(issuance));
        when(otpSender.send(any(), any(), any())).thenReturn(failed);

        assertThatThrownBy(() -> service.resend(new ResendRegistrationRequest("raw-flow-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_DELIVERY_FAILED);

        verify(deliveryStatusService).record(10L, 2, failed);
        verify(transactionService, never()).issueNewOtp("old-flow-token");
    }

    @Test
    void providerExceptionIsRecordedAsUnknown() {
        when(transactionService.issueNewOtp("raw-flow-token"))
                .thenReturn(RegistrationResendResult.success(issuance()));
        when(otpSender.send(any(), any(), any())).thenThrow(new RuntimeException("provider secret payload"));

        assertThatThrownBy(() -> service.resend(new ResendRegistrationRequest("raw-flow-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_DELIVERY_FAILED);

        verify(deliveryStatusService).record(10L, 2, OtpDeliveryResult.unknown());
    }

    @Test
    void blankFlowTokenIsRejectedBeforeDatabaseWork() {
        assertThatThrownBy(() -> service.resend(new ResendRegistrationRequest(" ")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
        assertThatThrownBy(() -> service.status(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);

        verify(transactionService, never()).issueNewOtp(any());
        verify(transactionService, never()).status(any());
    }

    private RegistrationOtpIssuance issuance() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 3, 0);
        return new RegistrationOtpIssuance(
                10L,
                2,
                RegistrationType.EMAIL,
                "student@example.com",
                "s***@example.com",
                "654321",
                now.plusMinutes(10),
                now.plusSeconds(60),
                now.plusHours(24)
        );
    }
}

