package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

/** Chi tiết bài viết phục vụ quản trị, không phơi bày Entity hoặc dữ liệu lưu trữ nội bộ. */
public record AdminPostDetailResponse(Long postId, String content, PostStatus status,
        AdminPostAuthorResponse author, List<AdminPostMediaResponse> media, String hashtag,
        int likeCount, int commentCount, long pendingReportCount, long totalReportCount,
        LocalDateTime hiddenAt, String hiddenReason, AdminPostHiddenByResponse hiddenBy,
        LocalDateTime deletedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
