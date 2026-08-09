package com.stu.edu.vn.backend.admin.dto.response;

import java.time.LocalDate;

/** Snapshot hồ sơ tại thời điểm USER gửi báo cáo. */
public record AdminProfileReportSnapshotResponse(
        String displayName,
        String avatarUrl,
        String bio,
        LocalDate dateOfBirth
) {
}
