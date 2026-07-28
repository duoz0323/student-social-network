package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test tích hợp tùy chọn trên MySQL thật để xác nhận cú pháp projection, bộ lọc và số lượng query.
 */
@SpringBootTest(properties = {
        "bootstrap-admin.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class AdminUserRepositoryIntegrationTest {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void queriesSearchFilterOrderExcludeAdminAndDoNotCauseNPlusOne() {
        String marker = "adm" + UUID.randomUUID().toString().replace("-", "");
        User activeUser = saveUser(marker + "ALPHA@example.com", UserRole.USER, UserStatus.ACTIVE);
        User blockedUser = saveUser(marker + "blocked@example.com", UserRole.USER, UserStatus.BLOCKED);
        User admin = saveUser(marker + "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
        saveProfile(activeUser, marker + " Name 50%_off=now", true);
        saveProfile(blockedUser, marker + " Blocked", false);
        saveProfile(admin, marker + " Admin", true);
        entityManager.flush();
        entityManager.clear();

        assertThat(adminUserRepository.findManagedUsers(
                marker.toLowerCase() + "alpha@example.com", null, PageRequest.of(0, 20)).getContent())
                .extracting(AdminUserListProjection::getUserId)
                .containsExactly(activeUser.getId());
        assertThat(adminUserRepository.findManagedUsers(
                marker.toUpperCase() + " NAME", null, PageRequest.of(0, 20)).getContent())
                .extracting(AdminUserListProjection::getUserId)
                .containsExactly(activeUser.getId());
        assertThat(adminUserRepository.findManagedUsers(
                "50=%=_off==now", null, PageRequest.of(0, 20)).getContent())
                .extracting(AdminUserListProjection::getUserId)
                .containsExactly(activeUser.getId());
        assertThat(adminUserRepository.findManagedUsers(
                null, null, PageRequest.of(0, 1)).getContent()).hasSize(1);

        List<Long> activeIds = adminUserRepository.findManagedUsers(
                        marker, "ACTIVE", PageRequest.of(0, 20)).getContent().stream()
                .map(AdminUserListProjection::getUserId)
                .toList();
        List<Long> blockedIds = adminUserRepository.findManagedUsers(
                        marker, "BLOCKED", PageRequest.of(0, 20)).getContent().stream()
                .map(AdminUserListProjection::getUserId)
                .toList();
        assertThat(activeIds).containsExactly(activeUser.getId()).doesNotContain(admin.getId());
        assertThat(blockedIds).containsExactly(blockedUser.getId());

        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var orderedPage = adminUserRepository.findManagedUsers(marker, null, PageRequest.of(0, 100));
        orderedPage.getContent().forEach(item -> {
            item.getDisplayName();
            item.getEmail();
            item.getProfileCompletedAt();
        });
        assertThat(orderedPage.getContent())
                .extracting(AdminUserListProjection::getUserId)
                .containsExactly(blockedUser.getId(), activeUser.getId());
        // Một data query và nhiều nhất một count query, không tăng theo số phần tử của trang.
        assertThat(statistics.getPrepareStatementCount()).isBetween(1L, 2L);

        statistics.clear();
        var detail = adminUserRepository.findManagedUserDetail(blockedUser.getId()).orElseThrow();
        assertThat(detail.getProfileCompletedAt()).isNull();
        assertThat(detail.getRole()).isEqualTo("USER");
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    private User saveUser(String email, UserRole role, UserStatus status) {
        User user = new User(email, "hash");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setRole(role);
        user.setStatus(status);
        if (status == UserStatus.BLOCKED) {
            // Fixture BLOCKED phải thỏa check constraint dữ liệu khóa hiện có của bảng users.
            user.setBlockedAt(LocalDateTime.of(2026, 7, 14, 8, 0));
            user.setBlockedReason("SPAM");
        }
        return userRepository.saveAndFlush(user);
    }

    private void saveProfile(User user, String displayName, boolean completed) {
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(displayName);
        // Chỉ hồ sơ đã hoàn tất mới cần ngày sinh theo check constraint hiện hành.
        profile.setDateOfBirth(completed ? LocalDate.of(2000, 1, 1) : null);
        profile.setProfileCompletedAt(completed ? LocalDateTime.of(2026, 7, 14, 8, 0) : null);
        userProfileRepository.saveAndFlush(profile);
    }
}
