package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.dto.response.AdminPostAuthorResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostMediaResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostStatusResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostHiddenByResponse;
import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.service.AdminPostService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.enums.PostMediaType;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.MediaType;

class AdminPostControllerTest {
    private final AdminPostService service = org.mockito.Mockito.mock(AdminPostService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminPostController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void listUsesDefaultsAndReturnsOnlyApprovedFields() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 8, 0);
        var item = new AdminPostListItemResponse(11L, "content", PostStatus.PUBLISHED, 9L,
                "Author", "avatar", UserStatus.BLOCKED, "thumb", 2, 3, 4, 1, now, now);
        when(service.getPosts(null, null, null, false, 0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.content[0].postId").value(11))
                .andExpect(jsonPath("$.data.content[0].authorAccountStatus").value("BLOCKED"))
                .andExpect(jsonPath("$.data.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].storagePublicId").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].token").doesNotExist());
        verify(service).getPosts(null, null, null, false, 0, 20);
    }

    @Test
    void listPassesFiltersSupportsBoundariesAndEmptyPage() throws Exception {
        when(service.getPosts("  spam  ", PostStatus.HIDDEN, 9L, true, 1, 1))
                .thenReturn(new PageResponse<>(List.of(), 1, 1, 0, 0, false, true));
        when(service.getPosts(null, null, null, false, 0, 100))
                .thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/admin/posts").param("keyword", "  spam  ")
                        .param("status", "HIDDEN").param("authorId", "9")
                        .param("reportedOnly", "true").param("page", "1").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
        mockMvc.perform(get("/api/v1/admin/posts").param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void listRejectsInvalidPageSizeStatusAndAuthor() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/posts").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/posts").param("size", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/posts").param("status", "INVALID"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/posts").param("authorId", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detailReturnsNestedSafeDataForAnyStatus() throws Exception {
        var detail = new AdminPostDetailResponse(11L, "content", PostStatus.DELETED,
                new AdminPostAuthorResponse(9L, "Author", "avatar", "a@example.com", UserStatus.BLOCKED),
                List.of(new AdminPostMediaResponse(21L, "url", PostMediaType.IMAGE,
                        "image/jpeg", null, null, 0)), "tag",
                3, 4, 1, 2, null, null, null, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        when(service.getPostDetail(11L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/admin/posts/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELETED"))
                .andExpect(jsonPath("$.data.author.accountStatus").value("BLOCKED"))
                .andExpect(jsonPath("$.data.author.phoneNumber").doesNotExist())
                .andExpect(jsonPath("$.data.media[0].sortOrder").value(0))
                .andExpect(jsonPath("$.data.author.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.media[0].storagePublicId").doesNotExist());
    }

    @Test
    void detailMissingReturnsAdminPostNotFound() throws Exception {
        when(service.getPostDetail(404L)).thenThrow(new BusinessException(ErrorCode.ADMIN_POST_NOT_FOUND));
        mockMvc.perform(get("/api/v1/admin/posts/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADMIN_POST_NOT_FOUND"));
    }

    @Test
    void hideAcceptsOnlyReasonCodeAndReturnsCompactStatus() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 8, 0);
        var response = new AdminPostStatusResponse(11L, PostStatus.HIDDEN, now, "OTHER",
                new AdminPostHiddenByResponse(1L, "Admin"), now);
        when(service.hidePost(11L, new AdminHidePostRequest(AdminPostHideReason.OTHER))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/posts/11/hide")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":\"OTHER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(11))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"))
                .andExpect(jsonPath("$.data.hiddenReason").value("OTHER"))
                .andExpect(jsonPath("$.data.hiddenBy.adminId").value(1))
                .andExpect(jsonPath("$.data.content").doesNotExist())
                .andExpect(jsonPath("$.data.media").doesNotExist());
        verify(service).hidePost(11L, new AdminHidePostRequest(AdminPostHideReason.OTHER));
    }

    @Test
    void hideRejectsNullInvalidReasonAndUnexpectedNote() throws Exception {
        when(service.hidePost(11L, new AdminHidePostRequest(null)))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_POST_HIDE_REASON_REQUIRED));

        mockMvc.perform(patch("/api/v1/admin/posts/11/hide").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ADMIN_POST_HIDE_REASON_REQUIRED"));
        mockMvc.perform(patch("/api/v1/admin/posts/11/hide").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"INVALID\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(patch("/api/v1/admin/posts/11/hide").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"SPAM\",\"note\":\"free text\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void restoreHasNoBodyAndReturnsPublishedStatus() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 8, 0);
        when(service.restorePost(11L))
                .thenReturn(new AdminPostStatusResponse(11L, PostStatus.PUBLISHED, null, null, null, now));

        mockMvc.perform(patch("/api/v1/admin/posts/11/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.hiddenAt").doesNotExist())
                .andExpect(jsonPath("$.data.hiddenReason").doesNotExist())
                .andExpect(jsonPath("$.data.hiddenBy").doesNotExist());
        verify(service).restorePost(11L);
    }

    @Test
    void mutationMapsStateErrorsToApprovedStatuses() throws Exception {
        when(service.hidePost(11L, new AdminHidePostRequest(AdminPostHideReason.SPAM)))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_POST_ALREADY_HIDDEN));
        when(service.restorePost(12L))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_POST_DELETED_ACTION_FORBIDDEN));

        mockMvc.perform(patch("/api/v1/admin/posts/11/hide").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADMIN_POST_ALREADY_HIDDEN"));
        mockMvc.perform(patch("/api/v1/admin/posts/12/restore"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADMIN_POST_DELETED_ACTION_FORBIDDEN"));
    }
}
