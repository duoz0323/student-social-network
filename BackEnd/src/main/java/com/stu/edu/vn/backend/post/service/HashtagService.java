package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionListResponse;

/**
 * Cung cấp dữ liệu hashtag đã tồn tại cho autocomplete, không làm thay đổi dữ liệu.
 */
public interface HashtagService {

    HashtagSuggestionListResponse getSuggestions(String keyword);
}
