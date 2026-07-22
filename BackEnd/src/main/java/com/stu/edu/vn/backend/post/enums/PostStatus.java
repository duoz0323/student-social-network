package com.stu.edu.vn.backend.post.enums;

/**
 * Trạng thái bài viết trong MVP, khớp ENUM status của bảng posts.
 */
public enum PostStatus {
    // Bài viết đang hoạt động và được phép hiển thị trong truy vấn thông thường.
    PUBLISHED,
    // Bài viết bị Admin ẩn khỏi Feed, tìm kiếm và hồ sơ công khai.
    HIDDEN,
    // Bài viết đã được tác giả xóa mềm và không còn hiển thị thông thường.
    DELETED
}
