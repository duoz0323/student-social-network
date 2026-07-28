package com.stu.edu.vn.backend.user.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.dto.response.BlockedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserBlockStatusResponse;
import com.stu.edu.vn.backend.user.service.UserBlockService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserBlockControllerTest {

    private final UserBlockService userBlockService = org.mockito.Mockito.mock(UserBlockService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserBlockController(userBlockService)).build();
    }

    @Test
    void blockAndUnblockPassOnlyTargetIdToService() throws Exception {
        when(userBlockService.block(20L)).thenReturn(new UserBlockStatusResponse(20L, true));
        when(userBlockService.unblock(20L)).thenReturn(new UserBlockStatusResponse(20L, false));

        mockMvc.perform(put("/api/v1/users/20/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetUserId").value(20))
                .andExpect(jsonPath("$.data.blocked").value(true));
        mockMvc.perform(delete("/api/v1/users/20/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocked").value(false));

        verify(userBlockService).block(20L);
        verify(userBlockService).unblock(20L);
    }

    @Test
    void blockedUsersResponseContainsOnlyPublicProjection() throws Exception {
        BlockedUserResponse item = new BlockedUserResponse(
                20L, "Nguyễn Văn B", "https://cdn.example/avatar.png",
                LocalDateTime.of(2026, 7, 28, 10, 0));
        PageResponse<BlockedUserResponse> page =
                new PageResponse<>(List.of(item), 0, 20, 1, 1, true, true);
        when(userBlockService.getMyBlockedUsers(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/users/me/blocked-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userId").value(20))
                .andExpect(jsonPath("$.data.content[0].displayName").value("Nguyễn Văn B"))
                .andExpect(jsonPath("$.data.content[0].email").doesNotExist());
    }
}
