package com.stu.edu.vn.backend.report.repository;

import com.stu.edu.vn.backend.report.entity.ModerationCase;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository quản lý vòng đời Moderation Case và khóa case khi Admin ra quyết định. */
public interface ModerationCaseRepository extends JpaRepository<ModerationCase, Long> {

    Optional<ModerationCase> findByPost_IdAndStatus(Long postId, ModerationCaseStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mc from ModerationCase mc where mc.id = :caseId")
    Optional<ModerationCase> findByIdForUpdate(@Param("caseId") Long caseId);
}
