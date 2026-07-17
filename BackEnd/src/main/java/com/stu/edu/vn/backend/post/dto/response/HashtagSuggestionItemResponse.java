package com.stu.edu.vn.backend.post.dto.response;

/**
 * Dữ liệu hashtag tối thiểu để Frontend hiển thị một lựa chọn autocomplete.
 */
public record HashtagSuggestionItemResponse(
        Long hashtagId,
        String name,
        int postCount
) {
}
