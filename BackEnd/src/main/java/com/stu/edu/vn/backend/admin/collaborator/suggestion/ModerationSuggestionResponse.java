package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import java.time.LocalDateTime;

public record ModerationSuggestionResponse(
        Long suggestionId,
        Long postId,
        String postSummary,
        ModerationSuggestionReason reason,
        String description,
        ModerationSuggestionStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        Long reviewedBy
) { }
