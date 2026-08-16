package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Phát hành Refresh Token opaque và chỉ lưu SHA-256 hash trong database. */
@Service
@RequiredArgsConstructor
public class RefreshTokenIssuer {

    private static final int RANDOM_BYTES = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public IssuedRefreshToken issue(User user, String deviceId, String deviceInfo, String ipAddress) {
        // Managed Social Identity không sở hữu phiên đăng nhập và chỉ được điều khiển qua Collaborator API.
        if (user.getAccountType() == UserAccountType.MANAGED) {
            throw new BusinessException(ErrorCode.MANAGED_ACCOUNT_LOGIN_FORBIDDEN);
        }
        byte[] randomBytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        LocalDateTime expiresAt = LocalDateTime.now(clock)
                .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()));

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawToken),
                normalizeMetadata(deviceId),
                normalizeMetadata(deviceInfo),
                normalizeMetadata(ipAddress),
                expiresAt
        );
        refreshTokenRepository.saveAndFlush(refreshToken);
        return new IssuedRefreshToken(rawToken, refreshTokenExpiresInSeconds());
    }

    private long refreshTokenExpiresInSeconds() {
        return Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()).toSeconds();
    }

    private String normalizeMetadata(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
