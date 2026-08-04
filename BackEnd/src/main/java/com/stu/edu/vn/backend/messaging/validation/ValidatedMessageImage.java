package com.stu.edu.vn.backend.messaging.validation;

import org.springframework.web.multipart.MultipartFile;

/** Kết quả validation dùng cho fingerprint và upload sau khi toàn bộ request hợp lệ. */
public record ValidatedMessageImage(MultipartFile file, String actualMimeType, long fileSizeBytes,
        int width, int height, String sha256) {
}
