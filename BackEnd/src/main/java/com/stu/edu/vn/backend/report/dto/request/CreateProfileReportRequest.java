package com.stu.edu.vn.backend.report.dto.request;

import com.stu.edu.vn.backend.report.enums.ProfileReportReason;
import jakarta.validation.constraints.NotNull;

/** Request chỉ nhận lý do; reporter luôn được lấy từ JWT hiện tại. */
public record CreateProfileReportRequest(
        @NotNull(message = "Lý do báo cáo không được để trống")
        ProfileReportReason reason
) {
}
