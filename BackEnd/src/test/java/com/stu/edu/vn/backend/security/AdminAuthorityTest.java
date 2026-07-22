package com.stu.edu.vn.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import org.junit.jupiter.api.Test;

class AdminAuthorityTest {

    @Test
    void adminPrincipalUsesRoleAdminAuthority() {
        CustomUserPrincipal principal = new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE);

        assertThat(principal.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void adminAccessTokenContainsAdminRoleClaim() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenSecret("access-token-secret-at-least-32-bytes-long");
        properties.setRefreshTokenSecret("refresh-token-secret-at-least-32-bytes-long");
        properties.setAccessTokenExpirationMillis(900_000L);
        properties.setRefreshTokenExpirationMillis(2_592_000_000L);
        JwtService jwtService = new JwtService(properties);

        String accessToken = jwtService.generateAccessToken(1L, UserRole.ADMIN.name());

        assertThat(jwtService.extractUserIdFromAccessToken(accessToken)).isEqualTo(1L);
        assertThat(jwtService.extractRoleFromAccessToken(accessToken)).isEqualTo("ADMIN");
    }
}
