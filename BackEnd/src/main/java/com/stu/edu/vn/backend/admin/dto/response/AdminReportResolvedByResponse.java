package com.stu.edu.vn.backend.admin.dto.response;

/** ADMIN đã xử lý báo cáo; không trả email, token hoặc thông tin xác thực. */
public record AdminReportResolvedByResponse(Long adminId, String displayName) {
}
