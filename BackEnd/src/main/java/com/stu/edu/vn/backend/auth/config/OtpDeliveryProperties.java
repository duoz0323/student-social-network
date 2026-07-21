package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình nhà cung cấp OTP; toàn bộ credential chỉ được nạp từ biến môi trường. */
@ConfigurationProperties(prefix = "auth.otp-delivery")
public class OtpDeliveryProperties {
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(8);
    private final Brevo brevo = new Brevo();

    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public Brevo getBrevo() { return brevo; }

    public static class Brevo {
        private String apiKey;
        private String senderEmail;
        private String senderName = "UniShare";
        private String baseUrl = "https://api.brevo.com";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getSenderEmail() { return senderEmail; }
        public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public boolean isConfigured() { return hasText(apiKey) && hasText(senderEmail); }
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
