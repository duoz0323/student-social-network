package com.stu.edu.vn.backend.post.dto.response;

/**
 * Kết quả Save/Unsave tối giản, không trả Entity hoặc thông tin nội bộ của người dùng.
 */
public record PostSaveResponse(
        Long postId,
        boolean saved
) {
}
