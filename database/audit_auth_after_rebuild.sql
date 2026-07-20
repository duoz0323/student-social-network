-- AUDIT SAU REBUILD - CHỈ ĐỌC
-- Tất cả query phát hiện vi phạm bên dưới phải trả 0 dòng hoặc count = 0.

SELECT DATABASE() AS database_name,
       @@hostname AS mysql_host,
       @@version AS mysql_version,
       UTC_TIMESTAMP(6) AS audited_at_utc;

-- 1. Bốn bảng Auth phải tồn tại; không có auth_challenges tổng quát.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'pending_registrations',
      'auth_method_link_challenges',
      'social_auth_challenges',
      'reauthentication_challenges',
      'auth_challenges'
  )
ORDER BY table_name;

-- 2. User không có auth method hợp lệ: phải trả 0 dòng.
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

-- 3. User/profile 1-1: phải trả 0 dòng.
SELECT u.id
FROM users u
LEFT JOIN user_profiles p ON p.user_id = u.id
WHERE p.user_id IS NULL;

-- 4. Pending lifecycle/key/secret: phải trả 0 dòng.
SELECT id, status
FROM pending_registrations
WHERE
    (
        status = 'PENDING'
        AND (
            identifier_normalized IS NULL
            OR active_identifier_key <>
                CONCAT(registration_type, ':', identifier_normalized)
            OR password_hash IS NULL
            OR flow_token_hash IS NULL
            OR otp_hash IS NULL
            OR terminal_at IS NOT NULL
        )
    )
    OR
    (
        status = 'COMPLETED'
        AND (
            identifier_normalized IS NULL
            OR active_identifier_key IS NOT NULL
            OR password_hash IS NOT NULL
            OR flow_token_hash IS NOT NULL
            OR otp_hash IS NOT NULL
            OR delivery_failure_code IS NOT NULL
            OR terminal_at IS NULL
        )
    )
    OR
    (
        status IN ('CANCELLED', 'EXPIRED')
        AND (
            identifier_normalized IS NOT NULL
            OR active_identifier_key IS NOT NULL
            OR password_hash IS NOT NULL
            OR flow_token_hash IS NOT NULL
            OR otp_hash IS NOT NULL
            OR delivery_failure_code IS NOT NULL
            OR completed_user_id IS NOT NULL
            OR terminal_at IS NULL
        )
    );

-- 5. Link challenge lifecycle: phải trả 0 dòng.
SELECT id, status
FROM auth_method_link_challenges
WHERE
    (
        status = 'PENDING'
        AND (
            identifier_normalized IS NULL
            OR active_identifier_key <>
                CONCAT(purpose, ':', identifier_normalized)
            OR active_user_purpose_key <>
                CONCAT(CAST(user_id AS CHAR), ':', purpose)
            OR flow_token_hash IS NULL
            OR otp_hash IS NULL
            OR terminal_at IS NOT NULL
        )
    )
    OR
    (
        status <> 'PENDING'
        AND (
            identifier_normalized IS NOT NULL
            OR active_identifier_key IS NOT NULL
            OR active_user_purpose_key IS NOT NULL
            OR flow_token_hash IS NOT NULL
            OR otp_hash IS NOT NULL
            OR delivery_failure_code IS NOT NULL
            OR terminal_at IS NULL
        )
    );

-- 6. Social challenge lifecycle: fingerprint được giữ, raw identity phải xóa terminal.
SELECT id, status
FROM social_auth_challenges
WHERE provider_identity_fingerprint IS NULL
   OR (
       status = 'PENDING'
       AND (
           conflict_token_hash IS NULL
           OR provider_user_id IS NULL
           OR active_provider_key <>
               CONCAT(provider, ':', provider_identity_fingerprint)
           OR terminal_at IS NOT NULL
       )
   )
   OR (
       status <> 'PENDING'
       AND (
           conflict_token_hash IS NOT NULL
           OR provider_user_id IS NOT NULL
           OR provider_email IS NOT NULL
           OR provider_email_verified IS NOT NULL
           OR active_provider_key IS NOT NULL
           OR terminal_at IS NULL
       )
   );

-- 7. Reauthentication lifecycle và unique user/scope: phải trả 0 dòng.
SELECT id, status
FROM reauthentication_challenges
WHERE
    (
        status = 'ACTIVE'
        AND (
            token_hash IS NULL
            OR active_user_scope_key <>
                CONCAT(CAST(user_id AS CHAR), ':', scope)
            OR terminal_at IS NOT NULL
        )
    )
    OR
    (
        status <> 'ACTIVE'
        AND (
            token_hash IS NOT NULL
            OR active_user_scope_key IS NOT NULL
            OR terminal_at IS NULL
        )
    );

-- 8. Provider identity duplicate: phải trả 0 dòng.
SELECT provider, provider_user_id, COUNT(*) AS duplicate_count
FROM user_auth_providers
GROUP BY provider, provider_user_id
HAVING COUNT(*) > 1;

-- 9. Terminal row quá hạn cleanup 7 ngày: tất cả count phải bằng 0.
SELECT 'pending_registrations' AS table_name, COUNT(*) AS overdue_rows
FROM pending_registrations
WHERE status <> 'PENDING'
  AND terminal_at < UTC_TIMESTAMP(6) - INTERVAL 7 DAY
UNION ALL
SELECT 'auth_method_link_challenges', COUNT(*)
FROM auth_method_link_challenges
WHERE status <> 'PENDING'
  AND terminal_at < UTC_TIMESTAMP(6) - INTERVAL 7 DAY
UNION ALL
SELECT 'social_auth_challenges', COUNT(*)
FROM social_auth_challenges
WHERE status <> 'PENDING'
  AND terminal_at < UTC_TIMESTAMP(6) - INTERVAL 7 DAY
UNION ALL
SELECT 'reauthentication_challenges', COUNT(*)
FROM reauthentication_challenges
WHERE status <> 'ACTIVE'
  AND terminal_at < UTC_TIMESTAMP(6) - INTERVAL 7 DAY;

-- 10. Seed tối thiểu sau rebuild.
SELECT role, status, COUNT(*) AS user_count
FROM users
GROUP BY role, status
ORDER BY role, status;

SELECT provider, COUNT(*) AS provider_count
FROM user_auth_providers
GROUP BY provider
ORDER BY provider;
