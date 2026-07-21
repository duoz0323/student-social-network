package com.stu.edu.vn.backend.storage;

/**
 * Kết quả upload ảnh đã được rút gọn, không trả metadata nội bộ của Cloudinary ra API.
 */
public record CloudinaryUploadResult(
        String url,
        String publicId
) {
}
