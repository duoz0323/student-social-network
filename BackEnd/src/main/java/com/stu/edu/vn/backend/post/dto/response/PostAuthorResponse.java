package com.stu.edu.vn.backend.post.dto.response;

/**
 * Thông tin công khai tối thiểu của tác giả bài viết, không trả email hoặc số điện thoại.
 */
public record PostAuthorResponse(
        Long id,
        String displayName,
        String avatarUrl
) {
}
