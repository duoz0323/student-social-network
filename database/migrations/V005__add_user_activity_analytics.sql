-- Bổ sung mốc hoạt động nhanh trên tài khoản và lịch sử hoạt động theo ngày cho analytics.
ALTER TABLE users
    ADD COLUMN first_active_at DATETIME(6) NULL AFTER blocked_reason,
    ADD COLUMN last_active_at DATETIME(6) NULL AFTER first_active_at;

CREATE TABLE user_daily_activities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    activity_date DATE NOT NULL,
    first_active_at DATETIME(6) NOT NULL,
    last_active_at DATETIME(6) NOT NULL,
    activity_count INT UNSIGNED NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_daily_activities_user_date (user_id, activity_date),
    KEY idx_user_daily_activities_date_user (activity_date, user_id),
    CONSTRAINT fk_user_daily_activities_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT chk_user_daily_activities_count CHECK (activity_count > 0),
    CONSTRAINT chk_user_daily_activities_time CHECK (first_active_at <= last_active_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
