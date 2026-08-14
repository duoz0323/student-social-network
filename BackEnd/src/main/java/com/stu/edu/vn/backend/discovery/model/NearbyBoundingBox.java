package com.stu.edu.vn.backend.discovery.model;

/** Bounding box chỉ dùng làm pre-filter; khoảng cách Haversine vẫn quyết định candidate cuối cùng. */
public record NearbyBoundingBox(
        double minimumLatitude,
        double maximumLatitude,
        double minimumLongitude,
        double maximumLongitude,
        boolean allLongitudes,
        boolean wrapsAntimeridian
) {
}
