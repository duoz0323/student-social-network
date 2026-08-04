package com.stu.edu.vn.backend.storage;

import java.time.OffsetDateTime;

/** Kết quả cấp quyền truy cập Cloudinary có thời hạn. */
public record CloudinaryAccessResult(String url, OffsetDateTime expiresAt) {
}
