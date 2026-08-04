package com.stu.edu.vn.backend.report.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.report.dto.request.CreateReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateReportResponse;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.entity.ModerationCase;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.mapper.ReportMapper;
import com.stu.edu.vn.backend.report.repository.ReportRepository;
import com.stu.edu.vn.backend.report.repository.ModerationCaseRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class ReportServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostMediaRepository postMediaRepository = org.mockito.Mockito.mock(PostMediaRepository.class);
    private final ReportRepository reportRepository = org.mockito.Mockito.mock(ReportRepository.class);
    private final ModerationCaseRepository moderationCaseRepository = org.mockito.Mockito.mock(ModerationCaseRepository.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);

    private ReportServiceImpl reportService;
    private User reporter;
    private Post reportedPost;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                postRepository,
                postMediaRepository,
                reportRepository,
                moderationCaseRepository,
                new ReportMapper(),
                entityManager,
                Clock.fixed(Instant.parse("2026-07-12T03:00:00Z"), ZoneOffset.UTC)
        );

        reporter = user(10L, UserStatus.ACTIVE);
        reportedPost = post(1L, user(20L, UserStatus.ACTIVE), "Noi dung goc", PostStatus.PUBLISHED);
        reportedPost.getMedia().add(media(reportedPost, "https://cdn.example/second.jpg", 1));
        reportedPost.getMedia().add(media(reportedPost, "https://cdn.example/first.jpg", 0));

        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(reporter));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(reporter)));
        when(postRepository.findReportTargetByIdForUpdate(1L)).thenReturn(Optional.of(reportedPost));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L)).thenReturn(reportedPost.getMedia());
        ModerationCase moderationCase = new ModerationCase(reportedPost, LocalDateTime.of(2026, 7, 12, 10, 0));
        ReflectionTestUtils.setField(moderationCase, "id", 50L);
        when(moderationCaseRepository.findByPost_IdAndStatus(1L, ModerationCaseStatus.OPEN))
                .thenReturn(Optional.of(moderationCase));
        when(reportRepository.existsEffectiveReport(10L, 1L, ModerationCaseStatus.OPEN))
                .thenReturn(false);
        when(reportRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            ReflectionTestUtils.setField(report, "id", 100L);
            ReflectionTestUtils.setField(report, "createdAt", LocalDateTime.of(2026, 7, 12, 10, 0));
            return report;
        });
    }

    @Test
    void createReportSuccessfullyUsesCurrentUserAndPendingStatus() {
        CreateReportResponse response = reportService.createPostReport(
                1L,
                new CreateReportRequest(ReportReason.SPAM, "  Quang cao lap lai  ")
        );

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(currentUserProvider).getCurrentUserId();
        verify(reportRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getReporter().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getModerationCase().getId()).isEqualTo(50L);
        assertThat(captor.getValue().getModerationCase().getReportCount()).isEqualTo(1);
        assertThat(captor.getValue().getDescription()).isEqualTo("Quang cao lap lai");
        assertThat(response.reportId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void firstReportCreatesOpenModerationCaseAndLinksReport() {
        when(moderationCaseRepository.findByPost_IdAndStatus(1L, ModerationCaseStatus.OPEN))
                .thenReturn(Optional.empty());
        when(moderationCaseRepository.save(any())).thenAnswer(invocation -> {
            ModerationCase moderationCase = invocation.getArgument(0);
            ReflectionTestUtils.setField(moderationCase, "id", 77L);
            return moderationCase;
        });

        reportService.createPostReport(1L, new CreateReportRequest(ReportReason.SPAM, null));

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getModerationCase().getId()).isEqualTo(77L);
        assertThat(reportCaptor.getValue().getModerationCase().getStatus()).isEqualTo(ModerationCaseStatus.OPEN);
        assertThat(reportCaptor.getValue().getModerationCase().getReportCount()).isEqualTo(1);
    }

    @Test
    void otherReasonRequiresNonBlankDescription() {
        assertBusinessError(
                () -> reportService.createPostReport(1L, new CreateReportRequest(ReportReason.OTHER, "   ")),
                ErrorCode.REPORT_DESCRIPTION_REQUIRED
        );
        verify(postRepository, never()).findReportTargetByIdForUpdate(any());
    }

    @Test
    void descriptionLongerThanOneThousandCharactersIsRejectedInService() {
        assertBusinessError(
                () -> reportService.createPostReport(
                        1L,
                        new CreateReportRequest(ReportReason.SPAM, "a".repeat(1001))
                ),
                ErrorCode.REPORT_DESCRIPTION_TOO_LONG
        );
    }

    @Test
    void userWithoutCompletedProfileIsRejected() {
        UserProfile profile = completedProfile(reporter);
        profile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.PROFILE_NOT_COMPLETED
        );
        verify(postRepository, never()).findReportTargetByIdForUpdate(any());
    }

    @Test
    void blockedUserIsRejected() {
        reporter.setStatus(UserStatus.BLOCKED);

        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.USER_BLOCKED
        );
        verify(userProfileRepository, never()).findById(any());
    }

    @Test
    void missingPostReturnsPostNotFound() {
        when(postRepository.findReportTargetByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.POST_NOT_FOUND
        );
    }

    @Test
    void hiddenOrDeletedPostIsRejected() {
        reportedPost.setStatus(PostStatus.HIDDEN);
        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.POST_NOT_AVAILABLE
        );

        reportedPost.setStatus(PostStatus.DELETED);
        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.POST_NOT_AVAILABLE
        );
        verify(reportRepository, never()).saveAndFlush(any());
    }

    @Test
    void userCannotReportOwnPost() {
        reportedPost = post(1L, reporter, "Bai cua toi", PostStatus.PUBLISHED);
        when(postRepository.findReportTargetByIdForUpdate(1L)).thenReturn(Optional.of(reportedPost));

        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.REPORT_OWN_POST_FORBIDDEN
        );
    }

    @Test
    void pendingDuplicateIsRejectedBeforeInsert() {
        when(reportRepository.existsEffectiveReport(10L, 1L, ModerationCaseStatus.OPEN))
                .thenReturn(true);

        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.REPORT_ALREADY_PENDING
        );
        verify(reportRepository, never()).saveAndFlush(any());
    }

    @Test
    void twoDifferentUsersCanReportTheSamePost() {
        reportService.createPostReport(1L, request());

        User secondReporter = user(11L, UserStatus.ACTIVE);
        when(currentUserProvider.getCurrentUserId()).thenReturn(11L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(secondReporter));
        when(userProfileRepository.findById(11L)).thenReturn(Optional.of(completedProfile(secondReporter)));
        when(reportRepository.existsEffectiveReport(11L, 1L, ModerationCaseStatus.OPEN))
                .thenReturn(false);
        reportService.createPostReport(1L, request());

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(2)).saveAndFlush(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(report -> report.getReporter().getId())
                .containsExactly(10L, 11L);
        assertThat(captor.getAllValues().get(0).getModerationCase())
                .isSameAs(captor.getAllValues().get(1).getModerationCase());
        assertThat(captor.getAllValues().get(1).getModerationCase().getReportCount()).isEqualTo(2);
    }

    @Test
    void contentAndOrderedMediaSnapshotsArePersistedFromDatabase() {
        reportService.createPostReport(1L, request());

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPostContentSnapshot()).isEqualTo("Noi dung goc");
        assertThat(captor.getValue().getPostMediaSnapshot())
                .isEqualTo("[\"https://cdn.example/first.jpg\",\"https://cdn.example/second.jpg\"]");
    }

    @Test
    void uniqueConstraintRaceIsMappedToReportAlreadyPending() {
        doThrow(new DataIntegrityViolationException("uq_reports_pending_key"))
                .when(reportRepository).saveAndFlush(any());

        assertBusinessError(
                () -> reportService.createPostReport(1L, request()),
                ErrorCode.REPORT_ALREADY_PENDING
        );
        verify(entityManager, never()).refresh(any());
    }

    @Test
    void creatingReportDoesNotChangePostStatus() {
        PostStatus statusBefore = reportedPost.getStatus();

        reportService.createPostReport(1L, request());

        assertThat(reportedPost.getStatus()).isEqualTo(statusBefore).isEqualTo(PostStatus.PUBLISHED);
        verify(postRepository, never()).save(any());
    }

    @Test
    void blankOptionalDescriptionIsStoredAsNull() {
        reportService.createPostReport(1L, new CreateReportRequest(ReportReason.SPAM, "   "));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getDescription()).isNull();
    }

    private CreateReportRequest request() {
        return new CreateReportRequest(ReportReason.SPAM, "Mo ta");
    }

    private void assertBusinessError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("student" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setStatus(status);
        return user;
    }

    private UserProfile completedProfile(User user) {
        UserProfile profile = new UserProfile(user);
        ReflectionTestUtils.setField(profile, "userId", user.getId());
        profile.setDisplayName("Nguyen Van A");
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 1, 10, 0));
        return profile;
    }

    private Post post(Long id, User author, String content, PostStatus status) {
        Post post = new Post(author, content);
        ReflectionTestUtils.setField(post, "id", id);
        post.setStatus(status);
        return post;
    }

    private PostMedia media(Post post, String mediaUrl, int displayOrder) {
        return new PostMedia(post, mediaUrl, "public-" + displayOrder, "image/jpeg", 100L, displayOrder);
    }
}
