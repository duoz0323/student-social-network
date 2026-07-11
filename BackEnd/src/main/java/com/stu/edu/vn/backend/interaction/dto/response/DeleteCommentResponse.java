package com.stu.edu.vn.backend.interaction.dto.response;

/**
 * Response xác nhận xóa mềm bình luận thành công.
 */
public record DeleteCommentResponse(
        Long commentId,
        boolean deleted
) {
}
