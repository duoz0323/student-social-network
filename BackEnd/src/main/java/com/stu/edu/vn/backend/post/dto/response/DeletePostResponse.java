package com.stu.edu.vn.backend.post.dto.response;

/**
 * DTO phản hồi sau khi xóa mềm bài viết, giúp API không trả Entity trực tiếp ra ngoài.
 */
public record DeletePostResponse(
        // Id bài viết vừa được chuyển sang trạng thái DELETED.
        Long postId,
        // Cờ xác nhận thao tác xóa mềm đã hoàn tất.
        boolean deleted
) {
}
