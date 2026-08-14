package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.time.LocalDateTime;

/** Major projection giữ Faculty ID để Frontend điều hướng hierarchy. */
public record AdminMajorResponse(
        Long id,
        Long facultyId,
        String name,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
