package com.stu.edu.vn.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.auth.dto.ReauthenticationRequest;
import com.stu.edu.vn.backend.auth.dto.ReauthenticationResponse;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.service.ReauthenticationService;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ReauthenticationControllerTest {

    private final ReauthenticationService service = org.mockito.Mockito.mock(ReauthenticationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ReauthenticationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @Test
    void successResponseUsesApiEnvelopeAndNoStoreWithoutLeakingSecrets() throws Exception {
        ReauthenticationRequest request = new ReauthenticationRequest(
                ReauthenticationMethod.PASSWORD, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.GOOGLE, "Password@1", null);
        when(service.reauthenticate(request)).thenReturn(new ReauthenticationResponse(
                "raw-reauth-token", ReauthenticationMethod.PASSWORD,
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE,
                LocalDateTime.of(2026, 7, 20, 10, 5), ReauthenticationChallengeStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/auth/reauthenticate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reauthenticationToken").value("raw-reauth-token"))
                .andExpect(jsonPath("$.data.method").value("PASSWORD"))
                .andExpect(jsonPath("$.data.purpose").value("UNLINK_AUTH_METHOD"))
                .andExpect(jsonPath("$.data.targetMethod").value("GOOGLE"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.providerCredential").doesNotExist())
                .andExpect(jsonPath("$.data.tokenHash").doesNotExist());
    }

    @Test
    void credentialMustMatchSelectedMethod() throws Exception {
        ReauthenticationRequest passwordWithProvider = new ReauthenticationRequest(
                ReauthenticationMethod.PASSWORD, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.EMAIL, "Password@1", "provider-token");
        ReauthenticationRequest providerWithPassword = new ReauthenticationRequest(
                ReauthenticationMethod.GOOGLE, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.PHONE, "Password@1", null);

        mockMvc.perform(post("/api/v1/auth/reauthenticate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(passwordWithProvider)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/auth/reauthenticate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(providerWithPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(service, never()).reauthenticate(any());
    }

    @Test
    void purposeTargetAndMethodAreRequiredAndEnumsRejectUnknownValues() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reauthenticate")
                        .contentType("application/json")
                        .content("{\"password\":\"Password@1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/auth/reauthenticate")
                        .contentType("application/json")
                        .content("{\"method\":\"OTP\",\"purpose\":\"CHANGE_PASSWORD\","
                                + "\"targetMethod\":\"UNKNOWN\",\"password\":\"Password@1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
