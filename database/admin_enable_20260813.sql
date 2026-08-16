-- Bổ sung quyền và loại audit cho chức năng mở khóa tài khoản quản trị viên.
-- ALTER TABLE nằm ngoài transaction vì MySQL tự commit khi thực thi DDL.
ALTER TABLE `admin_actions`
  MODIFY `action_type` enum(
    'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG',
    'HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE',
    'REJECT_MODERATION_CASE','RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT','CREATE_ADMIN','UPDATE_ADMIN',
    'DISABLE_ADMIN','ENABLE_ADMIN','ASSIGN_ADMIN_ROLE','REVOKE_ADMIN_ROLE','UPDATE_ROLE_PERMISSIONS'
  ) NOT NULL;

START TRANSACTION;

INSERT INTO `permissions` (`code`, `description`) VALUES
  ('ADMIN_ENABLE', 'Mở khóa tài khoản Admin.')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- SUPER_ADMIN luôn nhận permission mới; các role khác được cấu hình từ màn hình Phân quyền.
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code = 'ADMIN_ENABLE'
WHERE r.code = 'SUPER_ADMIN';

COMMIT;

SHOW COLUMNS FROM `admin_actions` LIKE 'action_type';
