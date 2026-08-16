package com.stu.edu.vn.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service tạo và kiểm tra JWT bằng JJWT; không ghi token hoặc secret ra log.
 */
@Service
public class JwtService {

    private static final int MIN_HS256_SECRET_BYTES = 32;
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String ADMIN_ROLES_CLAIM = "adminRoles";
    private static final String PERMISSIONS_CLAIM = "permissions";

    private final JwtProperties jwtProperties;
    private final AdminAuthorityResolver adminAuthorityResolver;

    @Autowired
    public JwtService(JwtProperties jwtProperties, AdminAuthorityResolver adminAuthorityResolver) {
        this.jwtProperties = jwtProperties;
        this.adminAuthorityResolver = adminAuthorityResolver;
    }

    /** Constructor giữ tương thích cho unit test thuần không khởi tạo Spring context. */
    public JwtService(JwtProperties jwtProperties) {
        this(jwtProperties, null);
    }

    public String generateAccessToken(Long userId, String role) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("role", role);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        if ("ADMIN".equals(role)) {
            AdminAuthorization authorization = adminAuthorityResolver == null
                    ? new AdminAuthorization(Set.of(), Set.of())
                    : adminAuthorityResolver.resolve(userId);
            claims.put(ADMIN_ROLES_CLAIM, authorization.roles());
            claims.put(PERMISSIONS_CLAIM, authorization.permissions());
        }
        return generateToken(
                String.valueOf(userId),
                claims,
                jwtProperties.getAccessTokenExpirationMillis(),
                jwtProperties.getAccessTokenSecret()
        );
    }

    public Long extractUserIdFromAccessToken(String token) {
        String subject = parseAccessTokenClaims(token).getSubject();
        return Long.valueOf(subject);
    }

    /**
     * Đọc snapshot đã được ký; trả null với token cũ chưa có claims RBAC để duy trì phiên đến khi hết hạn.
     */
    public AdminAuthorization extractAdminAuthorizationFromAccessToken(String token) {
        Claims claims = parseAccessTokenClaims(token);
        if (!claims.containsKey(ADMIN_ROLES_CLAIM) || !claims.containsKey(PERMISSIONS_CLAIM)) {
            return null;
        }
        return new AdminAuthorization(
                toStringSet(claims.get(ADMIN_ROLES_CLAIM, List.class)),
                toStringSet(claims.get(PERMISSIONS_CLAIM, List.class))
        );
    }

    private Set<String> toStringSet(List<?> values) {
        if (values == null) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        values.stream().filter(String.class::isInstance).map(String.class::cast).forEach(result::add);
        return result;
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
        Claims claims =     parseClaims(token, jwtProperties.getAccessTokenSecret());
        validateTokenType(claims, ACCESS_TOKEN_TYPE);
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
