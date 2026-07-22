package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserStatusResponse;
import com.stu.edu.vn.backend.admin.service.AdminUserService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * API dành cho ADMIN xem và thay đổi trạng thái tài khoản USER.
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserListItemResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validatePagination(page, size);
        PageResponse<AdminUserListItemResponse> response = adminUserService.getUsers(keyword, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(
            @PathVariable @Positive Long userId
    ) {
        AdminUserDetailResponse response = adminUserService.getUserDetail(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết người dùng thành công", response));
    }

    @PatchMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<AdminUserStatusResponse>> blockUser(
            @PathVariable @Positive Long userId,
            @RequestBody AdminBlockUserRequest request
    ) {
        AdminUserStatusResponse response = adminUserService.blockUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Khóa tài khoản người dùng thành công", response));
    }

    @PatchMapping("/{userId}/unblock")
    public ResponseEntity<ApiResponse<AdminUserStatusResponse>> unblockUser(
            @PathVariable @Positive Long userId
    ) {
        AdminUserStatusResponse response = adminUserService.unblockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Mở khóa tài khoản người dùng thành công", response));
    }

    private void validatePagination(int page, int size) {
        // Giữ cùng hành vi validation với SearchController kể cả khi method-validation proxy không hoạt động.
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
