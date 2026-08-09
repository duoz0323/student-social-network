-- =============================================================================
-- SEED 200 USER PHUC VU ANALYTICS QUAY LAI (LOCAL/TEST ONLY)
-- =============================================================================
-- Pham vi: tao 200 USER ACTIVE, ho so hoan tat va lich su hoat dong UTC tu
-- 2026-04-01 den toi da 2026-08-04. Ngay cuoi cung nam truoc ngay hien tai
-- 2026-08-05, vi vay khong the sinh du lieu tuong lai.
--
-- Seed dung email namespace analytics.seed.001@example.test den .200 de tach
-- biet hoan toan voi tai khoan that. Hash mat khau de NULL: day la tai khoan
-- tong hop cho analytics, khong dung de dang nhap local.
--
-- Script co the chay lai: chi cap nhat cac ban ghi trong namespace nay ve cung
-- mot du lieu test xac dinh. Khong xoa va khong cap nhat bat ky user khac.
-- =============================================================================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

SET @analytics_seed_created_at = '2026-03-01 08:00:00.000000';
SET @analytics_profile_completed_at = '2026-03-02 09:00:00.000000';
SET @analytics_max_activity_date = LEAST(UTC_DATE(), DATE('2026-08-05'));

START TRANSACTION;

-- Tao 200 USER ACTIVE co ngay tao/profile nam truoc thang activity dau tien.
INSERT INTO users (
    email,
    email_verified_at,
    password_hash,
    role,
    status,
    blocked_at,
    blocked_reason,
    first_active_at,
    last_active_at,
    created_at,
    updated_at
)
WITH RECURSIVE sequence_numbers AS (
    SELECT 1 AS seed_number
    UNION ALL
    SELECT seed_number + 1
    FROM sequence_numbers
    WHERE seed_number < 200
)
SELECT
    CONCAT('analytics.seed.', LPAD(seed_number, 3, '0'), '@example.test'),
    @analytics_seed_created_at,
    NULL,
    'USER',
    'ACTIVE',
    NULL,
    NULL,
    NULL,
    NULL,
    @analytics_seed_created_at,
    @analytics_seed_created_at
FROM sequence_numbers
ON DUPLICATE KEY UPDATE
    role = VALUES(role),
    status = VALUES(status),
    blocked_at = NULL,
    blocked_reason = NULL;

-- Hoan tat ho so de toan bo 200 USER deu du dieu kien tinh analytics.
INSERT INTO user_profiles (
    user_id,
    display_name,
    avatar_url,
    avatar_public_id,
    bio,
    date_of_birth,
    profile_completed_at,
    created_at,
    updated_at
)
SELECT
    u.id,
    CONCAT('Nguoi dung Analytics ', LPAD(CAST(SUBSTRING(u.email, 16, 3) AS UNSIGNED), 3, '0')),
    NULL,
    NULL,
    'Tai khoan du lieu test cho thong ke hoat dong va quay lai.',
    DATE_ADD(DATE('1998-01-01'), INTERVAL CAST(SUBSTRING(u.email, 16, 3) AS UNSIGNED) DAY),
    @analytics_profile_completed_at,
    @analytics_profile_completed_at,
    @analytics_profile_completed_at
FROM users u
WHERE u.email LIKE 'analytics.seed.___@example.test'
  AND CAST(SUBSTRING(u.email, 16, 3) AS UNSIGNED) BETWEEN 1 AND 200
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    avatar_url = VALUES(avatar_url),
    avatar_public_id = VALUES(avatar_public_id),
    bio = VALUES(bio),
    date_of_birth = VALUES(date_of_birth),
    profile_completed_at = VALUES(profile_completed_at);

-- Nhom A (001-050): quay lai moi thang sau khoang nghi lon hon 15 ngay.
-- Nhom B (051-100): hoat dong deu, cac lan lien nhau cach khong qua 15 ngay.
-- Nhom C (101-150): quay lai vao thang 06 va 08 sau thoi gian khong hoat dong.
-- Nhom D (151-175): ngung hoat dong sau thang 04 de tao mau inactive.
-- Nhom E (176-200): quay lai thang 07, sau do ngung hoat dong trong thang 08.
DROP TEMPORARY TABLE IF EXISTS tmp_analytics_activity_rows;

