package com.stu.edu.vn.backend.admin.dto.response;

import java.util.List;

/** Bằng chứng bất biến đã chụp tại thời điểm report được tạo. */
public record AdminReportEvidenceResponse(
        String contentSnapshot,
        List<String> mediaSnapshot
) {
}
