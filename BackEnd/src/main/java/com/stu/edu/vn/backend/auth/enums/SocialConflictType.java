package com.stu.edu.vn.backend.auth.enums;

/** Các xung đột social cần lựa chọn xử lý rõ ràng. */
public enum SocialConflictType {
    PENDING_EMAIL_MISMATCH,
    PENDING_PHONE_REQUIRES_CANCEL,
    ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER
}
