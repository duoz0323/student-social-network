-- Tạo quan hệ Repost thuần tham chiếu và counter đọc nhanh trên bài gốc.
ALTER TABLE posts
    ADD COLUMN repost_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER comment_count;

CREATE TABLE post_reposts (
    user_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, post_id),
    KEY idx_post_reposts_user_created (user_id, created_at DESC, post_id DESC),
    KEY idx_post_reposts_post_user (post_id, user_id),
    CONSTRAINT fk_post_reposts_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_post_reposts_post FOREIGN KEY (post_id)
        REFERENCES posts (id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DELIMITER ;;
CREATE TRIGGER trg_post_reposts_after_insert
AFTER INSERT ON post_reposts
FOR EACH ROW
BEGIN
    UPDATE posts SET repost_count = repost_count + 1 WHERE id = NEW.post_id;
END;;

CREATE TRIGGER trg_post_reposts_after_delete
AFTER DELETE ON post_reposts
FOR EACH ROW
BEGIN
    UPDATE posts SET repost_count = GREATEST(repost_count - 1, 0) WHERE id = OLD.post_id;
END;;
DELIMITER ;

-- Mở rộng enum Notification để lưu sự kiện Repost trong cùng transaction nghiệp vụ.
ALTER TABLE notifications
    MODIFY COLUMN type ENUM(
        'FOLLOW',
        'POST_LIKE',
        'POST_COMMENT',
        'COMMENT_REPLY',
        'POST_REPOST',
        'REPORT_RESOLVED',
        'REPORT_REJECTED',
        'POST_HIDDEN_BY_ADMIN',
        'POST_RESTORED_BY_ADMIN',
        'ACCOUNT_BLOCKED',
        'ACCOUNT_UNBLOCKED'
    ) NOT NULL;
