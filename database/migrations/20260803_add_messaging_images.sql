-- Mở rộng Messaging với ảnh; migration riêng, không sửa migration text-only đã chạy.
ALTER TABLE messages
  DROP CHECK chk_messages_content_not_blank,
  MODIFY COLUMN type enum('TEXT','IMAGE') NOT NULL,
  MODIFY COLUMN content varchar(2000) NULL,
  ADD COLUMN payload_fingerprint char(64) CHARACTER SET ascii COLLATE ascii_bin NULL AFTER content;

-- Dữ liệu text cũ vẫn hợp lệ; fingerprint legacy chỉ phục vụ NOT NULL, replay text tiếp tục so content.
UPDATE messages
SET payload_fingerprint = SHA2(CONCAT('TEXT', CHAR(10), conversation_id, CHAR(10), content), 256)
WHERE payload_fingerprint IS NULL;

ALTER TABLE messages
  MODIFY COLUMN payload_fingerprint char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  ADD CONSTRAINT chk_messages_payload_shape CHECK (
    (type = 'TEXT' AND content IS NOT NULL AND CHAR_LENGTH(TRIM(content)) > 0)
    OR type = 'IMAGE'
  );

CREATE TABLE message_attachments (
  id bigint unsigned NOT NULL AUTO_INCREMENT,
  message_id bigint unsigned NOT NULL,
  media_type enum('IMAGE') NOT NULL,
  storage_provider enum('CLOUDINARY') NOT NULL,
  storage_public_id varchar(255) NOT NULL,
  mime_type varchar(64) NOT NULL,
  file_size_bytes bigint unsigned NOT NULL,
  width int unsigned NOT NULL,
  height int unsigned NOT NULL,
  display_order tinyint unsigned NOT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_message_attachments_message_order (message_id, display_order),
  UNIQUE KEY uq_message_attachments_storage_asset (storage_provider, storage_public_id),
  KEY idx_message_attachments_message (message_id, id),
  CONSTRAINT fk_message_attachments_message FOREIGN KEY (message_id)
    REFERENCES messages(id) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_message_attachments_size CHECK (file_size_bytes > 0),
  CONSTRAINT chk_message_attachments_dimensions CHECK (width > 0 AND height > 0),
  CONSTRAINT chk_message_attachments_order CHECK (display_order BETWEEN 0 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE media_cleanup_tasks (
  id bigint unsigned NOT NULL AUTO_INCREMENT,
  storage_provider varchar(32) NOT NULL,
  storage_public_id varchar(255) NOT NULL,
  resource_type varchar(32) NOT NULL,
  reason varchar(64) NOT NULL,
  status enum('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL,
  attempt_count int unsigned NOT NULL DEFAULT 0,
  next_retry_at datetime(6) NOT NULL,
  last_error varchar(500) NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_media_cleanup_due (status, next_retry_at, id),
  KEY idx_media_cleanup_asset (storage_provider, storage_public_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
