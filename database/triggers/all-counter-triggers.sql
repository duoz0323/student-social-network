-- ============================================================================
-- Student Social Network - Toàn bộ trigger cập nhật counter hiện hành
-- MySQL 8.x
--
-- Cách dùng khi đã chọn đúng database:
--   mysql -u <username> -p <database_name> < database/triggers/all-counter-triggers.sql
--
-- File không hard-code tên database. Các bảng và cột liên quan phải tồn tại
-- trước khi import. DROP TRIGGER giúp file có thể chạy lại an toàn.
-- ============================================================================

SET NAMES utf8mb4;

DROP TRIGGER IF EXISTS `trg_post_likes_after_insert`;
DROP TRIGGER IF EXISTS `trg_post_likes_after_delete`;
DROP TRIGGER IF EXISTS `trg_post_reposts_after_insert`;
DROP TRIGGER IF EXISTS `trg_post_reposts_after_delete`;
DROP TRIGGER IF EXISTS `trg_comments_after_insert`;
DROP TRIGGER IF EXISTS `trg_comments_after_update`;
DROP TRIGGER IF EXISTS `trg_post_hashtags_after_insert`;
DROP TRIGGER IF EXISTS `trg_post_hashtags_after_delete`;

DELIMITER $$

-- Like mới làm tăng bộ đếm Like của bài viết tương ứng.
CREATE TRIGGER `trg_post_likes_after_insert`
AFTER INSERT ON `post_likes`
FOR EACH ROW
BEGIN
    UPDATE `posts`
    SET `like_count` = `like_count` + 1
    WHERE `id` = NEW.`post_id`;
END$$

-- Unlike làm giảm counter nhưng không cho phép giá trị âm.
CREATE TRIGGER `trg_post_likes_after_delete`
AFTER DELETE ON `post_likes`
FOR EACH ROW
BEGIN
    UPDATE `posts`
    SET `like_count` = GREATEST(`like_count` - 1, 0)
    WHERE `id` = OLD.`post_id`;
END$$

-- Repost mới làm tăng bộ đếm Repost của bài gốc.
CREATE TRIGGER `trg_post_reposts_after_insert`
AFTER INSERT ON `post_reposts`
FOR EACH ROW
BEGIN
    UPDATE `posts`
    SET `repost_count` = `repost_count` + 1
    WHERE `id` = NEW.`post_id`;
END$$

-- Bỏ Repost làm giảm counter nhưng không cho phép giá trị âm.
CREATE TRIGGER `trg_post_reposts_after_delete`
AFTER DELETE ON `post_reposts`
FOR EACH ROW
BEGIN
    UPDATE `posts`
    SET `repost_count` = GREATEST(`repost_count` - 1, 0)
    WHERE `id` = OLD.`post_id`;
END$$

-- Chỉ bình luận được tạo ở trạng thái PUBLISHED mới được tính vào counter.
CREATE TRIGGER `trg_comments_after_insert`
AFTER INSERT ON `comments`
FOR EACH ROW
BEGIN
    IF NEW.`status` = 'PUBLISHED' THEN
        UPDATE `posts`
        SET `comment_count` = `comment_count` + 1
        WHERE `id` = NEW.`post_id`;
    END IF;
END$$

-- Đồng bộ counter khi bình luận chuyển giữa PUBLISHED và DELETED.
CREATE TRIGGER `trg_comments_after_update`
AFTER UPDATE ON `comments`
FOR EACH ROW
BEGIN
    IF OLD.`status` = 'PUBLISHED' AND NEW.`status` = 'DELETED' THEN
        UPDATE `posts`
        SET `comment_count` = GREATEST(`comment_count` - 1, 0)
        WHERE `id` = NEW.`post_id`;
    ELSEIF OLD.`status` = 'DELETED' AND NEW.`status` = 'PUBLISHED' THEN
        UPDATE `posts`
        SET `comment_count` = `comment_count` + 1
        WHERE `id` = NEW.`post_id`;
    END IF;
END$$

-- Gắn hashtag vào Post làm tăng số Post đang tham chiếu hashtag đó.
CREATE TRIGGER `trg_post_hashtags_after_insert`
AFTER INSERT ON `post_hashtags`
FOR EACH ROW
BEGIN
    UPDATE `hashtags`
    SET `post_count` = `post_count` + 1
    WHERE `id` = NEW.`hashtag_id`;
END$$

-- Gỡ hashtag khỏi Post làm giảm counter nhưng không cho phép giá trị âm.
CREATE TRIGGER `trg_post_hashtags_after_delete`
AFTER DELETE ON `post_hashtags`
FOR EACH ROW
BEGIN
    UPDATE `hashtags`
    SET `post_count` = GREATEST(`post_count` - 1, 0)
    WHERE `id` = OLD.`hashtag_id`;
END$$

DELIMITER ;

-- Hiển thị các trigger vừa import để người vận hành kiểm tra nhanh.
SELECT `TRIGGER_NAME`, `EVENT_MANIPULATION`, `EVENT_OBJECT_TABLE`, `ACTION_TIMING`
FROM `information_schema`.`TRIGGERS`
WHERE `TRIGGER_SCHEMA` = DATABASE()
  AND `TRIGGER_NAME` IN (
      'trg_post_likes_after_insert',
      'trg_post_likes_after_delete',
      'trg_post_reposts_after_insert',
      'trg_post_reposts_after_delete',
      'trg_comments_after_insert',
      'trg_comments_after_update',
      'trg_post_hashtags_after_insert',
      'trg_post_hashtags_after_delete'
  )
ORDER BY `TRIGGER_NAME`;
