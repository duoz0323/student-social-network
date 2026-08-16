package com.stu.edu.vn.backend.messaging.event;

import com.stu.edu.vn.backend.messaging.dto.response.MessageCreatedRealtimeData;
import com.stu.edu.vn.backend.messaging.dto.response.MessageAttachmentResponse;
import com.stu.edu.vn.backend.messaging.dto.response.MessagesReadRealtimeData;
import com.stu.edu.vn.backend.messaging.dto.response.MessagingRealtimeEnvelope;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.messaging.enums.MessagingRealtimeEventType;
import com.stu.edu.vn.backend.messaging.repository.ConversationMemberRepository;
import com.stu.edu.vn.backend.messaging.repository.MessageAttachmentRepository;
import com.stu.edu.vn.backend.messaging.repository.MessageRepository;
import com.stu.edu.vn.backend.messaging.service.SharedPostMessageLoader;
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

/** Đọc lại trạng thái sau commit và phát Messaging realtime theo cơ chế best-effort. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingRealtimeListener {
    private static final int SCHEMA_VERSION = 1;
    private static final String USER_QUEUE = "/queue/messaging";

    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final SharedPostMessageLoader sharedPostMessageLoader;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageCreated(MessageCreatedEvent event) {
        try {
            var projection = messageRepository.findVisibleMessageForRealtime(event.messageId()).orElse(null);
            if (projection == null || !event.conversationId().equals(projection.getConversationId())) {
                return;
            }
            var attachments = attachmentRepository.findByMessageIdOrderByDisplayOrderAsc(projection.getMessageId())
                    .stream().map(item -> new MessageAttachmentResponse(item.getId(), item.getMediaType(),
                            item.getMimeType(), item.getFileSizeBytes(), item.getWidth(), item.getHeight(),
                            item.getDisplayOrder())).toList();
            String eventId = UUID.randomUUID().toString();
            LocalDateTime occurredAt = LocalDateTime.now(clock);
            send(projection.getParticipantLowId(), MessagingRealtimeEventType.MESSAGE_CREATED,
                    eventId, occurredAt, toMessageCreatedData(projection, attachments,
                            projection.getParticipantLowId()));
            send(projection.getParticipantHighId(), MessagingRealtimeEventType.MESSAGE_CREATED,
                    eventId, occurredAt, toMessageCreatedData(projection, attachments,
                            projection.getParticipantHighId()));
        } catch (RuntimeException exception) {
            log.warn("Không thể xử lý Messaging realtime messageId={} conversationId={}",
                    event.messageId(), event.conversationId(), exception);
        }
    }

    private MessageCreatedRealtimeData toMessageCreatedData(
            com.stu.edu.vn.backend.messaging.projection.MessageRealtimeProjection projection,
            java.util.List<MessageAttachmentResponse> attachments, Long viewerId) {
        var sharedPost = projection.getSharedPostId() == null ? null
                : sharedPostMessageLoader.loadVisible(viewerId, java.util.List.of(projection.getSharedPostId()))
                .get(projection.getSharedPostId());
        MessageType type = MessageType.valueOf(projection.getType());
        return new MessageCreatedRealtimeData(
                projection.getMessageId(), projection.getConversationId(), projection.getSenderId(),
                projection.getClientMessageId(), type, projection.getContent(), attachments, sharedPost,
                type == MessageType.POST_SHARE && sharedPost == null, projection.getCreatedAt());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessagesRead(MessagesReadEvent event) {
        try {
            var projection = memberRepository.findVisibleReadMarkerForRealtime(
                    event.conversationId(), event.readerId()).orElse(null);
            if (projection == null || projection.getLastReadMessageId() < event.lastReadMessageId()) {
                return;
            }
            var data = new MessagesReadRealtimeData(projection.getConversationId(), projection.getReaderId(),
                    projection.getLastReadMessageId(), projection.getLastReadAt());
            String eventId = UUID.randomUUID().toString();
            LocalDateTime occurredAt = LocalDateTime.now(clock);
            send(projection.getReaderId(), MessagingRealtimeEventType.MESSAGES_READ, eventId, occurredAt, data);
            send(projection.getOtherUserId(), MessagingRealtimeEventType.MESSAGES_READ, eventId, occurredAt, data);
        } catch (RuntimeException exception) {
            log.warn("Không thể xử lý Messaging realtime conversationId={} readerId={}",
                    event.conversationId(), event.readerId(), exception);
        }
    }

    private void send(Long recipientId, MessagingRealtimeEventType eventType, String eventId,
                      LocalDateTime occurredAt, Object data) {
        try {
            long unreadCount = messageRepository.countUnread(recipientId);
            var envelope = new MessagingRealtimeEnvelope(
                    SCHEMA_VERSION, eventId, eventType, occurredAt, data, unreadCount);
            messagingTemplate.convertAndSendToUser(String.valueOf(recipientId), USER_QUEUE, envelope);
        } catch (RuntimeException exception) {
            // Một session/broker lỗi không được ngăn phát cho participant còn lại hoặc ảnh hưởng REST đã commit.
            log.warn("Không thể phát Messaging realtime recipientId={} eventType={}",
                    recipientId, eventType, exception);
        }
    }
}
