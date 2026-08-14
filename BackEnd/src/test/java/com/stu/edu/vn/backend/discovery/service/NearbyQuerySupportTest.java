package com.stu.edu.vn.backend.discovery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;

class NearbyQuerySupportTest {
    private final NearbyQuerySupport support = new NearbyQuerySupport();

    @Test
    void acceptsCoordinateBoundariesAllowedRadiiAndLimitBoundaries() {
        for (double latitude : List.of(-90.0d, 90.0d, 0.0d)) {
            assertThatCode(() -> support.validate(latitude, 0.0d, 5, 10)).doesNotThrowAnyException();
        }
        for (double longitude : List.of(-180.0d, 180.0d, 0.0d)) {
            assertThatCode(() -> support.validate(0.0d, longitude, 5, 10)).doesNotThrowAnyException();
        }
        for (int radius : List.of(1, 3, 5, 10, 20)) {
            assertThatCode(() -> support.validate(0.0d, 0.0d, radius, 10)).doesNotThrowAnyException();
        }
        assertThatCode(() -> support.validate(0.0d, 0.0d, 5, 1)).doesNotThrowAnyException();
        assertThatCode(() -> support.validate(0.0d, 0.0d, 5, 20)).doesNotThrowAnyException();
    }

    @Test
    void rejectsOutOfRangeNonFiniteUnsupportedRadiusAndLimit() {
        for (double latitude : List.of(-90.000001d, 90.000001d, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertValidationError(() -> support.validate(latitude, 0.0d, 5, 10));
        }
        for (double longitude : List.of(-180.000001d, 180.000001d, Double.NaN, Double.NEGATIVE_INFINITY)) {
            assertValidationError(() -> support.validate(0.0d, longitude, 5, 10));
        }
        for (int radius : List.of(-1, 0, 2, 21)) {
            assertValidationError(() -> support.validate(0.0d, 0.0d, radius, 10));
        }
        assertValidationError(() -> support.validate(0.0d, 0.0d, 5, 0));
        assertValidationError(() -> support.validate(0.0d, 0.0d, 5, 21));
    }

    @Test
    void createsSafeBoundingBoxesForVietnamAntimeridianAndPoles() {
        var vietnam = support.boundingBox(10.8231d, 106.6297d, 20);
        assertThat(vietnam.minimumLatitude()).isLessThan(10.8231d);
        assertThat(vietnam.maximumLatitude()).isGreaterThan(10.8231d);
        assertThat(vietnam.allLongitudes()).isFalse();
        assertThat(vietnam.wrapsAntimeridian()).isFalse();

        var antimeridian = support.boundingBox(0.0d, 179.99d, 20);
        assertThat(antimeridian.wrapsAntimeridian()).isTrue();
        assertThat(antimeridian.minimumLongitude()).isPositive();
        assertThat(antimeridian.maximumLongitude()).isNegative();

        var pole = support.boundingBox(90.0d, 0.0d, 20);
        assertThat(pole.allLongitudes()).isTrue();
        assertThat(pole.minimumLongitude()).isEqualTo(-180.0d);
        assertThat(pole.maximumLongitude()).isEqualTo(180.0d);
    }

    @Test
    void fingerprintNormalizesEquivalentNumbersAndBindsCoordinateAndRadius() {
        String baseline = support.fingerprint(10.0d, 106.0d, 5);

        assertThat(support.fingerprint(10.00d, 106.000d, 5)).isEqualTo(baseline);
        assertThat(support.fingerprint(-0.0d, 0.0d, 5))
                .isEqualTo(support.fingerprint(0.0d, 0.0d, 5));
        assertThat(support.fingerprint(10.000001d, 106.0d, 5)).isNotEqualTo(baseline);
        assertThat(support.fingerprint(10.0d, 106.0d, 10)).isNotEqualTo(baseline);
        assertThat(baseline).hasSize(64);
    }

    private void assertValidationError(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }
}
