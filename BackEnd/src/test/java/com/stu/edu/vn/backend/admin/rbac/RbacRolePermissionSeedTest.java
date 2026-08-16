package com.stu.edu.vn.backend.admin.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Khóa ma trận seed RBAC để tránh vô tình tráo quyền ADS_MANAGER và MODERATOR. */
class RbacRolePermissionSeedTest {

    @Test
    void roleSeedMatchesApprovedAdminMenuMatrix() throws IOException {
        String sql = Files.readString(resolveDatabasePath("student_social_network.sql"));

        assertThat(sql).contains(
                "p.code IN ('DASHBOARD_BASIC_VIEW','USER_VIEW','USER_SEARCH','USER_FILTER',"
                        + "'USER_DETAIL_VIEW','USER_PROFILE_UPDATE','USER_BLOCK','USER_UNBLOCK','USER_ANALYTICS_VIEW') "
                        + "WHERE r.code = 'USER_MANAGER'",
                "p.code IN ('DASHBOARD_BASIC_VIEW','POST_VIEW','POST_HIDE','POST_RESTORE',"
                        + "'HASHTAG_VIEW','HASHTAG_SEARCH','HASHTAG_DELETE','REPORT_VIEW',"
                        + "'REPORT_DETAIL_VIEW','REPORT_RESOLVE_NO_VIOLATION','REPORT_RESOLVE_ACTION') "
                        + "WHERE r.code = 'MODERATOR'",
                "p.code = 'DASHBOARD_BASIC_VIEW' WHERE r.code = 'ADS_MANAGER'",
                "('COLLABORATOR', 'Collaborator', 'Tạo nội dung, tương tác xã hội và gửi đề xuất kiểm duyệt bằng Managed Social Identity.', 1)",
                "r.code = 'COLLABORATOR' AND p.code LIKE 'COLLABORATOR\\_%'",
                "p.code IN ('MODERATION_SUGGESTION_VIEW','MODERATION_SUGGESTION_DETAIL_VIEW',"
                        + "'MODERATION_SUGGESTION_REVIEW') WHERE r.code = 'MODERATOR'"
        );
    }

    @Test
    void baselineAndRuntimeMigrationsSupportRolePermissionAuditAction() throws IOException {
        // Khóa đồng bộ ENUM để cập nhật permission không rollback khi ghi lịch sử quản trị.
        assertThat(Files.readString(resolveDatabasePath("student_social_network.sql")))
                .contains("'UPDATE_ROLE_PERMISSIONS'");
        assertThat(Files.readString(resolveDatabasePath("rbac_role_permissions_20260812.sql")))
                .contains("'UPDATE_ROLE_PERMISSIONS'");
        assertThat(Files.readString(resolveDatabasePath("fix_admin_action_type_20260813.sql")))
                .contains("'UPDATE_ROLE_PERMISSIONS'")
                .contains("SHOW COLUMNS FROM `admin_actions` LIKE 'action_type'");
        assertThat(Files.readString(resolveDatabasePath("admin_enable_20260813.sql")))
                .contains("'ENABLE_ADMIN'")
                .contains("'ADMIN_ENABLE'")
                .contains("WHERE r.code = 'SUPER_ADMIN'");
        assertThat(Files.readString(resolveDatabasePath("admin_password_reset_20260814.sql")))
                .contains("'RESET_ADMIN_PASSWORD'")
                .contains("'ADMIN_PASSWORD_RESET'")
                .contains("WHERE r.code = 'SUPER_ADMIN'");
        assertThat(Files.readString(resolveDatabasePath("admin_role_creation_20260814.sql")))
                .contains("'CREATE_ADMIN_ROLE'")
                .contains("'RESET_ADMIN_PASSWORD'");
        assertThat(Files.readString(resolveDatabasePath("admin_self_profile_20260814.sql")))
                .contains("'UPDATE_ADMIN_PROFILE'")
                .contains("'CHANGE_ADMIN_PASSWORD'");
        assertThat(Files.readString(resolveDatabasePath("bootstrap_admin_delegation_guard_20260815.sql")))
                .contains("r.code <> 'SUPER_ADMIN'")
                .contains("'ADMIN_CREATE', 'ADMIN_ROLE_ASSIGN', 'ADMIN_ROLE_REVOKE'");
        assertThat(Files.readString(resolveDatabasePath("collaborator_managed_social_identity_20260815.sql")))
                .contains("CREATE TABLE `admin_social_identities`")
                .contains("CREATE TABLE `moderation_suggestions`")
                .contains("'COLLABORATOR_POST_CREATE'");
        assertThat(Files.readString(resolveDatabasePath("collaborator_role_lifecycle_20260815.sql")))
                .contains("`reserved` = 1")
                .contains("r.code = 'COLLABORATOR'")
                .contains("p.code LIKE 'COLLABORATOR\\_%'");
        assertThat(Files.readString(resolveDatabasePath("managed_profile_completion_hotfix_20260815.sql")))
                .contains("DROP CHECK `chk_user_profiles_completion_requires_birth_date`")
                .contains("owner_account_type <> 'MANAGED'")
                .contains("CREATE TRIGGER `trg_user_profiles_completion_birth_insert`");
    }

    private Path resolveDatabasePath(String fileName) {
        Path fromBackendDirectory = Path.of("..", "database", fileName);
        return Files.exists(fromBackendDirectory)
                ? fromBackendDirectory
                : Path.of("database", fileName);
    }
}
