package com.stu.edu.vn.backend.discovery.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Marker Location tổng hợp từ các bài mà viewer thực sự có quyền xem. */
public record MapLocationResponse(
        Long locationId,
        String displayName,
        String formattedAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        long postCount,
        LocalDateTime latestPostAt
) {
}
