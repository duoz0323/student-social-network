package com.stu.edu.vn.backend.report.enums;

/**
 * Trạng thái vòng đời báo cáo; API người dùng chỉ được tạo trạng thái PENDING.
 */
public enum ReportStatus {
    PENDING,
    RESOLVED,
    REJECTED
}
