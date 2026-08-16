package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.HashtagAnalyticsResponse;

/** Nghiệp vụ chỉ đọc của màn hình thống kê hashtag. */
public interface HashtagAnalyticsService {
    HashtagAnalyticsResponse getAnalytics(String range, String fromDate, String toDate);
}
