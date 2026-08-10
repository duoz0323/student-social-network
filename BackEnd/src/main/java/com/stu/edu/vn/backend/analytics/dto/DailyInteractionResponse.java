package com.stu.edu.vn.backend.analytics.dto;

import java.time.LocalDate;

/**
 * Tổng số request nghiệp vụ hợp lệ của USER trong một ngày UTC để dựng biểu đồ Dashboard.
 */
public record DailyInteractionResponse(
        LocalDate date,
        long interactionCount
) {
}
