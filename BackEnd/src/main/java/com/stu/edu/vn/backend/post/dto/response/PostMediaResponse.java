package com.stu.edu.vn.backend.post.dto.response;

/**
 * Metadata ảnh bài viết trả cho Frontend, không trả Cloudinary publicId.
 */
public record PostMediaResponse(
        Long id,
        String url,
        String mimeType,
        Long fileSize,
        Integer width,
        Integer height,
        Integer displayOrder
) {
}
