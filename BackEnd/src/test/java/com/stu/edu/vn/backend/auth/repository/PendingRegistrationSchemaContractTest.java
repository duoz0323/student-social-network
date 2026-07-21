package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/** Kiểm tra source schema vì Giai đoạn 4 không được import hoặc migrate database thật. */
class PendingRegistrationSchemaContractTest {

    @Test
    void terminalLifecycleAllowsRetainedOrCleanedFlowLookupHash() throws IOException {
        String sql = Files.readString(DatabaseSourcePaths.resolve("student_social_network.sql"));
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
    void bootstrapSchemaStoresOnlyRegistrationHashes() throws IOException {
        String sql = Files.readString(DatabaseSourcePaths.resolve("student_social_network.sql"));

        assertThat(sql)
                .contains("`flow_token_hash` char(64)")
                .contains("`otp_hash` char(64)")
                .doesNotContain("`raw_flow_token`")
                .doesNotContain("`raw_otp`");
    }
}
