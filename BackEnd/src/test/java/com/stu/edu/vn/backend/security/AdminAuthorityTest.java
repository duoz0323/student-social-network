package com.stu.edu.vn.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import org.junit.jupiter.api.Test;
import java.util.Set;

class AdminAuthorityTest {

    @Test
    void adminPrincipalUsesRoleAdminAuthority() {
        CustomUserPrincipal principal = new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE);

        assertThat(principal.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void adminAccessTokenContainsSignedRolesAndPermissions() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenSecret("access-token-secret-at-least-32-bytes-long");
        properties.setAccessTokenExpirationMillis(900_000L);
        properties.setRefreshTokenExpirationMillis(2_592_000_000L);
        JwtService jwtService = new JwtService(properties, adminId -> new AdminAuthorization(
                Set.of("MODERATOR", "COLLABORATOR"),
                Set.of("POST_VIEW", "POST_HIDE", "REPORT_VIEW")));

        String accessToken = jwtService.generateAccessToken(1L, UserRole.ADMIN.name());

        assertThat(jwtService.extractUserIdFromAccessToken(accessToken)).isEqualTo(1L);
        AdminAuthorization authorization = jwtService.extractAdminAuthorizationFromAccessToken(accessToken);
        assertThat(authorization.roles()).containsExactlyInAnyOrder("MODERATOR", "COLLABORATOR");
        assertThat(authorization.permissions()).containsExactlyInAnyOrder(
                "POST_VIEW", "POST_HIDE", "REPORT_VIEW");
    }

    @Test
    void principalUsesExactPermissionSnapshot() {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                2L, UserRole.ADMIN, UserStatus.ACTIVE, Set.of("ADS_MANAGER"), Set.of());

        assertThat(principal.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ADMIN_ROLE_ADS_MANAGER");
    }
}
