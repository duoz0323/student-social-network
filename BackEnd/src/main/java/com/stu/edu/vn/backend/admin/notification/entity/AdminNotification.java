package com.stu.edu.vn.backend.admin.notification.entity;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

/** Dữ liệu thông báo quản trị tách biệt hoàn toàn với Notification xã hội của USER. */
@Getter
@Entity
@Table(name = "admin_notifications")
public class AdminNotification extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_admin_id", nullable = false)
    private User recipientAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_admin_id")
    private User actorAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private AdminNotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "required_permission_code", length = 100)
    private String requiredPermissionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 64)
    private AdminNotificationReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "event_key", nullable = false, length = 190)
    private String eventKey;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected AdminNotification() {
        // Constructor rỗng dành cho JPA.
    }

    public void markRead(LocalDateTime now) {
        if (readAt == null) readAt = now;
    }

    public void softDelete(LocalDateTime now) {
        if (deletedAt == null) deletedAt = now;
    }
}
