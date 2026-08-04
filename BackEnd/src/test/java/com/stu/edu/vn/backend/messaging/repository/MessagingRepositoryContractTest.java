package com.stu.edu.vn.backend.messaging.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

/** Kiểm tra query keyset, block filter và unread không lệch contract. */
class MessagingRepositoryContractTest {
    @Test
    void inboxUsesStableKeysetBlockFilterAndNoTotalCount() throws Exception {
        Method method = ConversationRepository.class.getMethod(
                "findInboxAfter", Long.class, java.time.LocalDateTime.class, Long.class, int.class);
        String query = method.getAnnotation(Query.class).value();
        assertThat(query).contains("c.last_message_id IS NOT NULL", "user_blocks",
                "c.last_message_at < :cursorAt", "c.id < :cursorId",
                "ORDER BY c.last_message_at DESC, c.id DESC", "LIMIT :fetchLimit")
                .doesNotContain("password_hash", "email");
    }

    @Test
    void messageHistoryAndUnreadUseRequiredPredicates() throws Exception {
        Query history = MessageRepository.class.getMethod(
                "findAfter", Long.class, Long.class, int.class).getAnnotation(Query.class);
        Query unread = MessageRepository.class.getMethod("countUnread", Long.class).getAnnotation(Query.class);
        assertThat(history.value()).contains("conversation_id = :conversationId", "id < :cursorId",
                "ORDER BY id DESC", "LIMIT :fetchLimit");
        assertThat(unread.value()).contains("m.sender_id <> :userId",
                "m.id > COALESCE(cm.last_read_message_id, 0)", "user_blocks",
                "c.last_message_id IS NOT NULL");
    }

    @Test
    void typingQueryChecksBothParticipantsProfileMembershipAndBlockWithoutMessageAccess() throws Exception {
        Query typing = ConversationRepository.class.getMethod(
                "findTypingTarget", Long.class, Long.class).getAnnotation(Query.class);
        assertThat(typing.value()).contains(
                "sender_member.user_id = :senderId",
                "sender_user.role = 'USER'", "sender_user.status = 'ACTIVE'",
                "recipient_user.role = 'USER'", "recipient_user.status = 'ACTIVE'",
                "sender_profile.profile_completed_at IS NOT NULL",
                "recipient_profile.profile_completed_at IS NOT NULL", "user_blocks")
                .doesNotContain("messages ", "notifications", "user_restrictions");
    }
}
