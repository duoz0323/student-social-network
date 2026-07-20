package com.stu.edu.vn.backend.auth.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.service.AuthMethodManagementService;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthMethodControllerUnlinkTest {
    private final AuthMethodManagementService service =
            org.mockito.Mockito.mock(AuthMethodManagementService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthMethodController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void successReturnsNoContentAndNoStore() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/auth-providers/GOOGLE")
                        .header("X-Auth-Flow-Token", "reauth-token"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(service).unlink(AuthMethod.GOOGLE, "reauth-token");
    }

    @Test
    void unknownProviderIsRejectedBeforeService() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/auth-providers/TWITTER")
                        .header("X-Auth-Flow-Token", "reauth-token"))
                .andExpect(status().isBadRequest());

        verify(service, never()).unlink(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
