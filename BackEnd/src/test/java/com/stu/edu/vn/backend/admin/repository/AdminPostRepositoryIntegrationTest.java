package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.HashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
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

/** Test tùy chọn trên MySQL thật để xác nhận cú pháp native query và số query cố định. */
@SpringBootTest(properties = {"bootstrap-admin.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"})
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class AdminPostRepositoryIntegrationTest {
    @Autowired private AdminPostRepository adminPostRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private PostMediaRepository postMediaRepository;
    @Autowired private HashtagRepository hashtagRepository;
    @Autowired private PostHashtagRepository postHashtagRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void queriesReturnModerationDataAndUseFixedStatementCounts() {
        String marker = "ap" + UUID.randomUUID().toString().replace("-", "");
        User admin = saveUser(marker + "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
        User author = saveUser(marker + "author@example.com", UserRole.USER, UserStatus.BLOCKED);
        User reporter = saveUser(marker + "reporter@example.com", UserRole.USER, UserStatus.ACTIVE);
        saveProfile(admin, marker + " Admin");
        saveProfile(author, marker + " Author");
        saveProfile(reporter, marker + " Reporter");

        Post post = new Post(author, marker + " literal 50%_=");
        post.setStatus(PostStatus.HIDDEN);
        post.setHiddenBy(admin);
        post.setHiddenAt(LocalDateTime.of(2026, 7, 15, 8, 0));
        post.setHiddenReason("SPAM");
        post.setLikeCount(5);
        post.setCommentCount(6);
        post = postRepository.saveAndFlush(post);
        postMediaRepository.saveAndFlush(new PostMedia(post, "second.jpg", marker + "-2", "image/jpeg", 10L, 1));
        postMediaRepository.saveAndFlush(new PostMedia(post, "first.jpg", marker + "-1", "image/jpeg", 10L, 0));
        Hashtag beta = hashtagRepository.saveAndFlush(new Hashtag(marker + "beta", marker + "beta"));
        Hashtag alpha = hashtagRepository.saveAndFlush(new Hashtag(marker + "alpha", marker + "alpha"));
        postHashtagRepository.saveAndFlush(new PostHashtag(post, beta));
        postHashtagRepository.saveAndFlush(new PostHashtag(post, alpha));
        reportRepository.saveAndFlush(new Report(reporter, post, ReportReason.SPAM, null, post.getContent(), "[]"));
        entityManager.clear();

        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var page = adminPostRepository.findAdminPosts(
                LikePatternEscaper.escape(marker + " literal 50%_="), "HIDDEN", author.getId(), 1,
                PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getThumbnailUrl()).isEqualTo("first.jpg");
        assertThat(page.getContent().getFirst().getMediaCount()).isEqualTo(2);
        assertThat(page.getContent().getFirst().getPendingReportCount()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getAuthorAccountStatus()).isEqualTo("BLOCKED");
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        statistics.clear();
        var detail = adminPostRepository.findAdminPostDetail(post.getId()).orElseThrow();
        var media = adminPostRepository.findAdminPostMedia(post.getId());
        var hashtags = adminPostRepository.findAdminPostHashtags(post.getId());
        assertThat(detail.getStatus()).isEqualTo("HIDDEN");
        assertThat(detail.getHiddenByAdminId()).isEqualTo(admin.getId());
        assertThat(detail.getPendingReportCount()).isEqualTo(1);
        assertThat(media).extracting(item -> item.getSortOrder()).containsExactly(0, 1);
        assertThat(hashtags).extracting(item -> item.getName())
                .containsExactly(marker + "alpha", marker + "beta");
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
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
        profile.setUsername("admin_post_" + user.getId());
        profile.setDisplayName(displayName);
        // Hồ sơ hoàn tất phải có ngày sinh để phản ánh đúng contract database.
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 15, 7, 0));
        userProfileRepository.saveAndFlush(profile);
    }
}
