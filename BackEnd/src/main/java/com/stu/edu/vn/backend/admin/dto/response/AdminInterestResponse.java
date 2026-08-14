package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.time.LocalDateTime;

/** Interest Category projection dành cho bảng quản trị độc lập. */
public record AdminInterestResponse(
        Long id,
        String name,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
