package com.stu.edu.vn.backend.report.repository;

import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository truy cập báo cáo và kiểm tra trùng PENDING trước khi ghi dữ liệu.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporter_IdAndPost_IdAndStatus(Long reporterId, Long postId, ReportStatus status);
}
