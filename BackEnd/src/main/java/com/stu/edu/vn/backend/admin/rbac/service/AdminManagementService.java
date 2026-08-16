package com.stu.edu.vn.backend.admin.rbac.service;

import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountListItemResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminRoleResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminPermissionResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRoleRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.ResetAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.ChangeAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminProfileRequest;
import com.stu.edu.vn.backend.common.api.PageResponse;
import java.util.List;
import java.util.Set;

public interface AdminManagementService {
    PageResponse<AdminAccountListItemResponse> getAdmins(String keyword, String status, int page, int size);
    AdminAccountResponse getAdmin(Long adminId);
    AdminAccountResponse getCurrentAdminProfile();
    AdminAccountResponse updateCurrentAdminProfile(UpdateAdminProfileRequest request);
    void changeCurrentAdminPassword(ChangeAdminPasswordRequest request);
    AdminAccountResponse createAdmin(CreateAdminRequest request);
    AdminAccountResponse updateAdmin(Long adminId, UpdateAdminRequest request);
    AdminAccountResponse disableAdmin(Long adminId);
    AdminAccountResponse enableAdmin(Long adminId);
    void resetPassword(Long adminId, ResetAdminPasswordRequest request);
    AdminAccountResponse assignRole(Long adminId, String roleCode);
    AdminAccountResponse revokeRole(Long adminId, String roleCode);
    List<AdminRoleResponse> getRoleCatalog();
    AdminRoleResponse createRole(CreateAdminRoleRequest request);
    List<AdminPermissionResponse> getPermissionCatalog();
    AdminRoleResponse updateRolePermissions(String roleCode, Set<String> permissionCodes);
}
