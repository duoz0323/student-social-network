-- Báo cáo trang cá nhân được tách khỏi Report bài viết để giữ nguyên Moderation Case hiện tại.
ALTER TABLE admin_actions
    MODIFY COLUMN action_type ENUM(
        'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','HIDE_POST','RESTORE_POST',
        'RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE',
        'RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT'
    ) NOT NULL,
    MODIFY COLUMN target_type ENUM('USER','POST','REPORT','MODERATION_CASE','PROFILE_REPORT') NOT NULL;

CREATE TABLE profile_reports (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT UNSIGNED NOT NULL,
    reported_user_id BIGINT UNSIGNED NOT NULL,
    reason ENUM(
        'PROHIBITED_CONTENT','IMPERSONATION','UNDER_MINIMUM_AGE',
        'SCAM_OR_FRAUD','FALSE_INFORMATION','VIOLENCE_OR_DANGEROUS_ORGANIZATION'
    ) NOT NULL,
    status ENUM('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    resolved_by BIGINT UNSIGNED DEFAULT NULL,
    resolved_at DATETIME(6) DEFAULT NULL,
    resolution_note VARCHAR(1000) DEFAULT NULL,
    reporter_display_name_snapshot VARCHAR(100) NOT NULL,
    reported_display_name_snapshot VARCHAR(100) NOT NULL,
    reported_avatar_url_snapshot VARCHAR(1000) DEFAULT NULL,
    reported_bio_snapshot VARCHAR(500) DEFAULT NULL,
    reported_date_of_birth_snapshot DATE DEFAULT NULL,
    pending_report_key VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN status = 'PENDING' THEN CONCAT(reporter_id, ':', reported_user_id) ELSE NULL END
    ) STORED,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_profile_reports_pending_key (pending_report_key),
    KEY idx_profile_reports_status_created (status, created_at, id),
    KEY idx_profile_reports_target_created (reported_user_id, created_at DESC, id DESC),
    KEY idx_profile_reports_reporter_created (reporter_id, created_at DESC, id DESC),
    KEY idx_profile_reports_resolved_by (resolved_by),
    CONSTRAINT fk_profile_reports_reporter FOREIGN KEY (reporter_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_profile_reports_target FOREIGN KEY (reported_user_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_profile_reports_resolved_by FOREIGN KEY (resolved_by)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_profile_reports_not_self CHECK (reporter_id <> reported_user_id),
    CONSTRAINT chk_profile_reports_resolution_state CHECK (
        (status = 'PENDING' AND resolved_by IS NULL AND resolved_at IS NULL)
        OR
        (status IN ('RESOLVED','REJECTED') AND resolved_by IS NOT NULL AND resolved_at IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
