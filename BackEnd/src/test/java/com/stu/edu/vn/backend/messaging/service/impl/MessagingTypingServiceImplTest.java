package com.stu.edu.vn.backend.messaging.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.messaging.dto.request.TypingRequest;
import com.stu.edu.vn.backend.messaging.dto.response.TypingRealtimeEnvelope;
import com.stu.edu.vn.backend.messaging.enums.MessagingRealtimeEventType;
import com.stu.edu.vn.backend.messaging.projection.TypingTargetProjection;
import com.stu.edu.vn.backend.messaging.realtime.TypingRateLimiter;
import com.stu.edu.vn.backend.messaging.repository.ConversationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Typing chỉ phát cho participant đích và không phụ thuộc Message/Notification. */
class MessagingTypingServiceImplTest {
    private final ConversationRepository conversationRepository = mock(ConversationRepository.class);
    private final TypingRateLimiter rateLimiter = mock(TypingRateLimiter.class);
    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private MessagingTypingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessagingTypingServiceImpl(conversationRepository, rateLimiter, template,
                Clock.fixed(Instant.parse("2026-08-03T03:00:00Z"), ZoneOffset.UTC));
        when(rateLimiter.tryAcquire(10L)).thenReturn(true);
    }

    @Test
    void validFramesMapToStartedAndStoppedWithoutSenderEchoOrUnreadCount() {
        TypingTargetProjection target = target();
        when(conversationRepository.findTypingTarget(15L, 10L)).thenReturn(Optional.of(target));

        service.handleTyping("10", new TypingRequest(15L, true));
        service.handleTyping("10", new TypingRequest(15L, false));

        ArgumentCaptor<TypingRealtimeEnvelope> captor = ArgumentCaptor.forClass(TypingRealtimeEnvelope.class);
        verify(template, times(2)).convertAndSendToUser(eq("20"), eq("/queue/messaging"), captor.capture());
        assertThat(captor.getAllValues()).extracting(TypingRealtimeEnvelope::eventType)
                .containsExactly(MessagingRealtimeEventType.TYPING_STARTED,
                        MessagingRealtimeEventType.TYPING_STOPPED);
        assertThat(captor.getAllValues()).allSatisfy(envelope -> {
            assertThat(envelope.schemaVersion()).isEqualTo(1);
            assertThat(envelope.data().conversationId()).isEqualTo(15L);
            assertThat(envelope.data().userId()).isEqualTo(10L);
        });
        verify(template, never()).convertAndSendToUser(eq("10"), anyString(), any());
    }

    @Test
    void invalidPrincipalPayloadUnauthorizedConversationAndRateLimitAreSilent() {
        service.handleTyping(null, new TypingRequest(15L, true));
        service.handleTyping("invalid", new TypingRequest(15L, true));
        service.handleTyping("10", new TypingRequest(0L, true));
        service.handleTyping("10", new TypingRequest(15L, null));
        service.handleTyping("10", new TypingRequest(15L, true));
        when(rateLimiter.tryAcquire(10L)).thenReturn(false);
        service.handleTyping("10", new TypingRequest(15L, true));

        verify(conversationRepository, times(1)).findTypingTarget(15L, 10L);
        verifyNoInteractions(template);
    }

    @Test
    void handlerUsesReadOnlyTransactionAndBrokerFailureIsBestEffort() throws Exception {
        Transactional transaction = MessagingTypingServiceImpl.class
                .getMethod("handleTyping", String.class, TypingRequest.class)
                .getAnnotation(Transactional.class);
        assertThat(transaction.readOnly()).isTrue();
        TypingTargetProjection target = target();
        when(conversationRepository.findTypingTarget(15L, 10L)).thenReturn(Optional.of(target));
        doThrow(new IllegalStateException("broker down")).when(template)
                .convertAndSendToUser(anyString(), anyString(), any());

        assertThatCode(() -> service.handleTyping("10", new TypingRequest(15L, true)))
                .doesNotThrowAnyException();
    }

    private TypingTargetProjection target() {
        TypingTargetProjection projection = mock(TypingTargetProjection.class);
        when(projection.getConversationId()).thenReturn(15L);
        when(projection.getSenderId()).thenReturn(10L);
        when(projection.getRecipientId()).thenReturn(20L);
        return projection;
    }
}
