package com.stu.edu.vn.backend.analytics.tracking;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserActivityTrackingServiceImplTest {

    private final UserActivityTrackingRepository repository =
            org.mockito.Mockito.mock(UserActivityTrackingRepository.class);
    private final UserActivityTrackingServiceImpl service = new UserActivityTrackingServiceImpl(repository);

    @Test
    void updatesUserBoundsThenUpsertsOneDailyRow() {
        LocalDateTime activeAt = LocalDateTime.of(2026, 6, 16, 8, 30);
        when(repository.updateUserActivityBounds(10L, activeAt)).thenReturn(1);

        service.track(10L, activeAt);

        verify(repository).upsertDailyActivity(10L, activeAt.toLocalDate(), activeAt);
    }

    @Test
    void doesNotInsertActivityWhenUserIsNotEligible() {
        LocalDateTime activeAt = LocalDateTime.of(2026, 6, 16, 8, 30);
        when(repository.updateUserActivityBounds(10L, activeAt)).thenReturn(0);

        service.track(10L, activeAt);

        verify(repository, never()).upsertDailyActivity(10L, activeAt.toLocalDate(), activeAt);
    }
}
