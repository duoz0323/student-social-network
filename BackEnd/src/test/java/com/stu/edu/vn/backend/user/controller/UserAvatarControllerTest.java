package com.stu.edu.vn.backend.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.user.dto.response.AvatarResponse;
import com.stu.edu.vn.backend.user.service.UserAvatarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserAvatarControllerTest {

    private final UserAvatarService userAvatarService = org.mockito.Mockito.mock(UserAvatarService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserAvatarController(userAvatarService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadMyAvatarReturnsApiResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1});
        when(userAvatarService.uploadMyAvatar(any())).thenReturn(new AvatarResponse("https://cdn.example/avatar.png", true));

        mockMvc.perform(multipart("/api/v1/users/me/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.example/avatar.png"))
                .andExpect(jsonPath("$.data.profileCompleted").value(true));
    }

    @Test
    void uploadMyAvatarReturnsFileRequiredWhenFilePartMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/users/me/avatar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AVATAR_FILE_REQUIRED"));
    }
}
