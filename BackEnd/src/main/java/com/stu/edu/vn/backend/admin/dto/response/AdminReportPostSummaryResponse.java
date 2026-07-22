package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.user.enums.UserStatus;

/** Bài viết rút gọn trong danh sách báo cáo, kết hợp trạng thái hiện tại và nội dung snapshot. */
public record AdminReportPostSummaryResponse(
        Long postId,
        PostStatus currentStatus,
        String contentPreview,
        Long authorId,
        String authorDisplayName,
        UserStatus authorAccountStatus
) {
}
