-- UniShare - migration gộp các chức năng Admin/RBAC/Collaborator.
-- Mục tiêu: nâng cấp database đang tồn tại, không drop database, không drop table
-- và không nạp dữ liệu demo. Có thể chạy lại khi lần chạy trước bị gián đoạn.
--
-- Yêu cầu:
--   * MySQL 8.0+.
--   * Chọn đúng database đích trước khi chạy, ví dụ:
--       mysql --default-character-set=utf8mb4 -u root -p student_social_network \
--         < database/V20260816__admin_rbac_collaborator_features.sql
--   * Database nền phải có các bảng users, user_profiles, posts và admin_actions.
--
-- Phạm vi ghi dữ liệu chỉ gồm master data RBAC: roles, permissions,
-- role_permissions. Không cập nhật user, post, hashtag, report hoặc dữ liệu xã hội.
-- DDL của MySQL tự commit; hãy backup database trước khi chạy trên môi trường dùng chung.

SET NAMES utf8mb4;

DELIMITER $$

DROP PROCEDURE IF EXISTS `migrate_admin_rbac_collaborator_20260816`$$
CREATE PROCEDURE `migrate_admin_rbac_collaborator_20260816`()
BEGIN
  -- Dừng sớm nếu import nhầm mà chưa chọn database hoặc database nền không phù hợp.
  IF DATABASE() IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Hay chon database dich truoc khi chay migration';
  END IF;

  IF (SELECT COUNT(*) FROM information_schema.tables
      WHERE table_schema = DATABASE()
        AND table_name IN ('users', 'user_profiles', 'posts', 'admin_actions')) <> 4 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Thieu bang nen users, user_profiles, posts hoac admin_actions';
  END IF;

  -- Cột này chỉ phân biệt user thường với Managed Social Identity.
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'account_type'
  ) THEN
    ALTER TABLE `users`
      ADD COLUMN `account_type` varchar(16) NOT NULL DEFAULT 'NORMAL' AFTER `status`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'users'
      AND constraint_name = 'chk_users_account_type'
      AND constraint_type = 'CHECK'
  ) THEN
    ALTER TABLE `users`
      ADD CONSTRAINT `chk_users_account_type`
      CHECK (`account_type` IN ('NORMAL', 'MANAGED'));
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'users'
      AND constraint_name = 'chk_users_managed_auth'
      AND constraint_type = 'CHECK'
  ) THEN
    ALTER TABLE `users`
      ADD CONSTRAINT `chk_users_managed_auth` CHECK (
        `account_type` = 'NORMAL' OR
        (`role` = 'USER' AND `email` IS NULL
          AND `email_verified_at` IS NULL AND `password_hash` IS NULL)
      );
  END IF;

  -- Nới check hồ sơ ở mức cột; trigger bên dưới vẫn bắt buộc ngày sinh
  -- đối với NORMAL và chỉ miễn ngày sinh cho danh tính MANAGED.
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'user_profiles'
      AND constraint_name = 'chk_user_profiles_completion_consistency'
      AND constraint_type = 'CHECK'
  ) THEN
    ALTER TABLE `user_profiles`
      DROP CHECK `chk_user_profiles_completion_consistency`;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'user_profiles'
      AND constraint_name = 'chk_user_profiles_completion_requires_birth_date'
      AND constraint_type = 'CHECK'
  ) THEN
    ALTER TABLE `user_profiles`
      DROP CHECK `chk_user_profiles_completion_requires_birth_date`;
  END IF;

  ALTER TABLE `user_profiles`
    ADD CONSTRAINT `chk_user_profiles_completion_consistency` CHECK (
      `profile_completed_at` IS NULL OR
      (`username` IS NOT NULL AND `display_name` IS NOT NULL)
    );
END$$

CALL `migrate_admin_rbac_collaborator_20260816`()$$
DROP PROCEDURE `migrate_admin_rbac_collaborator_20260816`$$

