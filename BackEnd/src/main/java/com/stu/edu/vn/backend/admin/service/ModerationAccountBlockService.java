package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationEvent;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationRouter;
import com.stu.edu.vn.backend.admin.entity.AccountStatusHistory;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/** Thực hiện đầy đủ thao tác khóa tài khoản phát sinh từ quyết định kiểm duyệt. */
@Service
public class ModerationAccountBlockService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountStatusHistoryRepository historyRepository;
    private final AdminActionRepository actionRepository;
    private final NotificationService notificationService;
    private AdminNotificationRouter adminNotificationRouter;

    public ModerationAccountBlockService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AccountStatusHistoryRepository historyRepository,
            AdminActionRepository actionRepository,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.historyRepository = historyRepository;
        this.actionRepository = actionRepository;
        this.notificationService = notificationService;
    }

    @Autowired
    void setAdminNotificationRouter(AdminNotificationRouter adminNotificationRouter) {
        this.adminNotificationRouter = adminNotificationRouter;
    }

    /** Trả true khi tài khoản vừa được khóa; tài khoản đã khóa được xem là trạng thái đích hợp lệ. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean blockIfActive(
            Long userId,
            User admin,
            LocalDateTime now,
            AdminBlockReason reason
    ) {
        User target = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        if (target.getRole() != UserRole.USER) {
            throw new BusinessException(ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);
        }
        if (target.getStatus() == UserStatus.BLOCKED) return false;

        String reasonCode = reason.name();
        target.setStatus(UserStatus.BLOCKED);
        target.setBlockedAt(now);
        target.setBlockedReason(reasonCode);
        refreshTokenRepository.revokeAllActiveByUserId(userId, now);
        historyRepository.save(new AccountStatusHistory(
                target, UserStatus.ACTIVE, UserStatus.BLOCKED, admin, reasonCode));
        AdminAction action = actionRepository.save(new AdminAction(
                admin, AdminActionType.BLOCK_USER, AdminTargetType.USER, userId, reasonCode));
        notificationService.createAccountBlockedNotification(userId);
        if (reason == AdminBlockReason.REPEATED_VIOLATION && adminNotificationRouter != null) {
            adminNotificationRouter.notifyByAnyPermission(
                    java.util.List.of("USER_VIEW", "REPORT_VIEW"),
                    admin.getId(),
                    new AdminNotificationEvent(
                            AdminNotificationType.USER_AUTO_BLOCKED_REPEATED_VIOLATION,
                            "Tài khoản tự động bị khóa do vi phạm lặp lại",
                            "Một tài khoản đã đạt ngưỡng vi phạm và bị khóa tự động.",
                            AdminNotificationReferenceType.USER,
                            userId,
                            "ADMIN_ACTION:" + action.getId() + ":USER_AUTO_BLOCKED"));
        }
        return true;
    }
}
