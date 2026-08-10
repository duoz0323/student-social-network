package com.stu.edu.vn.backend.admin.service;

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

/** Thực hiện đầy đủ thao tác khóa tài khoản phát sinh từ quyết định kiểm duyệt. */
@Service
public class ModerationAccountBlockService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountStatusHistoryRepository historyRepository;
    private final AdminActionRepository actionRepository;
    private final NotificationService notificationService;

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
        actionRepository.save(new AdminAction(
                admin, AdminActionType.BLOCK_USER, AdminTargetType.USER, userId, reasonCode));
        notificationService.createAccountBlockedNotification(userId);
        return true;
    }
}
