package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class RegistrationVerificationServiceImplTest {

    private final RegistrationVerificationTransactionService transactionService =
            org.mockito.Mockito.mock(RegistrationVerificationTransactionService.class);
    private RegistrationVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistrationVerificationServiceImpl(
                transactionService,
                new AuthRegistrationProperties()
        );
    }

    @Test
    void returnsSuccessfulTransactionResponse() {
        VerifyRegistrationResponse response = response();
        when(transactionService.verify(any(), any()))
                .thenReturn(RegistrationVerificationResult.success(response));

        assertThat(service.verify(validRequest(), "203.0.113.10")).isSameAs(response);
    }

    @Test
    void missingFlowTokenIsRejectedBeforeTransaction() {
        assertThatThrownBy(() -> service.verify(
                new VerifyRegistrationRequest(" ", "123456", null, null),
                null
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
        verify(transactionService, never()).verify(any(), any());
    }

    @Test
    void missingOrMalformedOtpIsRejectedBeforeTransaction() {
        assertThatThrownBy(() -> service.verify(
                new VerifyRegistrationRequest("flow", "12A456", null, null),
                null
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_INVALID);
        verify(transactionService, never()).verify(any(), any());
    }

    @Test
    void failureOutcomeBecomesBusinessExceptionAfterTransactionReturns() {
        when(transactionService.verify(any(), any()))
                .thenReturn(RegistrationVerificationResult.failure(ErrorCode.AUTH_OTP_INVALID));

        assertThatThrownBy(() -> service.verify(validRequest(), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_OTP_INVALID);
    }

    @Test
    void identifierRaceMapsToSpecificConflictWithoutSqlDetails() {
        when(transactionService.verify(any(), any()))
                .thenThrow(new RegistrationIdentifierConflictException(
                        RegistrationType.PHONE,
                        new DataIntegrityViolationException("uq_users_phone_number")
                ));

        assertThatThrownBy(() -> service.verify(validRequest(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.AUTH_PHONE_ALREADY_EXISTS.getDefaultMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_PHONE_ALREADY_EXISTS);
    }

    @Test
    void persistenceFailureMapsToAccountCreationFailed() {
        when(transactionService.verify(any(), any()))
                .thenThrow(new DataIntegrityViolationException("internal constraint"));

        assertThatThrownBy(() -> service.verify(validRequest(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.AUTH_ACCOUNT_CREATION_FAILED.getDefaultMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_ACCOUNT_CREATION_FAILED);
    }

    private VerifyRegistrationRequest validRequest() {
        return new VerifyRegistrationRequest("raw-flow-token", "123456", "device", "Chrome");
    }

    private VerifyRegistrationResponse response() {
        return new VerifyRegistrationResponse(
                "access",
                "refresh",
                "Bearer",
                900,
                2_592_000,
                false,
                "ONBOARDING",
                new VerifyRegistrationResponse.UserSummary(1L, UserRole.USER)
        );
    }
}
