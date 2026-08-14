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
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationResponse;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationsResponse;
import com.stu.edu.vn.backend.discovery.service.DiscoveryMapService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Kiểm tra contract HTTP, envelope và validation đầu vào của hai endpoint Map. */
class DiscoveryMapControllerTest {
    private final DiscoveryMapService service = org.mockito.Mockito.mock(DiscoveryMapService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new DiscoveryMapController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsMarkerEnvelopeAndDelegatesExactViewport() throws Exception {
        MapLocationResponse marker = new MapLocationResponse(
                15L, "Thư viện STU", "180 Cao Lỗ", new BigDecimal("10.7381234"),
                new BigDecimal("106.6771234"), 7L, LocalDateTime.of(2026, 8, 14, 1, 20));
        when(service.getLocations(10.78d, 10.73d, 106.70d, 106.64d))
                .thenReturn(new MapLocationsResponse(List.of(marker), false));

        mockMvc.perform(get("/api/v1/discovery/map/locations")
                        .param("north", "10.78").param("south", "10.73")
                        .param("east", "106.70").param("west", "106.64"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locations[0].locationId").value(15))
                .andExpect(jsonPath("$.data.locations[0].postCount").value(7))
                .andExpect(jsonPath("$.data.truncated").value(false));
        verify(service).getLocations(10.78d, 10.73d, 106.70d, 106.64d);
    }

    @Test
    void usesDefaultPostLimitAndCursorWrapper() throws Exception {
        when(service.getLocationPosts(15L, 10, "opaque"))
                .thenReturn(new CursorPageResponse<>(List.of(), null, false));

        mockMvc.perform(get("/api/v1/discovery/map/locations/15/posts").param("cursor", "opaque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(false));
        verify(service).getLocationPosts(15L, 10, "opaque");
    }

    @Test
    void rejectsMissingOrOutOfRangeViewportAndPostLimit() throws Exception {
        when(service.getLocations(91.0d, 0.0d, 1.0d, 0.0d))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        when(service.getLocationPosts(15L, 0, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        when(service.getLocationPosts(15L, 21, null))
                .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));
        mockMvc.perform(get("/api/v1/discovery/map/locations")
                        .param("south", "0").param("east", "1").param("west", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/discovery/map/locations")
                        .param("north", "91").param("south", "0")
                        .param("east", "1").param("west", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/discovery/map/locations/15/posts").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/discovery/map/locations/15/posts").param("limit", "21"))
                .andExpect(status().isBadRequest());
    }
}
