-- Migration thủ công: mở rộng post_media để hỗ trợ một video MP4/WebM tối đa 180 giây.
-- Không tự chạy file này; cần backup và kiểm tra dữ liệu trước khi áp dụng ở từng môi trường.

ALTER TABLE post_media
    ADD COLUMN media_type ENUM('IMAGE', 'VIDEO') NOT NULL DEFAULT 'IMAGE' AFTER storage_public_id,
    MODIFY COLUMN mime_type ENUM(
        'image/jpeg', 'image/png', 'image/webp', 'video/mp4', 'video/webm'
    ) NOT NULL,
    ADD COLUMN duration_seconds SMALLINT UNSIGNED NULL AFTER height_px,
    ADD COLUMN thumbnail_url VARCHAR(1000) NULL AFTER duration_seconds;

ALTER TABLE post_media
    ALTER COLUMN media_type DROP DEFAULT,
    ADD CONSTRAINT chk_post_media_duration CHECK (
        (media_type = 'IMAGE' AND duration_seconds IS NULL)
        OR (media_type = 'VIDEO' AND duration_seconds BETWEEN 1 AND 180)
    );
