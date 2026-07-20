package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository cho vòng đời đăng ký local đang chờ OTP. */
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByFlowTokenHash(String flowTokenHash);

    Optional<PendingRegistration> findByActiveIdentifierKey(String activeIdentifierKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from PendingRegistration registration where registration.flowTokenHash = :tokenHash")
    Optional<PendingRegistration> findByFlowTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from PendingRegistration registration where registration.id = :id")
    Optional<PendingRegistration> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from PendingRegistration registration "
            + "where registration.activeIdentifierKey = :activeKey")
    Optional<PendingRegistration> findByActiveIdentifierKeyForUpdate(@Param("activeKey") String activeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from PendingRegistration registration "
            + "where registration.status = :status and registration.expiresAt <= :now order by registration.id")
    List<PendingRegistration> findExpiryBatchForUpdate(
            @Param("status") OtpChallengeStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from PendingRegistration registration "
            + "where registration.terminalAt is not null and registration.terminalAt <= :cutoff order by registration.id")
    List<PendingRegistration> findCleanupBatchForUpdate(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from PendingRegistration registration "
            + "where registration.status in :statuses and registration.terminalAt <= :cutoff order by registration.id")
    List<PendingRegistration> findCleanupBatchForUpdate(
            @Param("statuses") java.util.Collection<OtpChallengeStatus> statuses,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );
}
