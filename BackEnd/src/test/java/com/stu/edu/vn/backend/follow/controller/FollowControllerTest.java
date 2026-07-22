package com.stu.edu.vn.backend.follow.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.follow.dto.response.FollowStatusResponse;
import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import com.stu.edu.vn.backend.follow.service.FollowService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FollowControllerTest {

    private final FollowService followService = org.mockito.Mockito.mock(FollowService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FollowController(followService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void followAndUnfollowReturnExpectedApiResponses() throws Exception {
        when(followService.followUser(20L)).thenReturn(new FollowStatusResponse(20L, true));
        when(followService.unfollowUser(20L)).thenReturn(new FollowStatusResponse(20L, false));

        mockMvc.perform(post("/api/v1/users/20/follow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(20))
                .andExpect(jsonPath("$.data.followedByCurrentUser").value(true));
        mockMvc.perform(delete("/api/v1/users/20/follow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followedByCurrentUser").value(false));

        verify(followService).followUser(20L);
        verify(followService).unfollowUser(20L);
    }

    @Test
    void followersAndFollowingReturnUnpagedArraysWithFollowedAt() throws Exception {
        FollowUserResponse user = new FollowUserResponse(
                30L,
                "Nguyen Van B",
                null,
                "Sinh vien CNTT",
                LocalDateTime.of(2026, 7, 12, 10, 0),
                true
        );
        when(followService.getFollowers(20L)).thenReturn(List.of(user));
        when(followService.getFollowing(20L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/20/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(30))
                .andExpect(jsonPath("$.data[0].followedAt").exists())
                .andExpect(jsonPath("$.data[0].followedByCurrentUser").value(true));
        mockMvc.perform(get("/api/v1/users/20/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void controllerReturnsApprovedFollowErrorStatuses() throws Exception {
        when(followService.followUser(10L))
                .thenThrow(new BusinessException(ErrorCode.FOLLOW_SELF_FORBIDDEN));
        when(followService.followUser(20L))
                .thenThrow(new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS));
        when(followService.unfollowUser(30L))
                .thenThrow(new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/10/follow"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FOLLOW_SELF_FORBIDDEN"));
        mockMvc.perform(post("/api/v1/users/20/follow"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FOLLOW_ALREADY_EXISTS"));
        mockMvc.perform(delete("/api/v1/users/30/follow"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FOLLOW_NOT_FOUND"));
    }
}
