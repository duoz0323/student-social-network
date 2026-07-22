package com.stu.edu.vn.backend.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionItemResponse;
import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionListResponse;
import com.stu.edu.vn.backend.post.service.HashtagService;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestParam;

class HashtagControllerTest {

    private final HashtagService hashtagService = org.mockito.Mockito.mock(HashtagService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HashtagController(hashtagService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void suggestionsEndpointReturnsOkApiResponse() throws Exception {
        when(hashtagService.getSuggestions("Doan")).thenReturn(new HashtagSuggestionListResponse(
                "Doan",
                "doan",
                false,
                List.of(new HashtagSuggestionItemResponse(1L, "doantruong", 100)),
                true
        ));

        mockMvc.perform(get("/api/v1/hashtags/suggestions").param("keyword", "Doan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.keyword").value("Doan"))
                .andExpect(jsonPath("$.data.normalizedKeyword").value("doan"))
                .andExpect(jsonPath("$.data.exactMatch").value(false))
                .andExpect(jsonPath("$.data.suggestions[0].hashtagId").value(1))
                .andExpect(jsonPath("$.data.suggestions[0].name").value("doantruong"))
                .andExpect(jsonPath("$.data.suggestions[0].postCount").value(100))
                .andExpect(jsonPath("$.data.canUseAsNewHashtag").value(true));

        verify(hashtagService).getSuggestions("Doan");
    }

    @Test
    void controllerContractAcceptsOnlyKeywordWithoutPaginationParameters() throws Exception {
        Method method = HashtagController.class.getMethod("getSuggestions", String.class);

        assertThat(method.getParameterCount()).isEqualTo(1);
        RequestParam requestParam = method.getParameters()[0].getAnnotation(RequestParam.class);
        assertThat(requestParam).isNotNull();
        assertThat(requestParam.value()).isEqualTo("keyword");
    }

    @Test
    void incompleteProfileUsesProjectErrorConvention() throws Exception {
        when(hashtagService.getSuggestions("doan"))
                .thenThrow(new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED));

        mockMvc.perform(get("/api/v1/hashtags/suggestions").param("keyword", "doan"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_COMPLETED"));
    }
}
