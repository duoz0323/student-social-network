package com.stu.edu.vn.backend.analytics.tracking;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction độc lập giữ tracking best-effort tách khỏi transaction nghiệp vụ chính.
 */
@Service
public class UserActivityTrackingServiceImpl implements UserActivityTrackingService {

    private final UserActivityTrackingRepository repository;

    public UserActivityTrackingServiceImpl(UserActivityTrackingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void track(Long userId, LocalDateTime activeAt) {
        // UPDATE có điều kiện loại ADMIN và tài khoản không còn ACTIVE ngay tại thời điểm ghi.
        if (repository.updateUserActivityBounds(userId, activeAt) == 1) {
            repository.upsertDailyActivity(userId, activeAt.toLocalDate(), activeAt);
        }
    }
}
