package com.stu.edu.vn.backend.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.dto.response.DeletePostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostDetailResponse;
import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostViewerResponse;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.UpdatePostRequest;
import org.mockito.ArgumentCaptor;
import com.stu.edu.vn.backend.post.service.PostService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostControllerTest {

    private final PostService postService = org.mockito.Mockito.mock(PostService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostController(postService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createPostReturnsCreatedApiResponseForMultipartRequest() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 3, 1, 0);
        when(postService.createPost(any())).thenReturn(new PostResponse(
                1L,
                "Noi dung",
                PostStatus.PUBLISHED,
                false,
                0,
                0,
                now,
                now,
                now,
                new PostAuthorResponse(10L, "Nguyen Van A", "https://cdn.example/avatar.png"),
                List.of(),
                "sinhvien"
        ));
        MockMultipartFile image = new MockMultipartFile("mediaFiles", "one.png", "image/png", new byte[]{1});
        MockMultipartFile location = new MockMultipartFile("location", "", "application/json", """
                {"placeId":"ChIJ-id","displayName":"Đại học STU","formattedAddress":null,
                 "latitude":10.7382456,"longitude":106.6778123}
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(image)
                        .file(location)
                        .param("content", "Noi dung")
                        .param("hashtag", "sinhvien"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.author.id").value(10))
                .andExpect(jsonPath("$.data.hashtag").value("sinhvien"));

        ArgumentCaptor<CreatePostRequest> captor = ArgumentCaptor.forClass(CreatePostRequest.class);
        verify(postService).createPost(captor.capture());
        assertThat(captor.getValue().location().placeId()).isEqualTo("ChIJ-id");
    }

    @Test
    void getPostDetailReturnsOkApiResponse() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 3, 1, 0);
        when(postService.getPostDetail(1L)).thenReturn(new PostDetailResponse(
                1L,
                "Noi dung",
                false,
                3,
                2,
                now,
                now,
                now,
                new PostAuthorResponse(10L, "Nguyen Van A", "https://cdn.example/avatar.png"),
                List.of(),
                "sinhvien",
                new PostViewerResponse(true, true)
        ));

        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.viewer.owner").value(true))
                .andExpect(jsonPath("$.data.viewer.likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.hashtag").value("sinhvien"));

        verify(postService).getPostDetail(1L);
    }

    @Test
    void updatePostReturnsOkApiResponseForMultipartPutRequest() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 3, 1, 5);
        when(postService.updatePost(any(), any())).thenReturn(new PostDetailResponse(
                1L,
                "Noi dung moi",
                true,
                3,
                2,
                now,
                now,
                now,
                new PostAuthorResponse(10L, "Nguyen Van A", "https://cdn.example/avatar.png"),
                List.of(),
                "doan",
                new PostViewerResponse(true)
        ));
        MockMultipartFile image = new MockMultipartFile("newMediaFiles", "new.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart("/api/v1/posts/1")
                        .file(image)
                        .param("content", "Noi dung moi")
                        .param("hashtag", "doan")
                        .param("keepMediaIds", "10")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.isEdited").value(true))
                .andExpect(jsonPath("$.data.hashtag").value("doan"));

        verify(postService).updatePost(any(), any());
    }

    @Test
    void updatePostBindsReplaceActionAndJsonLocationPart() throws Exception {
        when(postService.updatePost(any(), any())).thenReturn(new PostDetailResponse(
                1L, "Noi dung", true, 0, 0, null, null, null,
                new PostAuthorResponse(10L, "A", null), List.of(), null, new PostViewerResponse(true)));
        MockMultipartFile location = new MockMultipartFile("location", "", "application/json", """
                {"placeId":"ChIJ-new","displayName":"Địa điểm mới",
                 "latitude":10.7,"longitude":106.6}
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/posts/1").file(location)
                        .param("locationAction", "REPLACE")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdatePostRequest> captor = ArgumentCaptor.forClass(UpdatePostRequest.class);
        verify(postService).updatePost(org.mockito.ArgumentMatchers.eq(1L), captor.capture());
        assertThat(captor.getValue().locationAction().name()).isEqualTo("REPLACE");
        assertThat(captor.getValue().location().placeId()).isEqualTo("ChIJ-new");
    }

    @Test
    void updatePostBindsEmptyKeepMediaIdsAsEmptyListToRemoveAllMedia() throws Exception {
        when(postService.updatePost(any(), any())).thenReturn(new PostDetailResponse(
                1L, "Noi dung", true, 0, 0, null, null, null,
                new PostAuthorResponse(10L, "A", null), List.of(), null, new PostViewerResponse(true)));

        mockMvc.perform(multipart("/api/v1/posts/1")
                        .param("content", "Noi dung")
                        .param("keepMediaIds", "")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdatePostRequest> captor = ArgumentCaptor.forClass(UpdatePostRequest.class);
        verify(postService).updatePost(org.mockito.ArgumentMatchers.eq(1L), captor.capture());
        assertThat(captor.getValue().keepMediaIds()).isEmpty();
    }

    @Test
    void invalidLocationActionReturnsDedicatedBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/posts/1")
                        .param("locationAction", "UNKNOWN")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_ACTION_INVALID"));
    }

    @Test
    void deletePostReturnsOkApiResponse() throws Exception {
        when(postService.deletePost(1L)).thenReturn(new DeletePostResponse(1L, true));

        mockMvc.perform(delete("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.deleted").value(true));

        verify(postService).deletePost(1L);
    }
}
