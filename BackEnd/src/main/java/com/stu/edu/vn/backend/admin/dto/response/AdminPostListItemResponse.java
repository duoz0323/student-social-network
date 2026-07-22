package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;

/** Dữ liệu tóm tắt một bài viết dành riêng cho màn hình quản trị. */
public record AdminPostListItemResponse(Long postId, String contentPreview, PostStatus status,
        Long authorId, String authorDisplayName, String authorAvatarUrl, UserStatus authorAccountStatus,
        String thumbnailUrl, long mediaCount, int likeCount, int commentCount, long pendingReportCount,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
}
