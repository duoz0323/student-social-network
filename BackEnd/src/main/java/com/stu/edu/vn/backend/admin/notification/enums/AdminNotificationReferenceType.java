package com.stu.edu.vn.backend.admin.notification.enums;

/** Loại tài nguyên đích; Frontend chỉ map các giá trị allowlist này sang route quản trị. */
public enum AdminNotificationReferenceType {
    MODERATION_CASE,
    PROFILE_REPORT,
    MODERATION_SUGGESTION,
    USER,
    POST,
    HASHTAG,
    ADMIN,
    ROLE
}
