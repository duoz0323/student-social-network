package com.stu.edu.vn.backend.user.repository;

import java.time.LocalDateTime;

public interface RestrictedUserProjection {
    Long getUserId();
    String getDisplayName();
    String getAvatarUrl();
    LocalDateTime getRestrictedAt();
}
