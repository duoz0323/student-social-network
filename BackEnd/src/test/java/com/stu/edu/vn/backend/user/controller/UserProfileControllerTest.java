package com.stu.edu.vn.backend.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.user.dto.response.UserProfileViewResponse;
import com.stu.edu.vn.backend.user.service.UserProfileService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserProfileControllerTest {

    private final UserProfileService userProfileService = org.mockito.Mockito.mock(UserProfileService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserProfileController(userProfileService)).build();
    }

    @Test
    void getMyProfileReturnsProfileResolvedByService() throws Exception {
        when(userProfileService.getMyProfile()).thenReturn(profile(10L, false));

        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.displayName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.data.followerCount").value(4));
    }

    @Test
    void getPublicProfilePassesRouteUserId() throws Exception {
        when(userProfileService.getPublicProfile(20L)).thenReturn(profile(20L, true));

        mockMvc.perform(get("/api/v1/users/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(20))
                .andExpect(jsonPath("$.data.followedByCurrentUser").value(true));
    }

    private UserProfileViewResponse profile(Long userId, boolean followed) {
        return new UserProfileViewResponse(
                userId,
                "nguyenvana",
                "Nguyễn Văn A",
                null,
                LocalDate.of(2000, 1, 1),
                "Sinh viên",
                4,
                3,
                followed,
                false,
                false
        );
    }
}
