package com.stu.edu.vn.backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ghi chú bắt buộc giúp kết luận báo cáo hồ sơ có thể được kiểm tra lại. */
public record AdminProfileReportResolutionRequest(
        @NotBlank(message = "Ghi chú xử lý không được để trống")
        @Size(max = 500, message = "Ghi chú xử lý không được vượt quá 500 ký tự")
        String resolutionNote,
        Boolean blockUser
) {
    public AdminProfileReportResolutionRequest(String resolutionNote) {
        this(resolutionNote, false);
    }

    public boolean shouldBlockUser() {
        return Boolean.TRUE.equals(blockUser);
    }
}
