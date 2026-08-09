-- Ghi nhận riêng thao tác đổi tên hashtag mà không sửa migration đã có thể được áp dụng trước đó.
ALTER TABLE admin_actions
    MODIFY COLUMN action_type ENUM(
        'BLOCK_USER', 'UNBLOCK_USER', 'UPDATE_USER_PROFILE',
        'CREATE_HASHTAG', 'UPDATE_HASHTAG', 'DELETE_HASHTAG',
        'HIDE_POST', 'RESTORE_POST', 'RESOLVE_REPORT', 'REJECT_REPORT',
        'RESOLVE_MODERATION_CASE', 'REJECT_MODERATION_CASE',
        'RESOLVE_PROFILE_REPORT', 'REJECT_PROFILE_REPORT'
    ) NOT NULL;
