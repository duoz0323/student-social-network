package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationEvent;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationRouter;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
public class ModerationSuggestionService {
    private final ModerationSuggestionRepository repository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AdminActionRepository actionRepository;
    private final Clock clock;
    private AdminNotificationRouter adminNotificationRouter;

    @Autowired
    void setAdminNotificationRouter(AdminNotificationRouter adminNotificationRouter) {
        this.adminNotificationRouter = adminNotificationRouter;
    }

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
            if (adminNotificationRouter != null) {
                adminNotificationRouter.notifyByPermission("MODERATION_SUGGESTION_VIEW", adminId,
                        new AdminNotificationEvent(
                                AdminNotificationType.MODERATION_SUGGESTION_CREATED,
                                "Có đề xuất kiểm duyệt mới",
                                "Một cộng tác viên vừa gửi đề xuất kiểm duyệt.",
                                AdminNotificationReferenceType.MODERATION_SUGGESTION,
                                saved.getId(),
                                "MODERATION_SUGGESTION_CREATED:" + saved.getId()));
            }
            return response(saved, loadActors(Set.of(adminId)));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.MODERATION_SUGGESTION_ALREADY_PENDING);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ModerationSuggestionResponse> getOwn(ModerationSuggestionStatus status, int page, int size) {
        Page<ModerationSuggestion> suggestions = repository.findOwn(currentUserProvider.getCurrentUserId(), status,
                PageRequest.of(page, size));
        Map<Long, ModerationSuggestionActorResponse> actors = loadActors(actorIds(suggestions.getContent()));
        return PageResponse.from(suggestions.map(suggestion -> response(suggestion, actors)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ModerationSuggestionResponse> getAll(ModerationSuggestionStatus status, int page, int size) {
        Page<ModerationSuggestion> suggestions = repository.findForReview(status, PageRequest.of(page, size));
        Map<Long, ModerationSuggestionActorResponse> actors = loadActors(actorIds(suggestions.getContent()));
        return PageResponse.from(suggestions.map(suggestion -> response(suggestion, actors)));
    }

    @Transactional(readOnly = true)
    public ModerationSuggestionResponse get(Long id) {
        ModerationSuggestion suggestion = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODERATION_SUGGESTION_NOT_FOUND));
        return response(suggestion, loadActors(actorIds(java.util.List.of(suggestion))));
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
        if (adminNotificationRouter != null) {
            boolean accepted = decision == ModerationSuggestionStatus.ACCEPTED;
            adminNotificationRouter.notifyDirectAdmin(
                    suggestion.getSuggestedBy().getId(),
                    reviewer.getId(),
                    new AdminNotificationEvent(
                            accepted ? AdminNotificationType.MODERATION_SUGGESTION_ACCEPTED
                                    : AdminNotificationType.MODERATION_SUGGESTION_REJECTED,
                            accepted ? "Đề xuất đã được chấp nhận" : "Đề xuất đã bị từ chối",
                            accepted ? "Đề xuất kiểm duyệt của bạn đã được chấp nhận."
                                    : "Đề xuất kiểm duyệt của bạn đã bị từ chối.",
                            AdminNotificationReferenceType.MODERATION_SUGGESTION,
                            suggestion.getId(),
                            "MODERATION_SUGGESTION_REVIEWED:" + suggestion.getId() + ":" + decision.name()));
        }
        return response(suggestion, loadActors(actorIds(java.util.List.of(suggestion))));
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

    private ModerationSuggestionResponse response(ModerationSuggestion suggestion,
            Map<Long, ModerationSuggestionActorResponse> actors) {
        String content = suggestion.getPost().getContent();
        String summary = content == null ? "Bài viết #" + suggestion.getPost().getId()
                : content.substring(0, Math.min(content.length(), 120));
        Long suggesterId = suggestion.getSuggestedBy().getId();
        Long reviewerId = suggestion.getReviewedBy() == null ? null : suggestion.getReviewedBy().getId();
        return new ModerationSuggestionResponse(suggestion.getId(), suggestion.getPost().getId(), summary,
                suggestion.getReason(), suggestion.getDescription(), suggestion.getStatus(), suggestion.getCreatedAt(),
                suggestion.getReviewedAt(), reviewerId, actors.get(suggesterId), actors.get(reviewerId));
    }

    private Set<Long> actorIds(Collection<ModerationSuggestion> suggestions) {
        Set<Long> ids = new LinkedHashSet<>();
        suggestions.forEach(suggestion -> {
            ids.add(suggestion.getSuggestedBy().getId());
            if (suggestion.getReviewedBy() != null) ids.add(suggestion.getReviewedBy().getId());
        });
        return ids;
    }

    private Map<Long, ModerationSuggestionActorResponse> loadActors(Collection<Long> adminIds) {
        if (adminIds.isEmpty()) return Map.of();
        Map<Long, ModerationSuggestionActorResponse> actors = new LinkedHashMap<>();
        repository.findActorsByAdminIds(adminIds).forEach(row -> {
            Set<String> roles = row.getRoleCodes() == null || row.getRoleCodes().isBlank()
                    ? Set.of()
                    : new LinkedHashSet<>(Arrays.asList(row.getRoleCodes().split(",")));
            actors.put(row.getAdminId(), new ModerationSuggestionActorResponse(
                    row.getAdminId(), row.getUsername(), row.getDisplayName(), row.getAvatarUrl(), roles));
        });
        return actors;
    }
}
