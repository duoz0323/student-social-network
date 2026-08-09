package com.stu.edu.vn.backend.report.repository;

import com.stu.edu.vn.backend.report.entity.ProfileReport;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập báo cáo hồ sơ và khóa báo cáo trước khi Admin kết luận. */
public interface ProfileReportRepository extends JpaRepository<ProfileReport, Long> {

    boolean existsByReporter_IdAndReportedUser_IdAndStatus(
            Long reporterId,
            Long reportedUserId,
            ReportStatus status
    );

    List<ProfileReport> findAllByReportCase_IdOrderByCreatedAtAscIdAsc(Long caseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select report from ProfileReport report where report.reportCase.id = :caseId and report.status = :status")
    List<ProfileReport> findAllByCaseIdAndStatusForUpdate(
            @Param("caseId") Long caseId, @Param("status") ReportStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select report from ProfileReport report where report.id = :reportId")
    Optional<ProfileReport> findByIdForUpdate(@Param("reportId") Long reportId);
}
