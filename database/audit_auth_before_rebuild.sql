-- AUDIT TRƯỚC REBUILD - CHỈ ĐỌC
-- Chạy thủ công trên database dự kiến rebuild và lưu kết quả để xác nhận đây là DEV/DEMO.
-- File không sửa schema hoặc dữ liệu.

SELECT DATABASE() AS database_name,
       @@hostname AS mysql_host,
       @@port AS mysql_port,
       @@version AS mysql_version,
       UTC_TIMESTAMP(6) AS audited_at_utc;

-- 1. Row count của toàn bộ bảng baseline có thể bị mất khi rebuild.
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'user_profiles', COUNT(*) FROM user_profiles
UNION ALL SELECT 'user_auth_providers', COUNT(*) FROM user_auth_providers
UNION ALL SELECT 'refresh_tokens', COUNT(*) FROM refresh_tokens
UNION ALL SELECT 'pending_registrations', COUNT(*) FROM pending_registrations
UNION ALL SELECT 'password_reset_tokens', COUNT(*) FROM password_reset_tokens
UNION ALL SELECT 'posts', COUNT(*) FROM posts
UNION ALL SELECT 'post_media', COUNT(*) FROM post_media
UNION ALL SELECT 'hashtags', COUNT(*) FROM hashtags
UNION ALL SELECT 'post_hashtags', COUNT(*) FROM post_hashtags
UNION ALL SELECT 'post_likes', COUNT(*) FROM post_likes
UNION ALL SELECT 'comments', COUNT(*) FROM comments
UNION ALL SELECT 'follows', COUNT(*) FROM follows
UNION ALL SELECT 'saved_posts', COUNT(*) FROM saved_posts
UNION ALL SELECT 'reports', COUNT(*) FROM reports
UNION ALL SELECT 'account_status_histories', COUNT(*) FROM account_status_histories
UNION ALL SELECT 'admin_actions', COUNT(*) FROM admin_actions
ORDER BY table_name;

-- 2. Khoảng thời gian dữ liệu để phát hiện database có lịch sử thật.
SELECT MIN(created_at) AS oldest_user,
       MAX(created_at) AS newest_user,
       COUNT(*) AS total_users
FROM users;

SELECT MIN(created_at) AS oldest_post,
       MAX(created_at) AS newest_post,
       COUNT(*) AS total_posts
FROM posts;

-- 3. Danh sách Admin phải được người phụ trách xác nhận là tài khoản demo.
SELECT id, email, phone_number, status, created_at
FROM users
WHERE role = 'ADMIN'
ORDER BY id;

-- 4. User không có phương thức đăng nhập hợp lệ.
SELECT u.id, u.email, u.phone_number, u.role, u.status
FROM users u
WHERE NOT (
    (
        u.password_hash IS NOT NULL
        AND (
             (u.email IS NOT NULL AND u.email_verified_at IS NOT NULL)
          OR (u.phone_number IS NOT NULL AND u.phone_verified_at IS NOT NULL)
        )
    )
    OR EXISTS (
        SELECT 1
        FROM user_auth_providers p
        WHERE p.user_id = u.id
          AND p.provider IN ('GOOGLE', 'FACEBOOK')
          AND p.provider_user_id IS NOT NULL
          AND CHAR_LENGTH(TRIM(p.provider_user_id)) > 0
    )
)
ORDER BY u.id;

-- 5. Duplicate identifier/provider cần xử lý nếu sau này chọn migration bảo tồn.
SELECT LOWER(TRIM(email)) AS normalized_email, COUNT(*) AS duplicate_count
FROM users
WHERE email IS NOT NULL
GROUP BY LOWER(TRIM(email))
HAVING COUNT(*) > 1;

SELECT phone_number, COUNT(*) AS duplicate_count
FROM users
WHERE phone_number IS NOT NULL
GROUP BY phone_number
HAVING COUNT(*) > 1;

SELECT provider, provider_user_id, COUNT(*) AS duplicate_count
FROM user_auth_providers
GROUP BY provider, provider_user_id
HAVING COUNT(*) > 1;

-- 6. Hai pending PENDING còn hiệu lực cho cùng identifier theo schema baseline cũ.
SELECT registration_type,
       CASE
           WHEN registration_type = 'EMAIL' THEN LOWER(TRIM(email))
           ELSE phone_number
       END AS identifier_normalized,
       COUNT(*) AS active_count
FROM pending_registrations
WHERE status = 'PENDING'
  AND expires_at > UTC_TIMESTAMP(6)
GROUP BY registration_type,
         CASE
             WHEN registration_type = 'EMAIL' THEN LOWER(TRIM(email))
             ELSE phone_number
         END
HAVING COUNT(*) > 1;

-- 7. Phiên đang hoạt động sẽ mất khi rebuild.
SELECT COUNT(*) AS active_refresh_tokens
FROM refresh_tokens
WHERE revoked_at IS NULL
  AND expires_at > UTC_TIMESTAMP(6);

-- 8. Media metadata và URL storage cần audit orphan riêng sau rebuild.
SELECT COUNT(*) AS post_media_rows,
       COUNT(DISTINCT post_id) AS posts_with_media
FROM post_media;

SELECT COUNT(*) AS profiles_with_avatar
FROM user_profiles
WHERE avatar_url IS NOT NULL OR avatar_public_id IS NOT NULL;

-- 9. Xác nhận constraint baseline còn tồn tại trước rebuild.
SELECT constraint_name, table_name, constraint_type
FROM information_schema.table_constraints
WHERE constraint_schema = DATABASE()
  AND constraint_name = 'chk_users_contact_required';

-- Chỉ được rebuild sau khi người phụ trách xác nhận toàn bộ kết quả trên là DEV/DEMO
-- và backup/snapshot đã hoàn tất.
