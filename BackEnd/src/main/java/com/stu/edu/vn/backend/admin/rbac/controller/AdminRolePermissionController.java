package com.stu.edu.vn.backend.admin.rbac.controller;

import com.stu.edu.vn.backend.admin.rbac.dto.AdminPermissionResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminRoleResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRoleRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateRolePermissionsRequest;
import com.stu.edu.vn.backend.admin.rbac.service.AdminManagementService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API riêng cho màn hình ma trận role-permission. */
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRolePermissionController {

    private final AdminManagementService service;

    @GetMapping
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<List<AdminRoleResponse>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vai trò thành công", service.getRoleCatalog()));
    }

    @PostMapping
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<AdminRoleResponse>> createRole(
            @Valid @RequestBody CreateAdminRoleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo vai trò quản trị thành công", service.createRole(request)));
    }

    @GetMapping("/permissions")
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<List<AdminPermissionResponse>>> getPermissions() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh mục permission thành công", service.getPermissionCatalog()));
    }

    @PutMapping("/{roleCode}/permissions")
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<AdminRoleResponse>> updatePermissions(
            @PathVariable String roleCode,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật permission cho vai trò thành công",
                service.updateRolePermissions(roleCode, request.permissionCodes())));
    }
}
