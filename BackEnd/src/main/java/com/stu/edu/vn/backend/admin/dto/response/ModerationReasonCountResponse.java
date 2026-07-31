package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportReason;

/** Thống kê một lý do trong Moderation Case. */
public record ModerationReasonCountResponse(ReportReason reason, long count) {
}
