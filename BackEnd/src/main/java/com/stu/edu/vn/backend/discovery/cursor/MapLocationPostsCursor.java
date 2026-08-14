package com.stu.edu.vn.backend.discovery.cursor;

import java.time.LocalDateTime;

/** Cursor versioned bind với Location để không thể tái sử dụng sai marker. */
public record MapLocationPostsCursor(
        int version,
        Long locationId,
        LocalDateTime publishedAt,
        Long postId
) {
    public static final int CURRENT_VERSION = 1;

    public boolean isValidFor(Long requestedLocationId) {
        return version == CURRENT_VERSION
                && locationId != null
                && locationId > 0
                && locationId.equals(requestedLocationId)
                && publishedAt != null
                && postId != null
                && postId > 0;
    }
}
