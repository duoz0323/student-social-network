-- Gom nhiều lượt báo cáo của cùng một trang cá nhân vào một vụ việc quản trị duy nhất.
CREATE TABLE profile_report_cases (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reported_user_id BIGINT UNSIGNED NOT NULL,
    status ENUM('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    resolved_by BIGINT UNSIGNED DEFAULT NULL,
    resolved_at DATETIME(6) DEFAULT NULL,
    resolution_note VARCHAR(1000) DEFAULT NULL,
    reported_display_name_snapshot VARCHAR(100) NOT NULL,
    reported_avatar_url_snapshot VARCHAR(1000) DEFAULT NULL,
    reported_bio_snapshot VARCHAR(500) DEFAULT NULL,
    reported_date_of_birth_snapshot DATE DEFAULT NULL,
    report_count INT UNSIGNED NOT NULL DEFAULT 0,
    latest_reported_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_profile_report_cases_target (reported_user_id),
    KEY idx_profile_report_cases_status_latest (status, latest_reported_at, id),
    KEY idx_profile_report_cases_resolved_by (resolved_by),
    CONSTRAINT fk_profile_report_cases_target FOREIGN KEY (reported_user_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_profile_report_cases_resolved_by FOREIGN KEY (resolved_by)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_profile_report_cases_resolution_state CHECK (
        (status = 'PENDING' AND resolved_by IS NULL AND resolved_at IS NULL)
        OR
        (status IN ('RESOLVED','REJECTED') AND resolved_by IS NOT NULL AND resolved_at IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dòng mới nhất đang chờ được ưu tiên làm trạng thái case; nếu không có thì dùng kết luận gần nhất.
INSERT INTO profile_report_cases (
    reported_user_id, status, resolved_by, resolved_at, resolution_note,
    reported_display_name_snapshot, reported_avatar_url_snapshot,
    reported_bio_snapshot, reported_date_of_birth_snapshot,
    report_count, latest_reported_at, created_at
)
SELECT ranked.reported_user_id,
       ranked.status,
       ranked.resolved_by,
       ranked.resolved_at,
       ranked.resolution_note,
       ranked.reported_display_name_snapshot,
       ranked.reported_avatar_url_snapshot,
       ranked.reported_bio_snapshot,
       ranked.reported_date_of_birth_snapshot,
       totals.report_count,
       totals.latest_reported_at,
       totals.first_reported_at
FROM (
    SELECT pr.*,
           ROW_NUMBER() OVER (
               PARTITION BY pr.reported_user_id
               ORDER BY (pr.status = 'PENDING') DESC, pr.created_at DESC, pr.id DESC
           ) AS row_number_in_case
    FROM profile_reports pr
) ranked
JOIN (
    SELECT reported_user_id,
           COUNT(*) AS report_count,
           MIN(created_at) AS first_reported_at,
           MAX(created_at) AS latest_reported_at
    FROM profile_reports
    GROUP BY reported_user_id
) totals ON totals.reported_user_id = ranked.reported_user_id
WHERE ranked.row_number_in_case = 1;

ALTER TABLE profile_reports
    ADD COLUMN case_id BIGINT UNSIGNED NULL AFTER id,
    ADD KEY idx_profile_reports_case_created (case_id, created_at, id);

UPDATE profile_reports pr
JOIN profile_report_cases prc ON prc.reported_user_id = pr.reported_user_id
SET pr.case_id = prc.id;

ALTER TABLE profile_reports
    MODIFY COLUMN case_id BIGINT UNSIGNED NOT NULL,
    ADD CONSTRAINT fk_profile_reports_case FOREIGN KEY (case_id)
        REFERENCES profile_report_cases (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Admin Action cũ đang trỏ tới report đơn lẻ; chuyển sang case để lịch sử vẫn mở đúng đối tượng.
UPDATE admin_actions aa
JOIN profile_reports pr ON aa.target_type = 'PROFILE_REPORT' AND aa.target_id = pr.id
SET aa.target_id = pr.case_id;
