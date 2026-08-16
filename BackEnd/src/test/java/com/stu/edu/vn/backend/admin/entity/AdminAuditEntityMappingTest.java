package com.stu.edu.vn.backend.admin.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AdminAuditEntityMappingTest {

    @Test
    void accountStatusHistoryMatchesExistingTableWithoutCascadeOrUpdatedAt() throws Exception {
        assertThat(AccountStatusHistory.class.getAnnotation(Table.class).name())
                .isEqualTo("account_status_histories");
        assertLazyRelationWithoutCascade(AccountStatusHistory.class.getDeclaredField("user"));
        assertLazyRelationWithoutCascade(AccountStatusHistory.class.getDeclaredField("changedBy"));
        assertCreatedAtDatabaseManaged(AccountStatusHistory.class.getDeclaredField("createdAt"));
        assertThat(BaseAuditEntity.class.isAssignableFrom(AccountStatusHistory.class)).isFalse();
        assertThat(Arrays.stream(AccountStatusHistory.class.getDeclaredFields()).map(Field::getName))
                .doesNotContain("updatedAt");
    }

    @Test
    void adminActionMatchesExistingTableJsonColumnsAndEnums() throws Exception {
        assertThat(AdminAction.class.getAnnotation(Table.class).name()).isEqualTo("admin_actions");
        assertLazyRelationWithoutCascade(AdminAction.class.getDeclaredField("admin"));
        assertCreatedAtDatabaseManaged(AdminAction.class.getDeclaredField("createdAt"));
        assertThat(AdminAction.class.getDeclaredField("oldData").getAnnotation(Column.class).columnDefinition())
                .isEqualTo("json");
        assertThat(AdminAction.class.getDeclaredField("newData").getAnnotation(Column.class).columnDefinition())
                .isEqualTo("json");
        assertThat(BaseAuditEntity.class.isAssignableFrom(AdminAction.class)).isFalse();
        assertThat(AdminActionType.values()).extracting(Enum::name).containsExactly(
                "BLOCK_USER", "UNBLOCK_USER", "UPDATE_USER_PROFILE", "CREATE_HASHTAG", "UPDATE_HASHTAG", "DELETE_HASHTAG",
                "HIDE_POST", "RESTORE_POST",
                "RESOLVE_REPORT", "REJECT_REPORT", "RESOLVE_MODERATION_CASE", "REJECT_MODERATION_CASE",
                "RESOLVE_PROFILE_REPORT", "REJECT_PROFILE_REPORT", "CREATE_ACADEMIC_DATA",
                "UPDATE_ACADEMIC_DATA", "CHANGE_ACADEMIC_STATUS",
                "CREATE_ADMIN", "UPDATE_ADMIN", "UPDATE_ADMIN_PROFILE", "DISABLE_ADMIN", "ENABLE_ADMIN",
                "RESET_ADMIN_PASSWORD", "CHANGE_ADMIN_PASSWORD",
                "ASSIGN_ADMIN_ROLE", "REVOKE_ADMIN_ROLE",
                "CREATE_ADMIN_ROLE", "UPDATE_ROLE_PERMISSIONS",
                "CREATE_MANAGED_SOCIAL_IDENTITY", "DISABLE_MANAGED_SOCIAL_IDENTITY",
                "COLLABORATOR_POST_CREATED", "COLLABORATOR_POST_UPDATED", "COLLABORATOR_POST_DELETED",
                "MODERATION_SUGGESTION_CREATED", "MODERATION_SUGGESTION_ACCEPTED", "MODERATION_SUGGESTION_REJECTED");
        assertThat(AdminTargetType.values()).extracting(Enum::name)
                .containsExactly("USER", "POST", "HASHTAG", "REPORT", "MODERATION_CASE", "PROFILE_REPORT",
                        "ACADEMIC_DATA", "MODERATION_SUGGESTION");
        assertThat(AdminBlockReason.values()).extracting(Enum::name).containsExactly(
                "SPAM", "HARASSMENT", "HARMFUL_CONTENT", "FAKE_ACCOUNT", "REPEATED_VIOLATION",
                "PROFILE_VIOLATION", "OTHER");
    }

    private void assertLazyRelationWithoutCascade(Field field) {
        ManyToOne relation = field.getAnnotation(ManyToOne.class);
        assertThat(relation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(relation.cascade()).isEmpty();
    }

    private void assertCreatedAtDatabaseManaged(Field field) {
        Column column = field.getAnnotation(Column.class);
        assertThat(column.name()).isEqualTo("created_at");
        assertThat(column.insertable()).isFalse();
        assertThat(column.updatable()).isFalse();
    }
}
