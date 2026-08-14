package com.stu.edu.vn.backend.discovery.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationResponse;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationsResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;

/** Contract read-only cho marker viewport và Post tại một Location. */
public interface DiscoveryMapService {
    MapLocationsResponse getLocations(double north, double south, double east, double west);

    CursorPageResponse<FeedPostResponse> getLocationPosts(Long locationId, int limit, String cursor);
}
