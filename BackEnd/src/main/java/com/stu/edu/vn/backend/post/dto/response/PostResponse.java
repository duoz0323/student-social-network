package com.stu.edu.vn.backend.post.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response bài viết dùng cho API tạo bài, chỉ chứa dữ liệu cần hiển thị.
 */
public record PostResponse(
        Long id,
        String content,
        PostStatus status,
        boolean isEdited,
        int likeCount,
        int commentCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        PostAuthorResponse author,
        List<PostMediaResponse> media,
        List<String> hashtags
) {
}
