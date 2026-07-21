package com.stu.edu.vn.backend.auth.delivery;

import com.stu.edu.vn.backend.auth.config.OtpDeliveryProperties;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Gửi OTP email qua Brevo mà không ghi credential hoặc OTP vào log. */
@Component
public class ProviderRegistrationOtpSender implements RegistrationOtpSender {
    private final OtpDeliveryProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public ProviderRegistrationOtpSender(OtpDeliveryProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout()).build());
    }

    ProviderRegistrationOtpSender(OtpDeliveryProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public OtpDeliveryResult send(RegistrationType type, String email, String rawOtp) {
        if (type != RegistrationType.EMAIL || email == null || rawOtp == null) {
            return OtpDeliveryResult.failed("INVALID_DELIVERY_REQUEST");
        }
        OtpDeliveryProperties.Brevo brevo = properties.getBrevo();
        if (!brevo.isConfigured()) return OtpDeliveryResult.failed("BREVO_NOT_CONFIGURED");
        try {
            String payload = objectMapper.writeValueAsString(new BrevoEmailRequest(
                    new Sender(brevo.getSenderName(), brevo.getSenderEmail()),
                    new Recipient[]{new Recipient(email)}, "Mã xác minh UniShare", emailContent(rawOtp)));
            HttpRequest request = HttpRequest.newBuilder(providerUri(brevo.getBaseUrl(), "/v3/smtp/email"))
                    .timeout(properties.getReadTimeout()).header("accept", "application/json")
                    .header("api-key", brevo.getApiKey()).header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8)).build();
            return sendRequest(request);
        } catch (IllegalArgumentException exception) {
            return OtpDeliveryResult.failed("BREVO_REQUEST_INVALID");
        }
    }

    private OtpDeliveryResult sendRequest(HttpRequest request) {
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() >= 200 && response.statusCode() < 300
                    ? OtpDeliveryResult.sent() : OtpDeliveryResult.failed("BREVO_REJECTED");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return OtpDeliveryResult.unknown();
        } catch (IOException exception) {
            return OtpDeliveryResult.unknown();
        }
    }

    String emailContent(String otp) {
        return "<div><h2>Xác minh tài khoản UniShare</h2><p>Mã OTP của bạn là:</p><strong>"
                + otp
                + "</strong><p>Mã có hiệu lực trong 10 phút.</p>"
                + "<p><strong>Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</strong> "
                + "UniShare không bao giờ yêu cầu bạn cung cấp mã OTP.</p></div>";
    }

    private URI providerUri(String baseUrl, String path) {
        return URI.create(baseUrl.replaceAll("/+$", "") + path);
    }

    private record Sender(String name, String email) { }
    private record Recipient(String email) { }
    private record BrevoEmailRequest(Sender sender, Recipient[] to, String subject, String htmlContent) { }
}
