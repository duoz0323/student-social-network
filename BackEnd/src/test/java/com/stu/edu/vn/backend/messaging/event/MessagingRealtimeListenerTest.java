package com.stu.edu.vn.backend.messaging.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.messaging.dto.response.MessagingRealtimeEnvelope;
import com.stu.edu.vn.backend.messaging.dto.response.MessageCreatedRealtimeData;
import com.stu.edu.vn.backend.messaging.dto.response.SharedPostAuthorResponse;
import com.stu.edu.vn.backend.messaging.dto.response.SharedPostResponse;
import com.stu.edu.vn.backend.messaging.enums.MessagingRealtimeEventType;
import com.stu.edu.vn.backend.messaging.projection.MessageRealtimeProjection;
import com.stu.edu.vn.backend.messaging.projection.MessagesReadRealtimeProjection;
import com.stu.edu.vn.backend.messaging.repository.ConversationMemberRepository;
import com.stu.edu.vn.backend.messaging.repository.MessageRepository;
import com.stu.edu.vn.backend.messaging.repository.MessageAttachmentRepository;
import com.stu.edu.vn.backend.messaging.service.SharedPostMessageLoader;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Contract test cho envelope, visibility và best-effort của Messaging realtime. */
class MessagingRealtimeListenerTest {
    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final ConversationMemberRepository memberRepository = mock(ConversationMemberRepository.class);
    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final MessageAttachmentRepository attachmentRepository = mock(MessageAttachmentRepository.class);
    private final SharedPostMessageLoader sharedPostMessageLoader = mock(SharedPostMessageLoader.class);
    private MessagingRealtimeListener listener;

