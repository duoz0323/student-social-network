package com.stu.edu.vn.backend.post.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.dto.request.PostLocationRequest;
import com.stu.edu.vn.backend.post.enums.LocationAction;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Validate và chuẩn hóa dữ liệu Location độc lập với content, hashtag và media của Post. */
@Component
public class PostLocationValidator {

    private static final int MAX_PLACE_ID_LENGTH = 255;
    private static final int MAX_DISPLAY_NAME_LENGTH = 255;
    private static final int MAX_FORMATTED_ADDRESS_LENGTH = 500;
    private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    public PostLocationRequest validateAndNormalizeLocation(PostLocationRequest request) {
        if (request == null) {
            return null;
        }

        String placeId = trimRequired(request.placeId(), ErrorCode.LOCATION_PLACE_ID_REQUIRED);
        String displayName = trimRequired(request.displayName(), ErrorCode.LOCATION_DISPLAY_NAME_REQUIRED);
        String formattedAddress = trimToNull(request.formattedAddress());
        validateLengths(placeId, displayName, formattedAddress);
        validateCoordinates(request.latitude(), request.longitude());

        return new PostLocationRequest(
                placeId,
                displayName,
                formattedAddress,
                request.latitude(),
                request.longitude()
        );
    }

    public LocationAction validateLocationUpdateAction(
            LocationAction action,
            PostLocationRequest locationRequest
    ) {
        LocationAction effectiveAction = action == null ? LocationAction.KEEP : action;
        switch (effectiveAction) {
            case KEEP, REMOVE -> {
                if (locationRequest != null) {
                    throw new BusinessException(ErrorCode.LOCATION_PAYLOAD_NOT_ALLOWED);
                }
            }
            case REPLACE -> {
                if (locationRequest == null) {
                    throw new BusinessException(ErrorCode.LOCATION_REQUIRED_FOR_REPLACE);
                }
            }
            default -> throw new BusinessException(ErrorCode.LOCATION_ACTION_INVALID);
        }
        return effectiveAction;
    }

    private String trimRequired(String value, ErrorCode errorCode) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(errorCode);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateLengths(String placeId, String displayName, String formattedAddress) {
        if (placeId.length() > MAX_PLACE_ID_LENGTH
                || displayName.length() > MAX_DISPLAY_NAME_LENGTH
                || (formattedAddress != null && formattedAddress.length() > MAX_FORMATTED_ADDRESS_LENGTH)) {
            throw new BusinessException(ErrorCode.LOCATION_FIELD_TOO_LONG);
        }
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.LOCATION_COORDINATES_REQUIRED);
        }
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new BusinessException(ErrorCode.LOCATION_LATITUDE_INVALID);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new BusinessException(ErrorCode.LOCATION_LONGITUDE_INVALID);
        }
    }
}
