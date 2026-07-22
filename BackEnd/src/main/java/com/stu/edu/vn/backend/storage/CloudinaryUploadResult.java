package com.stu.edu.vn.backend.storage;

/**
 * Kết quả upload ảnh đã được rút gọn, không trả metadata nội bộ của Cloudinary ra API.
 */
public record CloudinaryUploadResult(
        String url,
        String publicId,
        String mimeType,
        Long fileSize,
        Integer width,
        Integer height,
        Integer durationSeconds,
        String thumbnailUrl
) {

    public CloudinaryUploadResult(String url, String publicId) {
        // Constructor rút gọn giữ tương thích cho avatar khi không cần metadata ảnh chi tiết.
        this(url, publicId, null, null, null, null, null, null);
    }

    public CloudinaryUploadResult(String url, String publicId, String mimeType, Long fileSize,
            Integer width, Integer height) {
        // Constructor tương thích cho upload ảnh không có duration/thumbnail.
        this(url, publicId, mimeType, fileSize, width, height, null, null);
    }
}