    @BeforeEach
    void setUp() {
        listener = new MessagingRealtimeListener(messageRepository, memberRepository, attachmentRepository,
                sharedPostMessageLoader, template,
                Clock.fixed(Instant.parse("2026-08-03T03:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void messageCreatedIsAfterCommitRequiresNewAndSentToBothUsersWithOwnUnreadCount() throws Exception {
        var method = MessagingRealtimeListener.class.getMethod("onMessageCreated", MessageCreatedEvent.class);
        assertThat(method.getAnnotation(TransactionalEventListener.class).phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
        Transactional transaction = method.getAnnotation(Transactional.class);
        assertThat(transaction.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
        assertThat(transaction.readOnly()).isTrue();

        MessageRealtimeProjection row = messageProjection();
        when(messageRepository.findVisibleMessageForRealtime(901L)).thenReturn(Optional.of(row));
        when(messageRepository.countUnread(10L)).thenReturn(0L);
        when(messageRepository.countUnread(20L)).thenReturn(8L);

        listener.onMessageCreated(new MessageCreatedEvent(901L, 15L));

        ArgumentCaptor<MessagingRealtimeEnvelope> captor = ArgumentCaptor.forClass(MessagingRealtimeEnvelope.class);
        verify(template).convertAndSendToUser(eq("10"), eq("/queue/messaging"), captor.capture());
        verify(template).convertAndSendToUser(eq("20"), eq("/queue/messaging"), captor.capture());
        assertThat(captor.getAllValues()).extracting(MessagingRealtimeEnvelope::unreadCount).containsExactlyInAnyOrder(0L, 8L);
        assertThat(captor.getAllValues()).allSatisfy(envelope -> {
            assertThat(envelope.schemaVersion()).isEqualTo(1);
            assertThat(envelope.eventType()).isEqualTo(MessagingRealtimeEventType.MESSAGE_CREATED);
            assertThat(envelope.data().getClass().getSimpleName()).isEqualTo("MessageCreatedRealtimeData");
        });
    }

    @Test
    void visibilityFailureSuppressesEventAndBrokerFailureDoesNotStopOtherRecipient() {
        when(messageRepository.findVisibleMessageForRealtime(901L)).thenReturn(Optional.empty());
        listener.onMessageCreated(new MessageCreatedEvent(901L, 15L));
        verifyNoInteractions(template);

        reset(template);
        MessageRealtimeProjection visibleRow = messageProjection();
        when(messageRepository.findVisibleMessageForRealtime(901L)).thenReturn(Optional.of(visibleRow));
        doThrow(new IllegalStateException("broker down")).when(template)
                .convertAndSendToUser(eq("10"), eq("/queue/messaging"), any());
        listener.onMessageCreated(new MessageCreatedEvent(901L, 15L));
        verify(template).convertAndSendToUser(eq("20"), eq("/queue/messaging"), any());
    }

    @Test
    void postShareRealtimeHydratesPreviewSeparatelyForEachViewer() {
        MessageRealtimeProjection row = messageProjection();
        when(row.getType()).thenReturn("POST_SHARE");
        when(row.getSharedPostId()).thenReturn(125L);
        SharedPostResponse preview = new SharedPostResponse(125L,
                new SharedPostAuthorResponse(30L, "author", "Tác giả", null),
                "Nội dung", List.of(), 1, 2, 3);
        when(sharedPostMessageLoader.loadVisible(10L, List.of(125L))).thenReturn(Map.of(125L, preview));
        when(sharedPostMessageLoader.loadVisible(20L, List.of(125L))).thenReturn(Map.of());
        when(messageRepository.findVisibleMessageForRealtime(901L)).thenReturn(Optional.of(row));

        listener.onMessageCreated(new MessageCreatedEvent(901L, 15L));

        ArgumentCaptor<MessagingRealtimeEnvelope> sender = ArgumentCaptor.forClass(MessagingRealtimeEnvelope.class);
        ArgumentCaptor<MessagingRealtimeEnvelope> recipient = ArgumentCaptor.forClass(MessagingRealtimeEnvelope.class);
        verify(template).convertAndSendToUser(eq("10"), eq("/queue/messaging"), sender.capture());
        verify(template).convertAndSendToUser(eq("20"), eq("/queue/messaging"), recipient.capture());
        MessageCreatedRealtimeData senderData = (MessageCreatedRealtimeData) sender.getValue().data();
        MessageCreatedRealtimeData recipientData = (MessageCreatedRealtimeData) recipient.getValue().data();
        assertThat(senderData.sharedPost()).isEqualTo(preview);
        assertThat(senderData.sharedPostUnavailable()).isFalse();
        assertThat(recipientData.sharedPost()).isNull();
        assertThat(recipientData.sharedPostUnavailable()).isTrue();
    }

    @Test
    void readEventUsesAuthoritativeMonotonicMarkerAndSendsBothParticipants() throws Exception {
        var method = MessagingRealtimeListener.class.getMethod("onMessagesRead", MessagesReadEvent.class);
        assertThat(method.getAnnotation(TransactionalEventListener.class).phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
        MessagesReadRealtimeProjection row = mock(MessagesReadRealtimeProjection.class);
        when(row.getConversationId()).thenReturn(15L);
        when(row.getReaderId()).thenReturn(20L);
        when(row.getOtherUserId()).thenReturn(10L);
        when(row.getLastReadMessageId()).thenReturn(905L);
        when(row.getLastReadAt()).thenReturn(LocalDateTime.of(2026, 8, 3, 10, 1));
        when(memberRepository.findVisibleReadMarkerForRealtime(15L, 20L)).thenReturn(Optional.of(row));
        when(messageRepository.countUnread(20L)).thenReturn(0L);
        when(messageRepository.countUnread(10L)).thenReturn(2L);

        listener.onMessagesRead(new MessagesReadEvent(15L, 20L, 901L));

        verify(template).convertAndSendToUser(eq("20"), eq("/queue/messaging"), any(MessagingRealtimeEnvelope.class));
        verify(template).convertAndSendToUser(eq("10"), eq("/queue/messaging"), any(MessagingRealtimeEnvelope.class));
    }

    private MessageRealtimeProjection messageProjection() {
        MessageRealtimeProjection row = mock(MessageRealtimeProjection.class);
        when(row.getMessageId()).thenReturn(901L);
        when(row.getConversationId()).thenReturn(15L);
        when(row.getSenderId()).thenReturn(10L);
        when(row.getClientMessageId()).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(row.getType()).thenReturn("TEXT");
        when(row.getContent()).thenReturn("Xin chào");
        when(row.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 8, 3, 10, 0));
        when(row.getParticipantLowId()).thenReturn(10L);
        when(row.getParticipantHighId()).thenReturn(20L);
        return row;
    }
}
