package com.stu.edu.vn.backend.admin.enums;

/**
 * Loại thao tác quản trị, ánh xạ đầy đủ các giá trị ENUM hiện có của bảng admin_actions.
 */
public enum AdminActionType {
    BLOCK_USER,
    UNBLOCK_USER,
    UPDATE_USER_PROFILE,
    CREATE_HASHTAG,
    UPDATE_HASHTAG,
    DELETE_HASHTAG,
    HIDE_POST,
    RESTORE_POST,
    RESOLVE_REPORT,
    REJECT_REPORT,
    RESOLVE_MODERATION_CASE,
    REJECT_MODERATION_CASE,
    RESOLVE_PROFILE_REPORT,
    REJECT_PROFILE_REPORT,
    CREATE_ACADEMIC_DATA,
    UPDATE_ACADEMIC_DATA,
    CHANGE_ACADEMIC_STATUS
}
