package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.PostAnalyticsResponse;

public interface PostAnalyticsService {
    PostAnalyticsResponse getAnalytics(String range, String fromDate, String toDate);
}
