package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import java.time.LocalDateTime;

/** Kết quả terminal idempotent của thao tác hủy đăng ký. */
public record CancelRegistrationResponse(
        OtpChallengeStatus status,
        LocalDateTime terminalAt,
        String message
) {
}
