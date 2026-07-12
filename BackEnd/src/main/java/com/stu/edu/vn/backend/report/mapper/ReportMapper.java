package com.stu.edu.vn.backend.report.mapper;

import com.stu.edu.vn.backend.report.dto.response.CreateReportResponse;
import com.stu.edu.vn.backend.report.entity.Report;
import org.springframework.stereotype.Component;

/**
 * Mapper giới hạn dữ liệu report được phép trả cho API người dùng.
 */
@Component
public class ReportMapper {

    public CreateReportResponse toCreateResponse(Report report) {
        return new CreateReportResponse(
                report.getId(),
                report.getPost().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
