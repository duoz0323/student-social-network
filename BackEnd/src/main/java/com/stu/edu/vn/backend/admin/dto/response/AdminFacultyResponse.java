package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import java.time.LocalDateTime;

/** Faculty projection giữ School ID để Frontend điều hướng hierarchy. */
public record AdminFacultyResponse(
        Long id,
        Long schoolId,
        String name,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
