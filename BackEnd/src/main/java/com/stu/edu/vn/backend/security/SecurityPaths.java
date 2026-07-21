package com.stu.edu.vn.backend.security;

import java.util.Set;
import org.springframework.http.HttpMethod;

/**
 * Nguồn duy nhất khai báo các endpoint Auth public để SecurityConfig và JWT filter không bị lệch nhau.
 */
final class SecurityPaths {

    static final String REGISTRATIONS = "/api/v1/auth/registrations";
    static final String VERIFY_REGISTRATION = "/api/v1/auth/registrations/verify";
    static final String RESEND_REGISTRATION = "/api/v1/auth/registrations/resend";
    static final String REGISTRATION_STATUS = "/api/v1/auth/registrations/status";
    static final String CANCEL_REGISTRATION = "/api/v1/auth/registrations/cancel";
    static final String LOGIN = "/api/v1/auth/login";
    static final String REFRESH_TOKEN = "/api/v1/auth/refresh-token";
    static final String LOGOUT = "/api/v1/auth/logout";
    static final String GOOGLE_AUTH = "/api/v1/auth/oauth/google";
    static final String FACEBOOK_AUTH = "/api/v1/auth/oauth/facebook";
    static final String RESOLVE_SOCIAL_CONFLICT = "/api/v1/auth/registrations/resolve-social-conflict";
    static final String PASSWORD_RECOVERY = "/api/v1/auth/password-recovery";
    static final String PASSWORD_RECOVERY_VERIFY = PASSWORD_RECOVERY + "/verify";
    static final String PASSWORD_RECOVERY_RESEND = PASSWORD_RECOVERY + "/resend";
    static final String PASSWORD_RECOVERY_COMPLETE = PASSWORD_RECOVERY + "/complete";
    static final Set<String> PUBLIC_POST_AUTH_ENDPOINTS = Set.of(
            REGISTRATIONS,
            VERIFY_REGISTRATION,
            RESEND_REGISTRATION,
            CANCEL_REGISTRATION,
            LOGIN,
            REFRESH_TOKEN,
            LOGOUT,
            GOOGLE_AUTH,
            FACEBOOK_AUTH,
            RESOLVE_SOCIAL_CONFLICT,
            PASSWORD_RECOVERY, PASSWORD_RECOVERY_VERIFY, PASSWORD_RECOVERY_RESEND, PASSWORD_RECOVERY_COMPLETE
    );
    static final Set<String> PUBLIC_GET_AUTH_ENDPOINTS = Set.of(REGISTRATION_STATUS);

    static boolean isPublic(String method, String requestUri) {
        return (HttpMethod.POST.matches(method) && PUBLIC_POST_AUTH_ENDPOINTS.contains(requestUri))
                || (HttpMethod.GET.matches(method) && PUBLIC_GET_AUTH_ENDPOINTS.contains(requestUri));
    }

    private SecurityPaths() {
    }
}
