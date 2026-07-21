package com.stu.edu.vn.backend.auth.delivery;

import com.stu.edu.vn.backend.auth.enums.RegistrationType;

/** Payload chỉ sống trong bộ nhớ sau commit; tuyệt đối không ghi log raw OTP. */
public record PasswordRecoveryOtpRequested(
        Long challengeId, int otpVersion, RegistrationType channel, String destination, String rawOtp) { }
