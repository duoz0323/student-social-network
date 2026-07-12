package com.stu.edu.vn.backend.search.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.search.dto.response.SearchUserResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.search.service.SearchService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SearchControllerTest {

    private final SearchService searchService = org.mockito.Mockito.mock(SearchService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new SearchController(searchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void searchUsersReturnsApiResponseWithDefaultPaginationAndPublicFields() throws Exception {
        SearchUserResponse user = new SearchUserResponse(20L, "Nguyễn Minh", null, "Sinh viên");
        when(searchService.searchUsers("minh", 0, 20))
                .thenReturn(new PageResponse<>(List.of(user), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/search/users").param("q", "minh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(20))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].phoneNumber").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].role").doesNotExist());
        verify(searchService).searchUsers("minh", 0, 20);
    }

    @Test
    void missingAndBlankKeywordReturnApprovedBusinessError() throws Exception {
        when(searchService.searchUsers(null, 0, 20)).thenThrow(new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED));
        when(searchService.searchUsers("   ", 0, 20)).thenThrow(new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED));

        mockMvc.perform(get("/api/v1/search/users"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SEARCH_KEYWORD_REQUIRED"));
        mockMvc.perform(get("/api/v1/search/users").param("q", "   "))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SEARCH_KEYWORD_REQUIRED"));
    }

    @Test
    void invalidPageAndSizeReturnValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/search/users").param("q", "minh").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/search/users").param("q", "minh").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void searchPostsSupportsContentAndHashtagWithPageResponse() throws Exception {
        SearchPostResponse post = new SearchPostResponse(
                100L, "Học Java", false, 3, 2, null,
                new PostAuthorResponse(20L, "Minh", null), List.of(), List.of("java"), true, false);
        PageResponse<SearchPostResponse> page = new PageResponse<>(List.of(post), 0, 20, 1, 1, true, true);
        when(searchService.searchPosts("java", SearchPostType.CONTENT, 0, 20)).thenReturn(page);
        when(searchService.searchPosts("#java", SearchPostType.HASHTAG, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/search/posts").param("q", "java").param("type", "CONTENT"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].postId").value(100))
                .andExpect(jsonPath("$.data.content[0].likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.content[0].email").doesNotExist());
        mockMvc.perform(get("/api/v1/search/posts").param("q", "#java").param("type", "HASHTAG"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].hashtags[0]").value("java"));
    }

    @Test
    void searchPostsRejectsInvalidTypeKeywordAndPagination() throws Exception {
        when(searchService.searchPosts(null, SearchPostType.CONTENT, 0, 20))
                .thenThrow(new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED));
        mockMvc.perform(get("/api/v1/search/posts").param("type", "CONTENT"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SEARCH_KEYWORD_REQUIRED"));
        mockMvc.perform(get("/api/v1/search/posts").param("q", "java").param("type", "OTHER"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/search/posts").param("q", "java").param("type", "CONTENT").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/search/posts").param("q", "java").param("type", "CONTENT").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
