package com.stu.edu.vn.backend.notification.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.notification.dto.response.NotificationResponse;
import com.stu.edu.vn.backend.notification.enums.NotificationRealtimeEventType;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.mapper.NotificationMapper;
import com.stu.edu.vn.backend.notification.repository.NotificationRepository;
import com.stu.edu.vn.backend.notification.repository.projection.NotificationListProjection;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class NotificationRealtimeListenerTest {

    private final NotificationRepository notificationRepository =
            org.mockito.Mockito.mock(NotificationRepository.class);
    private final NotificationMapper notificationMapper =
            org.mockito.Mockito.mock(NotificationMapper.class);
    private final SimpMessagingTemplate messagingTemplate =
            org.mockito.Mockito.mock(SimpMessagingTemplate.class);
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-07-30T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final NotificationRealtimeListener listener =
            new NotificationRealtimeListener(notificationRepository, notificationMapper, messagingTemplate, clock);

    private NotificationListProjection projection;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        projection = org.mockito.Mockito.mock(NotificationListProjection.class);
        response = new NotificationResponse(
                99L, NotificationType.POST_LIKE, null, 200L, null, null, null,
                LocalDateTime.of(2026, 7, 30, 10, 0));
    }

    @Test
    void committedVisibleNotificationIsSentWithExpectedEnvelope() {
        when(notificationRepository.findVisibleNotificationForRealtime(99L, 20L))
                .thenReturn(Optional.of(projection));
        when(notificationMapper.toResponse(projection)).thenReturn(response);
        when(notificationRepository.countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(20L))
                .thenReturn(7L);

        listener.onNotificationCreated(new NotificationCreatedEvent(99L, 20L));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("20"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"),
                payload.capture()
        );
        var envelope = (com.stu.edu.vn.backend.notification.dto.response.NotificationRealtimeEnvelope)
                payload.getValue();
        assertThat(envelope.schemaVersion()).isEqualTo(1);
        assertThat(envelope.eventType()).isEqualTo(NotificationRealtimeEventType.NOTIFICATION_CREATED);
        assertThat(envelope.notificationId()).isEqualTo(99L);
        assertThat(envelope.notification()).isEqualTo(response);
        assertThat(envelope.unreadCount()).isEqualTo(7L);
        assertThat(envelope.occurredAt()).isEqualTo(LocalDateTime.of(2026, 7, 30, 10, 0));
        assertThatCode(() -> java.util.UUID.fromString(envelope.eventId())).doesNotThrowAnyException();
    }

    @Test
    void deletedBlockedOrInactiveRecipientNotificationIsNotSent() {
        when(notificationRepository.findVisibleNotificationForRealtime(99L, 20L))
                .thenReturn(Optional.empty());

        listener.onNotificationCreated(new NotificationCreatedEvent(99L, 20L));

        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
        verify(notificationRepository, never())
                .countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(any());
    }

    @Test
    void socketFailureDoesNotEscapeAfterCommitListener() {
        when(notificationRepository.findVisibleNotificationForRealtime(99L, 20L))
                .thenReturn(Optional.of(projection));
        when(notificationMapper.toResponse(projection)).thenReturn(response);
        when(notificationRepository.countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(20L))
                .thenReturn(7L);
        org.mockito.Mockito.doThrow(new IllegalStateException("broker unavailable"))
                .when(messagingTemplate).convertAndSendToUser(any(), any(), any());

        assertThatCode(() -> listener.onNotificationCreated(new NotificationCreatedEvent(99L, 20L)))
                .doesNotThrowAnyException();
    }
}
