package com.stu.edu.vn.backend.auth.repository;

import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý refresh token hash để hỗ trợ thu hồi phiên đăng nhập.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);
}
