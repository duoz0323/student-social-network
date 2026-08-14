package com.stu.edu.vn.backend.discovery.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.discovery.dto.response.NearbyPostItemResponse;
import com.stu.edu.vn.backend.discovery.service.NearbyDiscoveryService;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NearbyDiscoveryControllerTest {
    private final NearbyDiscoveryService service = org.mockito.Mockito.mock(NearbyDiscoveryService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new NearbyDiscoveryController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsDefaultRadiusLimitAndNearbyWrapper() throws Exception {
        FeedPostResponse post = new FeedPostResponse(
                15L, "Bài viết gần đây", false, 1, 2, 0,
                LocalDateTime.of(2026, 8, 13, 10, 0), null, List.of(), null,
                false, false, false, null);
        when(service.getNearby(10.8231d, 106.6297d, 5, 10, null))
                .thenReturn(new CursorPageResponse<>(
                        List.of(new NearbyPostItemResponse(post, 850L)), null, false));

        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "10.8231")
                        .param("longitude", "106.6297"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].post.postId").value(15))
                .andExpect(jsonPath("$.data.content[0].distanceMeters").value(850))
                .andExpect(jsonPath("$.data.hasNext").value(false));
        verify(service).getNearby(10.8231d, 106.6297d, 5, 10, null);
    }

    @Test
    void acceptsCoordinateAndLimitBoundaries() throws Exception {
        when(service.getNearby(90.0d, 180.0d, 20, 1, null))
                .thenReturn(new CursorPageResponse<>(List.of(), null, false));
        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "90")
                        .param("longitude", "180")
                        .param("radiusKm", "20")
                        .param("limit", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMissingAndOutOfRangeCoordinatesAndLimits() throws Exception {
        when(service.getNearby(90.0001d, 0.0d, 5, 10, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        when(service.getNearby(0.0d, -180.0001d, 5, 10, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        when(service.getNearby(0.0d, 0.0d, 5, 0, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        when(service.getNearby(0.0d, 0.0d, 5, 21, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        mockMvc.perform(get("/api/v1/discovery/nearby").param("longitude", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "90.0001").param("longitude", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "0").param("longitude", "-180.0001"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "0").param("longitude", "0").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "0").param("longitude", "0").param("limit", "21"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usesGlobalBusinessErrorForUnsupportedRadius() throws Exception {
        when(service.getNearby(0.0d, 0.0d, 2, 10, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));

        mockMvc.perform(get("/api/v1/discovery/nearby")
                        .param("latitude", "0").param("longitude", "0").param("radiusKm", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
