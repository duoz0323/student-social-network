package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình nội dung OTP; thông tin đăng nhập SMTP do Spring Mail nạp từ biến môi trường. */
@ConfigurationProperties(prefix = "auth.otp-delivery")
public class OtpDeliveryProperties {
    private String senderName = "UniShare";
    private Duration otpExpiration = Duration.ofMinutes(10);

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public Duration getOtpExpiration() { return otpExpiration; }
    public void setOtpExpiration(Duration otpExpiration) { this.otpExpiration = otpExpiration; }
}
