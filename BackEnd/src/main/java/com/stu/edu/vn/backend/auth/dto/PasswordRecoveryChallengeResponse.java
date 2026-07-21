package com.stu.edu.vn.backend.auth.dto;

import java.time.LocalDateTime;

/** Response giống nhau cho challenge thật và decoy. */
public record PasswordRecoveryChallengeResponse(
        boolean accepted, String flowType, String recoveryFlowToken, LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt, LocalDateTime challengeExpiresAt) { }
