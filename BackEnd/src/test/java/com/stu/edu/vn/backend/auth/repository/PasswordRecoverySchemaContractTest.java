package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/** Bảo vệ schema khởi tạo duy nhất và bảng Password Recovery legacy. */
class PasswordRecoverySchemaContractTest {
    @Test
    void bootstrapSchemaContainsProductionColumnsAndConstraints() throws Exception {
        String schema = Files.readString(DatabaseSourcePaths.resolve("student_social_network.sql"));
        assertThat(schema).contains("`password_recovery_challenges`")
                .contains("`is_decoy`").contains("`recovery_flow_token_hash`")
                .contains("`reset_token_hash`").contains("`completed_at`")
                .contains("`idx_recovery_user_status`").contains("`chk_recovery_decoy`");
    }

    @Test
    void legacyTableRemainsUntouched() throws Exception {
        String schema = Files.readString(DatabaseSourcePaths.resolve("student_social_network.sql"));
        assertThat(schema).contains("CREATE TABLE `password_reset_tokens`")
                .contains("`token_hash` char(64) NOT NULL")
                .contains("`idx_password_reset_tokens_user_state`");
    }
}

