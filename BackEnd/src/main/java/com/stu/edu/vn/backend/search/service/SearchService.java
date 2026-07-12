package com.stu.edu.vn.backend.search.service;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchUserResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.enums.SearchPostType;

/**
 * Service tìm kiếm người dùng trong phạm vi MVP.
 */
public interface SearchService {

    PageResponse<SearchUserResponse> searchUsers(String keyword, int page, int size);

    PageResponse<SearchPostResponse> searchPosts(String keyword, SearchPostType type, int page, int size);
}
