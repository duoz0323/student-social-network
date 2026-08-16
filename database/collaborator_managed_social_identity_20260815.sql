-- Bổ sung Collaborator bằng migration additive; không tạo lại các bảng Social hiện có.
ALTER TABLE `users`
  ADD COLUMN `account_type` varchar(16) NOT NULL DEFAULT 'NORMAL' AFTER `status`,
  ADD CONSTRAINT `chk_users_account_type` CHECK (`account_type` IN ('NORMAL','MANAGED')),
  ADD CONSTRAINT `chk_users_managed_auth` CHECK (
    `account_type` = 'NORMAL' OR
    (`role` = 'USER' AND `email` IS NULL AND `email_verified_at` IS NULL AND `password_hash` IS NULL)
  );

CREATE TABLE `admin_social_identities` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` bigint unsigned NOT NULL,
  `social_user_id` bigint unsigned NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_by` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_admin_social_identities_admin` (`admin_id`),
  UNIQUE KEY `uq_admin_social_identities_social_user` (`social_user_id`),
  KEY `idx_admin_social_identities_created_by` (`created_by`),
  CONSTRAINT `fk_admin_social_identities_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_social_identities_social_user` FOREIGN KEY (`social_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_social_identities_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_admin_social_identities_status` CHECK (`status` IN ('ACTIVE','DISABLED')),
  CONSTRAINT `chk_admin_social_identities_distinct_users` CHECK (`admin_id` <> `social_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `moderation_suggestions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `suggested_by_admin_id` bigint unsigned NOT NULL,
  `reason` varchar(32) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `reviewed_by_admin_id` bigint unsigned DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `pending_admin_id` bigint unsigned GENERATED ALWAYS AS (IF(`status` = 'PENDING', `suggested_by_admin_id`, NULL)) STORED,
  `pending_post_id` bigint unsigned GENERATED ALWAYS AS (IF(`status` = 'PENDING', `post_id`, NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_moderation_suggestions_pending` (`pending_admin_id`,`pending_post_id`),
  KEY `idx_moderation_suggestions_status_created` (`status`,`created_at` DESC,`id` DESC),
  KEY `idx_moderation_suggestions_post` (`post_id`,`created_at` DESC),
  KEY `idx_moderation_suggestions_reviewer` (`reviewed_by_admin_id`),
  CONSTRAINT `fk_moderation_suggestions_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_moderation_suggestions_suggester` FOREIGN KEY (`suggested_by_admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_moderation_suggestions_reviewer` FOREIGN KEY (`reviewed_by_admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_moderation_suggestions_reason` CHECK (`reason` IN ('SPAM','INAPPROPRIATE_CONTENT','SCAM_SUSPECTED','HARASSMENT','HARMFUL_CONTENT','OTHER')),
  CONSTRAINT `chk_moderation_suggestions_status` CHECK (`status` IN ('PENDING','ACCEPTED','REJECTED')),
  CONSTRAINT `chk_moderation_suggestions_review_state` CHECK (
    (`status` = 'PENDING' AND `reviewed_by_admin_id` IS NULL AND `reviewed_at` IS NULL) OR
    (`status` IN ('ACCEPTED','REJECTED') AND `reviewed_by_admin_id` IS NOT NULL AND `reviewed_at` IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `admin_actions`
  MODIFY `action_type` enum(
    'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG',
    'HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE',
    'RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT','CREATE_ADMIN','UPDATE_ADMIN','UPDATE_ADMIN_PROFILE',
    'DISABLE_ADMIN','ENABLE_ADMIN','RESET_ADMIN_PASSWORD','CHANGE_ADMIN_PASSWORD','ASSIGN_ADMIN_ROLE',
    'REVOKE_ADMIN_ROLE','CREATE_ADMIN_ROLE','UPDATE_ROLE_PERMISSIONS','CREATE_MANAGED_SOCIAL_IDENTITY',
    'DISABLE_MANAGED_SOCIAL_IDENTITY','COLLABORATOR_POST_CREATED','COLLABORATOR_POST_UPDATED',
    'COLLABORATOR_POST_DELETED','MODERATION_SUGGESTION_CREATED','MODERATION_SUGGESTION_ACCEPTED',
    'MODERATION_SUGGESTION_REJECTED'
  ) NOT NULL,
  MODIFY `target_type` enum('USER','POST','HASHTAG','REPORT','MODERATION_CASE','PROFILE_REPORT','MODERATION_SUGGESTION') NOT NULL;

START TRANSACTION;

INSERT INTO `permissions` (`code`,`description`) VALUES
  ('COLLABORATOR_DASHBOARD_VIEW','Xem Dashboard nội dung của Collaborator.'),
  ('COLLABORATOR_POST_VIEW_OWN','Xem bài viết của Managed Social Identity hiện tại.'),
  ('COLLABORATOR_POST_CREATE','Tạo bài viết bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_UPDATE_OWN','Sửa bài của Managed Social Identity trong thời hạn cho phép.'),
  ('COLLABORATOR_POST_DELETE_OWN','Xóa mềm bài của Managed Social Identity.'),
  ('COLLABORATOR_POST_ANALYTICS_VIEW','Xem analytics bài thuộc Managed Social Identity.'),
  ('COLLABORATOR_EXPLORE_VIEW','Khám phá nội dung bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_LIKE','Like hoặc Unlike bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_COMMENT','Bình luận bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_REPLY','Trả lời bình luận bằng Managed Social Identity.'),
  ('COLLABORATOR_POST_REPOST','Repost hoặc Unrepost bằng Managed Social Identity.'),
  ('COLLABORATOR_HASHTAG_VIEW','Xem hashtag trong khu vực Collaborator.'),
  ('COLLABORATOR_HASHTAG_SEARCH','Tìm kiếm hashtag trong khu vực Collaborator.'),
  ('COLLABORATOR_MODERATION_SUGGEST','Gửi đề xuất kiểm duyệt.'),
  ('COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN','Xem đề xuất kiểm duyệt do chính mình tạo.'),
  ('MODERATION_SUGGESTION_VIEW','Xem danh sách đề xuất kiểm duyệt.'),
  ('MODERATION_SUGGESTION_DETAIL_VIEW','Xem chi tiết đề xuất kiểm duyệt.'),
  ('MODERATION_SUGGESTION_REVIEW','Chấp nhận hoặc từ chối đề xuất kiểm duyệt.')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

UPDATE `roles` SET
  `description` = 'Tạo nội dung, tương tác xã hội và gửi đề xuất kiểm duyệt bằng Managed Social Identity.',
  `reserved` = 1
WHERE `code` = 'COLLABORATOR';

DELETE rp FROM `role_permissions` rp JOIN `roles` r ON r.id = rp.role_id WHERE r.code = 'COLLABORATOR';
INSERT IGNORE INTO `role_permissions` (`role_id`,`permission_id`)
SELECT r.id,p.id FROM `roles` r CROSS JOIN `permissions` p
WHERE r.code = 'COLLABORATOR' AND p.code LIKE 'COLLABORATOR\_%';

INSERT IGNORE INTO `role_permissions` (`role_id`,`permission_id`)
SELECT r.id,p.id FROM `roles` r CROSS JOIN `permissions` p
WHERE r.code = 'MODERATOR' AND p.code IN ('MODERATION_SUGGESTION_VIEW','MODERATION_SUGGESTION_DETAIL_VIEW','MODERATION_SUGGESTION_REVIEW');

INSERT IGNORE INTO `role_permissions` (`role_id`,`permission_id`)
SELECT r.id,p.id FROM `roles` r CROSS JOIN `permissions` p WHERE r.code = 'SUPER_ADMIN';

COMMIT;
