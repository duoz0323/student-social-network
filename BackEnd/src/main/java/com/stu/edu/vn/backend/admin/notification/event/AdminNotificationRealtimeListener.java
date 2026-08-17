package com.stu.edu.vn.backend.admin.notification.event;

import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationRealtimeEnvelope;
import com.stu.edu.vn.backend.admin.notification.repository.AdminNotificationRepository;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationServiceImpl;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Phát best-effort sau commit; broker lỗi không thể rollback dữ liệu nghiệp vụ. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationRealtimeListener {
    private final AdminNotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(AdminNotificationCreatedEvent event) {
        try {
            repository.findVisibleProjection(event.notificationId(), event.recipientAdminId()).ifPresent(row -> {
                var envelope = new AdminNotificationRealtimeEnvelope(
                        1,
                        UUID.randomUUID().toString(),
                        "ADMIN_NOTIFICATION_CREATED",
                        LocalDateTime.now(clock),
                        AdminNotificationServiceImpl.toResponse(row),
                        repository.countVisibleUnread(event.recipientAdminId()));
                messagingTemplate.convertAndSendToUser(
                        event.recipientAdminId().toString(), "/queue/admin-notifications", envelope);
            });
        } catch (RuntimeException exception) {
            log.warn("Không thể phát Admin Notification realtime notificationId={} recipientId={}",
                    event.notificationId(), event.recipientAdminId(), exception);
        }
    }
}
