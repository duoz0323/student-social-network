-- Đồng bộ lịch sử quản trị cho chức năng ADMIN chỉnh sửa hồ sơ USER.
ALTER TABLE admin_actions
    MODIFY COLUMN action_type ENUM(
        'BLOCK_USER',
        'UNBLOCK_USER',
        'UPDATE_USER_PROFILE',
        'HIDE_POST',
        'RESTORE_POST',
        'RESOLVE_REPORT',
        'REJECT_REPORT'
    ) NOT NULL;
