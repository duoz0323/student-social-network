package com.stu.edu.vn.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenSecret(SECRET);
        properties.setAccessTokenExpirationMillis(900_000);
        jwtService = new JwtService(properties);
    }

    @Test
    void generatedAccessTokenContainsExpectedUserId() {
        String token = jwtService.generateAccessToken(15L, "USER");

        assertThat(jwtService.extractUserIdFromAccessToken(token)).isEqualTo(15L);
    }

    @Test
    void tokenWithRefreshTypeCannotAuthenticateProtectedApi() {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("15")
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtService.extractUserIdFromAccessToken(token))
                .isInstanceOf(JwtException.class);
    }
}
