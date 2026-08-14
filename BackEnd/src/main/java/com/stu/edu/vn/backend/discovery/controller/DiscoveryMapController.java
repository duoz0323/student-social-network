package com.stu.edu.vn.backend.discovery.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationsResponse;
import com.stu.edu.vn.backend.discovery.service.DiscoveryMapService;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint Map chỉ bind request; viewer và quyền truy cập luôn do Service lấy từ SecurityContext. */
@Validated
@RestController
@RequestMapping("/api/v1/discovery/map/locations")
@RequiredArgsConstructor
public class DiscoveryMapController {
    private final DiscoveryMapService discoveryMapService;

    @GetMapping
    public ResponseEntity<ApiResponse<MapLocationsResponse>> getLocations(
            @RequestParam(required = false) @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double north,
            @RequestParam(required = false) @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double south,
            @RequestParam(required = false) @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double east,
            @RequestParam(required = false) @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double west
    ) {
        if (north == null || south == null || east == null || west == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy địa điểm trên bản đồ thành công",
                discoveryMapService.getLocations(north, south, east, west)
        ));
    }

    @GetMapping("/{locationId}/posts")
    public ResponseEntity<ApiResponse<CursorPageResponse<FeedPostResponse>>> getLocationPosts(
            @PathVariable @Positive Long locationId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy bài viết tại địa điểm thành công",
                discoveryMapService.getLocationPosts(locationId, limit, cursor)
        ));
    }
}
