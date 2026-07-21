package com.stu.edu.vn.backend.auth.delivery;

import static org.mockito.Mockito.*;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.service.PasswordRecoveryDeliveryStatusService;
import org.junit.jupiter.api.Test;

class PasswordRecoveryOtpListenerTest {
    private final RegistrationOtpSender sender = mock(RegistrationOtpSender.class);
    private final PasswordRecoveryDeliveryStatusService statuses = mock(PasswordRecoveryDeliveryStatusService.class);
    private final PasswordRecoveryOtpListener listener = new PasswordRecoveryOtpListener(sender, statuses);
    private final PasswordRecoveryOtpRequested event = new PasswordRecoveryOtpRequested(
            7L, 2, RegistrationType.EMAIL, "student@example.com", "123456");

    @Test void confirmedFailureRetriesOnceThenRecordsFinalOutcome() {
        when(statuses.claim(7L, 2)).thenReturn(true);
        when(sender.send(any(), anyString(), anyString()))
                .thenReturn(OtpDeliveryResult.failed("SMTP_DOWN"), OtpDeliveryResult.sent());
        listener.deliver(event);
        verify(sender, times(2)).send(RegistrationType.EMAIL, "student@example.com", "123456");
        verify(statuses).finish(7L, 2, OtpDeliveryOutcome.SENT, null, 2);
    }

    @Test void ambiguousExceptionIsNeverRetried() {
        when(statuses.claim(7L, 2)).thenReturn(true);
        when(sender.send(any(), anyString(), anyString())).thenThrow(new RuntimeException("timeout"));
        listener.deliver(event);
        verify(sender).send(any(), anyString(), anyString());
        verify(statuses).finish(7L, 2, OtpDeliveryOutcome.UNKNOWN, null, 1);
    }

    @Test void staleOrDuplicateCallbackDoesNothing() {
        when(statuses.claim(7L, 2)).thenReturn(false);
        listener.deliver(event);
        verifyNoInteractions(sender);
    }
}

