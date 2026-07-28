package com.stu.edu.vn.backend.user.dto.response;

import java.time.LocalDateTime;

public record RestrictedUserResponse(
        Long userId, String displayName, String avatarUrl, LocalDateTime restrictedAt) { }
