package com.stu.edu.vn.backend.auth.delivery;

import com.stu.edu.vn.backend.auth.config.OtpDeliveryProperties;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/** Gửi toàn bộ OTP email qua Gmail SMTP, không ghi OTP hoặc thông tin đăng nhập vào log. */
@Component
public class GmailSmtpRegistrationOtpSender implements RegistrationOtpSender {
    static final String SMTP_NOT_CONFIGURED = "SMTP_NOT_CONFIGURED";
    static final String SMTP_DELIVERY_FAILED = "SMTP_DELIVERY_FAILED";
    private final JavaMailSender mailSender;
    private final OtpDeliveryProperties properties;
    private final String username;

    public GmailSmtpRegistrationOtpSender(JavaMailSender mailSender, OtpDeliveryProperties properties,
            @Value("${spring.mail.username:}") String username) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.username = username;
    }

    @Override
    public OtpDeliveryResult send(RegistrationType type, String email, String rawOtp) {
        if (type != RegistrationType.EMAIL || isBlank(email) || isBlank(rawOtp)) {
            return OtpDeliveryResult.failed("INVALID_DELIVERY_REQUEST");
        }
        if (isBlank(username)) return OtpDeliveryResult.failed(SMTP_NOT_CONFIGURED);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(username, properties.getSenderName());
            helper.setTo(email);
            helper.setSubject("Mã xác minh UniShare");
            helper.setText(emailContent(rawOtp), true);
            mailSender.send(message);
            return OtpDeliveryResult.sent();
        } catch (MessagingException | UnsupportedEncodingException | MailException exception) {
            // Chỉ trả mã lỗi nội bộ ổn định; không truyền message SMTP có thể chứa dữ liệu nhạy cảm.
            return OtpDeliveryResult.failed(SMTP_DELIVERY_FAILED);
        }
    }

    String emailContent(String otp) {
        long minutes = Math.max(1, properties.getOtpExpiration().toMinutes());
        return "<div style=\"font-family:Arial,sans-serif;line-height:1.5;color:#202124\">"
                + "<h2>UniShare</h2><p>Mã OTP của bạn là:</p>"
                + "<p style=\"font-size:28px;font-weight:bold;letter-spacing:4px\">" + escapeHtml(otp) + "</p>"
                + "<p>Mã có hiệu lực trong " + minutes + " phút.</p>"
                + "<p>Nếu bạn không yêu cầu mã này, hãy bỏ qua email.</p>"
                + "<p>Không chia sẻ mã OTP với bất kỳ ai.</p></div>";
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
