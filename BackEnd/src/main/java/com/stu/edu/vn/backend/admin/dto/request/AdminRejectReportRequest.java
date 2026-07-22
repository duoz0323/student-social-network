package com.stu.edu.vn.backend.admin.dto.request;

/** Request từ chối một report; ADMIN hiện tại luôn được lấy từ SecurityContext. */
public record AdminRejectReportRequest(String resolutionNote) {
}
