package com.stu.edu.vn.backend.admin.dto.request;

import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.enums.ModerationCaseAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request hành động kiểm duyệt; không nhận status hoặc resolvedBy từ Client. */
public record ResolveModerationCaseActionRequest(
        @NotNull(message = "Hành động xử lý không được để trống")
        ModerationCaseAction action,

        @NotNull(message = "Lý do ẩn bài viết không được để trống")
        AdminPostHideReason reasonCode,

        @Size(max = 1000, message = "Kết luận xử lý không được vượt quá 1000 ký tự")
        String resolutionNote
) {
}
