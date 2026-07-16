package com.stu.edu.vn.backend.admin.dto.response;

/** ADMIN đã ẩn bài; displayName có thể null nếu hồ sơ chưa hoàn tất. */
public record AdminPostHiddenByResponse(Long adminId, String displayName) {
}
