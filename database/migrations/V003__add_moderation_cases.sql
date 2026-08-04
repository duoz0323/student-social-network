-- Bổ sung Moderation Case và backfill toàn bộ Report legacy mà không làm mất bằng chứng.
-- Migration chạy trên MySQL 8 và chủ động dừng nếu trạng thái cũ không thể suy ra an toàn.

DELIMITER $$
CREATE PROCEDURE validate_report_backfill_for_moderation_cases()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM reports r
        JOIN posts p ON p.id = r.post_id
        GROUP BY r.post_id, p.status
        HAVING SUM(r.status = 'PENDING') = 0
           AND NOT (
               SUM(r.status = 'REJECTED') = COUNT(*)
               OR (SUM(r.status = 'RESOLVED') > 0 AND p.status = 'HIDDEN')
           )
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Không thể mapping an toàn Report legacy sang Moderation Case';
    END IF;
END$$
DELIMITER ;

CALL validate_report_backfill_for_moderation_cases();
DROP PROCEDURE validate_report_backfill_for_moderation_cases;

ALTER TABLE admin_actions
    MODIFY action_type ENUM(
        'BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','HIDE_POST','RESTORE_POST',
        'RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE'
    ) NOT NULL,
    MODIFY target_type ENUM('USER','POST','REPORT','MODERATION_CASE') NOT NULL;

CREATE TABLE moderation_cases (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    status ENUM('OPEN','RESOLVED_NO_VIOLATION','RESOLVED_ACTION_TAKEN') NOT NULL DEFAULT 'OPEN',
    report_count INT UNSIGNED NOT NULL DEFAULT 0,
    resolved_by BIGINT UNSIGNED DEFAULT NULL,
    resolution_note VARCHAR(1000) DEFAULT NULL,
    first_reported_at DATETIME(6) NOT NULL,
    latest_reported_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) DEFAULT NULL,
    open_post_key BIGINT UNSIGNED GENERATED ALWAYS AS (
        CASE WHEN status = 'OPEN' THEN post_id ELSE NULL END
    ) STORED,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_moderation_cases_open_post (open_post_key),
    KEY idx_moderation_cases_post (post_id),
    KEY idx_moderation_cases_status_latest (status, latest_reported_at DESC, id DESC),
    KEY idx_moderation_cases_resolved_by (resolved_by),
    CONSTRAINT fk_moderation_cases_post FOREIGN KEY (post_id)
        REFERENCES posts (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_moderation_cases_resolved_by FOREIGN KEY (resolved_by)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_moderation_cases_report_count CHECK (report_count >= 0),
    CONSTRAINT chk_moderation_cases_reported_time CHECK (latest_reported_at >= first_reported_at),
    CONSTRAINT chk_moderation_cases_resolution_state CHECK (
        (status = 'OPEN' AND resolved_by IS NULL AND resolved_at IS NULL)
        OR
        (status IN ('RESOLVED_NO_VIOLATION','RESOLVED_ACTION_TAKEN')
         AND resolved_by IS NOT NULL AND resolved_at IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE reports
    ADD COLUMN moderation_case_id BIGINT UNSIGNED NULL AFTER post_id,
    ADD KEY idx_reports_moderation_case_created (moderation_case_id, created_at DESC, id DESC),
    ADD KEY idx_reports_reporter_post (reporter_id, post_id),
    ADD CONSTRAINT fk_reports_moderation_case FOREIGN KEY (moderation_case_id)
        REFERENCES moderation_cases (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

INSERT INTO moderation_cases (
    post_id, status, report_count, resolved_by, resolution_note,
    first_reported_at, latest_reported_at, resolved_at, created_at, updated_at
)
SELECT grouped.post_id,
       grouped.case_status,
       grouped.report_count,
       CASE WHEN grouped.case_status = 'OPEN' THEN NULL ELSE (
           SELECT r2.resolved_by FROM reports r2
           WHERE r2.post_id = grouped.post_id AND r2.resolved_by IS NOT NULL
           ORDER BY r2.resolved_at DESC, r2.id DESC LIMIT 1
       ) END,
       CASE WHEN grouped.case_status = 'OPEN' THEN NULL ELSE (
           SELECT r3.resolution_note FROM reports r3
           WHERE r3.post_id = grouped.post_id AND r3.resolved_by IS NOT NULL
           ORDER BY r3.resolved_at DESC, r3.id DESC LIMIT 1
       ) END,
       grouped.first_reported_at,
       grouped.latest_reported_at,
       CASE WHEN grouped.case_status = 'OPEN' THEN NULL ELSE grouped.resolved_at END,
       grouped.first_reported_at,
       grouped.latest_reported_at
FROM (
    SELECT r.post_id,
           CASE
               WHEN SUM(r.status = 'PENDING') > 0 THEN 'OPEN'
               WHEN SUM(r.status = 'REJECTED') = COUNT(*) THEN 'RESOLVED_NO_VIOLATION'
               ELSE 'RESOLVED_ACTION_TAKEN'
           END AS case_status,
           COUNT(*) AS report_count,
           MIN(r.created_at) AS first_reported_at,
           MAX(r.created_at) AS latest_reported_at,
           MAX(r.resolved_at) AS resolved_at
    FROM reports r
    GROUP BY r.post_id
) grouped;

UPDATE reports r
JOIN moderation_cases mc ON mc.post_id = r.post_id
SET r.moderation_case_id = mc.id
WHERE r.moderation_case_id IS NULL;

-- Xác nhận backfill hoàn tất; migration dừng nếu còn Report mồ côi.
DELIMITER $$
CREATE PROCEDURE validate_moderation_case_backfill()
BEGIN
    IF EXISTS (SELECT 1 FROM reports WHERE moderation_case_id IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Backfill failed: a report is not linked to a moderation case';
    END IF;
END$$
DELIMITER ;

CALL validate_moderation_case_backfill();
DROP PROCEDURE validate_moderation_case_backfill;
