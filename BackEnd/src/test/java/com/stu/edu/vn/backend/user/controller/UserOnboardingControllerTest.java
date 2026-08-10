package com.stu.edu.vn.backend.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.user.dto.response.UsernameAvailabilityResponse;
import com.stu.edu.vn.backend.user.service.UserOnboardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserOnboardingControllerTest {

    private final UserOnboardingService service = org.mockito.Mockito.mock(UserOnboardingService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserOnboardingController(service)).build();
    }

    @Test
    void usernameAvailabilityUsesExistingResponseEnvelope() throws Exception {
        when(service.checkUsernameAvailability("DuOz_03"))
                .thenReturn(new UsernameAvailabilityResponse("duoz_03", true));

        mockMvc.perform(get("/api/v1/users/me/onboarding/username-availability")
                        .queryParam("username", "DuOz_03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("duoz_03"))
                .andExpect(jsonPath("$.data.available").value(true));
    }
}
