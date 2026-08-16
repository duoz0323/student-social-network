package com.stu.edu.vn.backend.admin.rbac.service;

import com.stu.edu.vn.backend.admin.rbac.AdminRoleCode;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminPermissionRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.security.AdminAuthorization;
import com.stu.edu.vn.backend.security.AdminAuthorityResolver;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Hợp quyền theo tất cả role; SUPER_ADMIN tự nhận cả permission được bổ sung trong tương lai. */
@Service
@RequiredArgsConstructor
public class DatabaseAdminAuthorityResolver implements AdminAuthorityResolver {

    private final AdminRoleAssignmentRepository assignmentRepository;
    private final AdminPermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminAuthorization resolve(Long adminId) {
        List<String> roles = assignmentRepository.findRoleCodes(adminId);
        Set<String> permissions = roles.contains(AdminRoleCode.SUPER_ADMIN.name())
                ? new LinkedHashSet<>(permissionRepository.findAllCodes())
                : new LinkedHashSet<>(permissionRepository.findEffectiveCodes(adminId));
        return new AdminAuthorization(new LinkedHashSet<>(roles), permissions);
    }
}
