package com.stu.edu.vn.backend.interaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

class CommentServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final CommentRepository commentRepository = org.mockito.Mockito.mock(CommentRepository.class);
    private final CommentMapper commentMapper = org.mockito.Mockito.mock(CommentMapper.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-03T01:10:00Z"), ZoneId.of("UTC"));

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
                entityManager,
                clock
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(10L)));
        when(postRepository.findStatusById(1L)).thenReturn(Optional.of(PostStatus.PUBLISHED));
        when(postRepository.getReferenceById(1L)).thenReturn(post(1L));
    }

    @Test
    void createCommentUsesCurrentUserTrimsContentAndReturnsDto() {
        when(commentRepository.saveAndFlush(any(Comment.class))).thenAnswer(invocation -> savedComment(invocation.getArgument(0), 100L));
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(response(100L, 1L, 10L, "Noi dung binh luan"));

        CommentResponse response = commentService.createComment(1L, new CreateCommentRequest("  Noi dung binh luan  "));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getAuthor().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getPost().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getParentComment()).isNull();
        assertThat(captor.getValue().getContent()).isEqualTo("Noi dung binh luan");
        assertThat(response.commentId()).isEqualTo(100L);
        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(10L);
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
        when(postRepository.findStatusById(1L)).thenReturn(Optional.of(PostStatus.DELETED));

        assertThatThrownBy(() -> commentService.createComment(1L, new CreateCommentRequest("Noi dung")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_AVAILABLE);

        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void getPublishedCommentsReturnsOnlyPublishedCommentsInRepositoryOrder() {
        Comment first = savedComment(new Comment(post(1L), user(20L), "Binh luan 1"), 101L);
        Comment second = savedComment(new Comment(post(1L), user(21L), "Binh luan 2"), 102L);
        when(commentRepository.findByPost_IdAndStatusOrderByCreatedAtAscIdAsc(1L, CommentStatus.PUBLISHED))
                .thenReturn(List.of(first, second));
        when(commentMapper.toResponse(first)).thenReturn(response(101L, 1L, 20L, "Binh luan 1"));
        when(commentMapper.toResponse(second)).thenReturn(response(102L, 1L, 21L, "Binh luan 2"));

        List<CommentResponse> responses = commentService.getPublishedComments(1L);

        assertThat(responses).extracting(CommentResponse::commentId).containsExactly(101L, 102L);
        verify(commentRepository).findByPost_IdAndStatusOrderByCreatedAtAscIdAsc(1L, CommentStatus.PUBLISHED);
    }

    @Test
    void deleteCommentSoftDeletesOwnPublishedComment() {
        Comment comment = savedComment(new Comment(post(1L), user(10L), "Noi dung"), 100L);
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(commentRepository.softDeletePublishedComment(eq(100L), any(LocalDateTime.class))).thenReturn(1);

        DeleteCommentResponse response = commentService.deleteComment(100L);

        ArgumentCaptor<LocalDateTime> deletedAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(commentRepository).softDeletePublishedComment(eq(100L), deletedAtCaptor.capture());
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
        User user = new User("student" + userId + "@example.com", null, "hash");
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
        return new CommentResponse(
                commentId,
                postId,
                userId,
                "Nguyen Van A",
                "https://cdn.example/avatar.png",
                content,
                LocalDateTime.of(2026, 7, 3, 1, 0)
        );
    }
}
