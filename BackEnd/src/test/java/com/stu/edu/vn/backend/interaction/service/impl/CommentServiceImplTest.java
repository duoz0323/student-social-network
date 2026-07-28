package com.stu.edu.vn.backend.interaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;
import com.stu.edu.vn.backend.interaction.entity.Comment;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import com.stu.edu.vn.backend.interaction.mapper.CommentMapper;
import com.stu.edu.vn.backend.interaction.repository.CommentRepository;
import com.stu.edu.vn.backend.interaction.repository.projection.CommentReplyCountProjection;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.projection.PostInteractionTargetProjection;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

class CommentServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final CommentRepository commentRepository = org.mockito.Mockito.mock(CommentRepository.class);
    private final CommentMapper commentMapper = org.mockito.Mockito.mock(CommentMapper.class);
    private final NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-03T01:10:00Z"), ZoneId.of("UTC"));
    private final UserRelationshipPolicyService relationshipPolicyService =
            org.mockito.Mockito.mock(UserRelationshipPolicyService.class);

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                postRepository,
                commentRepository,
                commentMapper,
                notificationService,
                entityManager,
                clock,
                relationshipPolicyService
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(10L)));
        when(postRepository.findStatusById(1L)).thenReturn(Optional.of(PostStatus.PUBLISHED));
        PostInteractionTargetProjection publishedTarget = target(PostStatus.PUBLISHED, 20L);
        when(postRepository.findInteractionTargetById(1L)).thenReturn(Optional.of(publishedTarget));
        when(postRepository.getReferenceById(1L)).thenReturn(post(1L));
    }

    @Test
    void createCommentUsesCurrentUserTrimsContentAndReturnsDto() {
        when(commentRepository.saveAndFlush(any(Comment.class))).thenAnswer(invocation -> savedComment(invocation.getArgument(0), 100L));
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(response(100L, 1L, 10L, "Noi dung binh luan"));

        CommentResponse response = commentService.createComment(1L, new CreateCommentRequest("  Noi dung binh luan  "));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).saveAndFlush(captor.capture());
        verify(notificationService).createPostCommentNotification(10L, 20L, 1L, 100L);
        assertThat(captor.getValue().getAuthor().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getPost().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getParentComment()).isNull();
        assertThat(captor.getValue().getContent()).isEqualTo("Noi dung binh luan");
        assertThat(response.commentId()).isEqualTo(100L);
        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(10L);
    }

    @Test
    void createAndReadCommentsRejectBlockedPostAuthor() {
        doThrow(new BusinessException(ErrorCode.USER_RELATIONSHIP_BLOCKED))
                .when(relationshipPolicyService).assertNoBlock(10L, 20L);

        assertThatThrownBy(() -> commentService.createComment(
                1L, new CreateCommentRequest("Noi dung hop le")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_RELATIONSHIP_BLOCKED);
        assertThatThrownBy(() -> commentService.getPublishedComments(1L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_RELATIONSHIP_BLOCKED);
        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReplyUsesParentPostAndCreatesOnlyOneReplyLevel() {
        Comment parent = savedComment(new Comment(post(1L), user(20L), "Binh luan goc"), 90L);
        when(commentRepository.findForReplyCreationById(90L)).thenReturn(Optional.of(parent));
        when(commentRepository.saveAndFlush(any(Comment.class)))
                .thenAnswer(invocation -> savedComment(invocation.getArgument(0), 100L));
        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response(100L, 1L, 90L, 10L, "Noi dung tra loi", 0L, false));

        CommentResponse response = commentService.createReply(90L, new CreateCommentRequest("  Noi dung tra loi  "));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).saveAndFlush(captor.capture());
        verify(notificationService).createCommentReplyNotification(10L, 20L, 1L, 100L);
        verify(notificationService, never()).createPostCommentNotification(any(), any(), any(), any());
        assertThat(captor.getValue().getParentComment()).isSameAs(parent);
        assertThat(captor.getValue().getPost().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getContent()).isEqualTo("Noi dung tra loi");
        assertThat(response.parentCommentId()).isEqualTo(90L);
    }

    @Test
    void createReplyRejectsReplyToAnotherReply() {
        Comment root = savedComment(new Comment(post(1L), user(20L), "Binh luan goc"), 90L);
        Comment reply = savedComment(new Comment(post(1L), user(21L), root, "Tra loi"), 91L);
        when(commentRepository.findForReplyCreationById(91L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> commentService.createReply(91L, new CreateCommentRequest("Cap hai")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED);

        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReplyRejectsDeletedParent() {
        Comment parent = savedComment(new Comment(post(1L), user(20L), "Binh luan goc"), 90L);
        ReflectionTestUtils.setField(parent, "status", CommentStatus.DELETED);
        when(commentRepository.findForReplyCreationById(90L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> commentService.createReply(90L, new CreateCommentRequest("Tra loi")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_PARENT_NOT_AVAILABLE);
    }

    @Test
    void createCommentRejectsBlankContent() {
        assertThatThrownBy(() -> commentService.createComment(1L, new CreateCommentRequest("   ")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_CONTENT_REQUIRED);

        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createCommentRejectsHiddenOrDeletedPost() {
        PostInteractionTargetProjection deletedTarget = target(PostStatus.DELETED, 20L);
        when(postRepository.findInteractionTargetById(1L)).thenReturn(Optional.of(deletedTarget));

        assertThatThrownBy(() -> commentService.createComment(1L, new CreateCommentRequest("Noi dung")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_AVAILABLE);

        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void getPublishedCommentsReturnsPaginatedRootsWithReplyCounts() {
        Comment first = savedComment(new Comment(post(1L), user(20L), "Binh luan 1"), 101L);
        Comment second = savedComment(new Comment(post(1L), user(21L), "Binh luan 2"), 102L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(commentRepository.findVisibleRootComments(1L, 10L, CommentStatus.PUBLISHED, pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));
        CommentReplyCountProjection replyCount = org.mockito.Mockito.mock(CommentReplyCountProjection.class);
        when(replyCount.getCommentId()).thenReturn(101L);
        when(replyCount.getReplyCount()).thenReturn(2L);
        when(commentRepository.countVisibleRepliesByParentIdsAndStatus(
                List.of(101L, 102L), 10L, CommentStatus.PUBLISHED))
                .thenReturn(List.of(replyCount));
        when(commentMapper.toResponse(first, 2L))
                .thenReturn(response(101L, 1L, null, 20L, "Binh luan 1", 2L, false));
        when(commentMapper.toResponse(second, 0L))
                .thenReturn(response(102L, 1L, null, 21L, "Binh luan 2", 0L, false));

        var responses = commentService.getPublishedComments(1L, 0, 20);

        assertThat(responses.content()).extracting(CommentResponse::commentId).containsExactly(101L, 102L);
        assertThat(responses.content().getFirst().replyCount()).isEqualTo(2L);
        verify(commentRepository).findVisibleRootComments(1L, 10L, CommentStatus.PUBLISHED, pageable);
    }

    @Test
    void getPublishedRepliesReturnsOnlyRepliesOfRequestedRoot() {
        Comment parent = savedComment(new Comment(post(1L), user(20L), "Binh luan goc"), 90L);
        Comment reply = savedComment(new Comment(post(1L), user(21L), parent, "Tra loi"), 100L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(commentRepository.findWithPostAndParentById(90L)).thenReturn(Optional.of(parent));
        when(commentRepository.findVisibleReplies(
                90L, 10L, CommentStatus.PUBLISHED, pageable
        )).thenReturn(new PageImpl<>(List.of(reply), pageable, 1));
        when(commentMapper.toResponse(reply))
                .thenReturn(response(100L, 1L, 90L, 21L, "Tra loi", 0L, false));

        var responses = commentService.getPublishedReplies(90L, 0, 20);

        assertThat(responses.content()).extracting(CommentResponse::commentId).containsExactly(100L);
        assertThat(responses.content().getFirst().parentCommentId()).isEqualTo(90L);
    }

    @Test
    void getPublishedRepliesRejectsBlockedParentAndDoesNotExposeItsBranch() {
        Comment parent = savedComment(new Comment(post(1L), user(30L), "Binh luan bi an"), 90L);
        when(commentRepository.findWithPostAndParentById(90L)).thenReturn(Optional.of(parent));
        doThrow(new BusinessException(ErrorCode.USER_RELATIONSHIP_BLOCKED))
                .when(relationshipPolicyService).assertNoBlock(10L, 30L);

        assertThatThrownBy(() -> commentService.getPublishedReplies(90L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_RELATIONSHIP_BLOCKED);

        // Không truy vấn reply khi comment cha đã bị ẩn để tránh lộ nhánh hội thoại mồ côi.
        verify(commentRepository, never()).findVisibleReplies(any(), any(), any(), any());
    }

    @Test
    void deleteCommentSoftDeletesOwnPublishedComment() {
        Comment comment = savedComment(new Comment(post(1L), user(10L), "Noi dung"), 100L);
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(commentRepository.softDeletePublishedComment(eq(100L), any(LocalDateTime.class))).thenReturn(1);

        DeleteCommentResponse response = commentService.deleteComment(100L);

        ArgumentCaptor<LocalDateTime> deletedAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(commentRepository).softDeletePublishedComment(eq(100L), deletedAtCaptor.capture());
        verify(notificationService).deleteCommentNotification(100L);
        assertThat(deletedAtCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 7, 3, 1, 10));
        assertThat(response.commentId()).isEqualTo(100L);
        assertThat(response.deleted()).isTrue();
    }

    @Test
    void deleteCommentRejectsNonAuthor() {
        Comment comment = savedComment(new Comment(post(1L), user(20L), "Noi dung"), 100L);
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_FORBIDDEN);

        verify(commentRepository, never()).softDeletePublishedComment(any(), any());
    }

    @Test
    void deleteCommentReturnsNotFoundWhenCommentAlreadyDeleted() {
        Comment comment = savedComment(new Comment(post(1L), user(10L), "Noi dung"), 100L);
        ReflectionTestUtils.setField(comment, "status", CommentStatus.DELETED);
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    private User user(Long userId) {
        User user = new User("student" + userId + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private UserProfile completedProfile(Long userId) {
        UserProfile profile = new UserProfile(user(userId));
        ReflectionTestUtils.setField(profile, "userId", userId);
        profile.setDisplayName("Nguyen Van A");
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 3, 1, 0));
        return profile;
    }

    private Post post(Long postId) {
        Post post = new Post(user(20L), "Noi dung bai viet");
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private Comment savedComment(Comment comment, Long commentId) {
        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        return comment;
    }

    private CommentResponse response(Long commentId, Long postId, Long userId, String content) {
        return response(commentId, postId, null, userId, content, 0L, false);
    }

    private CommentResponse response(
            Long commentId,
            Long postId,
            Long parentCommentId,
            Long userId,
            String content,
            long replyCount,
            boolean deleted
    ) {
        return new CommentResponse(
                commentId,
                postId,
                parentCommentId,
                userId,
                "Nguyen Van A",
                "https://cdn.example/avatar.png",
                content,
                LocalDateTime.of(2026, 7, 3, 1, 0),
                replyCount,
                deleted
        );
    }

    private PostInteractionTargetProjection target(PostStatus status, Long authorId) {
        PostInteractionTargetProjection target = org.mockito.Mockito.mock(PostInteractionTargetProjection.class);
        when(target.getPostId()).thenReturn(1L);
        when(target.getAuthorId()).thenReturn(authorId);
        when(target.getStatus()).thenReturn(status);
        return target;
    }
}
