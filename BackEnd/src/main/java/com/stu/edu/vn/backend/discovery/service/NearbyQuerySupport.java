package com.stu.edu.vn.backend.discovery.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.model.NearbyBoundingBox;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Chuẩn hóa validation, bounding box và query binding để mọi trang dùng đúng cùng một quy tắc. */
@Component
public class NearbyQuerySupport {
    public static final int DEFAULT_RADIUS_KM = 5;
    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 20;
    private static final double EARTH_RADIUS_KM = 6371.0088d;
    private static final Set<Integer> ALLOWED_RADII_KM = Set.of(1, 3, 5, 10, 20);

    public void validate(double latitude, double longitude, int radiusKm, int limit) {
        if (!Double.isFinite(latitude) || latitude < -90.0d || latitude > 90.0d
                || !Double.isFinite(longitude) || longitude < -180.0d || longitude > 180.0d
                || !ALLOWED_RADII_KM.contains(radiusKm)
                || limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    public NearbyBoundingBox boundingBox(double latitude, double longitude, int radiusKm) {
        double angularDistance = radiusKm / EARTH_RADIUS_KM;
        double latitudeRadians = Math.toRadians(latitude);
        double latitudeDelta = Math.toDegrees(angularDistance);
        double minimumLatitude = Math.max(-90.0d, latitude - latitudeDelta);
        double maximumLatitude = Math.min(90.0d, latitude + latitudeDelta);

        if (minimumLatitude <= -90.0d || maximumLatitude >= 90.0d) {
            return new NearbyBoundingBox(
                    minimumLatitude, maximumLatitude, -180.0d, 180.0d, true, false);
        }

        // Công thức spherical cap bảo đảm bounding box không loại nhầm điểm hợp lệ gần biên radius.
        double longitudeDelta = Math.toDegrees(Math.asin(
                Math.min(1.0d, Math.sin(angularDistance) / Math.cos(latitudeRadians))));
        double rawMinimumLongitude = longitude - longitudeDelta;
        double rawMaximumLongitude = longitude + longitudeDelta;
        boolean wrapsAntimeridian = rawMinimumLongitude < -180.0d || rawMaximumLongitude > 180.0d;
        return new NearbyBoundingBox(
                minimumLatitude,
                maximumLatitude,
                normalizeLongitude(rawMinimumLongitude),
                normalizeLongitude(rawMaximumLongitude),
                false,
                wrapsAntimeridian
        );
    }

    public String fingerprint(double latitude, double longitude, int radiusKm) {
        String canonicalQuery = canonicalCoordinate(latitude)
                + "|" + canonicalCoordinate(longitude)
                + "|" + radiusKm;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalQuery.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String canonicalCoordinate(double value) {
        double normalized = value == 0.0d ? 0.0d : value;
        return BigDecimal.valueOf(normalized).stripTrailingZeros().toPlainString();
    }

    private double normalizeLongitude(double value) {
        double normalized = value;
        while (normalized < -180.0d) {
            normalized += 360.0d;
        }
        while (normalized > 180.0d) {
            normalized -= 360.0d;
        }
        return normalized;
    }
}
