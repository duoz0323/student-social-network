package com.stu.edu.vn.backend.report.dto.request;

import com.stu.edu.vn.backend.report.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request tạo báo cáo chỉ nhận dữ liệu người dùng được phép cung cấp, không nhận reporter hoặc trạng thái xử lý.
 */
public record CreateReportRequest(
        @NotNull(message = "Lý do báo cáo không được để trống")
        ReportReason reason,

        @Size(max = 1000, message = "Mô tả báo cáo không được vượt quá 1000 ký tự")
        String description
) {
}
