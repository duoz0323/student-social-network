package com.stu.edu.vn.backend.auth.enums;

/**
 * Vòng đời dùng chung cho pending registration và challenge liên kết local.
 */
public enum OtpChallengeStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    EXPIRED
}
