package com.stu.edu.vn.backend.admin.collaborator.analytics;

import java.util.List;

public record CollaboratorDashboardResponse(
        long totalPosts, long totalLikesReceived, long totalCommentsReceived, long totalRepostsReceived,
        List<CollaboratorPostListItem> recentPosts,
        List<CollaboratorPostListItem> topPosts,
        List<InteractionTrendPoint> interactionTrend
) { }
