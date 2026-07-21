package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.PasswordRecoveryChallenge;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy vấn challenge bằng token hash và khóa bản ghi trong các state transition. */
public interface PasswordRecoveryChallengeRepository extends JpaRepository<PasswordRecoveryChallenge, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PasswordRecoveryChallenge c where c.recoveryFlowTokenHash = :hash")
    Optional<PasswordRecoveryChallenge> findByFlowHashForUpdate(@Param("hash") String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PasswordRecoveryChallenge c where c.resetTokenHash = :hash")
    Optional<PasswordRecoveryChallenge> findByResetHashForUpdate(@Param("hash") String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PasswordRecoveryChallenge c where c.activeSubjectKeyHash = :hash")
    Optional<PasswordRecoveryChallenge> findActiveBySubjectForUpdate(@Param("hash") String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PasswordRecoveryChallenge c where c.id = :id and c.otpVersion = :otpVersion")
    Optional<PasswordRecoveryChallenge> findByIdAndOtpVersion(@Param("id") Long id, @Param("otpVersion") int otpVersion);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PasswordRecoveryChallenge c where c.status in :statuses and c.challengeExpiresAt <= :now order by c.id")
    List<PasswordRecoveryChallenge> findExpiryBatchForUpdate(@Param("statuses") List<com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus> statuses,
            @Param("now") LocalDateTime now, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PasswordRecoveryChallenge c where c.status in :statuses and c.updatedAt < :cutoff order by c.id")
    List<PasswordRecoveryChallenge> findCleanupBatchForUpdate(@Param("statuses") List<com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus> statuses,
            @Param("cutoff") LocalDateTime cutoff, Pageable pageable);
}
