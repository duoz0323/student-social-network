package com.stu.edu.vn.backend.auth.dto;

import java.time.LocalDateTime;

/** Reset token chỉ xuất hiện sau khi OTP của challenge thật hợp lệ. */
public record VerifyPasswordRecoveryResponse(String resetAuthorizedToken, LocalDateTime resetTokenExpiresAt) { }
