package com.stu.edu.vn.backend.admin.dto.response;

/** Kết quả xóa hashtag và số bài viết đã được chuyển về trạng thái không có hashtag. */
public record AdminHashtagDeleteResponse(
        Long hashtagId,
        String name,
        int detachedPostCount
) {
}
