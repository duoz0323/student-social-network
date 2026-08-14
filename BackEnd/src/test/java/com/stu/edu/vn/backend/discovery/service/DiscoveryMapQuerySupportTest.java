package com.stu.edu.vn.backend.discovery.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Bao phủ biên tọa độ, anti-meridian V1 và giới hạn page Location Posts. */
class DiscoveryMapQuerySupportTest {
    private final DiscoveryMapQuerySupport support = new DiscoveryMapQuerySupport();

    @Test
    void acceptsCoordinateBoundariesAndValidPostLimits() {
        support.validateViewport(90.0d, -90.0d, 180.0d, -180.0d);
        support.validateLocationPosts(1L, 1);
        support.validateLocationPosts(Long.MAX_VALUE, 20);
    }

    @ParameterizedTest
    @MethodSource("invalidViewports")
    void rejectsInvalidViewport(double north, double south, double east, double west) {
        assertValidation(() -> support.validateViewport(north, south, east, west));
    }

    @ParameterizedTest
    @MethodSource("invalidLocationPosts")
    void rejectsInvalidLocationOrLimit(Long locationId, int limit) {
        assertValidation(() -> support.validateLocationPosts(locationId, limit));
    }

    private static java.util.stream.Stream<Arguments> invalidViewports() {
        return java.util.stream.Stream.of(
                Arguments.of(90.1d, 0.0d, 1.0d, 0.0d),
                Arguments.of(1.0d, -90.1d, 1.0d, 0.0d),
                Arguments.of(1.0d, 0.0d, 180.1d, 0.0d),
                Arguments.of(1.0d, 0.0d, 1.0d, -180.1d),
                Arguments.of(0.0d, 0.0d, 1.0d, 0.0d),
                Arguments.of(-1.0d, 0.0d, 1.0d, 0.0d),
                Arguments.of(1.0d, 0.0d, 0.0d, 0.0d),
                Arguments.of(1.0d, 0.0d, -1.0d, 0.0d),
                Arguments.of(Double.NaN, 0.0d, 1.0d, 0.0d),
                Arguments.of(1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d));
    }

    private static java.util.stream.Stream<Arguments> invalidLocationPosts() {
        return java.util.stream.Stream.of(
                Arguments.of(null, 10),
                Arguments.of(0L, 10),
                Arguments.of(1L, 0),
                Arguments.of(1L, 21));
    }

    private void assertValidation(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }
}
