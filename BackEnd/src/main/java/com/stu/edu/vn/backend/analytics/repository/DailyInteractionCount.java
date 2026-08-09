package com.stu.edu.vn.backend.analytics.repository;

import java.time.LocalDate;

/**
 * Projection nội bộ của tổng activity_count theo ngày UTC.
 */
public record DailyInteractionCount(
        LocalDate date,
        long interactionCount
) {
}
