package com.stu.edu.vn.backend.analytics.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementItemResponse;
import com.stu.edu.vn.backend.analytics.dto.DailyInteractionResponse;
import com.stu.edu.vn.backend.analytics.dto.DashboardUserEngagementResponse;
import com.stu.edu.vn.backend.analytics.dto.FeaturedUserResponse;
import com.stu.edu.vn.backend.analytics.service.UserEngagementAnalyticsService;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminUserEngagementAnalyticsControllerTest {

    private final UserEngagementAnalyticsService service =
            org.mockito.Mockito.mock(UserEngagementAnalyticsService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserEngagementAnalyticsController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void summaryUsesCurrentMonthDefaultAndInactiveDays15() throws Exception {
        var item = emptyItem();
        when(service.getSummary(null, 15)).thenReturn(item);

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.month").value("2026-06"))
                .andExpect(jsonPath("$.data.activeUserRate").doesNotExist());

        verify(service).getSummary(null, 15);
    }

    @Test
    void monthlyMapsBusinessValidationError() throws Exception {
        when(service.getMonthly("2026-07", "2026-06", 15))
                .thenThrow(new BusinessException(ErrorCode.ANALYTICS_MONTH_RANGE_INVALID));

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/monthly")
                        .param("fromMonth", "2026-07")
                        .param("toMonth", "2026-06"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ANALYTICS_MONTH_RANGE_INVALID"));
    }

    @Test
    void dashboardUsesDefaultThirtyDays() throws Exception {
        when(service.getDashboard(30)).thenReturn(new DashboardUserEngagementResponse(
                LocalDate.of(2026, 5, 17),
                LocalDate.of(2026, 6, 15),
                List.of(new DailyInteractionResponse(LocalDate.of(2026, 6, 15), 12)),
                List.of(new FeaturedUserResponse(7L, "Mai", null, 2, 12))
        ));

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dailyInteractions[0].interactionCount").value(12))
                .andExpect(jsonPath("$.data.featuredUsers[0].displayName").value("Mai"));

        verify(service).getDashboard(30);
    }

    private MonthlyUserEngagementItemResponse emptyItem() {
        return new MonthlyUserEngagementItemResponse(
                YearMonth.of(2026, 6), LocalDate.of(2026, 6, 15),
                0, 0, null, 0, 0, null, 0, 0, 0, 0, 0, null, 0, null, 0
        );
    }
}
