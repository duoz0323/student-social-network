package com.stu.edu.vn.backend.admin.dto.request;

import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;

/** Request xác nhận report hợp lệ và tùy chọn ẩn bài viết bằng lý do cố định. */
public record AdminResolveReportRequest(
        String resolutionNote,
        Boolean hidePost,
        AdminPostHideReason hideReasonCode
) {
}
