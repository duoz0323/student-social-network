package com.stu.edu.vn.backend.discovery.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.discovery.dto.response.NearbyPostItemResponse;

public interface NearbyDiscoveryService {
    CursorPageResponse<NearbyPostItemResponse> getNearby(
            double latitude,
            double longitude,
            int radiusKm,
            int limit,
            String cursor
    );
}
