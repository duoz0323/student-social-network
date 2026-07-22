package com.stu.edu.vn.backend.post.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;
import com.stu.edu.vn.backend.post.service.PostLikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostLikeControllerTest {

    private final PostLikeService postLikeService = org.mockito.Mockito.mock(PostLikeService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostLikeController(postLikeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void likePostReturnsOkApiResponse() throws Exception {
        when(postLikeService.likePost(1L)).thenReturn(new PostLikeResponse(1L, true, 6));

        mockMvc.perform(post("/api/v1/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(6));

        verify(postLikeService).likePost(1L);
    }

    @Test
    void unlikePostReturnsOkApiResponse() throws Exception {
        when(postLikeService.unlikePost(1L)).thenReturn(new PostLikeResponse(1L, false, 5));

        mockMvc.perform(delete("/api/v1/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.likedByCurrentUser").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(5));

        verify(postLikeService).unlikePost(1L);
    }

    @Test
    void likePostReturnsBusinessErrorWhenPostAlreadyLiked() throws Exception {
        when(postLikeService.likePost(1L)).thenThrow(new BusinessException(ErrorCode.POST_ALREADY_LIKED));

        mockMvc.perform(post("/api/v1/posts/1/likes"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_ALREADY_LIKED"));
    }
}
