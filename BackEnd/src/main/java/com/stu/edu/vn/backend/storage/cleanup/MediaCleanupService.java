package com.stu.edu.vn.backend.storage.cleanup;

import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import java.time.*;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Ghi task bằng transaction độc lập và retry mà không che lỗi nghiệp vụ ban đầu. */
@Service
@RequiredArgsConstructor
public class MediaCleanupService {
    private final MediaCleanupTaskRepository repository;
    private final CloudinaryStorageService storageService;
    private final MediaCleanupProperties properties;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueue(String publicId, String reason) {
        repository.save(new MediaCleanupTask(publicId, reason, LocalDateTime.now(clock)));
    }

    @Transactional(readOnly = true)
    public List<Long> dueTaskIds() {
        return repository.findDueIds(EnumSet.of(MediaCleanupStatus.PENDING, MediaCleanupStatus.FAILED),
                LocalDateTime.now(clock), PageRequest.of(0, properties.getBatchSize()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(Long taskId) {
        MediaCleanupTask task = repository.findByIdForUpdate(taskId).orElse(null);
        LocalDateTime now = LocalDateTime.now(clock);
        if (task == null || (task.getStatus() != MediaCleanupStatus.PENDING
                && task.getStatus() != MediaCleanupStatus.FAILED) || task.getNextRetryAt().isAfter(now)) {
            return;
        }
        task.markProcessing(now);
        repository.flush();
        try {
            storageService.deleteMessageImage(task.getStoragePublicId());
            task.markCompleted(LocalDateTime.now(clock));
        } catch (RuntimeException exception) {
            task.markFailed(exception.getClass().getSimpleName(), now.plus(properties.getRetryDelay()), now);
        }
        repository.save(task);
    }
}
