package com.stu.edu.vn.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Service tạo và kiểm tra JWT bằng JJWT; không ghi token hoặc secret ra log.
 */
@Service
public class JwtService {

    private static final int MIN_HS256_SECRET_BYTES = 32;
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Long userId, String role) {
        return generateToken(
                String.valueOf(userId),
                Map.of("role", role, TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE),
                jwtProperties.getAccessTokenExpirationMillis(),
                jwtProperties.getAccessTokenSecret()
        );
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(
                String.valueOf(userId),
                Map.of(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE),
                jwtProperties.getRefreshTokenExpirationMillis(),
                jwtProperties.getRefreshTokenSecret()
        );
    }

    public boolean isAccessTokenValid(String token) {
        try {
            parseAccessTokenClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            parseRefreshTokenClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extractSubjectFromAccessToken(String token) {
        return parseAccessTokenClaims(token).getSubject();
    }

    public String extractSubjectFromRefreshToken(String token) {
        return parseRefreshTokenClaims(token).getSubject();
    }

    public Long extractUserIdFromAccessToken(String token) {
        String subject = parseAccessTokenClaims(token).getSubject();
        return Long.valueOf(subject);
    }

    public String extractRoleFromAccessToken(String token) {
        return parseAccessTokenClaims(token).get("role", String.class);
    }

    private String generateToken(
            String subject,
            Map<String, Object> claims,
            long expirationMillis,
            String secret
    ) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(toSecretKey(secret))
                .compact();
    }

    private Claims parseClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(toSecretKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims parseAccessTokenClaims(String token) {
        Claims claims = parseClaims(token, jwtProperties.getAccessTokenSecret());
        validateTokenType(claims, ACCESS_TOKEN_TYPE);
        return claims;
    }

    private Claims parseRefreshTokenClaims(String token) {
        Claims claims = parseClaims(token, jwtProperties.getRefreshTokenSecret());
        validateTokenType(claims, REFRESH_TOKEN_TYPE);
        return claims;
    }

    private void validateTokenType(Claims claims, String expectedType) {
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!expectedType.equals(tokenType)) {
            throw new JwtException("Invalid token type");
        }
    }

    private SecretKey toSecretKey(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_HS256_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
