package com.stu.edu.vn.backend.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

class NotificationEntityMappingTest {

    @Test
    void notificationMapsExpectedTableAuditAndLazyRelations() throws Exception {
        assertThat(Notification.class.getAnnotation(Table.class).name()).isEqualTo("notifications");
        assertThat(BaseAuditEntity.class).isAssignableFrom(Notification.class);
        assertLazyRelation("recipient", "recipient_id");
        assertLazyRelation("actor", "actor_id");
        assertLazyRelation("post", "post_id");
        assertLazyRelation("comment", "comment_id");
    }

    private void assertLazyRelation(String fieldName, String columnName) throws Exception {
        ManyToOne relation = Notification.class.getDeclaredField(fieldName).getAnnotation(ManyToOne.class);
        JoinColumn joinColumn = Notification.class.getDeclaredField(fieldName).getAnnotation(JoinColumn.class);
        assertThat(relation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(joinColumn.name()).isEqualTo(columnName);
    }
}
