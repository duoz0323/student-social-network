package com.stu.edu.vn.backend.notification.event;

import com.stu.edu.vn.backend.notification.dto.response.NotificationRealtimeEnvelope;
import com.stu.edu.vn.backend.notification.enums.NotificationRealtimeEventType;
import com.stu.edu.vn.backend.notification.mapper.NotificationMapper;
import com.stu.edu.vn.backend.notification.repository.NotificationRepository;
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

/**
 * Đọc lại Notification sau commit và phát best-effort; lỗi socket không ảnh hưởng nghiệp vụ đã lưu.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRealtimeListener {

    private static final int SCHEMA_VERSION = 1;
    private static final String USER_QUEUE = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        try {
            var projection = notificationRepository.findVisibleNotificationForRealtime(
                    event.notificationId(), event.recipientId());
            if (projection.isEmpty()) {
                return;
            }

            var notification = notificationMapper.toResponse(projection.get());
            long unreadCount = notificationRepository
                    .countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(event.recipientId());
            var envelope = new NotificationRealtimeEnvelope(
                    SCHEMA_VERSION,
                    UUID.randomUUID().toString(),
                    NotificationRealtimeEventType.NOTIFICATION_CREATED,
                    LocalDateTime.now(clock),
                    event.notificationId(),
                    notification,
                    unreadCount
            );
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(event.recipientId()),
                    USER_QUEUE,
                    envelope
            );
        } catch (RuntimeException exception) {
            // Chỉ log định danh nội bộ an toàn; tuyệt đối không log Access Token hoặc payload xác thực.
            log.warn(
                    "Không thể phát Notification realtime notificationId={} recipientId={}",
                    event.notificationId(),
                    event.recipientId(),
                    exception
            );
        }
    }
}
