package com.stu.edu.vn.backend.messaging.service.impl;

import com.stu.edu.vn.backend.messaging.dto.request.TypingRequest;
import com.stu.edu.vn.backend.messaging.dto.response.TypingRealtimeData;
import com.stu.edu.vn.backend.messaging.dto.response.TypingRealtimeEnvelope;
import com.stu.edu.vn.backend.messaging.enums.MessagingRealtimeEventType;
import com.stu.edu.vn.backend.messaging.realtime.TypingRateLimiter;
import com.stu.edu.vn.backend.messaging.repository.ConversationRepository;
import com.stu.edu.vn.backend.messaging.service.MessagingTypingService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Xác thực visibility bằng một projection rồi phát typing best-effort cho participant còn lại. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingTypingServiceImpl implements MessagingTypingService {
    private static final int SCHEMA_VERSION = 1;
    private static final String USER_QUEUE = "/queue/messaging";

    private final ConversationRepository conversationRepository;
    private final TypingRateLimiter rateLimiter;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public void handleTyping(String principalName, TypingRequest request) {
        Long senderId = parseUserId(principalName);
        if (senderId == null || request == null || request.conversationId() == null
                || request.conversationId() <= 0 || request.typing() == null
                || !rateLimiter.tryAcquire(senderId)) {
            return;
        }
        var target = conversationRepository.findTypingTarget(request.conversationId(), senderId).orElse(null);
        if (target == null) {
            // Trả về im lặng để không tiết lộ conversation tồn tại hay quyền membership.
            return;
        }
        MessagingRealtimeEventType eventType = request.typing()
                ? MessagingRealtimeEventType.TYPING_STARTED
                : MessagingRealtimeEventType.TYPING_STOPPED;
        var envelope = new TypingRealtimeEnvelope(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                eventType,
                LocalDateTime.now(clock),
                new TypingRealtimeData(target.getConversationId(), senderId));
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(target.getRecipientId()), USER_QUEUE, envelope);
        } catch (RuntimeException exception) {
            log.warn("Không thể phát Typing realtime conversationId={} senderId={} recipientId={}",
                    target.getConversationId(), senderId, target.getRecipientId(), exception);
        }
    }

    private Long parseUserId(String principalName) {
        try {
            long userId = Long.parseLong(principalName);
            return userId > 0 ? userId : null;
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
