package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.response.AdminPostDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostStatusResponse;
import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.admin.service.AdminPostService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API để ADMIN xem, ẩn và khôi phục bài viết theo state machine kiểm duyệt. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/posts")
public class AdminPostController {
    private final AdminPostService adminPostService;

    public AdminPostController(AdminPostService adminPostService) {
        this.adminPostService = adminPostService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminPostListItemResponse>>> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) @Positive Long authorId,
            @RequestParam(defaultValue = "false") boolean reportedOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        validatePagination(page, size);
        validateAuthorId(authorId);
        PageResponse<AdminPostListItemResponse> response = adminPostService.getPosts(
                keyword, status, authorId, reportedOnly, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài viết quản trị thành công", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<AdminPostDetailResponse>> getPostDetail(
            @PathVariable @Positive Long postId) {
        AdminPostDetailResponse response = adminPostService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết bài viết quản trị thành công", response));
    }

    @PatchMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<AdminPostStatusResponse>> hidePost(
            @PathVariable @Positive Long postId,
            @RequestBody AdminHidePostRequest request) {
        AdminPostStatusResponse response = adminPostService.hidePost(postId, request);
        return ResponseEntity.ok(ApiResponse.success("Ẩn bài viết thành công", response));
    }

    @PatchMapping("/{postId}/restore")
    public ResponseEntity<ApiResponse<AdminPostStatusResponse>> restorePost(
            @PathVariable @Positive Long postId) {
        AdminPostStatusResponse response = adminPostService.restorePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục bài viết thành công", response));
    }

    private void validatePagination(int page, int size) {
        // Giữ cùng mã lỗi kể cả khi method-validation proxy không hoạt động.
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateAuthorId(Long authorId) {
        // Kiểm tra phòng thủ để hành vi không phụ thuộc việc method-validation proxy đã được kích hoạt hay chưa.
        if (authorId != null && authorId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
