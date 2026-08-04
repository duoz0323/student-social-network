-- Migration thủ công bổ sung Location dùng chung cho Post.
-- Dự án chưa tích hợp Flyway/Liquibase; file này không tự chạy khi Backend khởi động.

CREATE TABLE `locations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `google_place_id` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `formatted_address` varchar(500) DEFAULT NULL,
  `latitude` decimal(10,7) NOT NULL,
  `longitude` decimal(10,7) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_locations_google_place_id` (`google_place_id`),
  CONSTRAINT `chk_locations_latitude` CHECK ((`latitude` between -(90) and 90)),
  CONSTRAINT `chk_locations_longitude` CHECK ((`longitude` between -(180) and 180))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `posts`
  ADD COLUMN `location_id` bigint unsigned DEFAULT NULL;

CREATE INDEX `idx_posts_location_id`
  ON `posts` (`location_id`);

ALTER TABLE `posts`
  ADD CONSTRAINT `fk_posts_location`
    FOREIGN KEY (`location_id`)
    REFERENCES `locations` (`id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT;
