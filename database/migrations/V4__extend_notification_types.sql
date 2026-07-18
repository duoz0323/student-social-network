-- Migration thủ công: mở rộng thông báo cho reply và kết quả quản trị.
-- Cần backup và áp dụng sau V3 trước khi khởi động Backend.

ALTER TABLE notifications
    DROP CHECK chk_notifications_target_by_type;

ALTER TABLE notifications
    MODIFY COLUMN type ENUM(
        'FOLLOW',
        'POST_LIKE',
        'POST_COMMENT',
        'COMMENT_REPLY',
        'REPORT_RESOLVED',
        'REPORT_REJECTED',
        'POST_HIDDEN_BY_ADMIN',
        'POST_RESTORED_BY_ADMIN',
        'ACCOUNT_BLOCKED',
        'ACCOUNT_UNBLOCKED'
    ) NOT NULL,
    ADD COLUMN report_id BIGINT UNSIGNED NULL AFTER comment_id,
    ADD CONSTRAINT fk_notifications_report FOREIGN KEY (report_id)
        REFERENCES reports (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    ADD CONSTRAINT chk_notifications_target_by_type CHECK (
        (type IN ('FOLLOW', 'ACCOUNT_BLOCKED', 'ACCOUNT_UNBLOCKED')
            AND post_id IS NULL AND comment_id IS NULL AND report_id IS NULL)
        OR
        (type IN ('POST_LIKE', 'POST_HIDDEN_BY_ADMIN', 'POST_RESTORED_BY_ADMIN')
            AND post_id IS NOT NULL AND comment_id IS NULL AND report_id IS NULL)
        OR
        (type IN ('POST_COMMENT', 'COMMENT_REPLY')
            AND post_id IS NOT NULL AND comment_id IS NOT NULL AND report_id IS NULL)
        OR
        (type IN ('REPORT_RESOLVED', 'REPORT_REJECTED')
            AND post_id IS NULL AND comment_id IS NULL AND report_id IS NOT NULL)
    );

CREATE INDEX idx_notifications_report_source
    ON notifications (report_id, type);
