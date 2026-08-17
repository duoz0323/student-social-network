package com.stu.edu.vn.backend.auth.support;

import com.stu.edu.vn.backend.auth.dto.AccountBlockedDetails;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;

/** Tạo error Auth nhất quán cho mọi phương thức đăng nhập của tài khoản bị khóa. */
public final class AccountBlockedErrors {
    private AccountBlockedErrors() {
    }

    public static BusinessException forUser(User user) {
        String reason = user.getBlockedReason();
        String message = "Tài khoản của bạn hiện không thể sử dụng Student Social Network.";
        return new BusinessException(ErrorCode.ACCOUNT_BLOCKED,
                new AccountBlockedDetails(reason, user.getBlockedAt(), message));
    }
}
