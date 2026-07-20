package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.AuthMethodLinkChallenge;
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

/** Repository cho challenge liên kết email/phone. */
public interface AuthMethodLinkChallengeRepository extends JpaRepository<AuthMethodLinkChallenge, Long> {

    Optional<AuthMethodLinkChallenge> findByFlowTokenHash(String flowTokenHash);

    Optional<AuthMethodLinkChallenge> findByActiveIdentifierKey(String activeIdentifierKey);

    Optional<AuthMethodLinkChallenge> findByActiveUserPurposeKey(String activeUserPurposeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from AuthMethodLinkChallenge challenge where challenge.id = :id")
    Optional<AuthMethodLinkChallenge> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from AuthMethodLinkChallenge challenge where challenge.flowTokenHash = :tokenHash")
    Optional<AuthMethodLinkChallenge> findByFlowTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from AuthMethodLinkChallenge challenge "
            + "where challenge.activeIdentifierKey = :activeKey")
    Optional<AuthMethodLinkChallenge> findByActiveIdentifierKeyForUpdate(@Param("activeKey") String activeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from AuthMethodLinkChallenge challenge "
            + "where challenge.activeUserPurposeKey = :activeKey")
    Optional<AuthMethodLinkChallenge> findByActiveUserPurposeKeyForUpdate(@Param("activeKey") String activeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from AuthMethodLinkChallenge challenge "
            + "where challenge.status = :status and challenge.expiresAt <= :now order by challenge.id")
    List<AuthMethodLinkChallenge> findExpiryBatchForUpdate(
            @Param("status") OtpChallengeStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from AuthMethodLinkChallenge challenge "
            + "where challenge.terminalAt is not null and challenge.terminalAt <= :cutoff order by challenge.id")
    List<AuthMethodLinkChallenge> findCleanupBatchForUpdate(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );
}
