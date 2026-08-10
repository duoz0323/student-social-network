package com.stu.edu.vn.backend.user.dto.request;

/**
 * Khối academic tùy chọn: bỏ toàn bộ object để giữ nguyên; gửi object với ID null để xóa liên kết.
 */
public record AcademicProfileRequest(
        Long schoolId,
        Long facultyId,
        Long majorId,
        Integer entryYear
) {
}
