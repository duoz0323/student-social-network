package com.stu.edu.vn.backend.admin.dto.response;

/** Thông tin media công khai, không chứa storage public id hoặc metadata nội bộ. */
public record AdminPostMediaResponse(Long mediaId, String mediaUrl, int sortOrder) {
}
