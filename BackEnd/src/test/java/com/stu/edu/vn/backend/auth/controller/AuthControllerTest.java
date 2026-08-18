package com.stu.edu.vn.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthResponse;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.SocialConflictDetails;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.dto.RegistrationStatusResponse;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.auth.service.AuthService;
import com.stu.edu.vn.backend.auth.service.GoogleAuthService;
import com.stu.edu.vn.backend.auth.service.FacebookAuthService;
import com.stu.edu.vn.backend.auth.service.SocialConflictException;
import com.stu.edu.vn.backend.auth.service.RegistrationService;
import com.stu.edu.vn.backend.auth.service.RegistrationLifecycleService;
import com.stu.edu.vn.backend.auth.service.RegistrationVerificationService;
import com.stu.edu.vn.backend.auth.service.SocialConflictResolutionService;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.common.util.ClientIpAddressProperties;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AuthControllerTest {

    private final AuthService authService = org.mockito.Mockito.mock(AuthService.class);
    private final GoogleAuthService googleAuthService = org.mockito.Mockito.mock(GoogleAuthService.class);
    private final FacebookAuthService facebookAuthService = org.mockito.Mockito.mock(FacebookAuthService.class);
    private final RegistrationService registrationService = org.mockito.Mockito.mock(RegistrationService.class);
    private final RegistrationVerificationService registrationVerificationService =
            org.mockito.Mockito.mock(RegistrationVerificationService.class);
    private final RegistrationLifecycleService registrationLifecycleService =
            org.mockito.Mockito.mock(RegistrationLifecycleService.class);
    private final SocialConflictResolutionService socialConflictResolutionService =
            org.mockito.Mockito.mock(SocialConflictResolutionService.class);
    private final ClientIpAddressResolver clientIpAddressResolver =
            new ClientIpAddressResolver(new ClientIpAddressProperties());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(
                        authService,
                        googleAuthService,
                        facebookAuthService,
                        registrationService,
                        registrationVerificationService,
                        registrationLifecycleService,
                        socialConflictResolutionService,
                        clientIpAddressResolver
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void startRegistrationReturnsAcceptedApiResponseWithoutSensitiveHashes() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 10, 0);
        RegisterResponse response = new RegisterResponse(
                "raw-flow-token",
                OtpChallengeStatus.PENDING,
                "s***@example.com",
                now.plusMinutes(10),
                now.plusSeconds(60),
                now.plusHours(24),
                false
        );
        when(registrationService.start(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/registrations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "student@example.com",
                                "Password@1",
                                "Password@1"
                        ))))
                .andExpect(status().isAccepted())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.registrationFlowToken").value("raw-flow-token"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.identifierType").doesNotExist())
                .andExpect(jsonPath("$.data.maskedIdentifier").value("s***@example.com"))
                .andExpect(jsonPath("$.data.otpHash").doesNotExist())
                .andExpect(jsonPath("$.data.flowTokenHash").doesNotExist())
                .andExpect(jsonPath("$.data.accessToken").doesNotExist());
    }

    @Test
    void startRegistrationReturnsBusinessErrorForInvalidIdentifier() throws Exception {
        when(registrationService.start(any(RegisterRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.AUTH_IDENTIFIER_INVALID));

        mockMvc.perform(post("/api/v1/auth/registrations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "not-an-identifier",
                                "short",
                                "different"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_IDENTIFIER_INVALID"));
    }

    @Test
    void verifyRegistrationReturnsTokenResponseWithoutSensitiveData() throws Exception {
        VerifyRegistrationResponse response = new VerifyRegistrationResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900,
                2_592_000,
                false,
                "ONBOARDING",
                new VerifyRegistrationResponse.UserSummary(10L, UserRole.USER)
        );
        when(registrationVerificationService.verify(any(VerifyRegistrationRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/registrations/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyRegistrationRequest(
                                "raw-flow-token",
                                "123456",
                                "device-1",
                                "Chrome"
                        ))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.profileCompleted").value(false))
                .andExpect(jsonPath("$.data.nextStep").value("ONBOARDING"))
                .andExpect(jsonPath("$.data.user.id").value(10))
                .andExpect(jsonPath("$.data.otpHash").doesNotExist())
                .andExpect(jsonPath("$.data.flowToken").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void verifyRegistrationReturnsOtpInvalidError() throws Exception {
        when(registrationVerificationService.verify(any(VerifyRegistrationRequest.class), anyString()))
                .thenThrow(new BusinessException(ErrorCode.AUTH_OTP_INVALID));

        mockMvc.perform(post("/api/v1/auth/registrations/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyRegistrationRequest(
                                "raw-flow-token",
                                "wrong",
                                null,
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_OTP_INVALID"));
    }

    @Test
    void resendRegistrationReceivesFlowTokenInJsonBody() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 10, 0);
        when(registrationLifecycleService.resend(any(ResendRegistrationRequest.class)))
                .thenReturn(new ResendRegistrationResponse(
                        OtpChallengeStatus.PENDING,
                        "s***@example.com",
                        now.plusMinutes(10),
                        now.plusSeconds(60),
                        now.plusHours(24),
                        "Đã phát hành OTP mới"
                ));

        mockMvc.perform(post("/api/v1/auth/registrations/resend")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ResendRegistrationRequest("raw-flow-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.maskedIdentifier").value("s***@example.com"))
                .andExpect(jsonPath("$.data.registrationFlowToken").doesNotExist())
                .andExpect(jsonPath("$.data.otpHash").doesNotExist());
    }

    @Test
    void registrationStatusReceivesFlowTokenOnlyFromHeader() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 19, 10, 0);
        when(registrationLifecycleService.status("raw-flow-token"))
                .thenReturn(new RegistrationStatusResponse(
                        OtpChallengeStatus.PENDING,
                        "s***@example.com",
                        now.plusMinutes(10),
                        now.plusSeconds(60),
                        now.plusHours(24),
                        0,
                        OtpDeliveryStatus.SENT,
                        false,
                        5,
                        RegistrationStatusResponse.NEXT_STEP_VERIFY_OTP
                ));

        mockMvc.perform(get("/api/v1/auth/registrations/status")
                        .header("X-Auth-Flow-Token", "raw-flow-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.remainingOtpAttempts").value(5))
                .andExpect(jsonPath("$.data.nextStep").value("VERIFY_OTP"))
                .andExpect(jsonPath("$.data.flowTokenHash").doesNotExist());
    }

    @Test
    void cancelRegistrationIsReturnedAsTerminalStatus() throws Exception {
        LocalDateTime terminalAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        when(registrationLifecycleService.cancel(any(CancelRegistrationRequest.class)))
                .thenReturn(new CancelRegistrationResponse(
                        OtpChallengeStatus.CANCELLED,
                        terminalAt,
                        "Đã hủy đăng ký"
                ));

        mockMvc.perform(post("/api/v1/auth/registrations/cancel")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CancelRegistrationRequest("raw-flow-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.terminalAt").exists())
                .andExpect(jsonPath("$.data.registrationFlowToken").doesNotExist());
    }

    @Test
    void missingStatusFlowTokenReturnsSafeBusinessError() throws Exception {
        when(registrationLifecycleService.status(null))
                .thenThrow(new BusinessException(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID));

        mockMvc.perform(get("/api/v1/auth/registrations/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REGISTRATION_FLOW_INVALID"));
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
                "new-refresh-token",
                RefreshTokenResponse.BEARER_TOKEN_TYPE,
                900,
                2_592_000,
                true
        );
        when(authService.refreshAccessToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("raw-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(2592000))
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
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
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

    @Test
    void googleAuthReturnsSystemTokensWithNoStore() throws Exception {
        GoogleAuthResponse response = new GoogleAuthResponse(
                "system-access", "system-refresh", "Bearer", 900, 2_592_000,
                false, "COMPLETE_PROFILE", com.stu.edu.vn.backend.auth.enums.AuthProvider.GOOGLE,
                new GoogleAuthResponse.UserSummary(50L, UserRole.USER)
        );
        when(googleAuthService.authenticate(any(GoogleAuthRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GoogleAuthRequest(
                                "header.payload.signature", "device-1", "Chrome"
                        ))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.accessToken").value("system-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("system-refresh"))
                .andExpect(jsonPath("$.data.authenticationMethod").value("GOOGLE"))
                .andExpect(jsonPath("$.data.idToken").doesNotExist())
                .andExpect(jsonPath("$.data.providerUserId").doesNotExist());
    }

    @Test
    void googleAuthRejectsMissingIdToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType("application/json")
                        .content("{\"idToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void googleAuthConflictReturnsSafeNoStoreFlowDetails() throws Exception {
        SocialConflictDetails details = new SocialConflictDetails(
                "raw-conflict-flow-token",
                SocialConflictDetails.FLOW_TYPE,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                java.util.List.of(
                        SocialResolutionAction.LOGIN_EXISTING_ACCOUNT,
                        SocialResolutionAction.START_ACCOUNT_RECOVERY
                ),
                300
        );
        when(googleAuthService.authenticate(any(GoogleAuthRequest.class), anyString()))
                .thenThrow(new SocialConflictException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT, details));

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GoogleAuthRequest(
                                "header.payload.signature", "device-1", "Chrome"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.code").value("AUTH_SOCIAL_ACCOUNT_CONFLICT"))
                .andExpect(jsonPath("$.details.flowToken").value("raw-conflict-flow-token"))
                .andExpect(jsonPath("$.details.flowType").value("SOCIAL_CONFLICT"))
                .andExpect(jsonPath("$.details.conflictType")
                        .value("ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER"))
                .andExpect(jsonPath("$.details.providerUserId").doesNotExist())
                .andExpect(jsonPath("$.details.targetUserId").doesNotExist());
    }

    @Test
    void facebookAuthConflictReturnsSafeNoStoreFlowDetails() throws Exception {
        SocialConflictDetails details = new SocialConflictDetails(
                "raw-facebook-conflict-token",
                SocialConflictDetails.FLOW_TYPE,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                java.util.List.of(
                        SocialResolutionAction.LOGIN_EXISTING_ACCOUNT,
                        SocialResolutionAction.CONTINUE_WITH_SEPARATE_ACCOUNT
                ),
                300
        );
        when(facebookAuthService.authenticate(any(FacebookAuthRequest.class), anyString()))
                .thenThrow(new SocialConflictException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT, details));

        mockMvc.perform(post("/api/v1/auth/oauth/facebook")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new FacebookAuthRequest(
                                "facebook-access-token", "device-1", "Chrome"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.code").value("AUTH_SOCIAL_ACCOUNT_CONFLICT"))
                .andExpect(jsonPath("$.details.flowToken").value("raw-facebook-conflict-token"))
                .andExpect(jsonPath("$.details.flowType").value("SOCIAL_CONFLICT"))
                .andExpect(jsonPath("$.details.conflictType")
                        .value("ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER"))
                .andExpect(jsonPath("$.details.providerUserId").doesNotExist())
                .andExpect(jsonPath("$.details.targetUserId").doesNotExist());
    }
}

