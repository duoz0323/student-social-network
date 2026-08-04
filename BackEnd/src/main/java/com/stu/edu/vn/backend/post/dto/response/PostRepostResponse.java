package com.stu.edu.vn.backend.post.dto.response;

/** Trạng thái Repost idempotent cùng counter mới nhất của bài gốc. */
public record PostRepostResponse(
        Long postId,
        boolean repostedByCurrentUser,
        int repostCount
) {
}
