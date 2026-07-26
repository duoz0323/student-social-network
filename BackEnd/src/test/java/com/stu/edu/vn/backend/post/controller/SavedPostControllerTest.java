package com.stu.edu.vn.backend.post.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.dto.response.PostSaveResponse;
import com.stu.edu.vn.backend.post.service.SavedPostService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SavedPostControllerTest {

    private final SavedPostService savedPostService = org.mockito.Mockito.mock(SavedPostService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SavedPostController(savedPostService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void savePostReturnsOkApiResponseWithoutUserIdRequestData() throws Exception {
        when(savedPostService.savePost(15L)).thenReturn(new PostSaveResponse(15L, true));

        mockMvc.perform(post("/api/v1/posts/15/saves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lưu bài viết thành công"))
                .andExpect(jsonPath("$.data.postId").value(15))
                .andExpect(jsonPath("$.data.saved").value(true))
                .andExpect(jsonPath("$.data.userId").doesNotExist());

        verify(savedPostService).savePost(15L);
    }

    @Test
    void unsavePostReturnsOkApiResponseWithoutEntityData() throws Exception {
        when(savedPostService.unsavePost(15L)).thenReturn(new PostSaveResponse(15L, false));

        mockMvc.perform(delete("/api/v1/posts/15/saves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bỏ lưu bài viết thành công"))
                .andExpect(jsonPath("$.data.postId").value(15))
                .andExpect(jsonPath("$.data.saved").value(false))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.user").doesNotExist())
                .andExpect(jsonPath("$.data.post").doesNotExist());

        verify(savedPostService).unsavePost(15L);
    }

    @Test
    void savePostReturnsExistingBusinessErrorConvention() throws Exception {
        when(savedPostService.savePost(15L)).thenThrow(new BusinessException(ErrorCode.POST_NOT_AVAILABLE));

        mockMvc.perform(post("/api/v1/posts/15/saves"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_NOT_AVAILABLE"));
    }

    @Test
    void getSavedPostsReturnsCurrentUsersPagedList() throws Exception {
        // Xác nhận Controller chuyển đúng tham số phân trang cho cùng SavedPostService.
        when(savedPostService.getSavedPosts(null, 10))
                .thenReturn(new CursorPageResponse<>(List.of(), null, false));

        mockMvc.perform(get("/api/v1/posts/saved")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.data.hasNext").value(false));

        verify(savedPostService).getSavedPosts(null, 10);
    }
}
