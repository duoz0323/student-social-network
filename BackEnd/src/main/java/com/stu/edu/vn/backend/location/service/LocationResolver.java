package com.stu.edu.vn.backend.location.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.location.repository.LocationRepository;
import com.stu.edu.vn.backend.post.dto.request.PostLocationRequest;
import com.stu.edu.vn.backend.post.validation.PostLocationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolve Location theo Google Place ID trong transaction của nghiệp vụ Post. */
@Component
@RequiredArgsConstructor
public class LocationResolver {

    private final LocationRepository locationRepository;
    private final PostLocationValidator postLocationValidator;

    public Location resolve(PostLocationRequest request) {
        PostLocationRequest normalized = postLocationValidator.validateAndNormalizeLocation(request);
        if (normalized == null) {
            return null;
        }

        Location existing = locationRepository.findByGooglePlaceId(normalized.placeId()).orElse(null);
        if (existing != null) {
            // Không cho dữ liệu Client ghi đè metadata Location dùng chung đã tồn tại.
            return existing;
        }

        // Native upsert tránh làm transaction rollback-only khi hai request cùng insert một Place ID.
        locationRepository.insertIfAbsent(
                normalized.placeId(),
                normalized.displayName(),
                normalized.formattedAddress(),
                normalized.latitude(),
                normalized.longitude()
        );
        return locationRepository.findByGooglePlaceId(normalized.placeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
    }
}
