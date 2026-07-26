package com.stu.edu.vn.backend.user.dto.response;

/** Kết quả idempotent của thao tác chặn hoặc bỏ chặn. */
public record UserBlockStatusResponse(Long targetUserId, boolean blocked) {
}
