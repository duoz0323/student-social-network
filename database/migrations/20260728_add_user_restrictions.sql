-- Migration bổ sung Restrict một chiều, không thay đổi dữ liệu tương tác hiện có.
CREATE TABLE IF NOT EXISTS `user_restrictions` (
  `restrictor_id` bigint unsigned NOT NULL,
  `restricted_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`restrictor_id`, `restricted_id`),
  KEY `idx_user_restrictions_restricted_restrictor` (`restricted_id`, `restrictor_id`),
  CONSTRAINT `fk_user_restrictions_restrictor`
    FOREIGN KEY (`restrictor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_restrictions_restricted`
    FOREIGN KEY (`restricted_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_restrictions_not_self`
    CHECK (`restrictor_id` <> `restricted_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
