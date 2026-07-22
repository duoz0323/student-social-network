package com.stu.edu.vn.backend.auth.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.OtpDeliveryProperties;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

class GmailSmtpRegistrationOtpSenderTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final OtpDeliveryProperties properties = new OtpDeliveryProperties();

    @Test
    void returnsFailureWhenSmtpIsNotConfigured() {
        var sender = new GmailSmtpRegistrationOtpSender(mailSender, properties, "");
        OtpDeliveryResult result = sender.send(RegistrationType.EMAIL, "student@example.com", "123456");
        assertThat(result).isEqualTo(OtpDeliveryResult.failed("SMTP_NOT_CONFIGURED"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendsUtf8HtmlThroughJavaMailSender() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        var sender = new GmailSmtpRegistrationOtpSender(mailSender, properties, "sender@gmail.com");

        assertThat(sender.send(RegistrationType.EMAIL, "student@example.com", "123456"))
                .isEqualTo(OtpDeliveryResult.sent());
        verify(mailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("Mã xác minh UniShare");
        assertThat(message.getContent().toString()).contains("UniShare", "123456", "10 phút", "hãy bỏ qua email");
    }

    @Test
    void normalizesSmtpExceptionWithoutExposingProviderMessage() {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        org.mockito.Mockito.doThrow(new MailSendException("secret SMTP response"))
                .when(mailSender).send(message);
        var sender = new GmailSmtpRegistrationOtpSender(mailSender, properties, "sender@gmail.com");

        assertThat(sender.send(RegistrationType.EMAIL, "student@example.com", "123456"))
                .isEqualTo(OtpDeliveryResult.failed("SMTP_DELIVERY_FAILED"));
    }

    @Test
    void rejectsMissingEmailWithoutSending() {
        var sender = new GmailSmtpRegistrationOtpSender(mailSender, properties, "sender@gmail.com");
        assertThat(sender.send(RegistrationType.EMAIL, null, "123456").outcome())
                .isEqualTo(OtpDeliveryOutcome.FAILED);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
