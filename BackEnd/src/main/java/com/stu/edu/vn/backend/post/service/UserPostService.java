package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;

/** Đọc danh sách bài công khai trên hồ sơ người dùng bằng cursor. */
public interface UserPostService {
    CursorPageResponse<FeedPostResponse> getUserPosts(Long userId, String cursor, int limit);
}
