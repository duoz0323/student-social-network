package com.stu.edu.vn.backend.discovery.cursor;

import java.time.LocalDateTime;

/** Cursor versioned giữ trọn khóa sắp xếp Nearby và fingerprint của truy vấn vị trí. */
public record NearbyCursor(
        int version,
        Long distanceMeters,
        LocalDateTime publishedAt,
        Long postId,
        String queryFingerprint
) {
    public static final int CURRENT_VERSION = 1;

    public boolean isValid() {
        return version == CURRENT_VERSION
                && distanceMeters != null
                && distanceMeters >= 0
                && publishedAt != null
                && postId != null
                && postId > 0
                && queryFingerprint != null
                && queryFingerprint.length() == 64;
    }
}
