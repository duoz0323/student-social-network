package com.stu.edu.vn.backend.admin.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.admin.notification.event.AdminNotificationCreatedEvent;
import com.stu.edu.vn.backend.admin.notification.repository.AdminNotificationRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class AdminNotificationRouterImplTest {
    private AdminNotificationRepository repository;
    private ApplicationEventPublisher publisher;
    private AdminNotificationRouterImpl router;

    @BeforeEach
    void setUp() {
        repository = org.mockito.Mockito.mock(AdminNotificationRepository.class);
        publisher = org.mockito.Mockito.mock(ApplicationEventPublisher.class);
        router = new AdminNotificationRouterImpl(
                repository,
                org.mockito.Mockito.mock(UserRepository.class),
                publisher);
    }

    @Test
    void anyPermissionDeduplicatesRecipientMatchingMultiplePermissions() {
        when(repository.findActiveRecipientIdsByAnyPermission(any())).thenReturn(List.of(21L));
        when(repository.insertIgnore(anyLong(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1, 0);
        when(repository.findIdByRecipientAndEventKey(21L, "USER_AUTO_BLOCKED:9"))
                .thenReturn(Optional.of(101L));

        router.notifyByAnyPermission(List.of("USER_VIEW", "REPORT_VIEW"), 99L, event("USER_AUTO_BLOCKED:9"));

        verify(repository, times(2)).insertIgnore(eq(21L), eq(99L), any(), any(), any(), any(), any(), any(),
                eq("USER_AUTO_BLOCKED:9"));
        verify(publisher).publishEvent(new AdminNotificationCreatedEvent(101L, 21L));
    }

    @Test
    void sameEventRetryDoesNotPublishOrInsertDuplicateRow() {
        when(repository.findActiveRecipientIdsByAnyPermission(any())).thenReturn(List.of(21L));
        when(repository.insertIgnore(anyLong(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1, 0);
        when(repository.findIdByRecipientAndEventKey(21L, "POST_REPORT:5"))
                .thenReturn(Optional.of(102L));

        router.notifyByPermission("REPORT_VIEW", null, event("POST_REPORT:5"));
        router.notifyByPermission("REPORT_VIEW", null, event("POST_REPORT:5"));

        verify(publisher, times(1)).publishEvent(new AdminNotificationCreatedEvent(102L, 21L));
    }

    @Test
    void actorIsExcludedFromPermissionAudience() {
        when(repository.findActiveRecipientIdsByAnyPermission(any())).thenReturn(List.of(21L, 22L));
        when(repository.insertIgnore(eq(22L), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        when(repository.findIdByRecipientAndEventKey(22L, "POST_HIDDEN:7"))
                .thenReturn(Optional.of(103L));

        router.notifyByPermission("POST_VIEW", 21L, event("POST_HIDDEN:7"));

        verify(repository, times(1)).insertIgnore(eq(22L), eq(21L), any(), any(), any(), any(), any(), any(), any());
        verify(publisher).publishEvent(new AdminNotificationCreatedEvent(103L, 22L));
    }

    private AdminNotificationEvent event(String key) {
        return new AdminNotificationEvent(
                AdminNotificationType.POST_REPORT_CREATED,
                "title",
                "message",
                AdminNotificationReferenceType.MODERATION_CASE,
                5L,
                key);
    }
}
