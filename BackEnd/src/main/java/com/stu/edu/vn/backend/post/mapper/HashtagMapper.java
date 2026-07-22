package com.stu.edu.vn.backend.post.mapper;

import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionItemResponse;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import org.springframework.stereotype.Component;

/**
 * Mapper giới hạn trường hashtag được phép trả cho chức năng autocomplete.
 */
@Component
public class HashtagMapper {

    public HashtagSuggestionItemResponse toSuggestionItem(Hashtag hashtag) {
        // displayName là tên dành cho giao diện; normalizedName chỉ phục vụ tìm kiếm và kiểm tra trùng.
        return new HashtagSuggestionItemResponse(
                hashtag.getId(),
                hashtag.getDisplayName(),
                hashtag.getPostCount()
        );
    }
}