-- Trigger bảo toàn nghiệp vụ onboarding sau khi check được nới cho MANAGED.
DROP TRIGGER IF EXISTS `trg_user_profiles_completion_birth_insert`$$
DROP TRIGGER IF EXISTS `trg_user_profiles_completion_birth_update`$$

CREATE TRIGGER `trg_user_profiles_completion_birth_insert`
BEFORE INSERT ON `user_profiles`
FOR EACH ROW
BEGIN
  DECLARE owner_account_type varchar(16);
  IF NEW.profile_completed_at IS NOT NULL THEN
    SELECT account_type INTO owner_account_type
    FROM users
    WHERE id = NEW.user_id;

    IF owner_account_type IS NULL
       OR (owner_account_type <> 'MANAGED' AND NEW.date_of_birth IS NULL) THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Completed normal profile requires date_of_birth';
    END IF;
  END IF;
END$$

CREATE TRIGGER `trg_user_profiles_completion_birth_update`
BEFORE UPDATE ON `user_profiles`
FOR EACH ROW
BEGIN
  DECLARE owner_account_type varchar(16);
  IF NEW.profile_completed_at IS NOT NULL THEN
    SELECT account_type INTO owner_account_type
    FROM users
    WHERE id = NEW.user_id;

    IF owner_account_type IS NULL
       OR (owner_account_type <> 'MANAGED' AND NEW.date_of_birth IS NULL) THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Completed normal profile requires date_of_birth';
    END IF;
  END IF;
END$$

DELIMITER ;

