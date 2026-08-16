package com.stu.edu.vn.backend.admin.rbac.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.rbac.repository.AdminPermissionRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.security.AdminAuthorization;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatabaseAdminAuthorityResolverTest {

    private final AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
    private final AdminPermissionRepository permissions = mock(AdminPermissionRepository.class);
    private final DatabaseAdminAuthorityResolver resolver = new DatabaseAdminAuthorityResolver(assignments, permissions);

    @Test
    void combinesPermissionsFromAllAssignedRoles() {
        when(assignments.findRoleCodes(10L)).thenReturn(List.of("MODERATOR", "COLLABORATOR"));
        when(permissions.findEffectiveCodes(10L)).thenReturn(List.of("POST_VIEW", "POST_HIDE", "HASHTAG_VIEW"));

        AdminAuthorization result = resolver.resolve(10L);

        assertThat(result.roles()).containsExactlyInAnyOrder("MODERATOR", "COLLABORATOR");
        assertThat(result.permissions()).containsExactlyInAnyOrder("POST_VIEW", "POST_HIDE", "HASHTAG_VIEW");
    }

    @Test
    void superAdminReceivesFuturePermissionWithoutRoleMappingChange() {
        when(assignments.findRoleCodes(1L)).thenReturn(List.of("SUPER_ADMIN"));
        when(permissions.findAllCodes()).thenReturn(List.of("POST_VIEW", "FUTURE_PERMISSION"));

        assertThat(resolver.resolve(1L).permissions()).containsExactlyInAnyOrder("POST_VIEW", "FUTURE_PERMISSION");
    }
}
