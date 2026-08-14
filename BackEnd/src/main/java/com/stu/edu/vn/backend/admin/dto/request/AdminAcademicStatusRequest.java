package com.stu.edu.vn.backend.admin.dto.request;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;

/** Payload đổi trạng thái master data, không nhận adminId hoặc cascade xuống bản ghi con. */
public record AdminAcademicStatusRequest(AcademicStatus status) {
}
