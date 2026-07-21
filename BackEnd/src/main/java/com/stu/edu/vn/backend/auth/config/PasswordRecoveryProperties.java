package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình vòng đời Password Recovery, tách khỏi registration để không trộn các flow token. */
@ConfigurationProperties(prefix = "auth.password-recovery")
public class PasswordRecoveryProperties {
    private Duration challengeExpiration = Duration.ofMinutes(15);
    private Duration otpExpiration = Duration.ofMinutes(10);
    private Duration resendCooldown = Duration.ofSeconds(60);
    private Duration resetTokenExpiration = Duration.ofMinutes(5);
    private int maxOtpAttempts = 5;

    public Duration getChallengeExpiration() { return challengeExpiration; }
    public void setChallengeExpiration(Duration value) { this.challengeExpiration = value; }
    public Duration getOtpExpiration() { return otpExpiration; }
    public void setOtpExpiration(Duration value) { this.otpExpiration = value; }
    public Duration getResendCooldown() { return resendCooldown; }
    public void setResendCooldown(Duration value) { this.resendCooldown = value; }
    public Duration getResetTokenExpiration() { return resetTokenExpiration; }
    public void setResetTokenExpiration(Duration value) { this.resetTokenExpiration = value; }
    public int getMaxOtpAttempts() { return maxOtpAttempts; }
    public void setMaxOtpAttempts(int value) { this.maxOtpAttempts = value; }
}
