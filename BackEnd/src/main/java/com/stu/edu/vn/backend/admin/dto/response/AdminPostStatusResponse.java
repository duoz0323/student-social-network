package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;

/** Trạng thái kiểm duyệt tối thiểu sau khi ADMIN ẩn hoặc khôi phục bài viết. */
public record AdminPostStatusResponse(
        Long postId,
        PostStatus status,
        LocalDateTime hiddenAt,
        String hiddenReason,
        AdminPostHiddenByResponse hiddenBy,
        LocalDateTime updatedAt
) {
}
