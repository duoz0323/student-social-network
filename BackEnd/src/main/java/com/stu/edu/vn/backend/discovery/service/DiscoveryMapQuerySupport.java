package com.stu.edu.vn.backend.discovery.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

/** Gom validation viewport để Controller và Service không phân tán quy tắc anti-meridian V1. */
@Component
public class DiscoveryMapQuerySupport {
    public static final int DEFAULT_POST_LIMIT = 10;
    public static final int MAX_POST_LIMIT = 20;

    public void validateViewport(double north, double south, double east, double west) {
        if (!Double.isFinite(north) || north < -90.0d || north > 90.0d
                || !Double.isFinite(south) || south < -90.0d || south > 90.0d
                || !Double.isFinite(east) || east < -180.0d || east > 180.0d
                || !Double.isFinite(west) || west < -180.0d || west > 180.0d
                || south >= north
                || west >= east) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    public void validateLocationPosts(Long locationId, int limit) {
        if (locationId == null || locationId <= 0 || limit < 1 || limit > MAX_POST_LIMIT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
