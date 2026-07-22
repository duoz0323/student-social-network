package com.stu.edu.vn.backend.post.dto.response;

import com.stu.edu.vn.backend.post.enums.PostMediaType;

/**
 * Metadata ảnh bài viết trả cho Frontend, không trả Cloudinary publicId.
 */
public record PostMediaResponse(
        Long id,
        String url,
        PostMediaType mediaType,
        String mimeType,
        Long fileSize,
        Integer width,
        Integer height,
        Integer durationSeconds,
        String thumbnailUrl,
        Integer displayOrder
) {
}
