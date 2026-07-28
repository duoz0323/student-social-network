package com.stu.edu.vn.backend.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.notification.enums.NotificationType;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class NotificationRepositoryContractTest {

    @Test
    void listUsesSingleProfileJoinProjectionStableOrderAndCountQuery() throws Exception {
        Method method = NotificationRepository.class.getMethod(
                "findVisibleNotifications", Long.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("LEFT JOIN user_profiles up ON up.user_id = n.actor_id")
                .contains("n.recipient_id = :recipientId")
                .contains("n.deleted_at IS NULL")
                .contains("n.actor_id IS NULL", "user_blocks", "ub.blocker_id", "ub.blocked_id")
                .contains("ORDER BY n.created_at DESC, n.id DESC")
                .doesNotContain("SELECT *", "password_hash", "email", "phone_number");
        assertThat(query.countQuery())
                .contains("COUNT(n.id)", "n.actor_id IS NULL", "user_blocks")
                .doesNotContain("JOIN", "ORDER BY");
    }

    @Test
    void unreadCountUsesSameBlockVisibilityRuleAsList() throws Exception {
        // Unread count phải dùng cùng điều kiện hiển thị với list để badge không làm lộ notification cũ.
        Method method = NotificationRepository.class.getMethod(
                "countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull", Long.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value()).contains(
                "n.read_at IS NULL",
                "n.actor_id IS NULL",
                "user_blocks",
                "ub.blocker_id = :recipientId",
                "ub.blocked_id = :recipientId"
        );
    }

    @Test
    void sourceDeleteQueriesAreModifyingAndReturnAffectedRows() throws Exception {
        assertDeleteContract("deleteFollowNotification", NotificationType.class, Long.class, Long.class);
        assertDeleteContract("deletePostLikeNotification", NotificationType.class, Long.class, Long.class);
        assertDeleteContract("deleteCommentNotification", Long.class);
    }

    private void assertDeleteContract(String name, Class<?>... parameters) throws Exception {
        Method method = NotificationRepository.class.getMethod(name, parameters);
        assertThat(method.getReturnType()).isEqualTo(int.class);
        assertThat(method.getAnnotation(Modifying.class)).isNotNull();
        assertThat(method.getAnnotation(Query.class).value()).contains("DELETE FROM Notification");
    }
}
