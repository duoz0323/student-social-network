package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository cho social conflict token một lần. */
public interface SocialAuthChallengeRepository extends JpaRepository<SocialAuthChallenge, Long> {

    Optional<SocialAuthChallenge> findByConflictTokenHash(String conflictTokenHash);

    Optional<SocialAuthChallenge> findByActiveProviderKey(String activeProviderKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from SocialAuthChallenge challenge where challenge.conflictTokenHash = :tokenHash")
    Optional<SocialAuthChallenge> findByConflictTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from SocialAuthChallenge challenge "
            + "where challenge.activeProviderKey = :activeKey")
    Optional<SocialAuthChallenge> findByActiveProviderKeyForUpdate(@Param("activeKey") String activeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from SocialAuthChallenge challenge "
            + "where challenge.status = :status and challenge.expiresAt <= :now order by challenge.id")
    List<SocialAuthChallenge> findExpiryBatchForUpdate(
            @Param("status") SocialAuthChallengeStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from SocialAuthChallenge challenge "
            + "where challenge.terminalAt is not null and challenge.terminalAt <= :cutoff order by challenge.id")
    List<SocialAuthChallenge> findCleanupBatchForUpdate(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );
}
