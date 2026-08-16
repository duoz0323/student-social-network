package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ModerationSuggestionServiceTest {
    @Mock private ModerationSuggestionRepository repository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private AdminActionRepository actionRepository;
    private ModerationSuggestionService service;

    @BeforeEach
    void setUp() {
        service = new ModerationSuggestionService(repository, postRepository, userRepository,
                currentUserProvider, actionRepository,
                Clock.fixed(Instant.parse("2026-08-15T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void duplicatePendingSuggestionIsRejectedForCurrentAdminAndPost() {
        User admin = user(15L);
        Post post = post(123L);
        when(currentUserProvider.getCurrentUserId()).thenReturn(15L);
        when(userRepository.findById(15L)).thenReturn(Optional.of(admin));
        when(postRepository.findReportTargetByIdForUpdate(123L)).thenReturn(Optional.of(post));
        when(repository.existsBySuggestedBy_IdAndPost_IdAndStatus(
                15L, 123L, ModerationSuggestionStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateModerationSuggestionRequest(
                123L, ModerationSuggestionReason.SPAM, "spam")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.MODERATION_SUGGESTION_ALREADY_PENDING));
    }

    @Test
    void moderatorCanAcceptPendingButCannotReviewFinalSuggestionAgain() {
        User suggester = user(15L);
        User reviewer = user(20L);
        ModerationSuggestion suggestion = new ModerationSuggestion(
                post(123L), suggester, ModerationSuggestionReason.SPAM, "spam");
        ReflectionTestUtils.setField(suggestion, "id", 7L);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(suggestion));
        when(currentUserProvider.getCurrentUserId()).thenReturn(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(reviewer));

        ModerationSuggestionResponse accepted = service.review(7L, ModerationSuggestionStatus.ACCEPTED);

        assertThat(accepted.status()).isEqualTo(ModerationSuggestionStatus.ACCEPTED);
        assertThat(accepted.reviewedBy()).isEqualTo(20L);
        verify(actionRepository).save(org.mockito.ArgumentMatchers.any());
        assertThatThrownBy(() -> service.review(7L, ModerationSuggestionStatus.REJECTED))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.MODERATION_SUGGESTION_ALREADY_RESOLVED));
    }

    private User user(Long id) {
        User user = new User("suggestion-" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post post(Long id) {
        Post post = new Post(user(100L), "Nội dung bài viết");
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 8, 15, 9, 0));
        return post;
    }
}
