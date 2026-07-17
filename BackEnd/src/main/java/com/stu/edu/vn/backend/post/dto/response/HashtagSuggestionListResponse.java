package com.stu.edu.vn.backend.post.dto.response;

import java.util.List;

/**
 * Kết quả gợi ý giữ cả từ khóa gốc và từ khóa chuẩn hóa để Frontend quyết định trải nghiệm nhập liệu.
 */
public record HashtagSuggestionListResponse(
        String keyword,
        String normalizedKeyword,
        boolean exactMatch,
        List<HashtagSuggestionItemResponse> suggestions,
        boolean canUseAsNewHashtag
) {
}
