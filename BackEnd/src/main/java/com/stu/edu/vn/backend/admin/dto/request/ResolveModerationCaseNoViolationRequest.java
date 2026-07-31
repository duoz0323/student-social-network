package com.stu.edu.vn.backend.admin.dto.request;

import jakarta.validation.constraints.Size;

/** Request kết luận bài viết không vi phạm; trạng thái cuối do Backend quyết định. */
public record ResolveModerationCaseNoViolationRequest(
        @Size(max = 1000, message = "Kết luận xử lý không được vượt quá 1000 ký tự")
        String resolutionNote
) {
}
