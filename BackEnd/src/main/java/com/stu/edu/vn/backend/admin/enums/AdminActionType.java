package com.stu.edu.vn.backend.admin.enums;

/**
 * Loại thao tác quản trị, ánh xạ đầy đủ các giá trị ENUM hiện có của bảng admin_actions.
 */
public enum AdminActionType {
    BLOCK_USER,
    UNBLOCK_USER,
    UPDATE_USER_PROFILE,
    HIDE_POST,
    RESTORE_POST,
    RESOLVE_REPORT,
    REJECT_REPORT
}
