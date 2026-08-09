package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.AdminCreateHashtagRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagDeleteResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagUpdateResponse;
import com.stu.edu.vn.backend.admin.service.AdminHashtagService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API để ADMIN tìm kiếm và xem danh sách hashtag của hệ thống. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/hashtags")
public class AdminHashtagController {
    private final AdminHashtagService adminHashtagService;

    public AdminHashtagController(AdminHashtagService adminHashtagService) {
        this.adminHashtagService = adminHashtagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminHashtagListItemResponse>>> getHashtags(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        validatePagination(page, size);
        PageResponse<AdminHashtagListItemResponse> response =
                adminHashtagService.getHashtags(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hashtag quản trị thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminHashtagListItemResponse>> createHashtag(
            @RequestBody AdminCreateHashtagRequest request) {
        AdminHashtagListItemResponse response = adminHashtagService.createHashtag(
                request == null ? null : request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo hashtag thành công", response));
    }

    @DeleteMapping("/{hashtagId}")
    public ResponseEntity<ApiResponse<AdminHashtagDeleteResponse>> deleteHashtag(
            @PathVariable Long hashtagId) {
        if (hashtagId == null || hashtagId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        AdminHashtagDeleteResponse response = adminHashtagService.deleteHashtag(hashtagId);
        return ResponseEntity.ok(ApiResponse.success("Xóa hashtag thành công", response));
    }

    @PatchMapping("/{hashtagId}")
    public ResponseEntity<ApiResponse<AdminHashtagUpdateResponse>> updateHashtag(
            @PathVariable Long hashtagId,
            @RequestBody AdminCreateHashtagRequest request) {
        if (hashtagId == null || hashtagId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        AdminHashtagUpdateResponse response = adminHashtagService.updateHashtag(
                hashtagId, request == null ? null : request.name());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hashtag thành công", response));
    }

    private void validatePagination(int page, int size) {
        // Giữ mã lỗi ổn định kể cả khi method-validation proxy chưa được kích hoạt.
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
