package com.stu.edu.vn.backend.auth.dto;

/** Báo cho Frontend biết mọi Refresh Token đã bị thu hồi sau đổi credential. */
public record PasswordMutationResponse(boolean passwordConfigured, boolean sessionsRevoked) { }
