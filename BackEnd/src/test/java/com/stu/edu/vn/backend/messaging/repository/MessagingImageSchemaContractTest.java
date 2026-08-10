package com.stu.edu.vn.backend.messaging.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.*;
import org.junit.jupiter.api.Test;

/** Khóa đồng bộ cấu trúc ảnh giữa SQL canonical và DBML. */
class MessagingImageSchemaContractTest {
    @Test
    void canonicalSqlAndDbmlContainSameImageMessagingStructures() throws Exception {
        String sql = Files.readString(databasePath("student_social_network.sql"));
        String dbml = Files.readString(databasePath("student_social_network.dbml"));
        for (String token : new String[]{"message_attachments", "media_cleanup_tasks", "payload_fingerprint",
                "uq_message_attachments_message_order", "uq_message_attachments_storage_asset",
                "idx_media_cleanup_due", "fk_message_attachments_message"}) {
            assertThat(sql).contains(token);
            assertThat(dbml).contains(token);
        }
    }

    private Path databasePath(String first, String... more) {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path database = Files.isDirectory(root.resolve("database")) ? root.resolve("database")
                : root.resolve("..").resolve("database").normalize();
        return database.resolve(Path.of(first, more));
    }
}
