package com.stu.edu.vn.backend.admin.dto.response;

/** Kết quả đổi tên hashtag; các quan hệ bài viết của hashtag không thay đổi. */
public record AdminHashtagUpdateResponse(
        Long hashtagId,
        String name
) {
}
