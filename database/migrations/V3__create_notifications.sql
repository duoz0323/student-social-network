-- Migration thủ công: thêm thông báo nội bộ giai đoạn 1 cho Follow, Like và bình luận gốc.
-- Không tự chạy file này; cần backup và áp dụng trước khi khởi động Backend vì Hibernate dùng ddl-auto=validate.

CREATE TABLE notifications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT UNSIGNED NOT NULL,
    actor_id BIGINT UNSIGNED NULL,
    type ENUM('FOLLOW', 'POST_LIKE', 'POST_COMMENT') NOT NULL,
    post_id BIGINT UNSIGNED NULL,
    comment_id BIGINT UNSIGNED NULL,
    read_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE SET NULL,
    CONSTRAINT fk_notifications_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_comment FOREIGN KEY (comment_id)
        REFERENCES comments (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT chk_notifications_not_self CHECK (
        actor_id IS NULL OR actor_id <> recipient_id
    ),
    CONSTRAINT chk_notifications_target_by_type CHECK (
        (type = 'FOLLOW' AND post_id IS NULL AND comment_id IS NULL)
        OR
        (type = 'POST_LIKE' AND post_id IS NOT NULL AND comment_id IS NULL)
        OR
        (type = 'POST_COMMENT' AND post_id IS NOT NULL AND comment_id IS NOT NULL)
    )
) ENGINE=InnoDB;

-- Phục vụ danh sách mới nhất theo cặp ổn định (created_at, id).
CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_id, deleted_at, created_at DESC, id DESC);

-- Phục vụ đếm thông báo chưa đọc của một người dùng.
CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_id, deleted_at, read_at);

-- Phục vụ xóa thông báo Follow khi Unfollow.
CREATE INDEX idx_notifications_follow_source
    ON notifications (actor_id, recipient_id, type);

-- Phục vụ xóa thông báo Like theo actor và bài viết khi Unlike.
CREATE INDEX idx_notifications_post_source
    ON notifications (actor_id, post_id, type);

-- comment_id đã đủ chọn lọc để xóa thông báo khi bình luận bị xóa mềm.
CREATE INDEX idx_notifications_comment_source
    ON notifications (comment_id, type);
