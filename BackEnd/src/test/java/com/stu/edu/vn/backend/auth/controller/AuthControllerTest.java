package com.stu.edu.vn.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.service.AuthService;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AuthControllerTest {

    private final AuthService authService = org.mockito.Mockito.mock(AuthService.class);
    private final ClientIpAddressResolver clientIpAddressResolver = new ClientIpAddressResolver();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, clientIpAddressResolver))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerReturnsCreatedApiResponse() throws Exception {
        RegisterResponse response = new RegisterResponse(
                1L,
                UserRole.USER,
                UserStatus.ACTIVE,
                false,
                RegisterResponse.ONBOARDING_PROFILE,
                "access-token",
                "refresh-token",
                900
        );
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "student@example.com",
                                "Password@1",
                                "Password@1"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.profileCompleted").value(false))
                .andExpect(jsonPath("$.data.nextStep").value("ONBOARDING_PROFILE"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900));
    }

    @Test
    void registerReturnsBadRequestWhenPayloadInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "not-an-identifier",
                                "short",
                                "different"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void loginReturnsOkApiResponse() throws Exception {
        LoginResponse response = new LoginResponse(
                "access-token",
                "refresh-token",
                LoginResponse.BEARER_TOKEN_TYPE,
                900,
                2_592_000,
                false,
                new LoginResponse.UserSummary(1L, UserRole.USER)
        );
        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", "203.0.113.10")
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "student@example.com",
                                "Password@1",
                                "device-1",
                                "Chrome on Windows"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(2_592_000))
                .andExpect(jsonPath("$.data.profileCompleted").value(false))
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andExpect(jsonPath("$.data.user.role").value("USER"));
    }

    @Test
    void loginReturnsBadRequestWhenPayloadInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "username-not-supported",
                                "",
                                "device-1",
                                "Chrome on Windows"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void refreshTokenReturnsOkApiResponse() throws Exception {
        RefreshTokenResponse response = new RefreshTokenResponse(
                "new-access-token",
                RefreshTokenResponse.BEARER_TOKEN_TYPE,
                900,
                true
        );
        when(authService.refreshAccessToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("raw-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900))
                .andExpect(jsonPath("$.data.profileCompleted").value(true));
    }

    @Test
    void refreshTokenReturnsBadRequestWhenPayloadInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("raw token with spaces"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void logoutReturnsOkApiResponse() throws Exception {
        when(authService.logout(any(LogoutRequest.class))).thenReturn(new LogoutResponse(true));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LogoutRequest("raw-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loggedOut").value(true));
    }

    @Test
    void logoutReturnsBadRequestWhenPayloadInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LogoutRequest("raw token with spaces"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
