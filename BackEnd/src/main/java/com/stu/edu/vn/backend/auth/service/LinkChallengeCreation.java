package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.time.LocalDateTime;

record LinkChallengeCreation(
        Long challengeId, int otpVersion, String rawFlowToken, String flowTokenHash, String rawOtp,
        RegistrationType deliveryType, AuthMethodLinkPurpose purpose, String identifier,
        LocalDateTime otpExpiresAt, LocalDateTime resendAvailableAt, LocalDateTime expiresAt
) { }
