-- Bổ sung quyền và audit action cho chức năng SUPER_ADMIN cấp lại mật khẩu tài khoản Admin.
ALTER TABLE `admin_actions`
  MODIFY `action_type` enum(
    'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG',
    'HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE',
    'REJECT_MODERATION_CASE','RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT','CREATE_ADMIN','UPDATE_ADMIN',
    'DISABLE_ADMIN','ENABLE_ADMIN','RESET_ADMIN_PASSWORD','ASSIGN_ADMIN_ROLE','REVOKE_ADMIN_ROLE',
    'UPDATE_ROLE_PERMISSIONS'
  ) NOT NULL;

START TRANSACTION;

INSERT INTO `permissions` (`code`, `description`) VALUES
  ('ADMIN_PASSWORD_RESET', 'Cấp lại mật khẩu cho tài khoản Admin.')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- SUPER_ADMIN nhận permission mới; role khác chỉ nhận khi được cấu hình rõ trong màn hình phân quyền.
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code = 'ADMIN_PASSWORD_RESET'
WHERE r.code = 'SUPER_ADMIN';

COMMIT;
