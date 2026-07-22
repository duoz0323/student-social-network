package com.stu.edu.vn.backend.search.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.search.dto.response.SearchUserResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.search.service.SearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tìm kiếm chỉ nhận query parameter và ủy quyền nghiệp vụ cho Service.
 */
@Validated
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<SearchUserResponse>>> searchUsers(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validatePagination(page, size);
        // currentUserId không có trong request; Service luôn lấy từ JWT/SecurityContext.
        PageResponse<SearchUserResponse> response = searchService.searchUsers(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm người dùng thành công", response));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<SearchPostResponse>>> searchPosts(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "type", required = false) SearchPostType type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validatePagination(page, size);
        // Client phải truyền type rõ ràng; Service không suy luận CONTENT/HASHTAG từ ký tự #.
        PageResponse<SearchPostResponse> response = searchService.searchPosts(keyword, type, page, size);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm bài viết thành công", response));
    }

    private void validatePagination(int page, int size) {
        // Kiểm tra biên request tối thiểu để bảo vệ cả trường hợp Controller được gọi ngoài method-validation proxy.
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
