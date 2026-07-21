package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import java.time.LocalDateTime;

public record LinkChallengeResponse(
        String flowToken,
        AuthMethodLinkPurpose flowType,
        String maskedIdentifier,
        LocalDateTime otpExpiresAt,
        LocalDateTime resendAvailableAt,
        LocalDateTime challengeExpiresAt
) { }
