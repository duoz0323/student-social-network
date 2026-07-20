package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.time.LocalDateTime;

/** Trạng thái an toàn để Frontend tiếp tục hoặc kết thúc luồng đăng ký. */
public record RegistrationStatusResponse(
        OtpChallengeStatus status,
        RegistrationType identifierType,
        String maskedIdentifier,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime pendingExpiresAt,
        Integer resendCount,
        OtpDeliveryStatus deliveryStatus,
        boolean canResend,
        Integer remainingOtpAttempts,
        String nextStep
) {
    public static final String NEXT_STEP_VERIFY_OTP = "VERIFY_OTP";
    public static final String NEXT_STEP_REGISTRATION_COMPLETED = "REGISTRATION_COMPLETED";
    public static final String NEXT_STEP_START_NEW_REGISTRATION = "START_NEW_REGISTRATION";
}
