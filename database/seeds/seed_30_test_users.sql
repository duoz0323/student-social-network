-- =============================================================================
-- SEED 30 TAI KHOAN USER CHO MOI TRUONG LOCAL/TEST
-- =============================================================================
-- Chi chay sau khi schema trong database/student_social_network.sql da duoc tao.
-- Khong chay tren production hoac moi truong co du lieu can bao ton.
-- File co y khong idempotent: chay lai se loi uq_users_email va rollback transaction.
-- Mat khau tho chi duoc ghi tai day de tester dang nhap; bang users chi nhan BCrypt hash.
-- BCrypt cost: 10.
--
-- DANH SACH THONG TIN DANG NHAP TRUOC KHI BAM
-- 01. test.user01@example.test | TestUser01@2026
-- 02. test.user02@example.test | TestUser02@2026
-- 03. test.user03@example.test | TestUser03@2026
-- 04. test.user04@example.test | TestUser04@2026
-- 05. test.user05@example.test | TestUser05@2026
-- 06. test.user06@example.test | TestUser06@2026
-- 07. test.user07@example.test | TestUser07@2026
-- 08. test.user08@example.test | TestUser08@2026
-- 09. test.user09@example.test | TestUser09@2026
-- 10. test.user10@example.test | TestUser10@2026
-- 11. test.user11@example.test | TestUser11@2026
-- 12. test.user12@example.test | TestUser12@2026
-- 13. test.user13@example.test | TestUser13@2026
-- 14. test.user14@example.test | TestUser14@2026
-- 15. test.user15@example.test | TestUser15@2026
-- 16. test.user16@example.test | TestUser16@2026
-- 17. test.user17@example.test | TestUser17@2026
-- 18. test.user18@example.test | TestUser18@2026
-- 19. test.user19@example.test | TestUser19@2026
-- 20. test.user20@example.test | TestUser20@2026
-- 21. test.user21@example.test | TestUser21@2026
-- 22. test.user22@example.test | TestUser22@2026
-- 23. test.user23@example.test | TestUser23@2026
-- 24. test.user24@example.test | TestUser24@2026
-- 25. test.user25@example.test | TestUser25@2026
-- 26. test.user26@example.test | TestUser26@2026
-- 27. test.user27@example.test | TestUser27@2026
-- 28. test.user28@example.test | TestUser28@2026 | chua hoan tat onboarding
-- 29. test.user29@example.test | TestUser29@2026 | chua hoan tat onboarding
-- 30. test.user30@example.test | TestUser30@2026 | tai khoan BLOCKED

SET NAMES utf8mb4;
SET time_zone = '+00:00';

START TRANSACTION;

SET @seed_time = '2026-07-01 03:00:00.000000';

