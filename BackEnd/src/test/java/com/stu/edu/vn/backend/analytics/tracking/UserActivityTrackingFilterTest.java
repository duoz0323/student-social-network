package com.stu.edu.vn.backend.analytics.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class UserActivityTrackingFilterTest {

    private final UserActivityTrackingFilter filter = new UserActivityTrackingFilter(
            org.mockito.Mockito.mock(UserActivityTrackingService.class), Clock.systemUTC());

    @Test
    void tracksRepresentativeBusinessRoutesButNotRefreshAdminOrBackground() {
        assertThat(matches("GET", "/api/v1/feeds/for-you")).isTrue();
        assertThat(matches("GET", "/api/v1/discovery/nearby")).isTrue();
        assertThat(matches("GET", "/api/v1/posts/15")).isTrue();
        assertThat(matches("POST", "/api/v1/posts/15/likes")).isTrue();
        assertThat(matches("POST", "/api/v1/users/9/follow")).isTrue();
        assertThat(matches("POST", "/api/v1/auth/refresh")).isFalse();
        assertThat(matches("GET", "/api/v1/admin/analytics/user-engagement/summary")).isFalse();
        assertThat(matches("GET", "/actuator/health")).isFalse();
    }

    private boolean matches(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        return filter.isRepresentativeActivity(request);
    }
}
