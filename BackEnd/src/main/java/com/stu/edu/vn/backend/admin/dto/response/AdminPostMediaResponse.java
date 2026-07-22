package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.post.enums.PostMediaType;

/** Thông tin media công khai, không chứa storage public id hoặc metadata nội bộ. */
public record AdminPostMediaResponse(Long mediaId, String mediaUrl, PostMediaType mediaType,
        String mimeType, Integer durationSeconds, String thumbnailUrl, int sortOrder) {
}
