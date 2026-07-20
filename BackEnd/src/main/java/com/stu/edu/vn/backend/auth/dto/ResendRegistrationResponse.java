package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.time.LocalDateTime;

/** Metadata của OTP version mới; không trả OTP, flow token hoặc bất kỳ hash nào. */
public record ResendRegistrationResponse(
        OtpChallengeStatus status,
        RegistrationType identifierType,
        String maskedIdentifier,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime pendingExpiresAt,
        String message
) {
}