-- Moi local account deu co email da xac minh va chi luu BCrypt hash.
INSERT INTO users (
    email,
    email_verified_at,
    password_hash,
    role,
    status,
    blocked_at,
    blocked_reason,
    created_at,
    updated_at
) VALUES
    ('test.user01@example.test', @seed_time, '$2y$10$aGQDqcH5qK7jLRKAt2hCZexSlVNmKdDODA57Njo/EWGgrlaEIJXI.', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user02@example.test', @seed_time, '$2y$10$DOMrp5YEHQunZ7rgEqQlPO3T4AqvLR3b1WM3xIw1OC.9OYxtEQDsm', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user03@example.test', @seed_time, '$2y$10$9V/LLWC5Ue/5IniO.y3IS.FroBdnZYs/5E3wOdey0O0htaT7k9FwS', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user04@example.test', @seed_time, '$2y$10$v17nwUWPAzZvBvab8NFsb.TBU39qI..2RLNnCjknfdDraPsdb.1g.', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user05@example.test', @seed_time, '$2y$10$8xvPLue3XOBoIqDzpuvtHOKHUCH8qv9qyqW3MQ7X3oHERnrbSwXEa', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user06@example.test', @seed_time, '$2y$10$owMHBLn/.Y14BWc9jsoj/enrZWkG5ycoRR6IlIu8lUQYFF1PLuhqq', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user07@example.test', @seed_time, '$2y$10$8xLmLaO5N8GvOQae6qChHO4i6Ehm5L4Xe871qBzB5kG06Vh0plMHC', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user08@example.test', @seed_time, '$2y$10$JED1leC9Q8CeDkouN20aQuPWGpCMgnw9Zw7iPm9qpEzMdbT6bsavO', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user09@example.test', @seed_time, '$2y$10$TQQixhxksI.cf8WDWQnUnej5AGBS8MFbSBSQs3NjEbyLhYqCsmDXa', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user10@example.test', @seed_time, '$2y$10$rBAG.O75P2pdkzw808yh/.gkH.8LkmkSofrAJBDXC.6QSDEK7YYNq', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user11@example.test', @seed_time, '$2y$10$FU.tww2ivPO.TXPCgjsKSei/bQXryqJwmXnO97JiiRk3ALz9xavyy', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user12@example.test', @seed_time, '$2y$10$W1ynFq9brwX5W0igDfOJ5O.N5R0tT3eTDA1OrfqzxL1SBBlL.3R3S', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user13@example.test', @seed_time, '$2y$10$aVbd4rtsGNBOHpr6nXKEpOTMfj8NWHoGCiuczapxrWrKwce7UyTgW', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user14@example.test', @seed_time, '$2y$10$IyPP.v8g5TUCgJDLS3PJYeDlkBXiyH2PijM14YNqIwmUAQSYd1v0.', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user15@example.test', @seed_time, '$2y$10$v6hXIV4rUDa6rfOI/6qo9.XzKeIHsO/SFfQTd77cWYl4cf4ShUVjK', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user16@example.test', @seed_time, '$2y$10$HRbB9/moVwL./bsZnOW9W.vvk.z/g6jSUrY3wWIkvhaIMTn6CjS9a', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user17@example.test', @seed_time, '$2y$10$5hUIWRR1aKhfxjANPWZwDO3OXT8x59tAxtxhm9qenyj2txmAF1WuW', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user18@example.test', @seed_time, '$2y$10$Q1Vp01EUFb/nNUTsLq6M4u3r4CZM7ggPdOb5vA3fYxnysH0r.pT2K', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user19@example.test', @seed_time, '$2y$10$iTVONDv7qeK2rb.OtGQ2pOwCs8eBS86F9gEgsB8iocVSwW37QO9kS', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user20@example.test', @seed_time, '$2y$10$5iwODmDnpdnS4VTTNyOTgeEspTmocr/NP3ClWe9Rhs4/.u/36iHla', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user21@example.test', @seed_time, '$2y$10$h7OEjwDb8Y8gqIWoJEAPqO7Fv9AcyQZzQeyTaZf3joN1SfxjBFqpK', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user22@example.test', @seed_time, '$2y$10$GtFcZFqBodJc1IISIymWnObYxIuTv.pHvodroyWfyd/JXcVNE4P2K', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user23@example.test', @seed_time, '$2y$10$UsVuHOISPxXPZpwqK9TO3uHqyH.AgA2/yS6FAwVPRqM0DpNGSyjm.', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user24@example.test', @seed_time, '$2y$10$.R6NkoitYZk65mYc95hgweSINoHjuA/krgOJ1h0mlefA7kOKGLvWm', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user25@example.test', @seed_time, '$2y$10$0ZVCUTIX8309J.0fQc6nwOUIbytuwqE6ARgp7vgVOVGz4Z1fJIozS', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user26@example.test', @seed_time, '$2y$10$vHTROazes62nVMHOegoKge7.2jy3UWkYJ7EvYEN97vFwvMs2OdOMC', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user27@example.test', @seed_time, '$2y$10$b1assPy8Op9y5aEVDYUECu6UC8kaMVcUGvLSsc0qs6pPX.bDLYXuy', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user28@example.test', @seed_time, '$2y$10$Oe7ijAi1TItYFS9E7S9byOK9M9U6Hn2unUX3Ir9.SFIiZ/3/qk3ae', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user29@example.test', @seed_time, '$2y$10$Dk005akspAp/susHx1NVyeTtrY7cdCJlKHGUIkPc.lGSRHgMFLt5C', 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    ('test.user30@example.test', @seed_time, '$2y$10$RUUXVhjjiaayglQzMTUzE.Pp7rmKl8c.vNjIodzi7j34P5WJsktK2', 'USER', 'BLOCKED', @seed_time, 'Tai khoan test trang thai BLOCKED', @seed_time, @seed_time);

-- Ho so 01-27 va 30 da hoan tat; 28-29 giu rong de test PROFILE_NOT_COMPLETED.
INSERT INTO user_profiles (
    user_id,
    username,
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
    CASE WHEN p.profile_completed_at IS NULL THEN NULL ELSE SUBSTRING_INDEX(u.email, '@', 1) END,
    p.display_name,
    NULL,
    NULL,
    p.bio,
    p.date_of_birth,
    p.profile_completed_at,
    @seed_time,
    @seed_time
FROM users u
JOIN (
    SELECT 'test.user01@example.test' AS email, 'Nguyen Minh Anh' AS display_name, 'Sinh vien yeu thich cong nghe va hoc tap.' AS bio, '2002-01-15' AS date_of_birth, @seed_time AS profile_completed_at
    UNION ALL SELECT 'test.user02@example.test', 'Tran Quoc Bao', 'Chia se trai nghiem hoc tap va cuoc song sinh vien.', '2001-02-20', @seed_time
    UNION ALL SELECT 'test.user03@example.test', 'Le Hoai Chi', 'Quan tam den thiet ke, truyen thong va nhiep anh.', '2003-03-12', @seed_time
    UNION ALL SELECT 'test.user04@example.test', 'Pham Tuan Dat', 'Dang hoc Spring Boot va React.', '2000-04-08', @seed_time
    UNION ALL SELECT 'test.user05@example.test', 'Hoang Ngoc Diep', 'Thich doc sach va tham gia cau lac bo.', '2002-05-24', @seed_time
    UNION ALL SELECT 'test.user06@example.test', 'Vo Gia Huy', 'Dam me lap trinh va cac du an ma nguon mo.', '2001-06-18', @seed_time
    UNION ALL SELECT 'test.user07@example.test', 'Bui Khanh Linh', 'Chia se tai lieu va kinh nghiem thi cu.', '2003-07-07', @seed_time
    UNION ALL SELECT 'test.user08@example.test', 'Dang Minh Khang', 'Yeu thich bong da va hoat dong ngoai khoa.', '2000-08-30', @seed_time
    UNION ALL SELECT 'test.user09@example.test', 'Do Thao My', 'Quan tam den du lieu va tri tue nhan tao.', '2002-09-09', @seed_time
    UNION ALL SELECT 'test.user10@example.test', 'Ngo Duc Nam', 'Tim kiem co hoi thuc tap va ket noi ban be.', '2001-10-14', @seed_time
    UNION ALL SELECT 'test.user11@example.test', 'Ly Bao Ngoc', 'Thich am nhac, phim anh va du lich.', '2003-11-22', @seed_time
    UNION ALL SELECT 'test.user12@example.test', 'Truong Hoang Phuc', 'Sinh vien cong nghe thong tin.', '2000-12-05', @seed_time
    UNION ALL SELECT 'test.user13@example.test', 'Nguyen Thanh Quan', 'Quan tam den an ninh mang va backend.', '2002-01-28', @seed_time
    UNION ALL SELECT 'test.user14@example.test', 'Tran Mai Quynh', 'Thich viet lach va sang tao noi dung.', '2001-02-11', @seed_time
    UNION ALL SELECT 'test.user15@example.test', 'Le Minh Son', 'Chia se kinh nghiem lam do an sinh vien.', '2003-03-19', @seed_time
    UNION ALL SELECT 'test.user16@example.test', 'Pham Thu Trang', 'Yeu thich thiet ke giao dien va trai nghiem nguoi dung.', '2000-04-27', @seed_time
    UNION ALL SELECT 'test.user17@example.test', 'Hoang Anh Tuan', 'Dang tim dong doi cho cac du an hoc tap.', '2002-05-06', @seed_time
    UNION ALL SELECT 'test.user18@example.test', 'Vo Ngoc Uyen', 'Quan tam den ngoai ngu va trao doi sinh vien.', '2001-06-16', @seed_time
    UNION ALL SELECT 'test.user19@example.test', 'Bui Thanh Viet', 'Thich the thao dien tu va lap trinh game.', '2003-07-25', @seed_time
    UNION ALL SELECT 'test.user20@example.test', 'Dang Ha Vy', 'Chia se nhung dia diem hoc va lam viec.', '2000-08-13', @seed_time
    UNION ALL SELECT 'test.user21@example.test', 'Do Minh Chau', 'Sinh vien yeu thich nghien cuu khoa hoc.', '2002-09-21', @seed_time
    UNION ALL SELECT 'test.user22@example.test', 'Ngo Quoc Cuong', 'Quan tam den dien toan dam may.', '2001-10-02', @seed_time
    UNION ALL SELECT 'test.user23@example.test', 'Ly Thu Ha', 'Thich cac hoat dong tinh nguyen.', '2003-11-10', @seed_time
    UNION ALL SELECT 'test.user24@example.test', 'Truong Gia Han', 'Chia se meo quan ly thoi gian hoc tap.', '2000-12-29', @seed_time
    UNION ALL SELECT 'test.user25@example.test', 'Nguyen Duc Long', 'Dang hoc ve co so du lieu va he thong phan tan.', '2002-01-03', @seed_time
    UNION ALL SELECT 'test.user26@example.test', 'Tran Yen Nhi', 'Thich chup anh va kham pha thanh pho.', '2001-02-17', @seed_time
    UNION ALL SELECT 'test.user27@example.test', 'Le Quang Thai', 'Quan tam den startup va san pham cong nghe.', '2003-03-26', @seed_time
    UNION ALL SELECT 'test.user28@example.test', NULL, NULL, NULL, NULL
    UNION ALL SELECT 'test.user29@example.test', NULL, NULL, NULL, NULL
    UNION ALL SELECT 'test.user30@example.test', 'Pham Hai Yen', 'Ho so dung de test tai khoan bi khoa.', '2000-04-09', @seed_time
) p ON p.email = u.email;

COMMIT;

-- Kiem tra nhanh sau khi seed: phai tra ve 30 users va 30 profiles.
SELECT
    COUNT(*) AS seeded_users,
    SUM(up.user_id IS NOT NULL) AS seeded_profiles,
    SUM(up.profile_completed_at IS NULL) AS onboarding_pending_profiles,
    SUM(u.status = 'BLOCKED') AS blocked_users
FROM users u
LEFT JOIN user_profiles up ON up.user_id = u.id
WHERE u.email LIKE 'test.user%@example.test';
