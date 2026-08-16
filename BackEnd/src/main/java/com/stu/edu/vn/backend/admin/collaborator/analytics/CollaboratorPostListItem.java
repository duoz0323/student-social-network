package com.stu.edu.vn.backend.admin.collaborator.analytics;

import java.time.LocalDateTime;

public record CollaboratorPostListItem(
        Long postId, String contentPreview, String thumbnail, String hashtag,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        int likeCount, int commentCount, int repostCount, String status,
        boolean canEdit, LocalDateTime editDeadline
) { }
