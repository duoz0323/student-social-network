package com.stu.edu.vn.backend.storage.cleanup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Test durable cleanup ghi lỗi để retry và đánh dấu hoàn tất khi xóa thành công. */
class MediaCleanupServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-03T03:00:00Z"), ZoneOffset.UTC);
    private final MediaCleanupTaskRepository repository = mock(MediaCleanupTaskRepository.class);
    private final CloudinaryStorageService storage = mock(CloudinaryStorageService.class);
    private final MediaCleanupProperties properties = new MediaCleanupProperties();
    private final MediaCleanupService service = new MediaCleanupService(repository, storage, properties, CLOCK);

    @Test
    void enqueueCreatesPendingDurableTask() {
        service.enqueue("asset-1", "ROLLBACK");
        verify(repository).save(argThat(task -> task.getStatus() == MediaCleanupStatus.PENDING
                && task.getStoragePublicId().equals("asset-1") && task.getAttemptCount() == 0));
    }

    @Test
    void failedDeleteIsRetriedLaterWithoutThrowing() {
        MediaCleanupTask task = new MediaCleanupTask("asset-2", "ROLLBACK", LocalDateTime.now(CLOCK));
        when(repository.findByIdForUpdate(2L)).thenReturn(Optional.of(task));
        doThrow(new IllegalStateException("storage down")).when(storage).deleteMessageImage("asset-2");
        service.processOne(2L);
        assertThat(task.getStatus()).isEqualTo(MediaCleanupStatus.FAILED);
        assertThat(task.getAttemptCount()).isEqualTo(1);
        assertThat(task.getNextRetryAt()).isAfter(LocalDateTime.now(CLOCK));
    }

    @Test
    void scheduledRetryCanCompleteDueTask() {
        MediaCleanupTask task = new MediaCleanupTask("asset-3", "ROLLBACK", LocalDateTime.now(CLOCK));
        when(repository.findByIdForUpdate(3L)).thenReturn(Optional.of(task));
        service.processOne(3L);
        assertThat(task.getStatus()).isEqualTo(MediaCleanupStatus.COMPLETED);
        verify(storage).deleteMessageImage("asset-3");
    }
}
