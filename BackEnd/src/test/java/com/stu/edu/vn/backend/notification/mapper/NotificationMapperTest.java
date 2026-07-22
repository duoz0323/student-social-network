package com.stu.edu.vn.backend.notification.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.repository.projection.NotificationListProjection;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();

    @Test
    void mapsProjectionToPublicResponseWithoutEntityAccess() {
        NotificationListProjection source = org.mockito.Mockito.mock(NotificationListProjection.class);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 18, 10, 0);
        when(source.getNotificationId()).thenReturn(1L);
        when(source.getType()).thenReturn("POST_LIKE");
        when(source.getActorId()).thenReturn(10L);
        when(source.getActorDisplayName()).thenReturn("Nguyen Van A");
        when(source.getActorAvatarUrl()).thenReturn("https://cdn.example/avatar.png");
        when(source.getPostId()).thenReturn(20L);
        when(source.getCreatedAt()).thenReturn(createdAt);

        var response = mapper.toResponse(source);

        assertThat(response.notificationId()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(NotificationType.POST_LIKE);
        assertThat(response.actor().userId()).isEqualTo(10L);
        assertThat(response.postId()).isEqualTo(20L);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void mapsAdministrativeNotificationWithoutExposingActor() {
        NotificationListProjection source = org.mockito.Mockito.mock(NotificationListProjection.class);
        when(source.getNotificationId()).thenReturn(2L);
        when(source.getType()).thenReturn("REPORT_RESOLVED");
        when(source.getActorId()).thenReturn(null);
        when(source.getReportId()).thenReturn(30L);

        var response = mapper.toResponse(source);

        assertThat(response.type()).isEqualTo(NotificationType.REPORT_RESOLVED);
        assertThat(response.actor()).isNull();
        assertThat(response.reportId()).isEqualTo(30L);
    }
}
