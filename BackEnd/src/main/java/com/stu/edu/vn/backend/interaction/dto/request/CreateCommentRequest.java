package com.stu.edu.vn.backend.interaction.dto.request;

/**
 * Request thêm bình luận, chỉ nhận nội dung và không nhận userId từ Client.
 */
public record CreateCommentRequest(
        String content
) {
}
