package com.stu.edu.vn.backend.messaging.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.*;
import org.junit.jupiter.api.Test;

/** Khóa đồng bộ SQL canonical và DBML trước khi chạy MySQL integration. */
class MessagingSchemaContractTest {
    @Test
    void fullSqlAndDbmlDescribeSameTablesKeysAndIndexes() throws Exception {
        String sql = Files.readString(databasePath("student_social_network.sql"));
        String dbml = Files.readString(databasePath("student_social_network.dbml"));
        for (String token : new String[]{"conversations", "conversation_members", "messages",
                "uq_conversations_participant_pair", "idx_conversations_last_message",
                "idx_conversation_members_user_cursor", "uq_messages_sender_client_message",
                "idx_messages_conversation_cursor", "fk_messages_sender_member"}) {
            assertThat(sql).contains(token);
            assertThat(dbml).contains(token);
        }
    }

    @Test
    void restCoreDoesNotDependOnNotificationOrWebSocketPublisher() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path source = (Files.isDirectory(root.resolve("src")) ? root : root.resolve("BackEnd"))
                .resolve("src/main/java/com/stu/edu/vn/backend/messaging/service/impl/MessagingServiceImpl.java");
        // Giai đoạn 1C cho phép publish domain event nhẹ, nhưng REST core không được phụ thuộc broker.
        assertThat(Files.readString(source)).doesNotContain(
                ".notification.", "SimpMessagingTemplate", "convertAndSendToUser");
    }

    private Path databasePath(String first, String... more) {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path database = Files.isDirectory(root.resolve("database")) ? root.resolve("database")
                : root.resolve("..").resolve("database").normalize();
        return database.resolve(Path.of(first, more));
    }
}
