package com.stu.edu.vn.backend.admin.dto.request;

/** Payload tạo/cập nhật School; shortName là thông tin tùy chọn. */
public record AdminAcademicSchoolRequest(String name, String shortName) {
}
