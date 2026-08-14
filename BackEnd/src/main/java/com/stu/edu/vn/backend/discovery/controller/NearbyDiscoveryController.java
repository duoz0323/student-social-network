package com.stu.edu.vn.backend.discovery.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.dto.response.NearbyPostItemResponse;
import com.stu.edu.vn.backend.discovery.service.NearbyDiscoveryService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint Nearby chỉ bind input; viewer luôn được lấy từ SecurityContext ở Service. */
@Validated
@RestController
@RequestMapping("/api/v1/discovery")
@RequiredArgsConstructor
public class NearbyDiscoveryController {
    private final NearbyDiscoveryService nearbyDiscoveryService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<CursorPageResponse<NearbyPostItemResponse>>> getNearby(
            @RequestParam(required = false) @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @RequestParam(required = false) @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @RequestParam(defaultValue = "5") int radiusKm,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(required = false) String cursor
    ) {
        if (latitude == null || longitude == null) {
            // Tránh autounboxing null thành lỗi 500 khi Client bỏ sót query parameter bắt buộc.
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy bài viết gần bạn thành công",
                nearbyDiscoveryService.getNearby(latitude, longitude, radiusKm, limit, cursor)
        ));
    }
}
