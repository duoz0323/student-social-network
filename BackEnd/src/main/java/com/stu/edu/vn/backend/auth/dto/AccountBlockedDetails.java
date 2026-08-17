package com.stu.edu.vn.backend.auth.dto;

import java.time.LocalDateTime;

/** Payload public-safe để Auth giải thích trạng thái khóa mà không lộ ghi chú nội bộ hoặc Admin. */
public record AccountBlockedDetails(
        String reasonCode,
        LocalDateTime blockedAt,
        String message
) {
}
