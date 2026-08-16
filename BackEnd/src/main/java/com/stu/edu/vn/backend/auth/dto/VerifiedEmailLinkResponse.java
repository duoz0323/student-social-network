package com.stu.edu.vn.backend.auth.dto;

import java.time.LocalDateTime;

/** Proof đã rotate sau khi OTP hợp lệ; chỉ dùng cho bước đặt mật khẩu. */
public record VerifiedEmailLinkResponse(String flowToken, String maskedIdentifier, LocalDateTime expiresAt) { }
