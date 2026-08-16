package com.stu.edu.vn.backend.admin.rbac.controller;

import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.ChangeAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminProfileRequest;
import com.stu.edu.vn.backend.admin.rbac.service.AdminManagementService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API tự quản lý hồ sơ của quản trị viên; danh tính luôn lấy từ JWT. */
@RestController
@RequestMapping("/api/v1/admin/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    private final AdminManagementService service;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminAccountResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hồ sơ quản trị viên thành công", service.getCurrentAdminProfile()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<AdminAccountResponse>> updateProfile(
            @Valid @RequestBody UpdateAdminProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật hồ sơ quản trị viên thành công", service.updateCurrentAdminProfile(request)));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangeAdminPasswordRequest request
    ) {
        service.changeCurrentAdminPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi mật khẩu thành công. Vui lòng đăng nhập lại", null));
    }
}
