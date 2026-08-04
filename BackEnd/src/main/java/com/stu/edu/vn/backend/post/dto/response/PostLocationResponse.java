package com.stu.edu.vn.backend.post.dto.response;

import java.math.BigDecimal;

/** Location công khai gắn với Post; API dùng placeId thay cho tên field googlePlaceId trong Entity. */
public record PostLocationResponse(
        Long id,
        String placeId,
        String displayName,
        String formattedAddress,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
