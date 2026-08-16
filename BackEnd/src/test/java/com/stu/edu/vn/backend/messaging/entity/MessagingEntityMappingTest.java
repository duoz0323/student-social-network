package com.stu.edu.vn.backend.messaging.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.*;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

/** Bảo vệ mapping LAZY và composite membership theo schema đã chốt. */
class MessagingEntityMappingTest {
    @Test
    void allRelationshipsAreLazyAndEntitiesUseExpectedTables() throws Exception {
        assertThat(Conversation.class.getAnnotation(Table.class).name()).isEqualTo("conversations");
        assertThat(ConversationMember.class.getAnnotation(Table.class).name()).isEqualTo("conversation_members");
        assertThat(Message.class.getAnnotation(Table.class).name()).isEqualTo("messages");
        assertLazy(Conversation.class, "participantLow");
        assertLazy(Conversation.class, "participantHigh");
        assertLazy(Conversation.class, "lastMessage");
        assertLazy(ConversationMember.class, "conversation");
        assertLazy(ConversationMember.class, "user");
        assertLazy(ConversationMember.class, "lastReadMessage");
        assertLazy(Message.class, "conversation");
        assertLazy(Message.class, "sender");
        assertLazy(Message.class, "sharedPost");
    }

    @Test
    void memberIdIsSerializableCompositeKey() throws Exception {
        assertThat(Serializable.class).isAssignableFrom(ConversationMemberId.class);
        assertThat(ConversationMember.class.getDeclaredField("id").getAnnotation(EmbeddedId.class)).isNotNull();
        assertThat(new ConversationMemberId(1L, 2L)).isEqualTo(new ConversationMemberId(1L, 2L));
    }

    private void assertLazy(Class<?> type, String field) throws Exception {
        ManyToOne relation = type.getDeclaredField(field).getAnnotation(ManyToOne.class);
        assertThat(relation).isNotNull();
        assertThat(relation.fetch()).isEqualTo(FetchType.LAZY);
    }
}
