package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Kiểm tra source schema vì Giai đoạn 4 không được import hoặc migrate database thật. */
class PendingRegistrationSchemaContractTest {

    @Test
    void terminalLifecycleAllowsRetainedOrCleanedFlowLookupHash() throws IOException {
        String sql = Files.readString(Path.of("..", "database", "student_social_network_db.sql"));
        String lifecycleConstraint = sql.lines()
                .filter(line -> line.contains("CONSTRAINT `chk_pending_lifecycle`"))
                .findFirst()
                .orElseThrow();

        assertThat(lifecycleConstraint)
                .contains("`status` = _utf8mb4'PENDING'")
                .contains("`flow_token_hash` is not null")
                .contains("`password_hash` is null")
                .contains("`otp_hash` is null")
                .contains("`active_identifier_key` is null")
                .doesNotContain("`password_hash` is null and `flow_token_hash` is null");
    }

    @Test
    void dbmlDocumentsHmacRetentionWithoutRawToken() throws IOException {
        String dbml = Files.readString(Path.of("..", "database", "student_social_network_db.dbml"));

        assertThat(dbml)
                .contains("HMAC-SHA-256 lookup hash")
                .contains("raw token không được lưu")
                .contains("cleanup được phép đặt NULL");
    }
}
