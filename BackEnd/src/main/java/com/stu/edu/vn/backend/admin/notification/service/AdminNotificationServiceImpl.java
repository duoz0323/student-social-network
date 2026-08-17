package com.stu.edu.vn.backend.admin.notification.service;

import com.stu.edu.vn.backend.admin.notification.cursor.AdminNotificationCursor;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationMutationResponse;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationResponse;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationUnreadCountResponse;
import com.stu.edu.vn.backend.admin.notification.entity.AdminNotification;
import com.stu.edu.vn.backend.admin.notification.repository.AdminNotificationRepository;
import com.stu.edu.vn.backend.admin.notification.repository.projection.AdminNotificationProjection;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Hộp thư ADMIN áp dụng visibility policy giống nhau cho list, count và mutation. */
@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {
    private final AdminNotificationRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final CursorCodec cursorCodec;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<AdminNotificationResponse> getNotifications(int limit, String cursor) {
        Long adminId = currentAdminId();
        AdminNotificationCursor decoded = cursorCodec.decode(cursor, AdminNotificationCursor.class);
        if (decoded != null && (decoded.createdAt() == null || decoded.id() == null || decoded.id() <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        List<AdminNotificationProjection> rows = repository.findVisiblePage(
                adminId,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.id(),
                limit + 1);
        boolean hasNext = rows.size() > limit;
        List<AdminNotificationProjection> page = hasNext ? rows.subList(0, limit) : rows;
        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            AdminNotificationProjection last = page.get(page.size() - 1);
            nextCursor = cursorCodec.encode(new AdminNotificationCursor(last.getCreatedAt(), last.getNotificationId()));
        }
        List<AdminNotificationResponse> content = new ArrayList<>(page.size());
        page.forEach(row -> content.add(toResponse(row)));
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminNotificationUnreadCountResponse getUnreadCount() {
        return new AdminNotificationUnreadCountResponse(repository.countVisibleUnread(currentAdminId()));
    }

    @Override
    @Transactional
    public AdminNotificationMutationResponse markRead(Long notificationId) {
        Long adminId = currentAdminId();
        requireVisible(notificationId, adminId);
        AdminNotification notification = requireOwned(notificationId, adminId);
        notification.markRead(LocalDateTime.now(clock));
        return new AdminNotificationMutationResponse(notificationId, notification.getReadAt(), false);
    }

    @Override
    @Transactional
    public int markAllRead() {
        return repository.markAllVisibleRead(currentAdminId(), LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public AdminNotificationMutationResponse delete(Long notificationId) {
        Long adminId = currentAdminId();
        requireVisible(notificationId, adminId);
        AdminNotification notification = requireOwned(notificationId, adminId);
        notification.softDelete(LocalDateTime.now(clock));
        return new AdminNotificationMutationResponse(notificationId, notification.getReadAt(), true);
    }

    private Long currentAdminId() {
        var principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.ADMIN || principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return principal.getUserId();
    }

    private void requireVisible(Long id, Long adminId) {
        repository.findVisibleProjection(id, adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOTIFICATION_NOT_FOUND));
    }

    private AdminNotification requireOwned(Long id, Long adminId) {
        return repository.findByIdAndRecipientAdmin_IdAndDeletedAtIsNull(id, adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOTIFICATION_NOT_FOUND));
    }

    public static AdminNotificationResponse toResponse(AdminNotificationProjection row) {
        return new AdminNotificationResponse(
                row.getNotificationId(), row.getType(), row.getTitle(), row.getMessage(),
                row.getReferenceType(), row.getReferenceId(), row.getReadAt(), row.getCreatedAt());
    }
}
