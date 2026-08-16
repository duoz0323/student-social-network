-- Migration thủ công cho Post Sharing V1 trên MySQL 8.x.
-- Có thể chạy lại sau lần import dở dang; migration đã hoàn tất thì không làm gì thêm.
-- Backend không tự chạy file này vì ddl-auto đang ở chế độ validate.

DELIMITER $$

DROP PROCEDURE IF EXISTS `migrate_post_share_v1`$$
CREATE PROCEDURE `migrate_post_share_v1`()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'messages'
      AND column_name = 'shared_post_id'
  ) THEN
    -- Lần import cũ có thể đã drop CHECK trước khi ALTER kế tiếp thất bại.
    IF EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE()
        AND table_name = 'messages'
        AND constraint_name = 'chk_messages_payload_shape'
        AND constraint_type = 'CHECK'
    ) THEN
      ALTER TABLE `messages` DROP CHECK `chk_messages_payload_shape`;
    END IF;

    ALTER TABLE `messages`
      MODIFY COLUMN `type` enum('TEXT','IMAGE','POST_SHARE') NOT NULL,
      ADD COLUMN `shared_post_id` bigint unsigned DEFAULT NULL AFTER `content`,
      ADD KEY `idx_messages_shared_post` (`shared_post_id`,`id`),
      ADD CONSTRAINT `fk_messages_shared_post`
        FOREIGN KEY (`shared_post_id`) REFERENCES `posts` (`id`)
        ON DELETE SET NULL ON UPDATE RESTRICT,
      ADD CONSTRAINT `chk_messages_payload_shape`
        CHECK (((`type` = 'TEXT'
                  AND `content` IS NOT NULL
                  AND char_length(trim(`content`)) > 0)
                OR `type` IN ('IMAGE','POST_SHARE')));
  END IF;
END$$

CALL `migrate_post_share_v1`()$$
DROP PROCEDURE `migrate_post_share_v1`$$

DELIMITER ;
