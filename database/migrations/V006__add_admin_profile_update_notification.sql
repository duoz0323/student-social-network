-- Bổ sung thông báo hệ thống khi ADMIN điều chỉnh hồ sơ USER vi phạm tiêu chuẩn.
ALTER TABLE notifications
    MODIFY COLUMN type ENUM(
        'FOLLOW',
        'POST_LIKE',
        'POST_COMMENT',
        'COMMENT_REPLY',
        'POST_REPOST',
        'REPORT_RESOLVED',
        'REPORT_REJECTED',
        'POST_HIDDEN_BY_ADMIN',
        'POST_RESTORED_BY_ADMIN',
        'PROFILE_UPDATED_BY_ADMIN',
        'ACCOUNT_BLOCKED',
        'ACCOUNT_UNBLOCKED'
    ) NOT NULL;
