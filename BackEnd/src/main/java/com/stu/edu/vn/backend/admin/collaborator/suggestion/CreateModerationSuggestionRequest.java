package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateModerationSuggestionRequest(
        @NotNull @Positive Long postId,
        @NotNull ModerationSuggestionReason reason,
        @Size(max = 500) String description
) { }
