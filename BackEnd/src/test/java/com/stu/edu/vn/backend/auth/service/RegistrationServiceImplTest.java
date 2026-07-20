package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.support.IdentifierMasker;
import com.stu.edu.vn.backend.auth.support.NormalizedIdentifier;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;

class RegistrationServiceImplTest {

    private final RegistrationTransactionService transactionService =
            org.mockito.Mockito.mock(RegistrationTransactionService.class);
    private final RegistrationDeliveryStatusService deliveryStatusService =
            org.mockito.Mockito.mock(RegistrationDeliveryStatusService.class);
    private final RegistrationOtpSender otpSender = org.mockito.Mockito.mock(RegistrationOtpSender.class);
    private RegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistrationServiceImpl(
                transactionService,
                deliveryStatusService,
                otpSender,
                new PasswordPolicyValidator(),
                new IdentifierMasker()
        );
    }

    @Test
    void returnsRawFlowTokenOnlyAfterPendingCommitAndSuccessfulDelivery() {
        RegistrationCreation creation = emailCreation();
        when(transactionService.create(any(NormalizedIdentifier.class), any())).thenReturn(creation);
        when(otpSender.send(RegistrationType.EMAIL, "student@example.com", "123456"))
                .thenReturn(OtpDeliveryResult.sent());

        RegisterResponse response = service.start(validEmailRequest());

        InOrder order = inOrder(transactionService, otpSender, deliveryStatusService);
        order.verify(transactionService).create(any(NormalizedIdentifier.class), any());
        order.verify(otpSender).send(RegistrationType.EMAIL, "student@example.com", "123456");
        order.verify(deliveryStatusService).record("flow-hash", OtpDeliveryResult.sent());
        assertThat(response.registrationFlowToken()).isEqualTo("raw-flow-token");
        assertThat(response.maskedIdentifier()).isEqualTo("s***@example.com");
        assertThat(response.identifierType()).isEqualTo(RegistrationType.EMAIL);
    }

    @Test
    void normalizesPhoneBeforeCreatingPending() {
        RegistrationCreation creation = new RegistrationCreation(
                RegistrationType.PHONE,
                "0912345678",
                "123456",
                "raw-flow-token",
                "flow-hash",
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().plusSeconds(60),
                LocalDateTime.now().plusHours(24)
        );
        when(transactionService.create(any(NormalizedIdentifier.class), any())).thenReturn(creation);
        when(otpSender.send(any(), any(), any())).thenReturn(OtpDeliveryResult.sent());

        RegisterResponse response = service.start(
                new RegisterRequest("091 234-5678", "Password@123", "Password@123")
        );

        assertThat(response.identifierType()).isEqualTo(RegistrationType.PHONE);
        assertThat(response.maskedIdentifier()).isEqualTo("******5678");
    }

    @Test
    void rejectsWeakPasswordBeforeDatabaseWork() {
        assertThatThrownBy(() -> service.start(
                new RegisterRequest("student@example.com", "weak", "weak")
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);

        verify(transactionService, never()).create(any(), any());
    }

    @Test
    void rejectsBlankIdentifierBeforeDatabaseWork() {
        assertThatThrownBy(() -> service.start(
                new RegisterRequest("  ", "Password@123", "Password@123")
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_IDENTIFIER_INVALID);

        verify(transactionService, never()).create(any(), any());
    }

    @Test
    void rejectsPasswordConfirmationMismatch() {
        assertThatThrownBy(() -> service.start(
                new RegisterRequest("student@example.com", "Password@123", "Different@123")
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_PASSWORD_CONFIRMATION_MISMATCH);
    }

    @Test
    void databaseRaceBecomesBusinessErrorAndDoesNotSendOtp() {
        when(transactionService.create(any(), any()))
                .thenThrow(new DataIntegrityViolationException("constraint details must not escape"));

        assertThatThrownBy(() -> service.start(validEmailRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_ALREADY_PENDING);

        verify(otpSender, never()).send(any(), any(), any());
        verify(deliveryStatusService, never()).record(any(), any());
    }

    @Test
    void confirmedDeliveryFailureIsRecordedAndPendingIsKeptForLaterRecovery() {
        RegistrationCreation creation = emailCreation();
        OtpDeliveryResult failed = OtpDeliveryResult.failed("SMTP_REJECTED");
        when(transactionService.create(any(), any())).thenReturn(creation);
        when(otpSender.send(any(), any(), any())).thenReturn(failed);

        assertThatThrownBy(() -> service.start(validEmailRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_DELIVERY_FAILED);

        verify(deliveryStatusService).record("flow-hash", failed);
    }

    @Test
    void uncertainProviderExceptionIsRecordedAsUnknown() {
        when(transactionService.create(any(), any())).thenReturn(emailCreation());
        when(otpSender.send(any(), any(), any())).thenThrow(new RuntimeException("provider payload"));

        assertThatThrownBy(() -> service.start(validEmailRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_DELIVERY_FAILED);

        verify(deliveryStatusService).record("flow-hash", OtpDeliveryResult.unknown());
    }

    private RegisterRequest validEmailRequest() {
        return new RegisterRequest(" Student@Example.COM ", "Password@123", "Password@123");
    }

    private RegistrationCreation emailCreation() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 3, 0);
        return new RegistrationCreation(
                RegistrationType.EMAIL,
                "student@example.com",
                "123456",
                "raw-flow-token",
                "flow-hash",
                now.plusMinutes(10),
                now.plusSeconds(60),
                now.plusHours(24)
        );
    }
}
