package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.time.LocalDateTime;

/** Dữ liệu nội bộ đi qua ranh giới commit; raw OTP không được ghi log hoặc lưu database. */
record RegistrationCreation(
        RegistrationType type,
        String normalizedIdentifier,
        String rawOtp,
        String rawFlowToken,
        String flowTokenHash,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime pendingExpiresAt,
        boolean resumed
) {
}
