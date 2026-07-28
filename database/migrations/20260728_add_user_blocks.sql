-- Migration bổ sung quan hệ User Block cho schema đang có dữ liệu.
-- Script chỉ tạo bảng mới, không xóa hoặc sửa dữ liệu hiện hữu.
CREATE TABLE IF NOT EXISTS `user_blocks` (
  `blocker_id` bigint unsigned NOT NULL,
  `blocked_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`blocker_id`, `blocked_id`),
  KEY `idx_user_blocks_blocked_blocker` (`blocked_id`, `blocker_id`),
  CONSTRAINT `fk_user_blocks_blocker`
    FOREIGN KEY (`blocker_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_blocks_blocked`
    FOREIGN KEY (`blocked_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_blocks_not_self`
    CHECK (`blocker_id` <> `blocked_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
