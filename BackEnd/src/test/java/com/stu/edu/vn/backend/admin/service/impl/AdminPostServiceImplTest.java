package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;

import com.stu.edu.vn.backend.admin.mapper.AdminPostMapper;
import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminPostRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostHashtagProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostMediaProjection;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;

class AdminPostServiceImplTest {
    private final AdminPostRepository repository = org.mockito.Mockito.mock(AdminPostRepository.class);
    private final AdminActionRepository actionRepository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);
    private AdminPostServiceImpl service;
    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User("admin@example.com", null, "hash");
        admin.setRole(UserRole.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
        service = new AdminPostServiceImpl(repository, new AdminPostMapper(), actionRepository,
                currentUserProvider, entityManager,
                Clock.fixed(Instant.parse("2026-07-15T01:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")),
                notificationService);
    }

    @Test
    void hideReasonEnumMatchesApprovedContractExactly() {
        assertThat(AdminPostHideReason.values()).extracting(Enum::name).containsExactly(
                "SPAM", "HARASSMENT", "HARMFUL_CONTENT", "VIOLENCE", "MISINFORMATION",
                "SCHOOL_POLICY_VIOLATION", "INAPPROPRIATE_CONTENT", "OTHER");
    }

    @Test
    void listNormalizesFiltersAndMapsAllApprovedFields() {
        var projection = listProjection();
        when(repository.findAdminPosts(any(), any(), any(), any(Integer.class), any()))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 20), 1));

        var response = service.getPosts("  50%_off=now  ", PostStatus.HIDDEN, 9L, true, 0, 20);

        verify(repository).findAdminPosts("50=%=_off==now", "HIDDEN", 9L, 1, PageRequest.of(0, 20));
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst())
                .extracting("postId", "status", "authorAccountStatus", "thumbnailUrl",
                        "mediaCount", "pendingReportCount")
                .containsExactly(11L, PostStatus.HIDDEN, UserStatus.BLOCKED, "small.jpg", 2L, 3L);
    }

    @Test
    void listTreatsBlankAsNullAndSupportsDefaultsEmptyPageAndBoundarySizes() {
        when(repository.findAdminPosts(any(), any(), any(), any(Integer.class), any()))
                .thenAnswer(invocation -> new PageImpl<AdminPostListProjection>(List.of(),
                        invocation.getArgument(4), 0));

        assertThat(service.getPosts("   ", null, null, false, 0, 1).content()).isEmpty();
        assertThat(service.getPosts(null, null, null, false, 0, 100).size()).isEqualTo(100);
        verify(repository).findAdminPosts(null, null, null, 0, PageRequest.of(0, 1));
    }

    @Test
    void listRejectsInvalidPaginationAndOverlongKeywordBeforeQuery() {
        assertError(() -> service.getPosts(null, null, null, false, -1, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getPosts(null, null, null, false, 0, 0), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getPosts(null, null, null, false, 0, 101), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getPosts(null, null, 0L, false, 0, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> service.getPosts("x".repeat(101), null, null, false, 0, 20),
                ErrorCode.ADMIN_POST_KEYWORD_TOO_LONG);
        verify(repository, never()).findAdminPosts(any(), any(), any(), any(Integer.class), any());
    }

    @Test
    void detailMapsBlockedAuthorModerationFieldsOrderedMediaAndSingleHashtag() {
        var detail = detailProjection();
        var secondMedia = mediaProjection(22L, "second.jpg", 1);
        var firstMedia = mediaProjection(21L, "first.jpg", 0);
        var alpha = hashtagProjection("alpha");
        when(repository.findAdminPostDetail(11L)).thenReturn(Optional.of(detail));
        when(repository.findAdminPostMedia(11L)).thenReturn(List.of(firstMedia, secondMedia));
        when(repository.findAdminPostHashtags(11L)).thenReturn(List.of(alpha));

        var response = service.getPostDetail(11L);

        assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
        assertThat(response.author().accountStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(response.media()).extracting("mediaId").containsExactly(21L, 22L);
        assertThat(response.hashtag()).isEqualTo("alpha");
        assertThat(response.pendingReportCount()).isEqualTo(2);
        assertThat(response.totalReportCount()).isEqualTo(4);
        assertThat(response.hiddenBy().adminId()).isEqualTo(1L);
    }

    @Test
    void detailMissingUsesAdminSpecificErrorAndDoesNotRunChildQueries() {
        when(repository.findAdminPostDetail(404L)).thenReturn(Optional.empty());

        assertError(() -> service.getPostDetail(404L), ErrorCode.ADMIN_POST_NOT_FOUND);
        verify(repository, never()).findAdminPostMedia(any());
        verify(repository, never()).findAdminPostHashtags(any());
    }

    @Test
    void hidePublishedPostStoresReasonAdminAndHideAction() {
        Post post = post(11L, PostStatus.PUBLISHED);
        when(repository.findByIdForUpdate(11L)).thenReturn(Optional.of(post));
        when(repository.findAdminDisplayName(1L)).thenReturn(Optional.of("Admin"));

        var response = service.hidePost(11L, new AdminHidePostRequest(AdminPostHideReason.SPAM));

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(post.getHiddenBy()).isSameAs(admin);
        assertThat(post.getHiddenAt()).isEqualTo(LocalDateTime.of(2026, 7, 15, 8, 0));
        assertThat(post.getHiddenReason()).isEqualTo("SPAM");
        assertThat(response.hiddenBy()).extracting("adminId", "displayName").containsExactly(1L, "Admin");
        assertAction(AdminActionType.HIDE_POST, "SPAM");
        verify(notificationService).createPostHiddenByAdminNotification(8L, 11L);
        verify(entityManager).flush();
        verify(entityManager).refresh(post);
    }

    @Test
    void hideOtherStoresOtherDirectly() {
        Post post = post(11L, PostStatus.PUBLISHED);
        when(repository.findByIdForUpdate(11L)).thenReturn(Optional.of(post));
        when(repository.findAdminDisplayName(1L)).thenReturn(Optional.of("Admin"));

        service.hidePost(11L, new AdminHidePostRequest(AdminPostHideReason.OTHER));

        assertThat(post.getHiddenReason()).isEqualTo("OTHER");
        assertAction(AdminActionType.HIDE_POST, "OTHER");
    }

    @Test
    void hideRejectsMissingReasonMissingPostHiddenAndDeletedStates() {
        assertError(() -> service.hidePost(11L, null), ErrorCode.ADMIN_POST_HIDE_REASON_REQUIRED);
        assertError(() -> service.hidePost(11L, new AdminHidePostRequest(null)),
                ErrorCode.ADMIN_POST_HIDE_REASON_REQUIRED);
        when(repository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertError(() -> service.hidePost(404L, new AdminHidePostRequest(AdminPostHideReason.SPAM)),
                ErrorCode.ADMIN_POST_NOT_FOUND);
        when(repository.findByIdForUpdate(12L)).thenReturn(Optional.of(post(12L, PostStatus.HIDDEN)));
        assertError(() -> service.hidePost(12L, new AdminHidePostRequest(AdminPostHideReason.SPAM)),
                ErrorCode.ADMIN_POST_ALREADY_HIDDEN);
        when(repository.findByIdForUpdate(13L)).thenReturn(Optional.of(post(13L, PostStatus.DELETED)));
        assertError(() -> service.hidePost(13L, new AdminHidePostRequest(AdminPostHideReason.SPAM)),
                ErrorCode.ADMIN_POST_DELETED_ACTION_FORBIDDEN);
        verifyNoInteractions(actionRepository);
    }

    @Test
    void restoreHiddenPostClearsModerationAndStoresRestoreAction() {
        Post post = post(11L, PostStatus.HIDDEN);
        post.setHiddenBy(admin);
        post.setHiddenAt(LocalDateTime.of(2026, 7, 15, 7, 0));
        post.setHiddenReason("SPAM");
        when(repository.findByIdForUpdate(11L)).thenReturn(Optional.of(post));

        var response = service.restorePost(11L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getHiddenBy()).isNull();
        assertThat(post.getHiddenAt()).isNull();
        assertThat(post.getHiddenReason()).isNull();
        assertThat(response.hiddenBy()).isNull();
        assertAction(AdminActionType.RESTORE_POST, "ADMIN_RESTORE");
        verify(notificationService).createPostRestoredByAdminNotification(8L, 11L);
        verify(repository, never()).findAdminDisplayName(any());
    }

    @Test
    void restoreRejectsMissingPublishedAndDeletedStates() {
        when(repository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertError(() -> service.restorePost(404L), ErrorCode.ADMIN_POST_NOT_FOUND);
        when(repository.findByIdForUpdate(12L)).thenReturn(Optional.of(post(12L, PostStatus.PUBLISHED)));
        assertError(() -> service.restorePost(12L), ErrorCode.ADMIN_POST_ALREADY_PUBLISHED);
        when(repository.findByIdForUpdate(13L)).thenReturn(Optional.of(post(13L, PostStatus.DELETED)));
        assertError(() -> service.restorePost(13L), ErrorCode.ADMIN_POST_DELETED_ACTION_FORBIDDEN);
        verifyNoInteractions(actionRepository);
    }

    @Test
    void actionFailurePropagatesBeforeFlushSoTransactionCanRollback() {
        Post post = post(11L, PostStatus.PUBLISHED);
        when(repository.findByIdForUpdate(11L)).thenReturn(Optional.of(post));
        doThrow(new IllegalStateException("audit failed")).when(actionRepository).save(any(AdminAction.class));

        assertThatThrownBy(() -> service.hidePost(11L,
                new AdminHidePostRequest(AdminPostHideReason.SPAM))).isInstanceOf(IllegalStateException.class);
        verify(entityManager, never()).flush();
    }

    private Post post(Long id, PostStatus status) {
        User author = new User("author" + id + "@example.com", null, "hash");
        ReflectionTestUtils.setField(author, "id", 8L);
        Post post = new Post(author, "content");
        ReflectionTestUtils.setField(post, "id", id);
        post.setStatus(status);
        if (status == PostStatus.DELETED) {
            post.setDeletedAt(LocalDateTime.of(2026, 7, 15, 7, 0));
        }
        return post;
    }

    private void assertAction(AdminActionType type, String note) {
        ArgumentCaptor<AdminAction> captor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(captor.capture());
        assertThat(captor.getValue())
                .extracting(AdminAction::getAdmin, AdminAction::getActionType, AdminAction::getTargetType,
                        AdminAction::getTargetId, AdminAction::getNote,
                        AdminAction::getOldData, AdminAction::getNewData)
                .containsExactly(admin, type, AdminTargetType.POST, 11L, note, null, null);
    }

    private void assertError(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(code);
    }

    private AdminPostListProjection listProjection() {
        AdminPostListProjection p = org.mockito.Mockito.mock(AdminPostListProjection.class);
        when(p.getPostId()).thenReturn(11L);
        when(p.getContentPreview()).thenReturn("content");
        when(p.getStatus()).thenReturn("HIDDEN");
        when(p.getAuthorId()).thenReturn(9L);
        when(p.getAuthorDisplayName()).thenReturn("Author");
        when(p.getAuthorAccountStatus()).thenReturn("BLOCKED");
        when(p.getThumbnailUrl()).thenReturn("small.jpg");
        when(p.getMediaCount()).thenReturn(2L);
        when(p.getLikeCount()).thenReturn(5);
        when(p.getCommentCount()).thenReturn(6);
        when(p.getPendingReportCount()).thenReturn(3L);
        when(p.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 15, 8, 0));
        when(p.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 7, 15, 8, 0));
        return p;
    }

    private AdminPostDetailProjection detailProjection() {
        AdminPostDetailProjection p = org.mockito.Mockito.mock(AdminPostDetailProjection.class);
        when(p.getPostId()).thenReturn(11L);
        when(p.getContent()).thenReturn("detail");
        when(p.getStatus()).thenReturn("HIDDEN");
        when(p.getAuthorId()).thenReturn(9L);
        when(p.getAuthorDisplayName()).thenReturn("Author");
        when(p.getAuthorAccountStatus()).thenReturn("BLOCKED");
        when(p.getLikeCount()).thenReturn(5);
        when(p.getCommentCount()).thenReturn(6);
        when(p.getPendingReportCount()).thenReturn(2L);
        when(p.getTotalReportCount()).thenReturn(4L);
        when(p.getHiddenByAdminId()).thenReturn(1L);
        when(p.getHiddenByDisplayName()).thenReturn("Admin");
        return p;
    }

    private AdminPostMediaProjection mediaProjection(Long id, String url, int order) {
        AdminPostMediaProjection p = org.mockito.Mockito.mock(AdminPostMediaProjection.class);
        when(p.getMediaId()).thenReturn(id);
        when(p.getMediaUrl()).thenReturn(url);
        when(p.getSortOrder()).thenReturn(order);
        return p;
    }

    private AdminPostHashtagProjection hashtagProjection(String name) {
        AdminPostHashtagProjection p = org.mockito.Mockito.mock(AdminPostHashtagProjection.class);
        when(p.getName()).thenReturn(name);
        return p;
    }
}
