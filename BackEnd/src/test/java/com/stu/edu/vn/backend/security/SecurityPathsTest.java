package com.stu.edu.vn.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SecurityPathsTest {

    @Test
    void registrationLifecycleAndLocalLoginEndpointsArePublic() {
        assertThat(SecurityPaths.PUBLIC_POST_AUTH_ENDPOINTS)
                .isEqualTo(Set.of(
                        "/api/v1/auth/registrations",
                        "/api/v1/auth/registrations/verify",
                        "/api/v1/auth/registrations/resend",
                        "/api/v1/auth/registrations/cancel",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh-token",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/oauth/google",
                        "/api/v1/auth/oauth/facebook",
                        "/api/v1/auth/registrations/resolve-social-conflict",
                        "/api/v1/auth/password-recovery",
                        "/api/v1/auth/password-recovery/verify",
                        "/api/v1/auth/password-recovery/resend",
                        "/api/v1/auth/password-recovery/complete"
                ))
                .doesNotContain(
                        "/api/v1/auth/register"
                );
        assertThat(SecurityPaths.PUBLIC_GET_AUTH_ENDPOINTS)
                .containsExactlyInAnyOrder("/health", "/api/v1/auth/registrations/status");
        assertThat(SecurityPaths.isPublic("GET", "/health")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/health")).isFalse();
        assertThat(SecurityPaths.isPublic("GET", "/api/v1/auth/registrations/status")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/login")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/refresh-token")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/logout")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/oauth/google")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/oauth/facebook")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/registrations/resolve-social-conflict")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/password-recovery")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/password-recovery/complete")).isTrue();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/registrations/status")).isFalse();
        assertThat(SecurityPaths.isPublic("GET", "/api/v1/users/me/auth-providers")).isFalse();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/users/me/auth-providers/email")).isFalse();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/users/me/auth-providers/google")).isFalse();
        assertThat(SecurityPaths.isPublic("POST", "/api/v1/auth/reauthenticate")).isFalse();
    }
}

