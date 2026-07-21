package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import java.time.LocalDateTime;

/**
 * Response chỉ trả raw flow token một lần và không lộ các giá trị hash hoặc OTP.
 */
public record RegisterResponse(
        String registrationFlowToken,
        OtpChallengeStatus status,
        String maskedIdentifier,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime pendingExpiresAt,
        boolean resumed
) {
}
