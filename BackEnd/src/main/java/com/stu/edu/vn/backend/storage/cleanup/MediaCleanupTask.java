package com.stu.edu.vn.backend.storage.cleanup;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Durable task bảo đảm file upload ngoài transaction cuối cùng vẫn được dọn. */
@Entity
@Table(name = "media_cleanup_tasks", indexes = @Index(
        name = "idx_media_cleanup_due", columnList = "status,next_retry_at,id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaCleanupTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "storage_provider", nullable = false, length = 32)
    private String storageProvider;
    @Column(name = "storage_public_id", nullable = false, length = 255)
    private String storagePublicId;
    @Column(name = "resource_type", nullable = false, length = 32)
    private String resourceType;
    @Column(name = "reason", nullable = false, length = 64)
    private String reason;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MediaCleanupStatus status;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;
    @Column(name = "last_error", length = 500)
    private String lastError;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MediaCleanupTask(String publicId, String reason, LocalDateTime now) {
        this.storageProvider = "CLOUDINARY";
        this.storagePublicId = publicId;
        this.resourceType = "IMAGE";
        this.reason = reason;
        this.status = MediaCleanupStatus.PENDING;
        this.nextRetryAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markProcessing(LocalDateTime now) {
        status = MediaCleanupStatus.PROCESSING;
        attemptCount++;
        updatedAt = now;
    }

    public void markCompleted(LocalDateTime now) {
        status = MediaCleanupStatus.COMPLETED;
        lastError = null;
        updatedAt = now;
    }

    public void markFailed(String error, LocalDateTime nextRetry, LocalDateTime now) {
        status = MediaCleanupStatus.FAILED;
        lastError = error == null ? "UNKNOWN" : error.substring(0, Math.min(500, error.length()));
        nextRetryAt = nextRetry;
        updatedAt = now;
    }
}
