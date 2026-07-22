package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.service.AdminActionService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Test tùy chọn trên MySQL thật cho cú pháp filter, thứ tự và số query batch cố định. */
@SpringBootTest(properties = {
        "bootstrap-admin.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class AdminActionRepositoryIntegrationTest {
    @Autowired private AdminActionRepository adminActionRepository;
    @Autowired private AdminActionService adminActionService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void listAndDetailUseStableOrderFiltersAndFixedBatchStatementCount() {
        String marker = "aa" + UUID.randomUUID().toString().replace("-", "");
        User admin = saveUser(marker + "admin@example.com", UserRole.ADMIN);
        User target = saveUser(marker + "target@example.com", UserRole.USER);
        saveProfile(admin, marker + " Admin");
        saveProfile(target, marker + " Target");

        AdminAction first = adminActionRepository.saveAndFlush(new AdminAction(
                admin, AdminActionType.BLOCK_USER, AdminTargetType.USER, target.getId(), "SPAM"));
        AdminAction second = adminActionRepository.saveAndFlush(new AdminAction(
                admin, AdminActionType.UNBLOCK_USER, AdminTargetType.USER, target.getId(), "ADMIN_UNBLOCK"));
        long missingTargetId = Long.MAX_VALUE - admin.getId();
        AdminAction third = adminActionRepository.saveAndFlush(new AdminAction(
                admin, AdminActionType.BLOCK_USER, AdminTargetType.USER, missingTargetId, "OTHER"));

        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 16, 8, 0);
        setCreatedAt(first.getId(), createdAt);
        setCreatedAt(second.getId(), createdAt);
        setCreatedAt(third.getId(), createdAt);
        entityManager.clear();

        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var page = adminActionService.getActions(null, AdminTargetType.USER, admin.getId(),
                createdAt.minusSeconds(1), createdAt.plusSeconds(1), 0, 20);

        assertThat(page.content()).extracting("actionId")
                .containsExactly(third.getId(), second.getId(), first.getId());
        assertThat(page.content().getFirst().target().targetAvailable()).isFalse();
        assertThat(page.content().get(1).target().targetAvailable()).isTrue();
        // Page query, count query và đúng một batch USER query; số action không làm tăng số statement.
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);

        statistics.clear();
        var filtered = adminActionService.getActions(AdminActionType.BLOCK_USER, AdminTargetType.USER,
                admin.getId(), createdAt, createdAt, 0, 20);
        assertThat(filtered.content()).extracting("actionId").containsExactly(third.getId(), first.getId());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);

        statistics.clear();
        var detail = adminActionService.getActionDetail(third.getId());
        assertThat(detail.target().targetAvailable()).isFalse();
        assertThat(detail.oldData()).isNull();
        assertThat(detail.newData()).isNull();
        // Chi tiết gồm một projection action và một batch query cho loại target tương ứng.
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }

    private User saveUser(String email, UserRole role) {
        User user = new User(email, "hash");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.saveAndFlush(user);
    }

    private void saveProfile(User user, String displayName) {
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(displayName);
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 16, 7, 0));
        userProfileRepository.saveAndFlush(profile);
    }

    private void setCreatedAt(Long actionId, LocalDateTime createdAt) {
        entityManager.createNativeQuery("UPDATE admin_actions SET created_at = :createdAt WHERE id = :actionId")
                .setParameter("createdAt", createdAt)
                .setParameter("actionId", actionId)
                .executeUpdate();
    }
}
