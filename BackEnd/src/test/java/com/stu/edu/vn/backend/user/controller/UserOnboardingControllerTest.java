package com.stu.edu.vn.backend.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.service.UserOnboardingService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

class UserOnboardingControllerTest {

    private final UserOnboardingService userOnboardingService = org.mockito.Mockito.mock(UserOnboardingService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserOnboardingController(userOnboardingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void completeOnboardingWithoutAvatarReturnsApiResponse() throws Exception {
        when(userOnboardingService.completeOnboarding(any(CompleteOnboardingRequest.class), isNull()))
                .thenReturn(response(null));

        mockMvc.perform(putMultipart("/api/v1/users/me/onboarding").file(requestPart(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data.avatarUrl").doesNotExist())
                .andExpect(jsonPath("$.data.profileCompleted").value(true));
    }

    @Test
    void completeOnboardingWithAvatarPassesAvatarToService() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[]{1});
        when(userOnboardingService.completeOnboarding(any(CompleteOnboardingRequest.class), any(MultipartFile.class)))
                .thenReturn(response("https://cdn.example/avatar.png"));

        mockMvc.perform(putMultipart("/api/v1/users/me/onboarding")
                        .file(requestPart(validRequest()))
                        .file(avatar))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.example/avatar.png"));

        verify(userOnboardingService).completeOnboarding(any(CompleteOnboardingRequest.class), any(MultipartFile.class));
    }

    @Test
    void completeOnboardingReturnsAvatarValidationErrorFromService() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.txt", "text/plain", new byte[]{1});
        when(userOnboardingService.completeOnboarding(any(CompleteOnboardingRequest.class), any(MultipartFile.class)))
                .thenThrow(new BusinessException(ErrorCode.AVATAR_FILE_TYPE_NOT_ALLOWED));

        mockMvc.perform(putMultipart("/api/v1/users/me/onboarding")
                        .file(requestPart(validRequest()))
                        .file(avatar))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AVATAR_FILE_TYPE_NOT_ALLOWED"));
    }

    private MockMultipartFile requestPart(CompleteOnboardingRequest request) throws Exception {
        return new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                        {"displayName":"%s","dateOfBirth":"%s","bio":"%s"}"""
                        .formatted(request.displayName(), request.dateOfBirth(), request.bio())
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private CompleteOnboardingRequest validRequest() {
        return new CompleteOnboardingRequest("Nguyen Van A", LocalDate.of(2000, 1, 1), "Sinh vien");
    }

    private CompleteOnboardingResponse response(String avatarUrl) {
        return new CompleteOnboardingResponse(
                "Nguyen Van A",
                avatarUrl,
                LocalDate.of(2000, 1, 1),
                "Sinh vien",
                true,
                "FEED"
        );
    }

    private MockMultipartHttpServletRequestBuilder putMultipart(String path) {
        return multipart(path).with(request -> {
            request.setMethod("PUT");
            return request;
        });
    }
}
