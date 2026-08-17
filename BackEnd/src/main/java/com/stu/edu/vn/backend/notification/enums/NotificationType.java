package com.stu.edu.vn.backend.notification.enums;

/**
 * Ba loại thông báo tương tác được hỗ trợ trong giai đoạn 1.
 */
public enum NotificationType {
    FOLLOW,
    POST_LIKE,
    POST_REPOST,
    POST_COMMENT,
    COMMENT_REPLY,
    REPORT_RESOLVED,
    REPORT_REJECTED,
    POST_HIDDEN_BY_ADMIN,
    POST_RESTORED_BY_ADMIN,
    PROFILE_UPDATED_BY_ADMIN,
    CONTENT_VIOLATION_WARNING,
    CONTENT_VIOLATION_FINAL_WARNING,
    ACCOUNT_BLOCKED,
    ACCOUNT_UNBLOCKED
}
