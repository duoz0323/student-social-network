package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;

/** Trạng thái Post tối thiểu sau khi xử lý Report. */
public record AdminReportStatusPostResponse(
        Long postId,
        PostStatus status,
        LocalDateTime hiddenAt,
        String hiddenReason
) {
}
