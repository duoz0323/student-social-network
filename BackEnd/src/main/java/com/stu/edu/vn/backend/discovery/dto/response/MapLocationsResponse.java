package com.stu.edu.vn.backend.discovery.dto.response;

import java.util.List;

/** Marker endpoint không phân trang nhưng báo rõ khi viewport vượt giới hạn cấu hình. */
public record MapLocationsResponse(List<MapLocationResponse> locations, boolean truncated) {
}
