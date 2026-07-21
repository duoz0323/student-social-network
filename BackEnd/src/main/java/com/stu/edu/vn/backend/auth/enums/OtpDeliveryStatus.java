package com.stu.edu.vn.backend.auth.enums;

/**
 * Kết quả giao OTP sau khi challenge đã được commit.
 */
public enum OtpDeliveryStatus {
    NOT_APPLICABLE,
    PENDING,
    SENDING,
    SENT,
    FAILED,
    UNKNOWN
}
