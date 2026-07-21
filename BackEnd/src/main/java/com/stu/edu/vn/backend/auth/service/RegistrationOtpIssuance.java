package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.time.LocalDateTime;

/** Dữ liệu nội bộ sau commit để gửi đúng OTP version ngoài transaction database. */
record RegistrationOtpIssuance(
        Long pendingId,
        int otpVersion,
        RegistrationType type,
        String normalizedIdentifier,
        String maskedIdentifier,
        String rawOtp,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime pendingExpiresAt
) {
}
