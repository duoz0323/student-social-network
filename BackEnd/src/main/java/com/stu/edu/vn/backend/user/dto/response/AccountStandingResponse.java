package com.stu.edu.vn.backend.user.dto.response;

import com.stu.edu.vn.backend.user.enums.UserStatus;

/** Read model công khai về số lần vi phạm bài viết đã được quản trị viên xác nhận. */
public record AccountStandingResponse(
        UserStatus status,
        long confirmedViolationCount,
        long violationThreshold,
        long remainingBeforeBlock
) {
}
