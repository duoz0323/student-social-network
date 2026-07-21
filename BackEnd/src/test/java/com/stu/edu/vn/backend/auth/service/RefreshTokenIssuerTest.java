package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.user.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefreshTokenIssuerTest {

    @Test
    void returnsRawTokenButPersistsOnlySha256HashAndNormalizedMetadata() {
        RefreshTokenRepository repository = org.mockito.Mockito.mock(RefreshTokenRepository.class);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        JwtProperties properties = new JwtProperties();
        properties.setRefreshTokenExpirationMillis(2_592_000_000L);
        Clock clock = Clock.fixed(Instant.parse("2026-07-19T03:00:00Z"), ZoneOffset.UTC);
        TokenHashService hashService = new TokenHashService();
        RefreshTokenIssuer issuer = new RefreshTokenIssuer(repository, hashService, properties, clock);
        User user = new User("student@example.com", "hash");

        IssuedRefreshToken result = issuer.issue(user, " device-1 ", " Chrome ", " 203.0.113.10 ");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).saveAndFlush(captor.capture());
        RefreshToken stored = captor.getValue();
        assertThat(result.rawToken()).isNotBlank().doesNotContain(".");
        assertThat(stored.getTokenHash()).isEqualTo(hashService.sha256Hex(result.rawToken())).hasSize(64);
        assertThat(stored.getTokenHash()).isNotEqualTo(result.rawToken());
        assertThat(stored.getDeviceId()).isEqualTo("device-1");
        assertThat(stored.getDeviceInfo()).isEqualTo("Chrome");
        assertThat(stored.getIpAddress()).isEqualTo("203.0.113.10");
        assertThat(stored.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 8, 18, 3, 0));
        assertThat(result.expiresInSeconds()).isEqualTo(2_592_000);
    }
}
