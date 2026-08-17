package com.stu.edu.vn.backend.interaction.dto.response;

import java.time.LocalDateTime;
import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.List;

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
        boolean deleted,
        List<PublicUserBadge> badges
) {
    public CommentResponse(Long commentId, Long postId, Long parentCommentId, Long userId,
                           String displayName, String avatarUrl, String content, LocalDateTime createdAt,
                           long replyCount, boolean deleted) {
        this(commentId, postId, parentCommentId, userId, displayName, avatarUrl, content,
                createdAt, replyCount, deleted, List.of());
    }
}
