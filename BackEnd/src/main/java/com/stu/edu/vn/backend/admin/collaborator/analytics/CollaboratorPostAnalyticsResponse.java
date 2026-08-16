package com.stu.edu.vn.backend.admin.collaborator.analytics;

import java.util.List;

public record CollaboratorPostAnalyticsResponse(
        CollaboratorPostListItem post,
        long likeCount,
        long commentCount,
        long repostCount,
        long totalInteractions,
        List<InteractionTrendPoint> interactionTrend
) { }
