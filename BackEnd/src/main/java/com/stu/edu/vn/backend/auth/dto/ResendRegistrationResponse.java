package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import java.time.LocalDateTime;

/** Metadata của OTP version mới; không trả OTP, flow token hoặc bất kỳ hash nào. */
public record ResendRegistrationResponse(
        OtpChallengeStatus status,
        String maskedIdentifier,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime pendingExpiresAt,
        String message
) {
}
