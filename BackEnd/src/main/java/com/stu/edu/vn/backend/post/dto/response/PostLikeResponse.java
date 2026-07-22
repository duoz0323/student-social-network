package com.stu.edu.vn.backend.post.dto.response;

/**
 * Response Like/Unlike chỉ trả dữ liệu cần thiết, không trả Entity post_likes trực tiếp ra API.
 */
public record PostLikeResponse(
        Long postId,
        boolean likedByCurrentUser,
        int likeCount
) {
}
