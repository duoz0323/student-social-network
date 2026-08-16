package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationSuggestionService {
    private final ModerationSuggestionRepository repository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AdminActionRepository actionRepository;
    private final Clock clock;

    @Transactional
    public ModerationSuggestionResponse create(CreateModerationSuggestionRequest request) {
        Long adminId = currentUserProvider.getCurrentUserId();
        User admin = requireAdmin(adminId);
        Post post = postRepository.findReportTargetByIdForUpdate(request.postId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
        if (repository.existsBySuggestedBy_IdAndPost_IdAndStatus(
                adminId, post.getId(), ModerationSuggestionStatus.PENDING)) {
            throw new BusinessException(ErrorCode.MODERATION_SUGGESTION_ALREADY_PENDING);
        }
        String description = normalizeDescription(request.description());
        try {
            ModerationSuggestion saved = repository.saveAndFlush(
                    new ModerationSuggestion(post, admin, request.reason(), description));
            actionRepository.save(new AdminAction(admin, AdminActionType.MODERATION_SUGGESTION_CREATED,
                    AdminTargetType.MODERATION_SUGGESTION, saved.getId(), "postId=" + post.getId()));
            return response(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.MODERATION_SUGGESTION_ALREADY_PENDING);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ModerationSuggestionResponse> getOwn(ModerationSuggestionStatus status, int page, int size) {
        return PageResponse.from(repository.findOwn(currentUserProvider.getCurrentUserId(), status,
                PageRequest.of(page, size)).map(this::response));
    }

    @Transactional(readOnly = true)
    public PageResponse<ModerationSuggestionResponse> getAll(ModerationSuggestionStatus status, int page, int size) {
        return PageResponse.from(repository.findForReview(status, PageRequest.of(page, size)).map(this::response));
    }

    @Transactional(readOnly = true)
    public ModerationSuggestionResponse get(Long id) {
        return response(repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODERATION_SUGGESTION_NOT_FOUND)));
    }

    @Transactional
    public ModerationSuggestionResponse review(Long id, ModerationSuggestionStatus decision) {
        if (decision == ModerationSuggestionStatus.PENDING) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        ModerationSuggestion suggestion = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODERATION_SUGGESTION_NOT_FOUND));
        if (suggestion.getStatus() != ModerationSuggestionStatus.PENDING) {
            throw new BusinessException(ErrorCode.MODERATION_SUGGESTION_ALREADY_RESOLVED);
        }
        User reviewer = requireAdmin(currentUserProvider.getCurrentUserId());
        suggestion.review(decision, reviewer, LocalDateTime.now(clock));
        AdminActionType type = decision == ModerationSuggestionStatus.ACCEPTED
                ? AdminActionType.MODERATION_SUGGESTION_ACCEPTED
                : AdminActionType.MODERATION_SUGGESTION_REJECTED;
        actionRepository.save(new AdminAction(reviewer, type, AdminTargetType.MODERATION_SUGGESTION,
                suggestion.getId(), "postId=" + suggestion.getPost().getId()));
        return response(suggestion);
    }

    private User requireAdmin(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private String normalizeDescription(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().replaceAll("(?U)\\s+", " ");
        if (normalized.length() > 500) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        return normalized;
    }

    private ModerationSuggestionResponse response(ModerationSuggestion suggestion) {
        String content = suggestion.getPost().getContent();
        String summary = content == null ? "Bài viết #" + suggestion.getPost().getId()
                : content.substring(0, Math.min(content.length(), 120));
        return new ModerationSuggestionResponse(suggestion.getId(), suggestion.getPost().getId(), summary,
                suggestion.getReason(), suggestion.getDescription(), suggestion.getStatus(), suggestion.getCreatedAt(),
                suggestion.getReviewedAt(), suggestion.getReviewedBy() == null ? null : suggestion.getReviewedBy().getId());
    }
}
