package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagDeleteResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagUpdateResponse;
import com.stu.edu.vn.backend.admin.service.AdminHashtagService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.MediaType;

class AdminHashtagControllerTest {
    private final AdminHashtagService service = org.mockito.Mockito.mock(AdminHashtagService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminHashtagController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void listUsesDefaultsAndReturnsRequiredColumns() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 8, 0);
        LocalDateTime latestUsedAt = LocalDateTime.of(2026, 8, 9, 10, 30);
        var item = new AdminHashtagListItemResponse(7L, "sinhvien", 12, createdAt, latestUsedAt);
        when(service.getHashtags(null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/admin/hashtags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].hashtagId").value(7))
                .andExpect(jsonPath("$.data.content[0].name").value("sinhvien"))
                .andExpect(jsonPath("$.data.content[0].postCount").value(12))
                .andExpect(jsonPath("$.data.content[0].createdAt").exists())
                .andExpect(jsonPath("$.data.content[0].latestUsedAt").exists())
                .andExpect(jsonPath("$.data.content[0].normalizedName").doesNotExist());
        verify(service).getHashtags(null, 0, 20);
    }

    @Test
    void listPassesSearchAndPagination() throws Exception {
        when(service.getHashtags("student", 2, 10))
                .thenReturn(new PageResponse<>(List.of(), 2, 10, 0, 0, false, true));

        mockMvc.perform(get("/api/v1/admin/hashtags")
                        .param("keyword", "student").param("page", "2").param("size", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
        verify(service).getHashtags("student", 2, 10);
    }

    @Test
    void listRejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/admin/hashtags").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/hashtags").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/hashtags").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns201AndNormalizedHashtag() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 12, 0);
        when(service.createHashtag("##Sinh Viên"))
                .thenReturn(new AdminHashtagListItemResponse(15L, "sinh viên", 0, now, null));

        mockMvc.perform(post("/api/v1/admin/hashtags")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"##Sinh Viên\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.hashtagId").value(15))
                .andExpect(jsonPath("$.data.name").value("sinh viên"))
                .andExpect(jsonPath("$.data.postCount").value(0));
        verify(service).createHashtag("##Sinh Viên");
    }

    @Test
    void deleteReturnsDetachedPostCountAndValidatesId() throws Exception {
        when(service.deleteHashtag(7L)).thenReturn(new AdminHashtagDeleteResponse(7L, "sinhvien", 3));

        mockMvc.perform(delete("/api/v1/admin/hashtags/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hashtagId").value(7))
                .andExpect(jsonPath("$.data.detachedPostCount").value(3));
        mockMvc.perform(delete("/api/v1/admin/hashtags/0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void updateReturnsNormalizedNameAndValidatesId() throws Exception {
        when(service.updateHashtag(7L, "Tên mới"))
                .thenReturn(new AdminHashtagUpdateResponse(7L, "tên mới"));

        mockMvc.perform(patch("/api/v1/admin/hashtags/7")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Tên mới\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hashtagId").value(7))
                .andExpect(jsonPath("$.data.name").value("tên mới"));
        verify(service).updateHashtag(7L, "Tên mới");
        mockMvc.perform(patch("/api/v1/admin/hashtags/0")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"new\"}"))
                .andExpect(status().isBadRequest());
    }
}
