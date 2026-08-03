package com.stu.edu.vn.backend.analytics.tracking;

import java.time.LocalDateTime;

/**
 * Ghi nhận một hành vi người dùng hợp lệ vào ngày UTC tương ứng.
 */
public interface UserActivityTrackingService {
    void track(Long userId, LocalDateTime activeAt);
}
