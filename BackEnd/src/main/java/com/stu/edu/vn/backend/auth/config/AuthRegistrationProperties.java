package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình dùng chung cho vòng đời đăng ký local; secret phải được cấp qua biến môi trường.
 */
@ConfigurationProperties(prefix = "auth.registration")
public class AuthRegistrationProperties {

    private int otpLength = 6;
    private Duration otpExpiration = Duration.ofMinutes(10);
    private Duration resendCooldown = Duration.ofSeconds(60);
    private int maxOtpAttempts = 5;
    private Duration pendingExpiration = Duration.ofHours(24);
    private int flowTokenRandomBytes = 32;
    private String otpHmacSecret;
    private String flowTokenHmacSecret;

    public int getOtpLength() {
        return otpLength;
    }

    public void setOtpLength(int otpLength) {
        this.otpLength = otpLength;
    }

    public Duration getOtpExpiration() {
        return otpExpiration;
    }

    public void setOtpExpiration(Duration otpExpiration) {
        this.otpExpiration = otpExpiration;
    }

    public Duration getResendCooldown() {
        return resendCooldown;
    }

    public void setResendCooldown(Duration resendCooldown) {
        this.resendCooldown = resendCooldown;
    }

    public int getMaxOtpAttempts() {
        return maxOtpAttempts;
    }

    public void setMaxOtpAttempts(int maxOtpAttempts) {
        this.maxOtpAttempts = maxOtpAttempts;
    }

    public Duration getPendingExpiration() {
        return pendingExpiration;
    }

    public void setPendingExpiration(Duration pendingExpiration) {
        this.pendingExpiration = pendingExpiration;
    }

    public int getFlowTokenRandomBytes() {
        return flowTokenRandomBytes;
    }

    public void setFlowTokenRandomBytes(int flowTokenRandomBytes) {
        this.flowTokenRandomBytes = flowTokenRandomBytes;
    }

    public String getOtpHmacSecret() {
        return otpHmacSecret;
    }

    public void setOtpHmacSecret(String otpHmacSecret) {
        this.otpHmacSecret = otpHmacSecret;
    }

    public String getFlowTokenHmacSecret() {
        return flowTokenHmacSecret;
    }

    public void setFlowTokenHmacSecret(String flowTokenHmacSecret) {
        this.flowTokenHmacSecret = flowTokenHmacSecret;
    }
}
