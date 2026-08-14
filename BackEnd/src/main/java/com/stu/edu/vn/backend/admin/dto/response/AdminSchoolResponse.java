package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.time.LocalDateTime;

/** School projection dành cho bảng quản trị. */
public record AdminSchoolResponse(
        Long id,
        String name,
        String shortName,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
