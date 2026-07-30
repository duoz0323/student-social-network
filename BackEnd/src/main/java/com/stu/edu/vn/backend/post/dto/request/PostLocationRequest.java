package com.stu.edu.vn.backend.post.dto.request;

import java.math.BigDecimal;

/** Dữ liệu Google Place do người dùng chủ động chọn trên Frontend, không chứa khóa database nội bộ. */
public record PostLocationRequest(
        String placeId,
        String displayName,
        String formattedAddress,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
