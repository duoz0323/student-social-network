package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionListResponse;
import com.stu.edu.vn.backend.post.service.HashtagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller nhận từ khóa autocomplete và ủy quyền toàn bộ nghiệp vụ cho HashtagService.
 */
@RestController
@RequestMapping("/api/v1/hashtags")
@RequiredArgsConstructor
public class HashtagController {

    private final HashtagService hashtagService;

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<HashtagSuggestionListResponse>> getSuggestions(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        HashtagSuggestionListResponse response = hashtagService.getSuggestions(keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy gợi ý hashtag thành công", response));
    }
}
