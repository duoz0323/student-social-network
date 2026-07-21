package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý refresh token hash để hỗ trợ thu hồi phiên đăng nhập.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Thu hồi mọi phiên còn hoạt động trong cùng transaction hoàn tất reset mật khẩu. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken token set token.revokedAt = :now where token.user.id = :userId and token.revokedAt is null")
    int revokeAllActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Khóa phiên trong transaction để hai request không thể refresh hoặc logout cùng một token đồng thời.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.expiresAt <= :now order by token.id")
    List<RefreshToken> findExpiredBatchForUpdate(@Param("now") LocalDateTime now, Pageable pageable);
}
