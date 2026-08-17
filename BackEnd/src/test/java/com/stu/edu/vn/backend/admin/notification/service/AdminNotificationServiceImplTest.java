package com.stu.edu.vn.backend.admin.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.notification.cursor.AdminNotificationCursor;
import com.stu.edu.vn.backend.admin.notification.repository.AdminNotificationRepository;
import com.stu.edu.vn.backend.admin.notification.repository.projection.AdminNotificationProjection;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminNotificationServiceImplTest {
    private AdminNotificationRepository repository;
    private CurrentUserProvider currentUserProvider;
    private CursorCodec cursorCodec;
    private AdminNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = org.mockito.Mockito.mock(AdminNotificationRepository.class);
        currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
        cursorCodec = org.mockito.Mockito.mock(CursorCodec.class);
        service = new AdminNotificationServiceImpl(repository, currentUserProvider, cursorCodec,
                Clock.fixed(Instant.parse("2026-08-17T10:00:00Z"), ZoneOffset.UTC));
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(7L, UserRole.ADMIN, UserStatus.ACTIVE));
    }

    @Test
    void cursorPageUsesLimitPlusOneAndStableLastKey() {
        LocalDateTime t3 = LocalDateTime.of(2026, 8, 17, 9, 3);
        LocalDateTime t2 = LocalDateTime.of(2026, 8, 17, 9, 2);
        // Tạo projection trước khi bắt đầu stubbing repository để tránh nested Mockito stubbing.
        AdminNotificationProjection newest = row(3L, t3);
        AdminNotificationProjection cursorRow = row(2L, t2);
        AdminNotificationProjection lookahead = row(1L, t2.minusMinutes(1));
        when(repository.findVisiblePage(7L, null, null, 3))
                .thenReturn(List.of(newest, cursorRow, lookahead));
        when(cursorCodec.encode(new AdminNotificationCursor(t2, 2L))).thenReturn("opaque-next");

        var page = service.getNotifications(2, null);

        assertThat(page.content()).extracting("notificationId").containsExactly(3L, 2L);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isEqualTo("opaque-next");
    }

    @Test
    void unreadAndReadAllUseSameVisibleRepositoryPolicy() {
        when(repository.countVisibleUnread(7L)).thenReturn(4L);
        when(repository.markAllVisibleRead(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(4);

        assertThat(service.getUnreadCount().unreadCount()).isEqualTo(4L);
        assertThat(service.markAllRead()).isEqualTo(4);
        verify(repository).countVisibleUnread(7L);
    }

    @Test
    void cannotReadAnotherAdminsNotification() {
        when(repository.findVisibleProjection(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(99L))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.ADMIN_NOTIFICATION_NOT_FOUND));
    }

    @Test
    void userAccountCannotUseAdminNotificationService() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(8L, UserRole.USER, UserStatus.ACTIVE));
        assertThatThrownBy(service::getUnreadCount)
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    private AdminNotificationProjection row(Long id, LocalDateTime createdAt) {
        AdminNotificationProjection row = org.mockito.Mockito.mock(AdminNotificationProjection.class);
        when(row.getNotificationId()).thenReturn(id);
        when(row.getType()).thenReturn("POST_REPORT_CREATED");
        when(row.getTitle()).thenReturn("Report");
        when(row.getMessage()).thenReturn("Có báo cáo mới");
        when(row.getReferenceType()).thenReturn("MODERATION_CASE");
        when(row.getReferenceId()).thenReturn(12L);
        when(row.getCreatedAt()).thenReturn(createdAt);
        return row;
    }
}
