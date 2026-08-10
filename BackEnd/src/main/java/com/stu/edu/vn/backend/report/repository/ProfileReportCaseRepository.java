package com.stu.edu.vn.backend.report.repository;

import com.stu.edu.vn.backend.report.entity.ProfileReportCase;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập vụ việc báo cáo trang cá nhân đã được gom theo tài khoản bị báo cáo. */
public interface ProfileReportCaseRepository extends JpaRepository<ProfileReportCase, Long> {

    Optional<ProfileReportCase> findByReportedUser_Id(Long reportedUserId);

    Page<ProfileReportCase> findByStatus(ReportStatus status, Pageable pageable);

    Page<ProfileReportCase> findByReportedDisplayNameSnapshotContainingIgnoreCase(
            String keyword, Pageable pageable);

    Page<ProfileReportCase> findByStatusAndReportedDisplayNameSnapshotContainingIgnoreCase(
            ReportStatus status, String keyword, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reportCase from ProfileReportCase reportCase where reportCase.id = :caseId")
    Optional<ProfileReportCase> findByIdForUpdate(@Param("caseId") Long caseId);
}
