package com.stu.edu.vn.backend.auth.service;

/** Raw Refresh Token chỉ tồn tại trong kết quả nội bộ để trả đúng một lần cho Client. */
public record IssuedRefreshToken(String rawToken, long expiresInSeconds) {
}