-- Các bảng RBAC được tạo bổ sung nếu database cũ chưa có.
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `display_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `reserved` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_roles_code` (`code`),
  CONSTRAINT `chk_roles_code_not_blank` CHECK (char_length(trim(`code`)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `permissions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_permissions_code` (`code`),
  CONSTRAINT `chk_permissions_code_not_blank` CHECK (char_length(trim(`code`)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role_permissions` (
  `role_id` bigint unsigned NOT NULL,
  `permission_id` bigint unsigned NOT NULL,
  `assigned_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_role_permissions_permission_role` (`permission_id`, `role_id`),
  CONSTRAINT `fk_role_permissions_role`
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_permissions_permission`
    FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_roles` (
  `admin_id` bigint unsigned NOT NULL,
  `role_id` bigint unsigned NOT NULL,
  `assigned_by` bigint unsigned DEFAULT NULL,
  `assigned_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`admin_id`, `role_id`),
  KEY `idx_admin_roles_role_admin` (`role_id`, `admin_id`),
  KEY `idx_admin_roles_assigned_by` (`assigned_by`),
  CONSTRAINT `fk_admin_roles_admin`
    FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_roles_role`
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_roles_assigned_by`
    FOREIGN KEY (`assigned_by`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Một admin có đúng một Managed Social Identity, không tái tạo bảng Social cũ.
CREATE TABLE IF NOT EXISTS `admin_social_identities` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` bigint unsigned NOT NULL,
  `social_user_id` bigint unsigned NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_by` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_admin_social_identities_admin` (`admin_id`),
  UNIQUE KEY `uq_admin_social_identities_social_user` (`social_user_id`),
  KEY `idx_admin_social_identities_created_by` (`created_by`),
  CONSTRAINT `fk_admin_social_identities_admin`
    FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_social_identities_social_user`
    FOREIGN KEY (`social_user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_social_identities_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_admin_social_identities_status`
    CHECK (`status` IN ('ACTIVE', 'DISABLED')),
  CONSTRAINT `chk_admin_social_identities_distinct_users`
    CHECK (`admin_id` <> `social_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng đề xuất kiểm duyệt là dữ liệu riêng của module Collaborator/Moderator.
CREATE TABLE IF NOT EXISTS `moderation_suggestions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `suggested_by_admin_id` bigint unsigned NOT NULL,
  `reason` varchar(32) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `reviewed_by_admin_id` bigint unsigned DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  `pending_admin_id` bigint unsigned GENERATED ALWAYS AS (
    IF(`status` = 'PENDING', `suggested_by_admin_id`, NULL)
  ) STORED,
  `pending_post_id` bigint unsigned GENERATED ALWAYS AS (
    IF(`status` = 'PENDING', `post_id`, NULL)
  ) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_moderation_suggestions_pending`
    (`pending_admin_id`, `pending_post_id`),
  KEY `idx_moderation_suggestions_status_created`
    (`status`, `created_at` DESC, `id` DESC),
  KEY `idx_moderation_suggestions_post` (`post_id`, `created_at` DESC),
  KEY `idx_moderation_suggestions_reviewer` (`reviewed_by_admin_id`),
  CONSTRAINT `fk_moderation_suggestions_post`
    FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_moderation_suggestions_suggester`
    FOREIGN KEY (`suggested_by_admin_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_moderation_suggestions_reviewer`
    FOREIGN KEY (`reviewed_by_admin_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_moderation_suggestions_reason` CHECK (
    `reason` IN (
      'SPAM', 'INAPPROPRIATE_CONTENT', 'SCAM_SUSPECTED',
      'HARASSMENT', 'HARMFUL_CONTENT', 'OTHER'
    )
  ),
  CONSTRAINT `chk_moderation_suggestions_status`
    CHECK (`status` IN ('PENDING', 'ACCEPTED', 'REJECTED')),
  CONSTRAINT `chk_moderation_suggestions_review_state` CHECK (
    (`status` = 'PENDING'
      AND `reviewed_by_admin_id` IS NULL AND `reviewed_at` IS NULL)
    OR
    (`status` IN ('ACCEPTED', 'REJECTED')
      AND `reviewed_by_admin_id` IS NOT NULL AND `reviewed_at` IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đồng bộ tập giá trị audit theo enum Backend hiện tại. Tập mới là superset
-- của tập cũ nên không làm mất các bản ghi lịch sử đã có.
ALTER TABLE `admin_actions`
  MODIFY `action_type` enum(
    'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE',
    'CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG',
    'HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT',
    'RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE',
    'RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT',
    'CREATE_ADMIN','UPDATE_ADMIN','UPDATE_ADMIN_PROFILE',
    'DISABLE_ADMIN','ENABLE_ADMIN','RESET_ADMIN_PASSWORD',
    'CHANGE_ADMIN_PASSWORD','ASSIGN_ADMIN_ROLE','REVOKE_ADMIN_ROLE',
    'CREATE_ADMIN_ROLE','UPDATE_ROLE_PERMISSIONS',
    'CREATE_MANAGED_SOCIAL_IDENTITY','DISABLE_MANAGED_SOCIAL_IDENTITY',
    'COLLABORATOR_POST_CREATED','COLLABORATOR_POST_UPDATED',
    'COLLABORATOR_POST_DELETED','MODERATION_SUGGESTION_CREATED',
    'MODERATION_SUGGESTION_ACCEPTED','MODERATION_SUGGESTION_REJECTED'
  ) NOT NULL,
  MODIFY `target_type` enum(
    'USER','POST','HASHTAG','REPORT','MODERATION_CASE',
    'PROFILE_REPORT','MODERATION_SUGGESTION'
  ) NOT NULL;

START TRANSACTION;

-- Upsert chỉ cập nhật đúng master data của module RBAC.
INSERT INTO `roles` (`code`, `display_name`, `description`, `reserved`) VALUES
  ('SUPER_ADMIN', 'Super Admin',
    'Quản trị viên cao nhất, có toàn bộ permission hiện tại và bổ sung sau này.', 0),
  ('USER_MANAGER', 'User Manager',
    'Xem Dashboard, quản lý người dùng và analytics người dùng.', 0),
  ('MODERATOR', 'Moderator',
    'Xem Dashboard, kiểm duyệt bài viết, hashtag và báo cáo.', 0),
  ('ADS_MANAGER', 'Ads Manager',
    'Role dự phòng, hiện chỉ được xem Dashboard tổng quan.', 1),
  ('COLLABORATOR', 'Collaborator',
    'Tạo nội dung, tương tác xã hội và gửi đề xuất kiểm duyệt bằng Managed Social Identity.', 1)
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `description` = VALUES(`description`),
  `reserved` = VALUES(`reserved`);

INSERT INTO `permissions` (`code`, `description`) VALUES
  ('DASHBOARD_BASIC_VIEW', 'Xem Dashboard quản trị cơ bản.'),
  ('USER_VIEW', 'Xem danh sách người dùng.'),
  ('USER_SEARCH', 'Tìm kiếm người dùng.'),
  ('USER_FILTER', 'Lọc danh sách người dùng.'),
  ('USER_DETAIL_VIEW', 'Xem chi tiết người dùng.'),
  ('USER_PROFILE_UPDATE', 'Sửa nội dung hồ sơ người dùng.'),
  ('USER_BLOCK', 'Khóa tài khoản USER.'),
  ('USER_UNBLOCK', 'Mở khóa tài khoản USER.'),
  ('USER_ANALYTICS_VIEW', 'Xem analytics hoạt động USER.'),
  ('POST_VIEW', 'Xem danh sách và chi tiết bài viết trong Admin.'),
  ('POST_HIDE', 'Ẩn bài viết PUBLISHED.'),
  ('POST_RESTORE', 'Khôi phục bài viết HIDDEN.'),
  ('HASHTAG_VIEW', 'Xem danh sách hashtag.'),
  ('HASHTAG_SEARCH', 'Tìm kiếm hashtag.'),
  ('HASHTAG_DELETE', 'Xóa hashtag và gỡ liên kết khỏi bài viết.'),
  ('REPORT_VIEW', 'Xem danh sách báo cáo hoặc Moderation Case.'),
  ('REPORT_DETAIL_VIEW', 'Xem chi tiết báo cáo hoặc Moderation Case.'),
  ('REPORT_RESOLVE_NO_VIOLATION', 'Kết luận không vi phạm.'),
  ('REPORT_RESOLVE_ACTION', 'Kết luận vi phạm và thực hiện hành động hiện có.'),
  ('ADMIN_VIEW', 'Xem danh sách Admin.'),
  ('ADMIN_DETAIL_VIEW', 'Xem chi tiết Admin.'),
  ('ADMIN_CREATE', 'Tạo tài khoản Admin.'),
  ('ADMIN_UPDATE', 'Cập nhật thông tin Admin.'),
  ('ADMIN_DISABLE', 'Vô hiệu hóa Admin.'),
  ('ADMIN_ENABLE', 'Mở khóa tài khoản Admin.'),
  ('ADMIN_PASSWORD_RESET', 'Cấp lại mật khẩu cho tài khoản Admin.'),
  ('ADMIN_ROLE_ASSIGN', 'Gán role cho Admin.'),
  ('ADMIN_ROLE_REVOKE', 'Thu hồi role khỏi Admin.'),
  ('COLLABORATOR_DASHBOARD_VIEW', 'Xem Dashboard nội dung của Collaborator.'),
  ('COLLABORATOR_POST_VIEW_OWN', 'Xem bài viết của Managed Social Identity hiện tại.'),
  ('COLLABORATOR_POST_CREATE', 'Tạo bài viết bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_UPDATE_OWN', 'Sửa bài của Managed Social Identity trong thời hạn cho phép.'),
  ('COLLABORATOR_POST_DELETE_OWN', 'Xóa mềm bài của Managed Social Identity.'),
  ('COLLABORATOR_POST_ANALYTICS_VIEW', 'Xem analytics bài thuộc Managed Social Identity.'),
  ('COLLABORATOR_EXPLORE_VIEW', 'Khám phá nội dung bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_LIKE', 'Like hoặc Unlike bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_COMMENT', 'Bình luận bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_REPLY', 'Trả lời bình luận bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_REPOST', 'Repost hoặc Unrepost bằng Managed Social Identity.'),
  ('COLLABORATOR_HASHTAG_VIEW', 'Xem hashtag trong khu vực Collaborator.'),
  ('COLLABORATOR_HASHTAG_SEARCH', 'Tìm kiếm hashtag trong khu vực Collaborator.'),
  ('COLLABORATOR_MODERATION_SUGGEST', 'Gửi đề xuất kiểm duyệt.'),
  ('COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN', 'Xem đề xuất do chính Collaborator tạo.'),
  ('MODERATION_SUGGESTION_VIEW', 'Xem danh sách đề xuất kiểm duyệt.'),
  ('MODERATION_SUGGESTION_DETAIL_VIEW', 'Xem chi tiết đề xuất kiểm duyệt.'),
  ('MODERATION_SUGGESTION_REVIEW', 'Chấp nhận hoặc từ chối đề xuất kiểm duyệt.')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- Xóa đúng hai permission trung gian đã bị loại khỏi contract; FK cascade
-- chỉ xóa các dòng nối tương ứng trong role_permissions.
DELETE FROM `permissions`
WHERE `code` IN (
  'ROLE_PERMISSION_VIEW',
  'ROLE_PERMISSION_UPDATE',
  'MODERATION_PROPOSAL_CREATE'
);

-- Chỉ đồng bộ ma trận của bốn role hệ thống giới hạn. Role tùy chỉnh và
-- admin_roles đang có được giữ nguyên.
DELETE rp
FROM `role_permissions` rp
JOIN `roles` r ON r.id = rp.role_id
WHERE r.code IN ('USER_MANAGER', 'MODERATOR', 'ADS_MANAGER', 'COLLABORATOR');

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code IN (
  'DASHBOARD_BASIC_VIEW','USER_VIEW','USER_SEARCH','USER_FILTER',
  'USER_DETAIL_VIEW','USER_PROFILE_UPDATE','USER_BLOCK','USER_UNBLOCK',
  'USER_ANALYTICS_VIEW'
)
WHERE r.code = 'USER_MANAGER';

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code IN (
  'DASHBOARD_BASIC_VIEW','POST_VIEW','POST_HIDE','POST_RESTORE',
  'HASHTAG_VIEW','HASHTAG_SEARCH','HASHTAG_DELETE','REPORT_VIEW',
  'REPORT_DETAIL_VIEW','REPORT_RESOLVE_NO_VIOLATION','REPORT_RESOLVE_ACTION',
  'MODERATION_SUGGESTION_VIEW','MODERATION_SUGGESTION_DETAIL_VIEW',
  'MODERATION_SUGGESTION_REVIEW'
)
WHERE r.code = 'MODERATOR';

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
JOIN `permissions` p ON p.code = 'DASHBOARD_BASIC_VIEW'
WHERE r.code = 'ADS_MANAGER';

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
CROSS JOIN `permissions` p
WHERE r.code = 'COLLABORATOR'
  AND p.code LIKE 'COLLABORATOR\_%';

-- SUPER_ADMIN nhận mọi permission hiện hành.
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
CROSS JOIN `permissions` p
WHERE r.code = 'SUPER_ADMIN';

-- Ba quyền có thể tiếp tục phân quyền chỉ thuộc SUPER_ADMIN.
DELETE rp
FROM `role_permissions` rp
JOIN `roles` r ON r.id = rp.role_id
JOIN `permissions` p ON p.id = rp.permission_id
WHERE r.code <> 'SUPER_ADMIN'
  AND p.code IN ('ADMIN_CREATE', 'ADMIN_ROLE_ASSIGN', 'ADMIN_ROLE_REVOKE');

COMMIT;

-- Kết quả kiểm tra chỉ đọc; không làm thay đổi dữ liệu nghiệp vụ.
SELECT
  DATABASE() AS migrated_database,
  (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'account_type') AS account_type_ready,
  (SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN (
        'roles', 'permissions', 'role_permissions', 'admin_roles',
        'admin_social_identities', 'moderation_suggestions'
      )) AS related_tables_ready;

SHOW COLUMNS FROM `admin_actions` LIKE 'action_type';
SHOW COLUMNS FROM `admin_actions` LIKE 'target_type';
