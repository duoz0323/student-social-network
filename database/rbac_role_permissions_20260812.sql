-- Đồng bộ ma trận RBAC cho database UniShare đang tồn tại.
-- Chạy sau khi bốn bảng roles/permissions/role_permissions/admin_roles đã được tạo.
ALTER TABLE `admin_actions`
  MODIFY `action_type` enum(
    'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG',
    'HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE',
    'REJECT_MODERATION_CASE','RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT','CREATE_ADMIN','UPDATE_ADMIN',
    'DISABLE_ADMIN','ENABLE_ADMIN','ASSIGN_ADMIN_ROLE','REVOKE_ADMIN_ROLE','UPDATE_ROLE_PERMISSIONS'
  ) NOT NULL;

START TRANSACTION;

INSERT INTO `permissions` (`code`, `description`) VALUES
  ('USER_PROFILE_UPDATE', 'Sửa nội dung hồ sơ người dùng.'),
  ('ADMIN_ENABLE', 'Mở khóa tài khoản Admin.')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- Dọn hai meta-permission của bản thiết kế trung gian; phân quyền chỉ dành cho SUPER_ADMIN bất biến.
DELETE FROM `permissions` WHERE `code` IN ('ROLE_PERMISSION_VIEW', 'ROLE_PERMISSION_UPDATE');

UPDATE `roles`
SET `description` = CASE `code`
  WHEN 'USER_MANAGER' THEN 'Xem Dashboard, quản lý người dùng và analytics người dùng.'
  WHEN 'MODERATOR' THEN 'Xem Dashboard, kiểm duyệt bài viết, hashtag và báo cáo.'
  WHEN 'ADS_MANAGER' THEN 'Role dự phòng, hiện chỉ được xem Dashboard tổng quan.'
  WHEN 'COLLABORATOR' THEN 'Cộng tác viên, hiện chỉ được xem Dashboard tổng quan.'
  ELSE `description`
END
WHERE `code` IN ('USER_MANAGER', 'MODERATOR', 'ADS_MANAGER', 'COLLABORATOR');

-- Xóa quyền cũ của bốn role giới hạn; SUPER_ADMIN vẫn giữ toàn bộ permission.
DELETE rp FROM `role_permissions` rp
JOIN `roles` r ON r.id = rp.role_id
WHERE r.code IN ('USER_MANAGER','MODERATOR','ADS_MANAGER','COLLABORATOR');

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code IN (
  'DASHBOARD_BASIC_VIEW','USER_VIEW','USER_SEARCH','USER_FILTER','USER_DETAIL_VIEW','USER_PROFILE_UPDATE',
  'USER_BLOCK','USER_UNBLOCK','USER_ANALYTICS_VIEW'
)
WHERE r.code = 'USER_MANAGER';

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code IN (
  'DASHBOARD_BASIC_VIEW','POST_VIEW','POST_HIDE','POST_RESTORE',
  'HASHTAG_VIEW','HASHTAG_SEARCH','HASHTAG_DELETE',
  'REPORT_VIEW','REPORT_DETAIL_VIEW',
  'REPORT_RESOLVE_NO_VIOLATION','REPORT_RESOLVE_ACTION'
)
WHERE r.code = 'MODERATOR';

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code = 'DASHBOARD_BASIC_VIEW'
WHERE r.code IN ('ADS_MANAGER','COLLABORATOR');

-- SUPER_ADMIN nhận mọi permission hiện tại và permission mới thêm sau này.
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `roles` r CROSS JOIN `permissions` p WHERE r.code = 'SUPER_ADMIN';

COMMIT;
