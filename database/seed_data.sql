-- Seed Auth DEV/DEMO sau khi rebuild database.
-- Không chạy trên môi trường có dữ liệu cần bảo tồn.
-- File cố ý không idempotent: chạy lần hai phải lỗi unique/primary key thay vì tạo Admin trùng.
-- Khi dùng file này phải đặt APP_BOOTSTRAP_ADMIN_ENABLED=false.

SET NAMES utf8mb4;
SET time_zone = '+00:00';

START TRANSACTION;

-- BCrypt cost 10 cho mật khẩu demo DEV: Demo@12345
-- Không sử dụng credential này ngoài môi trường local/demo.
SET @demo_password_hash = '$2a$10$OZDWQo86Ao3A2cbcPxTzUOhaV4At2WuPcQMXK6xSRCfdVVnzSsXAy';
SET @seed_time = '2026-07-19 03:00:00.000000';

INSERT INTO users (
    id,
    email,
    phone_number,
    email_verified_at,
    phone_verified_at,
    password_hash,
    role,
    status,
    blocked_at,
    blocked_reason,
    created_at,
    updated_at
) VALUES
    (1001, 'admin.demo@example.test', NULL, @seed_time, NULL, @demo_password_hash, 'ADMIN', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1002, 'local.email@example.test', NULL, @seed_time, NULL, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1003, NULL, '+84901000003', NULL, @seed_time, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1004, NULL, NULL, NULL, NULL, NULL, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1005, NULL, NULL, NULL, NULL, NULL, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1006, 'local.google@example.test', NULL, @seed_time, NULL, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1007, 'blocked.demo@example.test', NULL, @seed_time, NULL, @demo_password_hash, 'USER', 'BLOCKED', @seed_time, 'Tài khoản demo dùng để kiểm thử BLOCKED', @seed_time, @seed_time),
    (1008, 'onboarding.pending@example.test', NULL, @seed_time, NULL, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time);

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
) VALUES
    (1001, 'Admin Demo', NULL, NULL, 'Tài khoản quản trị demo', '1995-01-01', @seed_time, @seed_time, @seed_time),
    (1002, 'Local Email Demo', NULL, NULL, NULL, '2000-02-02', @seed_time, @seed_time, @seed_time),
    (1003, 'Local Phone Demo', NULL, NULL, NULL, '2000-03-03', @seed_time, @seed_time, @seed_time),
    (1004, 'Google Only Demo', NULL, NULL, NULL, '2000-04-04', @seed_time, @seed_time, @seed_time),
    (1005, 'Facebook Only Demo', NULL, NULL, NULL, '2000-05-05', @seed_time, @seed_time, @seed_time),
    (1006, 'Local Google Demo', NULL, NULL, NULL, '2000-06-06', @seed_time, @seed_time, @seed_time),
    (1007, 'Blocked Demo', NULL, NULL, NULL, '2000-07-07', @seed_time, @seed_time, @seed_time),
    (1008, NULL, NULL, NULL, NULL, NULL, NULL, @seed_time, @seed_time);

INSERT INTO user_auth_providers (
    id,
    user_id,
    provider,
    provider_user_id,
    provider_email,
    provider_email_verified,
    created_at,
    updated_at
) VALUES
    (2001, 1004, 'GOOGLE', 'demo-google-only-1004', 'google.only@example.test', 1, @seed_time, @seed_time),
    (2002, 1005, 'FACEBOOK', 'demo-facebook-only-1005', NULL, NULL, @seed_time, @seed_time),
    (2003, 1006, 'GOOGLE', 'demo-local-google-1006', 'local.google@example.test', 1, @seed_time, @seed_time);

COMMIT;

-- Hậu kiểm nhanh: phải trả 8 users, 8 profiles, 3 provider links và 1 ADMIN.
SELECT COUNT(*) AS seeded_users FROM users WHERE id BETWEEN 1001 AND 1008;
SELECT COUNT(*) AS seeded_profiles FROM user_profiles WHERE user_id BETWEEN 1001 AND 1008;
SELECT COUNT(*) AS seeded_provider_links FROM user_auth_providers WHERE id BETWEEN 2001 AND 2003;
SELECT COUNT(*) AS seeded_admins FROM users WHERE role = 'ADMIN' AND id BETWEEN 1001 AND 1008;