CREATE TEMPORARY TABLE tmp_analytics_activity_rows AS
WITH seed_users AS (
    SELECT
        u.id AS user_id,
        ROW_NUMBER() OVER (ORDER BY u.email) AS seed_number
    FROM users u
    WHERE u.email LIKE 'analytics.seed.___@example.test'
),
activity_candidates AS (
    SELECT user_id, seed_number, 1 AS event_order,
           DATE_ADD(DATE('2026-04-01'), INTERVAL FLOOR(RAND(seed_number * 101 + 1) * 4) DAY) AS activity_date
    FROM seed_users WHERE seed_number BETWEEN 1 AND 50
    UNION ALL
    SELECT user_id, seed_number, 2,
           DATE_ADD(DATE('2026-05-20'), INTERVAL FLOOR(RAND(seed_number * 101 + 2) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 1 AND 50
    UNION ALL
    SELECT user_id, seed_number, 3,
           DATE_ADD(DATE('2026-06-09'), INTERVAL FLOOR(RAND(seed_number * 101 + 3) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 1 AND 50
    UNION ALL
    SELECT user_id, seed_number, 4,
           DATE_ADD(DATE('2026-07-01'), INTERVAL FLOOR(RAND(seed_number * 101 + 4) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 1 AND 50
    UNION ALL
    SELECT user_id, seed_number, 5,
           DATE_ADD(DATE('2026-08-01'), INTERVAL FLOOR(RAND(seed_number * 101 + 5) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 1 AND 50
    UNION ALL
    SELECT user_id, seed_number, event_order,
           DATE_ADD(month_start, INTERVAL day_offset + FLOOR(RAND(seed_number * 211 + event_order) * 3) DAY)
    FROM seed_users
    CROSS JOIN (
        SELECT 1 AS event_order, DATE('2026-04-01') AS month_start, 0 AS day_offset
        UNION ALL SELECT 2, DATE('2026-04-01'), 9
        UNION ALL SELECT 3, DATE('2026-04-01'), 18
        UNION ALL SELECT 4, DATE('2026-05-01'), 0
        UNION ALL SELECT 5, DATE('2026-05-01'), 9
        UNION ALL SELECT 6, DATE('2026-05-01'), 18
        UNION ALL SELECT 7, DATE('2026-06-01'), 0
        UNION ALL SELECT 8, DATE('2026-06-01'), 9
        UNION ALL SELECT 9, DATE('2026-06-01'), 18
        UNION ALL SELECT 10, DATE('2026-07-01'), 0
        UNION ALL SELECT 11, DATE('2026-07-01'), 9
        UNION ALL SELECT 12, DATE('2026-07-01'), 18
        UNION ALL SELECT 13, DATE('2026-08-01'), 0
        UNION ALL SELECT 14, DATE('2026-08-01'), 2
    ) regular_activity_plan
    WHERE seed_number BETWEEN 51 AND 100
    UNION ALL
    SELECT user_id, seed_number, 1,
           DATE_ADD(DATE('2026-04-01'), INTERVAL FLOOR(RAND(seed_number * 307 + 1) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 101 AND 150
    UNION ALL
    SELECT user_id, seed_number, 2,
           DATE_ADD(DATE('2026-06-09'), INTERVAL FLOOR(RAND(seed_number * 307 + 2) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 101 AND 150
    UNION ALL
    SELECT user_id, seed_number, 3,
           DATE_ADD(DATE('2026-08-01'), INTERVAL FLOOR(RAND(seed_number * 307 + 3) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 101 AND 150
    UNION ALL
    SELECT user_id, seed_number, 1,
           DATE_ADD(DATE('2026-04-01'), INTERVAL FLOOR(RAND(seed_number * 401 + 1) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 151 AND 175
    UNION ALL
    SELECT user_id, seed_number, 2,
           DATE_ADD(DATE('2026-04-16'), INTERVAL FLOOR(RAND(seed_number * 401 + 2) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 151 AND 175
    UNION ALL
    SELECT user_id, seed_number, 1,
           DATE_ADD(DATE('2026-05-01'), INTERVAL FLOOR(RAND(seed_number * 503 + 1) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 176 AND 200
    UNION ALL
    SELECT user_id, seed_number, 2,
           DATE_ADD(DATE('2026-07-01'), INTERVAL FLOOR(RAND(seed_number * 503 + 2) * 4) DAY)
    FROM seed_users WHERE seed_number BETWEEN 176 AND 200
),
activity_times AS (
    SELECT
        user_id,
        event_order,
        activity_date,
        TIMESTAMP(activity_date)
            + INTERVAL (6 + FLOOR(RAND(seed_number * 701 + event_order) * 14)) HOUR
            + INTERVAL FLOOR(RAND(seed_number * 709 + event_order) * 60) MINUTE
            + INTERVAL FLOOR(RAND(seed_number * 719 + event_order) * 60) SECOND AS first_active_at,
        1 + FLOOR(RAND(seed_number * 727 + event_order) * 8) AS activity_count
    FROM activity_candidates
    WHERE activity_date <= @analytics_max_activity_date
)
SELECT
    user_id,
    activity_date,
    first_active_at,
    first_active_at + INTERVAL (5 + FLOOR(RAND(user_id * 733 + event_order) * 176)) MINUTE AS last_active_at,
    activity_count
FROM activity_times
;

-- Insert don gian tu bang tam giu kieu du lieu ro rang va van idempotent theo (user_id, activity_date).
INSERT INTO user_daily_activities (
    user_id,
    activity_date,
    first_active_at,
    last_active_at,
    activity_count,
    created_at,
    updated_at
)
SELECT
    user_id,
    activity_date,
    first_active_at,
    last_active_at,
    activity_count,
    first_active_at,
    last_active_at
FROM tmp_analytics_activity_rows
ON DUPLICATE KEY UPDATE
    first_active_at = VALUES(first_active_at),
    last_active_at = VALUES(last_active_at),
    activity_count = VALUES(activity_count),
    updated_at = VALUES(updated_at);

-- Dong bo hai moc tong hop tren users voi lich su hoat dong theo ngay vua tao.
UPDATE users u
INNER JOIN (
    SELECT
        uda.user_id,
        MIN(uda.first_active_at) AS first_active_at,
        MAX(uda.last_active_at) AS last_active_at
    FROM user_daily_activities uda
    INNER JOIN users seeded_user ON seeded_user.id = uda.user_id
    WHERE seeded_user.email LIKE 'analytics.seed.___@example.test'
      AND CAST(SUBSTRING(seeded_user.email, 16, 3) AS UNSIGNED) BETWEEN 1 AND 200
      AND uda.activity_date BETWEEN DATE('2026-04-01') AND @analytics_max_activity_date
    GROUP BY uda.user_id
) activity_bounds ON activity_bounds.user_id = u.id
SET
    u.first_active_at = activity_bounds.first_active_at,
    u.last_active_at = activity_bounds.last_active_at;

DROP TEMPORARY TABLE IF EXISTS tmp_analytics_activity_rows;

COMMIT;

-- Hau kiem: users=200, profiles_completed=200, future_activity_rows=0.
SELECT
    COUNT(*) AS analytics_seed_users,
    SUM(up.profile_completed_at IS NOT NULL) AS completed_profiles,
    COUNT(uda.id) AS activity_rows,
    COUNT(DISTINCT uda.user_id) AS users_with_activity,
    SUM(uda.activity_date > UTC_DATE()) AS future_activity_rows,
    MIN(uda.activity_date) AS first_activity_date,
    MAX(uda.activity_date) AS last_activity_date
FROM users u
LEFT JOIN user_profiles up ON up.user_id = u.id
LEFT JOIN user_daily_activities uda ON uda.user_id = u.id
WHERE u.email LIKE 'analytics.seed.___@example.test'
  AND CAST(SUBSTRING(u.email, 16, 3) AS UNSIGNED) BETWEEN 1 AND 200;

-- Hau kiem theo thang de dashboard quay lai co du lieu da dang.
SELECT
    DATE_FORMAT(activity_date, '%Y-%m') AS activity_month,
    COUNT(*) AS activity_rows,
    COUNT(DISTINCT user_id) AS active_users
FROM user_daily_activities
WHERE user_id IN (
    SELECT id
    FROM users
    WHERE email LIKE 'analytics.seed.___@example.test'
      AND CAST(SUBSTRING(email, 16, 3) AS UNSIGNED) BETWEEN 1 AND 200
)
  AND activity_date BETWEEN DATE('2026-04-01') AND @analytics_max_activity_date
GROUP BY DATE_FORMAT(activity_date, '%Y-%m')
ORDER BY activity_month;
