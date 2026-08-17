package com.stu.edu.vn.backend.search.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchUserResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.enums.SearchPostType;

/**
 * Service tìm kiếm người dùng trong phạm vi MVP.
 */
public interface SearchService {

    PageResponse<SearchUserResponse> searchUsers(String keyword, int page, int size);

    CursorPageResponse<SearchPostResponse> searchPosts(
            String keyword, SearchPostType type, String cursor, int limit);

    /** Tìm bài viết với viewer đã được module gọi xác thực và resolve trước. */
    CursorPageResponse<SearchPostResponse> searchPostsAs(
            Long viewerId, String keyword, SearchPostType type, String cursor, int limit);
}
