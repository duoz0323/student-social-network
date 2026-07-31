package com.stu.edu.vn.backend.report.repository;

import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập báo cáo và kiểm tra trùng PENDING trước khi ghi dữ liệu.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporter_IdAndPost_IdAndStatus(Long reporterId, Long postId, ReportStatus status);

    @Query("""
            select (count(r) > 0) from Report r
            where r.reporter.id = :reporterId
              and r.post.id = :postId
              and r.moderationCase.status = :caseStatus
            """)
    boolean existsEffectiveReport(
            @Param("reporterId") Long reporterId,
            @Param("postId") Long postId,
            @Param("caseStatus") ModerationCaseStatus caseStatus);

    @EntityGraph(attributePaths = {"reporter"})
    List<Report> findByModerationCase_IdOrderByCreatedAtDescIdDesc(Long moderationCaseId);
}
