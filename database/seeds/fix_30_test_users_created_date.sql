-- =============================================================================
-- SUA NGAY CUA 30 TAI KHOAN TEST DA IMPORT
-- =============================================================================
-- Chi cap nhat cac tai khoan test.user01@example.test den test.user30@example.test.
-- Cac timestamp NULL (vi du profile chua onboarding) van duoc giu nguyen.

SET NAMES utf8mb4;
SET time_zone = '+00:00';

START TRANSACTION;

SET @old_seed_time = '2026-08-02 03:00:00.000000';
SET @new_seed_time = '2026-07-01 03:00:00.000000';

-- Sua cac moc thoi gian cua tai khoan, nhung chi khi van con gia tri seed cu.
UPDATE users
SET
    email_verified_at = CASE
        WHEN email_verified_at = @old_seed_time THEN @new_seed_time
        ELSE email_verified_at
    END,
    blocked_at = CASE
        WHEN blocked_at = @old_seed_time THEN @new_seed_time
        ELSE blocked_at
    END,
    created_at = CASE
        WHEN created_at = @old_seed_time THEN @new_seed_time
        ELSE created_at
    END,
    updated_at = CASE
        WHEN updated_at = @old_seed_time THEN @new_seed_time
        ELSE updated_at
    END
WHERE email IN (
    'test.user01@example.test', 'test.user02@example.test', 'test.user03@example.test',
    'test.user04@example.test', 'test.user05@example.test', 'test.user06@example.test',
    'test.user07@example.test', 'test.user08@example.test', 'test.user09@example.test',
    'test.user10@example.test', 'test.user11@example.test', 'test.user12@example.test',
    'test.user13@example.test', 'test.user14@example.test', 'test.user15@example.test',
    'test.user16@example.test', 'test.user17@example.test', 'test.user18@example.test',
    'test.user19@example.test', 'test.user20@example.test', 'test.user21@example.test',
    'test.user22@example.test', 'test.user23@example.test', 'test.user24@example.test',
    'test.user25@example.test', 'test.user26@example.test', 'test.user27@example.test',
    'test.user28@example.test', 'test.user29@example.test', 'test.user30@example.test'
);

-- Sua timestamp ho so tuong ung; profile_completed_at NULL duoc giu nguyen.
UPDATE user_profiles up
JOIN users u ON u.id = up.user_id
SET
    up.profile_completed_at = CASE
        WHEN up.profile_completed_at = @old_seed_time THEN @new_seed_time
        ELSE up.profile_completed_at
    END,
    up.created_at = CASE
        WHEN up.created_at = @old_seed_time THEN @new_seed_time
        ELSE up.created_at
    END,
    up.updated_at = CASE
        WHEN up.updated_at = @old_seed_time THEN @new_seed_time
        ELSE up.updated_at
    END
WHERE u.email IN (
    'test.user01@example.test', 'test.user02@example.test', 'test.user03@example.test',
    'test.user04@example.test', 'test.user05@example.test', 'test.user06@example.test',
    'test.user07@example.test', 'test.user08@example.test', 'test.user09@example.test',
    'test.user10@example.test', 'test.user11@example.test', 'test.user12@example.test',
    'test.user13@example.test', 'test.user14@example.test', 'test.user15@example.test',
    'test.user16@example.test', 'test.user17@example.test', 'test.user18@example.test',
    'test.user19@example.test', 'test.user20@example.test', 'test.user21@example.test',
    'test.user22@example.test', 'test.user23@example.test', 'test.user24@example.test',
    'test.user25@example.test', 'test.user26@example.test', 'test.user27@example.test',
    'test.user28@example.test', 'test.user29@example.test', 'test.user30@example.test'
);

COMMIT;

-- Kiem tra ket qua sau khi sua.
SELECT
    u.email,
    u.email_verified_at,
    u.blocked_at,
    u.created_at AS user_created_at,
    u.updated_at AS user_updated_at,
    up.profile_completed_at,
    up.created_at AS profile_created_at,
    up.updated_at AS profile_updated_at
FROM users u
JOIN user_profiles up ON up.user_id = u.id
WHERE u.email IN (
    'test.user01@example.test', 'test.user02@example.test', 'test.user03@example.test',
    'test.user04@example.test', 'test.user05@example.test', 'test.user06@example.test',
    'test.user07@example.test', 'test.user08@example.test', 'test.user09@example.test',
    'test.user10@example.test', 'test.user11@example.test', 'test.user12@example.test',
    'test.user13@example.test', 'test.user14@example.test', 'test.user15@example.test',
    'test.user16@example.test', 'test.user17@example.test', 'test.user18@example.test',
    'test.user19@example.test', 'test.user20@example.test', 'test.user21@example.test',
    'test.user22@example.test', 'test.user23@example.test', 'test.user24@example.test',
    'test.user25@example.test', 'test.user26@example.test', 'test.user27@example.test',
    'test.user28@example.test', 'test.user29@example.test', 'test.user30@example.test'
)
ORDER BY u.email;
