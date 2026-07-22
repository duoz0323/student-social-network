package com.stu.edu.vn.backend.interaction.dto.response;

import java.time.LocalDateTime;

/**
 * Response bình luận chỉ chứa dữ liệu cần hiển thị, không trả Entity comments trực tiếp ra API.
 */
public record CommentResponse(
        Long commentId,
        Long postId,
        Long parentCommentId,
        Long userId,
        String displayName,
        String avatarUrl,
        String content,
        LocalDateTime createdAt,
        long replyCount,
        boolean deleted
) {
}
