package com.stu.edu.vn.backend.auth.enums;

/** Hành động được phép dùng để xử lý social conflict. */
public enum SocialResolutionAction {
    CONTINUE_OTP,
    CANCEL_PENDING_AND_CONTINUE_SOCIAL,
    LOGIN_EXISTING_ACCOUNT,
    START_ACCOUNT_RECOVERY
}
