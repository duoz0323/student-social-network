package com.stu.edu.vn.backend.notification.realtime;

import com.stu.edu.vn.backend.notification.dto.response.RealtimeNotificationResponse;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.event.PostRepostNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Chỉ phát WebSocket sau commit; lỗi realtime không được làm sai dữ liệu REST/MySQL đã commit. */
@Component
public class PostRepostRealtimeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostRepostRealtimeListener.class);
    private final SimpMessagingTemplate messagingTemplate;

    public PostRepostRealtimeListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostRepost(PostRepostNotificationEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    event.recipientId().toString(),
                    "/queue/notifications",
                    new RealtimeNotificationResponse(NotificationType.POST_REPOST, event.actorId(), event.postId()));
        } catch (RuntimeException exception) {
            // Realtime là best-effort; client luôn có thể đọc lại trạng thái chuẩn qua REST.
            LOGGER.warn("Không thể phát realtime POST_REPOST cho recipientId={}", event.recipientId(), exception);
        }
    }
}
