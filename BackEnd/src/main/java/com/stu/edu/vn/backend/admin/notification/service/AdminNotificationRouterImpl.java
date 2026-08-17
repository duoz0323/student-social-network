package com.stu.edu.vn.backend.admin.notification.service;

import com.stu.edu.vn.backend.admin.notification.event.AdminNotificationCreatedEvent;
import com.stu.edu.vn.backend.admin.notification.repository.AdminNotificationRepository;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Resolver tập trung fan-out theo RBAC authoritative và chống trùng bằng unique event key. */
@Service
@RequiredArgsConstructor
public class AdminNotificationRouterImpl implements AdminNotificationRouter {
    private final AdminNotificationRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyByPermission(String permissionCode, Long actorAdminId, AdminNotificationEvent event) {
        notifyByAnyPermission(List.of(permissionCode), actorAdminId, event);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyByAnyPermission(
            Collection<String> permissionCodes,
            Long actorAdminId,
            AdminNotificationEvent event
    ) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        permissionCodes.forEach(code -> normalized.add(code.trim().toUpperCase(Locale.ROOT)));
        // Chạy theo từng permission để mỗi recipient lưu đúng một permission mà họ thực sự đang có.
        for (String permissionCode : normalized) {
            repository.findActiveRecipientIdsByAnyPermission(List.of(permissionCode)).stream()
                    .filter(recipientId -> !recipientId.equals(actorAdminId))
                    .forEach(recipientId -> insert(recipientId, actorAdminId, permissionCode, event));
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyDirectAdmin(Long recipientAdminId, Long actorAdminId, AdminNotificationEvent event) {
        User recipient = userRepository.findById(recipientAdminId).orElse(null);
        if (recipient == null || recipient.getRole() != UserRole.ADMIN || recipient.getStatus() != UserStatus.ACTIVE) {
            return;
        }
        insert(recipientAdminId, actorAdminId, null, event);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoleHolders(Long roleId, Long actorAdminId, AdminNotificationEvent event) {
        repository.findActiveRecipientIdsByRoleId(roleId).stream()
                .filter(recipientId -> !recipientId.equals(actorAdminId))
                .forEach(recipientId -> insert(recipientId, actorAdminId, null, event));
    }

    private void insert(Long recipientId, Long actorId, String permissionCode, AdminNotificationEvent event) {
        int inserted = repository.insertIgnore(
                recipientId,
                actorId,
                event.type().name(),
                event.title(),
                event.message(),
                permissionCode,
                event.referenceType() == null ? null : event.referenceType().name(),
                event.referenceId(),
                event.eventKey());
        if (inserted == 1) {
            Long notificationId = repository.findIdByRecipientAndEventKey(recipientId, event.eventKey())
                    .orElseThrow();
            eventPublisher.publishEvent(new AdminNotificationCreatedEvent(notificationId, recipientId));
        }
    }
}
