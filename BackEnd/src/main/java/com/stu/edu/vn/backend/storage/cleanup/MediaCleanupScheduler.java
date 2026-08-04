package com.stu.edu.vn.backend.storage.cleanup;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduler chỉ điều phối; transaction và lock nằm trong service của từng task. */
@Component
@RequiredArgsConstructor
public class MediaCleanupScheduler {
    private final MediaCleanupService service;
    private final MediaCleanupProperties properties;

    @Scheduled(fixedDelayString = "${media.cleanup.fixed-delay:60000}")
    public void retryDueTasks() {
        if (!properties.isEnabled()) {
            return;
        }
        for (Long taskId : service.dueTaskIds()) {
            service.processOne(taskId);
        }
    }
}
