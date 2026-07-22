package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;

/** Trạng thái hiện tại của bài bị báo cáo; bằng chứng lịch sử được tách riêng trong evidence. */
public record AdminReportedPostResponse(
        Long postId,
        PostStatus currentStatus,
        String currentContent,
        AdminReportUserResponse author,
        LocalDateTime hiddenAt,
        String hiddenReason,
        LocalDateTime deletedAt
) {
}
