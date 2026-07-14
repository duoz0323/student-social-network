package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý refresh token hash để hỗ trợ thu hồi phiên đăng nhập.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

    /**
     * Thu hồi hàng loạt duy nhất các Refresh Token chưa bị thu hồi và còn hạn của một tài khoản.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            update RefreshToken token
            set token.revokedAt = :revokedAt
            where token.user.id = :userId
              and token.revokedAt is null
              and token.expiresAt > :revokedAt
            """)
    int revokeActiveTokensByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}
