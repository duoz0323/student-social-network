-- Hotfix cho database đang chạy trước khi chức năng phân quyền được bổ sung.
-- Có thể chạy lại an toàn: MODIFY ENUM chỉ đồng bộ định nghĩa cột, không tạo dữ liệu mới.
ALTER TABLE `admin_actions`
  MODIFY `action_type` enum(
    'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG',
    'HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE',
    'REJECT_MODERATION_CASE','RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT','CREATE_ADMIN','UPDATE_ADMIN',
    'DISABLE_ADMIN','ENABLE_ADMIN','ASSIGN_ADMIN_ROLE','REVOKE_ADMIN_ROLE','UPDATE_ROLE_PERMISSIONS'
  ) NOT NULL;

-- Kết quả phải chứa UPDATE_ROLE_PERMISSIONS trước khi khởi động lại Backend.
SHOW COLUMNS FROM `admin_actions` LIKE 'action_type';
