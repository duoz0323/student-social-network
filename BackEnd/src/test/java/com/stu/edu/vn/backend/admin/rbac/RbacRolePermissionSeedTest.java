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
    void baselineAndConsolidatedMigrationSupportAllAdminContracts() throws IOException {
        // Khóa đồng bộ contract giữa file rebuild và migration tổng duy nhất.
        assertThat(Files.readString(resolveDatabasePath("student_social_network.sql")))
                .contains("'UPDATE_ROLE_PERMISSIONS'", "'CREATE_ACADEMIC_DATA'", "'ACADEMIC_DATA'");

        assertThat(Files.readString(resolveDatabasePath("V20260816__admin_rbac_collaborator_features.sql")))
                .contains("'UPDATE_ROLE_PERMISSIONS'")
                .contains("'ENABLE_ADMIN'")
                .contains("'ADMIN_ENABLE'")
                .contains("'RESET_ADMIN_PASSWORD'")
                .contains("'ADMIN_PASSWORD_RESET'")
                .contains("'CREATE_ADMIN_ROLE'")
                .contains("'UPDATE_ADMIN_PROFILE'")
                .contains("'CHANGE_ADMIN_PASSWORD'");

        assertThat(Files.readString(resolveDatabasePath("V20260816__admin_rbac_collaborator_features.sql")))
                .contains("WHERE r.code <> 'SUPER_ADMIN'")
                .contains("'ADMIN_CREATE', 'ADMIN_ROLE_ASSIGN', 'ADMIN_ROLE_REVOKE'")
                .contains("CREATE TABLE IF NOT EXISTS `admin_social_identities`")
                .contains("CREATE TABLE IF NOT EXISTS `moderation_suggestions`")
                .contains("'COLLABORATOR_POST_CREATE'")
                .contains("r.code = 'COLLABORATOR'")
                .contains("p.code LIKE 'COLLABORATOR\\_%'")
                .contains("DROP CHECK `chk_user_profiles_completion_requires_birth_date`")
                .contains("owner_account_type <> 'MANAGED'")
                .contains("CREATE TRIGGER `trg_user_profiles_completion_birth_insert`")
                .contains("'CREATE_ACADEMIC_DATA'", "'ACADEMIC_DATA'");
    }

    private Path resolveDatabasePath(String fileName) {
        Path fromBackendDirectory = Path.of("..", "database", fileName);
        return Files.exists(fromBackendDirectory)
                ? fromBackendDirectory
                : Path.of("database", fileName);
    }
}
