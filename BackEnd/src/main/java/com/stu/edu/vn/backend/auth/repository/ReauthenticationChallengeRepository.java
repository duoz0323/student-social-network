package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository cho challenge xác thực lại trước thao tác nhạy cảm. */
public interface ReauthenticationChallengeRepository extends JpaRepository<ReauthenticationChallenge, Long> {

    Optional<ReauthenticationChallenge> findByTokenHash(String tokenHash);

    Optional<ReauthenticationChallenge> findByActiveUserScopeKey(String activeUserScopeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from ReauthenticationChallenge challenge where challenge.tokenHash = :tokenHash")
    Optional<ReauthenticationChallenge> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from ReauthenticationChallenge challenge "
            + "where challenge.activeUserScopeKey = :activeKey")
    Optional<ReauthenticationChallenge> findByActiveUserScopeKeyForUpdate(@Param("activeKey") String activeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from ReauthenticationChallenge challenge "
            + "where challenge.status = :status and challenge.expiresAt <= :now order by challenge.id")
    List<ReauthenticationChallenge> findExpiryBatchForUpdate(
            @Param("status") ReauthenticationChallengeStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from ReauthenticationChallenge challenge "
            + "where challenge.terminalAt is not null and challenge.terminalAt <= :cutoff order by challenge.id")
    List<ReauthenticationChallenge> findCleanupBatchForUpdate(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );
}
