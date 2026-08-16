package com.stu.edu.vn.backend.admin.rbac.controller;

import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountListItemResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminRoleResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.ResetAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.service.AdminManagementService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API quản lý tài khoản và vai trò admin, chỉ dựa trên authority trong JWT đã ký. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/admins")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminManagementService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<AdminAccountListItemResponse>>> getAdmins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quản trị viên thành công",
                service.getAdmins(keyword, status, page, size)));
    }

    @GetMapping("/{adminId}")
    @PreAuthorize("hasAuthority('ADMIN_DETAIL_VIEW')")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> getAdmin(@PathVariable @Positive Long adminId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết quản trị viên thành công",
                service.getAdmin(adminId)));
    }

    @PostMapping
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo quản trị viên thành công", service.createAdmin(request)));
    }

    @PutMapping("/{adminId}")
    @PreAuthorize("hasAuthority('ADMIN_UPDATE')")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> updateAdmin(
            @PathVariable @Positive Long adminId,
            @Valid @RequestBody UpdateAdminRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật quản trị viên thành công",
                service.updateAdmin(adminId, request)));
    }

    @PatchMapping("/{adminId}/disable")
    @PreAuthorize("hasAuthority('ADMIN_DISABLE')")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> disableAdmin(
            @PathVariable @Positive Long adminId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa quản trị viên thành công",
                service.disableAdmin(adminId)));
    }

    @PatchMapping("/{adminId}/enable")
    @PreAuthorize("hasAuthority('ADMIN_ENABLE')")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> enableAdmin(
            @PathVariable @Positive Long adminId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Mở khóa quản trị viên thành công",
                service.enableAdmin(adminId)));
    }

    @PatchMapping("/{adminId}/password")
    @PreAuthorize("hasAuthority('ADMIN_PASSWORD_RESET')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable @Positive Long adminId,
            @Valid @RequestBody ResetAdminPasswordRequest request
    ) {
        service.resetPassword(adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Cấp lại mật khẩu quản trị viên thành công", null));
    }

    @PostMapping("/{adminId}/roles/{roleCode}")
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> assignRole(
            @PathVariable @Positive Long adminId,
            @PathVariable String roleCode
    ) {
        return ResponseEntity.ok(ApiResponse.success("Gán vai trò quản trị thành công",
                service.assignRole(adminId, roleCode)));
    }

    @PatchMapping("/{adminId}/roles/{roleCode}/revoke")
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> revokeRole(
            @PathVariable @Positive Long adminId,
            @PathVariable String roleCode
    ) {
        return ResponseEntity.ok(ApiResponse.success("Thu hồi vai trò quản trị thành công",
                service.revokeRole(adminId, roleCode)));
    }

    @GetMapping("/roles/catalog")
    @PreAuthorize("hasAnyAuthority('ADMIN_VIEW','ADMIN_CREATE','ADMIN_ROLE_ASSIGN','ADMIN_ROLE_REVOKE')")
    public ResponseEntity<ApiResponse<List<AdminRoleResponse>>> getRoleCatalog() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục vai trò thành công", service.getRoleCatalog()));
    }
}
