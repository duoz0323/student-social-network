package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.repository.ReportRepository;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/** Test tùy chọn trên MySQL thật để xác nhận native query, snapshot và số statement cố định. */
@SpringBootTest(properties = {"bootstrap-admin.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"})
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class AdminReportRepositoryIntegrationTest {
    @Autowired private AdminReportRepository adminReportRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void projectionsIncludeBlockedActorsHiddenDeletedPostsAndUseFixedQueries() {
        String marker = "ar" + UUID.randomUUID().toString().replace("-", "");
        User admin = saveUser(marker + "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
        User reporter = saveUser(marker + "reporter@example.com", UserRole.USER, UserStatus.BLOCKED);
        User reporterTwo = saveUser(marker + "reporter2@example.com", UserRole.USER, UserStatus.ACTIVE);
        User author = saveUser(marker + "author@example.com", UserRole.USER, UserStatus.BLOCKED);
        saveProfile(admin, marker + " Admin");
        saveProfile(reporter, marker + " Reporter");
        saveProfile(reporterTwo, marker + " Reporter Two");
        saveProfile(author, marker + " Author");

        Post hiddenPost = new Post(author, marker + " current hidden");
        hiddenPost.setStatus(PostStatus.HIDDEN);
        hiddenPost.setHiddenBy(admin);
        hiddenPost.setHiddenAt(LocalDateTime.of(2026, 7, 15, 9, 0));
        hiddenPost.setHiddenReason("SPAM");
        hiddenPost = postRepository.saveAndFlush(hiddenPost);
        Post deletedPost = new Post(author, marker + " current deleted");
        deletedPost.setStatus(PostStatus.DELETED);
        deletedPost.setDeletedAt(LocalDateTime.of(2026, 7, 15, 9, 30));
        deletedPost = postRepository.saveAndFlush(deletedPost);
        Post publishedPost = postRepository.saveAndFlush(new Post(author, marker + " current published"));

        Report olderPending = reportRepository.saveAndFlush(new Report(reporter, hiddenPost,
                ReportReason.SPAM, "older", marker + " evidence 50%_=",
                "[\"old-1.jpg\",\"old-2.jpg\"]"));
        Report newerPending = reportRepository.saveAndFlush(new Report(reporterTwo, deletedPost,
                ReportReason.HARASSMENT, "newer", marker + " second evidence", "[]"));
        Report earliestPending = reportRepository.saveAndFlush(new Report(reporterTwo, publishedPost,
                ReportReason.OTHER, "earliest", marker + " earliest evidence", "[]"));
        setCreatedAt(olderPending.getId(), "2026-07-15 07:00:00.000000");
        setCreatedAt(newerPending.getId(), "2026-07-15 08:00:00.000000");
        setCreatedAt(earliestPending.getId(), "2026-07-15 06:00:00.000000");

        // Chuyển bản ghi thứ hai sang RESOLVED bằng SQL để không mở rộng entity chỉ phục vụ test đọc.
        entityManager.createNativeQuery("""
                UPDATE reports SET status = 'RESOLVED', resolved_by = :adminId,
                    resolved_at = '2026-07-15 10:00:00.000000', resolution_note = 'valid'
                WHERE id = :reportId
                """).setParameter("adminId", admin.getId())
                .setParameter("reportId", newerPending.getId()).executeUpdate();
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var all = adminReportRepository.findAdminReports(null, null, null, null, author.getId(),
                LikePatternEscaper.escape(marker), 0, PageRequest.of(0, 1));
        assertThat(all.getTotalElements()).isEqualTo(3);
        assertThat(all.getContent().getFirst().getReportId()).isEqualTo(newerPending.getId());
        assertThat(all.getContent().getFirst().getPostCurrentStatus()).isEqualTo("DELETED");
        assertThat(all.getContent().getFirst().getAuthorAccountStatus()).isEqualTo("BLOCKED");
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        statistics.clear();
        var pending = adminReportRepository.findAdminReports("PENDING", "SPAM", hiddenPost.getId(),
                reporter.getId(), author.getId(), LikePatternEscaper.escape("50%_="), 1,
                PageRequest.of(0, 20));
        assertThat(pending.getContent()).hasSize(1);
        assertThat(pending.getContent().getFirst().getReporterAccountStatus()).isEqualTo("BLOCKED");
        assertThat(pending.getContent().getFirst().getPostCurrentStatus()).isEqualTo("HIDDEN");
        assertThat(pending.getContent().getFirst().getSnapshotMediaCount()).isEqualTo(2);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        var pendingQueue = adminReportRepository.findAdminReports("PENDING", null, null,
                null, author.getId(), LikePatternEscaper.escape(marker), 1, PageRequest.of(0, 20));
        assertThat(pendingQueue.getContent()).extracting(item -> item.getReportId())
                .containsExactly(earliestPending.getId(), olderPending.getId());

        statistics.clear();
        var detail = adminReportRepository.findAdminReportDetail(olderPending.getId()).orElseThrow();
        assertThat(detail.getContentSnapshot()).isEqualTo(marker + " evidence 50%_=");
        assertThat(detail.getMediaSnapshot()).isEqualTo("[\"old-1.jpg\", \"old-2.jpg\"]");
        assertThat(detail.getPostCurrentStatus()).isEqualTo("HIDDEN");
        assertThat(detail.getReporterAccountStatus()).isEqualTo("BLOCKED");
        assertThat(detail.getAuthorAccountStatus()).isEqualTo("BLOCKED");
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        var resolved = adminReportRepository.findAdminReportDetail(newerPending.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo("RESOLVED");
        assertThat(resolved.getResolvedByAdminId()).isEqualTo(admin.getId());
        assertThat(resolved.getResolutionNote()).isEqualTo("valid");
    }

    private void setCreatedAt(Long reportId, String timestamp) {
        entityManager.createNativeQuery("UPDATE reports SET created_at = :createdAt WHERE id = :reportId")
                .setParameter("createdAt", timestamp).setParameter("reportId", reportId).executeUpdate();
    }

    private User saveUser(String email, UserRole role, UserStatus status) {
        User user = new User(email, "hash");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setRole(role);
        user.setStatus(status);
        if (status == UserStatus.BLOCKED) {
            user.setBlockedAt(LocalDateTime.of(2026, 7, 15, 7, 0));
            user.setBlockedReason("SPAM");
        }
        return userRepository.saveAndFlush(user);
    }

    private void saveProfile(User user, String displayName) {
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(displayName);
        // Hồ sơ hoàn tất phải có ngày sinh để phản ánh đúng contract database.
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 15, 7, 0));
        userProfileRepository.saveAndFlush(profile);
    }
}
