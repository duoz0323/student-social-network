package com.stu.edu.vn.backend.interaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;
import com.stu.edu.vn.backend.interaction.service.CommentService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CommentControllerTest {

    private final CommentService commentService = org.mockito.Mockito.mock(CommentService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CommentController(commentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCommentReturnsCreatedApiResponse() throws Exception {
        when(commentService.createComment(any(), any())).thenReturn(commentResponse(100L, 1L, 10L, "Noi dung"));

        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Noi dung\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value(100))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.content").value("Noi dung"));

        verify(commentService).createComment(any(), any());
    }

    @Test
    void createReplyReturnsCreatedApiResponse() throws Exception {
        when(commentService.createReply(any(), any()))
                .thenReturn(commentResponse(101L, 1L, 100L, 10L, "Tra loi"));

        mockMvc.perform(post("/api/v1/comments/100/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Tra loi\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value(101))
                .andExpect(jsonPath("$.data.parentCommentId").value(100));

        verify(commentService).createReply(any(), any());
    }

    @Test
    void getCommentsReturnsPublishedComments() throws Exception {
        when(commentService.getPublishedComments(1L, 0, 20)).thenReturn(pageResponse(List.of(
                commentResponse(100L, 1L, 10L, "Binh luan 1"),
                commentResponse(101L, 1L, 11L, "Binh luan 2")
        )));

        mockMvc.perform(get("/api/v1/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].commentId").value(100))
                .andExpect(jsonPath("$.data.content[1].commentId").value(101));

        verify(commentService).getPublishedComments(1L, 0, 20);
    }

    @Test
    void getRepliesReturnsPublishedReplies() throws Exception {
        when(commentService.getPublishedReplies(100L, 0, 20)).thenReturn(pageResponse(List.of(
                commentResponse(101L, 1L, 100L, 11L, "Tra loi")
        )));

        mockMvc.perform(get("/api/v1/comments/100/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].commentId").value(101))
                .andExpect(jsonPath("$.data.content[0].parentCommentId").value(100));

        verify(commentService).getPublishedReplies(100L, 0, 20);
    }

    @Test
    void deleteCommentReturnsOkApiResponse() throws Exception {
        when(commentService.deleteComment(100L)).thenReturn(new DeleteCommentResponse(100L, true));

        mockMvc.perform(delete("/api/v1/comments/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value(100))
                .andExpect(jsonPath("$.data.deleted").value(true));

        verify(commentService).deleteComment(100L);
    }

    @Test
    void createCommentReturnsBusinessErrorWhenContentBlank() throws Exception {
        when(commentService.createComment(any(), any())).thenThrow(new BusinessException(ErrorCode.COMMENT_CONTENT_REQUIRED));

        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMENT_CONTENT_REQUIRED"));
    }

    private CommentResponse commentResponse(Long commentId, Long postId, Long userId, String content) {
        return commentResponse(commentId, postId, null, userId, content);
    }

    private CommentResponse commentResponse(
            Long commentId,
            Long postId,
            Long parentCommentId,
            Long userId,
            String content
    ) {
        return new CommentResponse(
                commentId,
                postId,
                parentCommentId,
                userId,
                "Nguyen Van A",
                "https://cdn.example/avatar.png",
                content,
                LocalDateTime.of(2026, 7, 3, 1, 0),
                0L,
                false
        );
    }

    private PageResponse<CommentResponse> pageResponse(List<CommentResponse> content) {
        return new PageResponse<>(content, 0, 20, content.size(), 1, true, true);
    }
}
