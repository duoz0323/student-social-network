-- =============================================================================
-- SEED 1.000 USER VA 1.000 POST CHO MOI TRUONG LOCAL/TEST
-- =============================================================================
-- Chi chay tren database student_social_network dung cho local/test.
-- Script xoa toan bo du lieu nghiep vu hien co, giu nguyen schema va trigger.
-- Tat ca tai khoan local dung chung mat khau test: TestUser01@2026
-- Anh demo dung URL seed on dinh cua Lorem Picsum, khong luu file dang BLOB.

USE `student_social_network`;
SET NAMES utf8mb4;
SET time_zone = '+07:00';

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `media_cleanup_tasks`;
TRUNCATE TABLE `message_attachments`;
TRUNCATE TABLE `conversation_members`;
TRUNCATE TABLE `messages`;
TRUNCATE TABLE `conversations`;
TRUNCATE TABLE `notifications`;
TRUNCATE TABLE `admin_actions`;
TRUNCATE TABLE `account_status_histories`;
TRUNCATE TABLE `reports`;
TRUNCATE TABLE `moderation_cases`;
TRUNCATE TABLE `saved_posts`;
TRUNCATE TABLE `post_reposts`;
TRUNCATE TABLE `comments`;
TRUNCATE TABLE `post_likes`;
TRUNCATE TABLE `post_hashtags`;
TRUNCATE TABLE `hashtags`;
TRUNCATE TABLE `post_media`;
TRUNCATE TABLE `posts`;
TRUNCATE TABLE `locations`;
TRUNCATE TABLE `user_restrictions`;
TRUNCATE TABLE `user_blocks`;
TRUNCATE TABLE `follows`;
TRUNCATE TABLE `user_daily_activities`;
TRUNCATE TABLE `password_reset_tokens`;
TRUNCATE TABLE `password_recovery_challenges`;
TRUNCATE TABLE `reauthentication_challenges`;
TRUNCATE TABLE `social_auth_challenges`;
TRUNCATE TABLE `auth_method_link_challenges`;
TRUNCATE TABLE `pending_registrations`;
TRUNCATE TABLE `refresh_tokens`;
TRUNCATE TABLE `user_auth_providers`;
TRUNCATE TABLE `user_interests`;
TRUNCATE TABLE `user_profiles`;
TRUNCATE TABLE `users`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `locations`
    (`google_place_id`, `display_name`, `formatted_address`, `latitude`, `longitude`)
VALUES
    ('demo-hcmute', 'Trường Đại học Sư phạm Kỹ thuật TP.HCM', '1 Võ Văn Ngân, Thủ Đức, TP.HCM', 10.8506000, 106.7719000),
    ('demo-uit', 'Trường Đại học Công nghệ Thông tin', 'Khu phố 6, Thủ Đức, TP.HCM', 10.8700000, 106.8030000),
    ('demo-vnuhcm', 'Đại học Quốc gia TP.HCM', 'Linh Trung, Thủ Đức, TP.HCM', 10.8799000, 106.8033000),
    ('demo-book-street', 'Đường sách Nguyễn Văn Bình', 'Quận 1, TP.HCM', 10.7802000, 106.7005000),
    ('demo-youth-house', 'Nhà Văn hóa Thanh niên', '4 Phạm Ngọc Thạch, Quận 1, TP.HCM', 10.7831000, 106.6959000),
    ('demo-library', 'Thư viện Khoa học Tổng hợp', '69 Lý Tự Trọng, Quận 1, TP.HCM', 10.7757000, 106.6990000),
    ('demo-independence', 'Dinh Độc Lập', '135 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM', 10.7770000, 106.6953000),
    ('demo-central-park', 'Công viên Trung tâm', 'Bình Thạnh, TP.HCM', 10.7944000, 106.7218000),
    ('demo-dormitory-a', 'Ký túc xá Khu A', 'Đông Hòa, Dĩ An, Bình Dương', 10.8778000, 106.8002000),
    ('demo-dormitory-b', 'Ký túc xá Khu B', 'Đông Hòa, Dĩ An, Bình Dương', 10.8831000, 106.7827000),
    ('demo-cafe-study', 'Không gian học tập cộng đồng', 'Thủ Đức, TP.HCM', 10.8498000, 106.7711000),
    ('demo-stadium', 'Sân vận động sinh viên', 'Thủ Đức, TP.HCM', 10.8752000, 106.8011000);

INSERT INTO `hashtags` (`normalized_name`, `display_name`) VALUES
    ('hoc tap', 'Học tập'),
    ('cong nghe', 'Công nghệ'),
    ('do an', 'Đồ án'),
    ('thuc tap', 'Thực tập'),
    ('viec lam', 'Việc làm'),
    ('an uong', 'Ăn uống'),
    ('du lich', 'Du lịch'),
    ('the thao', 'Thể thao'),
    ('am nhac', 'Âm nhạc'),
    ('nhiep anh', 'Nhiếp ảnh'),
    ('tinh nguyen', 'Tình nguyện'),
    ('doi song sinh vien', 'Đời sống sinh viên');

DROP PROCEDURE IF EXISTS `seed_website_cases`;
DELIMITER $$
CREATE PROCEDURE `seed_website_cases`()
BEGIN
    DECLARE user_no INT DEFAULT 1;
    DECLARE post_no INT DEFAULT 1;
    DECLARE media_no INT DEFAULT 0;
    DECLARE relation_no INT DEFAULT 1;
    DECLARE conversation_no INT DEFAULT 1;
    DECLARE current_post_id BIGINT UNSIGNED;
    DECLARE current_case_id BIGINT UNSIGNED;
    DECLARE current_conversation_id BIGINT UNSIGNED;
    DECLARE first_message_id BIGINT UNSIGNED;
    DECLARE second_message_id BIGINT UNSIGNED;
    DECLARE current_author_id BIGINT UNSIGNED;
    DECLARE current_user_id BIGINT UNSIGNED;
    DECLARE current_target_id BIGINT UNSIGNED;
    DECLARE current_status VARCHAR(16);
    DECLARE seed_time_value DATETIME(6);
    DECLARE seed_post_status VARCHAR(16);
    DECLARE seed_post_author_id BIGINT UNSIGNED;
    DECLARE seed_published_at DATETIME(6);

    START TRANSACTION;

    -- Tao dung 1.000 tai khoan, trong do 10 tai khoan cuoi dang cho onboarding.
    WHILE user_no <= 1000 DO
        SET seed_time_value = TIMESTAMP('2026-01-01 08:00:00') + INTERVAL MOD(user_no * 13, 210) DAY;
        SET current_status = IF(user_no = 6 OR MOD(user_no, 100) = 0, 'BLOCKED', 'ACTIVE');

        INSERT INTO `users` (
            `email`, `email_verified_at`, `password_hash`, `role`, `status`,
            `blocked_at`, `blocked_reason`, `first_active_at`, `last_active_at`,
            `created_at`, `updated_at`
        ) VALUES (
            CONCAT('demo.user', LPAD(user_no, 4, '0'), '@example.test'),
            seed_time_value,
            IF(user_no IN (3, 4), NULL, '$2y$10$aGQDqcH5qK7jLRKAt2hCZexSlVNmKdDODA57Njo/EWGgrlaEIJXI.'),
            IF(user_no = 1, 'ADMIN', 'USER'),
            current_status,
            IF(current_status = 'BLOCKED', seed_time_value + INTERVAL 30 DAY, NULL),
            IF(current_status = 'BLOCKED', 'Tài khoản test bị khóa để kiểm tra giao diện quản trị', NULL),
            IF(user_no <= 990, seed_time_value + INTERVAL 1 HOUR, NULL),
            IF(user_no <= 990, seed_time_value + INTERVAL MOD(user_no, 72) HOUR, NULL),
            seed_time_value,
            seed_time_value
        );

        SET current_user_id = LAST_INSERT_ID();

        INSERT INTO `user_profiles` (
            `user_id`, `username`, `display_name`, `avatar_url`, `avatar_public_id`,
            `bio`, `date_of_birth`, `school_id`, `faculty_id`, `major_id`, `entry_year`,
            `profile_completed_at`, `created_at`, `updated_at`
        ) VALUES (
            current_user_id,
            IF(user_no <= 990, CONCAT('student_', LPAD(user_no, 4, '0')), NULL),
            IF(
                user_no > 990,
                NULL,
                CASE
                    WHEN user_no = 1 THEN 'Quản trị viên Demo'
                    WHEN user_no = 2 THEN 'Nguyễn Minh Anh'
                    WHEN MOD(user_no, 5) = 0 THEN CONCAT('Trần Gia Bảo ', LPAD(user_no, 4, '0'))
                    WHEN MOD(user_no, 5) = 1 THEN CONCAT('Lê Hoài An ', LPAD(user_no, 4, '0'))
                    WHEN MOD(user_no, 5) = 2 THEN CONCAT('Phạm Khánh Linh ', LPAD(user_no, 4, '0'))
                    WHEN MOD(user_no, 5) = 3 THEN CONCAT('Võ Minh Khang ', LPAD(user_no, 4, '0'))
                    ELSE CONCAT('Đặng Thảo Vy ', LPAD(user_no, 4, '0'))
                END
            ),
            IF(user_no <= 990 AND MOD(user_no, 5) <> 0,
                CONCAT('https://i.pravatar.cc/300?img=', 1 + MOD(user_no, 70)), NULL),
            IF(user_no <= 990 AND MOD(user_no, 5) <> 0,
                CONCAT('demo/avatars/student-', LPAD(user_no, 4, '0')), NULL),
            IF(
                user_no > 990,
                NULL,
                CASE MOD(user_no, 6)
                    WHEN 0 THEN 'Sinh viên yêu thích công nghệ, lập trình và các dự án mã nguồn mở.'
                    WHEN 1 THEN 'Chia sẻ kinh nghiệm học tập, ôn thi và cuộc sống sinh viên.'
                    WHEN 2 THEN 'Quan tâm đến thiết kế, nhiếp ảnh và sáng tạo nội dung.'
                    WHEN 3 THEN 'Đang tìm cơ hội thực tập và kết nối với các bạn cùng ngành.'
                    WHEN 4 THEN 'Yêu thích thể thao, hoạt động ngoại khóa và tình nguyện.'
                    ELSE 'Khám phá địa điểm ăn uống, học nhóm và vui chơi quanh thành phố.'
                END
            ),
            IF(user_no <= 990, DATE('1998-01-01') + INTERVAL MOD(user_no * 29, 2555) DAY, NULL),
            IF(user_no <= 990, 1 + MOD(user_no - 1, 5), NULL),
            IF(
                user_no > 990,
                NULL,
                CASE MOD(user_no - 1, 5)
                    WHEN 0 THEN 1 + MOD(user_no, 6)
                    WHEN 1 THEN 7
                    WHEN 2 THEN 8
                    WHEN 3 THEN 9
                    ELSE 10
                END
            ),
            IF(
                user_no > 990,
                NULL,
                CASE MOD(user_no - 1, 5)
                    WHEN 0 THEN (2 * (1 + MOD(user_no, 6)) - 1) + MOD(user_no, 2)
                    WHEN 1 THEN 13
                    WHEN 2 THEN 14
                    WHEN 3 THEN 15
                    ELSE 16
                END
            ),
            IF(user_no <= 990, 2018 + MOD(user_no, 9), NULL),
            IF(user_no <= 990, seed_time_value + INTERVAL 30 MINUTE, NULL),
            seed_time_value,
            seed_time_value
        );

        -- Gan hai so thich khac nhau cho profile da hoan tat de demo loc/goi y sau nay.
        IF user_no <= 990 THEN
            INSERT INTO `user_interests` (`user_id`, `interest_id`) VALUES
                (current_user_id, 1 + MOD(user_no - 1, 16)),
                (current_user_id, 1 + MOD(user_no + 4, 16));
        END IF;

        SET user_no = user_no + 1;
    END WHILE;

    -- Social-only va linked-provider de test cac nhanh xac thuc.
    INSERT INTO `user_auth_providers`
        (`user_id`, `provider`, `provider_user_id`, `provider_email`, `provider_email_verified`)
    VALUES
        (3, 'GOOGLE', 'demo-google-0003', 'demo.user0003@example.test', 1),
        (4, 'FACEBOOK', 'demo-facebook-0004', 'demo.user0004@example.test', 1),
        (5, 'GOOGLE', 'demo-google-0005', 'demo.user0005@example.test', 1),
        (5, 'FACEBOOK', 'demo-facebook-0005', 'demo.user0005@example.test', 1);

    SET user_no = 10;
    WHILE user_no <= 200 DO
        INSERT IGNORE INTO `user_auth_providers`
            (`user_id`, `provider`, `provider_user_id`, `provider_email`, `provider_email_verified`)
        VALUES (
            user_no,
            IF(MOD(user_no, 2) = 0, 'GOOGLE', 'FACEBOOK'),
            CONCAT(IF(MOD(user_no, 2) = 0, 'demo-google-', 'demo-facebook-'), LPAD(user_no, 4, '0')),
            CONCAT('demo.user', LPAD(user_no, 4, '0'), '@example.test'),
            1
        );
        SET user_no = user_no + 1;
    END WHILE;

    -- Tao dung 1.000 bai viet; moi bai co tu 1 den 4 anh.
    WHILE post_no <= 1000 DO
        SET current_author_id = 2 + MOD(post_no * 37, 989);
        SET seed_time_value = TIMESTAMP('2026-08-08 10:00:00') - INTERVAL MOD(post_no * 17, 180) DAY
            - INTERVAL MOD(post_no * 43, 86400) SECOND;
        SET current_status = CASE
            WHEN MOD(post_no, 20) = 0 THEN 'DELETED'
            WHEN MOD(post_no, 20) = 1 THEN 'HIDDEN'
            ELSE 'PUBLISHED'
        END;

        INSERT INTO `posts` (
            `author_id`, `content`, `status`, `is_edited`, `published_at`,
            `hidden_by`, `hidden_at`, `hidden_reason`, `deleted_at`,
            `created_at`, `updated_at`, `location_id`
        ) VALUES (
            current_author_id,
            IF(
                MOD(post_no, 25) = 0,
                NULL,
                CASE MOD(post_no, 10)
                    WHEN 0 THEN CONCAT('Chia sẻ tài liệu ôn tập hữu ích cho kỳ thi sắp tới. Bài số ', post_no, '.')
                    WHEN 1 THEN CONCAT('Một góc học tập yên tĩnh dành cho sinh viên hôm nay. Bài số ', post_no, '.')
                    WHEN 2 THEN CONCAT('Nhật ký làm đồ án: thêm một ngày sửa lỗi và học được nhiều điều mới. #', post_no)
                    WHEN 3 THEN CONCAT('Có bạn nào đang tìm nhóm học Spring Boot và React không? Bài số ', post_no, '.')
                    WHEN 4 THEN CONCAT('Gợi ý địa điểm ăn uống giá sinh viên, không gian thoải mái. Bài số ', post_no, '.')
                    WHEN 5 THEN CONCAT('Khoảnh khắc đáng nhớ trong hoạt động tình nguyện cuối tuần. Bài số ', post_no, '.')
                    WHEN 6 THEN CONCAT('Kinh nghiệm chuẩn bị CV và phỏng vấn thực tập cho sinh viên. Bài số ', post_no, '.')
                    WHEN 7 THEN CONCAT('Một buổi chiều thể thao cùng câu lạc bộ của trường. Bài số ', post_no, '.')
                    WHEN 8 THEN CONCAT('Ảnh chụp quanh thành phố sau giờ học. Bài số ', post_no, '.')
                    ELSE CONCAT('Cùng trao đổi cách quản lý thời gian học tập hiệu quả. Bài số ', post_no, '.')
                END
            ),
            current_status,
            IF(MOD(post_no, 9) = 0, 1, 0),
            seed_time_value,
            IF(current_status = 'HIDDEN', 1, NULL),
            IF(current_status = 'HIDDEN', seed_time_value + INTERVAL 2 HOUR, NULL),
            IF(current_status = 'HIDDEN', 'Nội dung demo được ẩn để kiểm tra màn hình quản trị', NULL),
            IF(current_status = 'DELETED', seed_time_value + INTERVAL 3 HOUR, NULL),
            seed_time_value,
            IF(MOD(post_no, 9) = 0, seed_time_value + INTERVAL 10 MINUTE, seed_time_value),
            IF(MOD(post_no, 3) = 0, 1 + MOD(post_no, 12), NULL)
        );

        SET current_post_id = LAST_INSERT_ID();
        SET media_no = 0;
        WHILE media_no < 1 + MOD(post_no, 4) DO
            INSERT INTO `post_media` (
                `post_id`, `media_url`, `storage_public_id`, `media_type`, `mime_type`,
                `file_size_bytes`, `width_px`, `height_px`, `duration_seconds`,
                `thumbnail_url`, `display_order`, `created_at`
            ) VALUES (
                current_post_id,
                CONCAT('https://picsum.photos/seed/unishare-post-', LPAD(post_no, 4, '0'), '-', media_no, '/1200/800'),
                CONCAT('demo/posts/post-', LPAD(post_no, 4, '0'), '-image-', media_no),
                'IMAGE',
                'image/jpeg',
                180000 + MOD(post_no * 7919 + media_no * 3571, 2200000),
                1200,
                800,
                NULL,
                NULL,
                media_no,
                seed_time_value
            );
            SET media_no = media_no + 1;
        END WHILE;

        IF MOD(post_no, 5) <> 0 THEN
            INSERT INTO `post_hashtags` (`post_id`, `hashtag_id`, `created_at`)
            VALUES (current_post_id, 1 + MOD(post_no, 12), seed_time_value);
        END IF;

        SET post_no = post_no + 1;
    END WHILE;

    -- Follow phuc vu Following Feed va thong ke profile.
    SET relation_no = 1;
    WHILE relation_no <= 6000 DO
        SET current_user_id = 2 + MOD(FLOOR((relation_no - 1) / 6), 989);
        SET current_target_id = 2 + MOD(
            current_user_id - 2 + (1 + MOD(relation_no - 1, 6)) * 37,
            989
        );
        IF current_user_id <> current_target_id THEN
            INSERT IGNORE INTO `follows` (`follower_id`, `following_id`, `created_at`)
            VALUES (current_user_id, current_target_id, TIMESTAMP('2026-03-01 09:00:00') + INTERVAL MOD(relation_no, 150) DAY);
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Like chi gan vao bai PUBLISHED va khong tu like bai cua minh.
    SET relation_no = 1;
    WHILE relation_no <= 12000 DO
        SET current_user_id = 2 + MOD(relation_no * 23, 989);
        SET current_post_id = 1 + MOD(relation_no * 47, 1000);
        SELECT `status`, `author_id`, `published_at`
        INTO seed_post_status, seed_post_author_id, seed_published_at
        FROM `posts`
        WHERE `id` = current_post_id;
        IF seed_post_status = 'PUBLISHED' AND seed_post_author_id <> current_user_id THEN
            INSERT IGNORE INTO `post_likes` (`user_id`, `post_id`, `created_at`)
            VALUES (current_user_id, current_post_id, seed_published_at + INTERVAL 1 DAY);
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Comment gom ca comment thuong va comment da xoa de test state UI.
    SET relation_no = 1;
    WHILE relation_no <= 3000 DO
        SET current_user_id = 2 + MOD(relation_no * 29, 989);
        SET current_post_id = 1 + MOD(relation_no * 41, 1000);
        SELECT `status`, `published_at`
        INTO seed_post_status, seed_published_at
        FROM `posts`
        WHERE `id` = current_post_id;
        IF seed_post_status = 'PUBLISHED' THEN
            INSERT INTO `comments` (
                `post_id`, `user_id`, `parent_comment_id`, `content`, `status`,
                `deleted_at`, `created_at`, `updated_at`
            ) VALUES (
                current_post_id,
                current_user_id,
                NULL,
                CASE MOD(relation_no, 5)
                    WHEN 0 THEN 'Cảm ơn bạn đã chia sẻ thông tin hữu ích!'
                    WHEN 1 THEN 'Mình cũng đang quan tâm chủ đề này.'
                    WHEN 2 THEN 'Hình ảnh đẹp và nội dung rất gần gũi.'
                    WHEN 3 THEN 'Bạn có thể chia sẻ thêm tài liệu không?'
                    ELSE 'Chúc bạn có một ngày học tập hiệu quả.'
                END,
                IF(MOD(relation_no, 20) = 0, 'DELETED', 'PUBLISHED'),
                IF(MOD(relation_no, 20) = 0, seed_published_at + INTERVAL 2 DAY, NULL),
                seed_published_at + INTERVAL 1 DAY,
                seed_published_at + INTERVAL 1 DAY
            );
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    SET relation_no = 1;
    WHILE relation_no <= 4000 DO
        SET current_user_id = 2 + MOD(relation_no * 19, 989);
        SET current_post_id = 1 + MOD(relation_no * 53, 1000);
        INSERT IGNORE INTO `saved_posts` (`user_id`, `post_id`, `created_at`)
        SELECT current_user_id, p.id, p.published_at + INTERVAL 3 DAY
        FROM `posts` p
        WHERE p.id = current_post_id AND p.status = 'PUBLISHED';
        SET relation_no = relation_no + 1;
    END WHILE;

    SET relation_no = 1;
    WHILE relation_no <= 3000 DO
        SET current_user_id = 2 + MOD(relation_no * 11, 989);
        SET current_post_id = 1 + MOD(relation_no * 59, 1000);
        SELECT `status`, `author_id`, `published_at`
        INTO seed_post_status, seed_post_author_id, seed_published_at
        FROM `posts`
        WHERE `id` = current_post_id;
        IF seed_post_status = 'PUBLISHED' AND seed_post_author_id <> current_user_id THEN
            INSERT IGNORE INTO `post_reposts` (`user_id`, `post_id`, `created_at`)
            VALUES (current_user_id, current_post_id, seed_published_at + INTERVAL 4 DAY);
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Quan he block/restrict co huong de test an profile, post va interaction.
    SET relation_no = 1;
    WHILE relation_no <= 30 DO
        INSERT IGNORE INTO `user_blocks` (`blocker_id`, `blocked_id`, `created_at`)
        VALUES (1 + relation_no, 101 + relation_no, TIMESTAMP('2026-07-01 08:00:00') + INTERVAL relation_no DAY);
        INSERT IGNORE INTO `user_restrictions` (`restrictor_id`, `restricted_id`, `created_at`)
        VALUES (201 + relation_no, 301 + relation_no, TIMESTAMP('2026-07-01 09:00:00') + INTERVAL relation_no DAY);
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Moderation Case phu ca OPEN, khong vi pham va da xu ly co hanh dong.
    SET relation_no = 1;
    WHILE relation_no <= 30 DO
        SET current_post_id = relation_no * 20 + 2;
        SET seed_time_value = TIMESTAMP('2026-07-01 10:00:00') + INTERVAL relation_no DAY;
        INSERT INTO `moderation_cases` (
            `post_id`, `status`, `report_count`, `resolved_by`, `resolution_note`,
            `first_reported_at`, `latest_reported_at`, `resolved_at`, `created_at`, `updated_at`
        ) VALUES (
            current_post_id,
            CASE MOD(relation_no, 3)
                WHEN 0 THEN 'OPEN'
                WHEN 1 THEN 'RESOLVED_NO_VIOLATION'
                ELSE 'RESOLVED_ACTION_TAKEN'
            END,
            1,
            IF(MOD(relation_no, 3) = 0, NULL, 1),
            IF(MOD(relation_no, 3) = 0, NULL, 'Ghi chú xử lý moderation dành cho dữ liệu demo'),
            seed_time_value,
            seed_time_value,
            IF(MOD(relation_no, 3) = 0, NULL, seed_time_value + INTERVAL 1 DAY),
            seed_time_value,
            seed_time_value
        );
        SET current_case_id = LAST_INSERT_ID();
        SET current_user_id = 700 + relation_no;

        INSERT INTO `reports` (
            `reporter_id`, `post_id`, `moderation_case_id`, `reason`, `description`,
            `status`, `resolved_by`, `resolved_at`, `resolution_note`,
            `post_content_snapshot`, `post_media_snapshot`, `created_at`, `updated_at`
        )
        SELECT
            current_user_id,
            p.id,
            current_case_id,
            CASE MOD(relation_no, 7)
                WHEN 0 THEN 'SPAM'
                WHEN 1 THEN 'HARASSMENT'
                WHEN 2 THEN 'HARMFUL_CONTENT'
                WHEN 3 THEN 'VIOLENCE'
                WHEN 4 THEN 'MISINFORMATION'
                WHEN 5 THEN 'INAPPROPRIATE'
                ELSE 'OTHER'
            END,
            'Báo cáo demo để kiểm tra luồng quản trị nội dung.',
            CASE MOD(relation_no, 3)
                WHEN 0 THEN 'PENDING'
                WHEN 1 THEN 'REJECTED'
                ELSE 'RESOLVED'
            END,
            IF(MOD(relation_no, 3) = 0, NULL, 1),
            IF(MOD(relation_no, 3) = 0, NULL, seed_time_value + INTERVAL 1 DAY),
            IF(MOD(relation_no, 3) = 0, NULL, 'Kết quả xử lý report demo'),
            p.content,
            JSON_ARRAY(JSON_OBJECT(
                'mediaUrl', CONCAT('https://picsum.photos/seed/unishare-post-', LPAD(p.id, 4, '0'), '-0/1200/800'),
                'mediaType', 'IMAGE'
            )),
            seed_time_value,
            seed_time_value
        FROM `posts` p
        WHERE p.id = current_post_id;

        SET relation_no = relation_no + 1;
    END WHILE;

    -- Lich su va notification tao du lieu cho dashboard/notification center.
    INSERT INTO `account_status_histories`
        (`user_id`, `old_status`, `new_status`, `changed_by`, `reason`, `created_at`)
    SELECT
        u.id, 'ACTIVE', 'BLOCKED', 1,
        'Khóa tài khoản demo để kiểm tra lịch sử quản trị',
        u.blocked_at
    FROM `users` u
    WHERE u.status = 'BLOCKED';

    INSERT INTO `admin_actions`
        (`admin_id`, `action_type`, `target_type`, `target_id`, `note`, `old_data`, `new_data`, `created_at`)
    SELECT
        1, 'BLOCK_USER', 'USER', u.id,
        'Thao tác quản trị demo',
        JSON_OBJECT('status', 'ACTIVE'),
        JSON_OBJECT('status', 'BLOCKED'),
        u.blocked_at
    FROM `users` u
    WHERE u.status = 'BLOCKED';

    SET relation_no = 1;
    WHILE relation_no <= 300 DO
        INSERT INTO `notifications` (
            `recipient_id`, `actor_id`, `type`, `post_id`, `comment_id`,
            `report_id`, `read_at`, `deleted_at`, `created_at`, `updated_at`
        ) VALUES (
            2 + MOD(relation_no, 100),
            102 + MOD(relation_no * 7, 500),
            CASE MOD(relation_no, 5)
                WHEN 0 THEN 'FOLLOW'
                WHEN 1 THEN 'POST_LIKE'
                WHEN 2 THEN 'POST_COMMENT'
                WHEN 3 THEN 'POST_REPOST'
                ELSE 'COMMENT_REPLY'
            END,
            IF(MOD(relation_no, 5) = 0, NULL, 2 + MOD(relation_no * 13, 998)),
            NULL,
            NULL,
            IF(MOD(relation_no, 3) = 0, TIMESTAMP('2026-08-01 08:00:00') + INTERVAL relation_no MINUTE, NULL),
            NULL,
            TIMESTAMP('2026-08-01 07:00:00') + INTERVAL relation_no MINUTE,
            TIMESTAMP('2026-08-01 07:00:00') + INTERVAL relation_no MINUTE
        );
        SET relation_no = relation_no + 1;
    END WHILE;

    -- 50 hoi thoai, moi hoi thoai co hai tin nhan de man hinh Inbox co du lieu.
    SET conversation_no = 1;
    WHILE conversation_no <= 50 DO
        SET current_target_id = 2 + conversation_no;
        SET seed_time_value = TIMESTAMP('2026-08-07 08:00:00') + INTERVAL conversation_no MINUTE;

        INSERT INTO `conversations`
            (`participant_low_id`, `participant_high_id`, `created_at`, `updated_at`)
        VALUES (2, current_target_id, seed_time_value, seed_time_value);
        SET current_conversation_id = LAST_INSERT_ID();

        INSERT INTO `conversation_members`
            (`conversation_id`, `user_id`, `created_at`, `updated_at`)
        VALUES
            (current_conversation_id, 2, seed_time_value, seed_time_value),
            (current_conversation_id, current_target_id, seed_time_value, seed_time_value);

        INSERT INTO `messages` (
            `conversation_id`, `sender_id`, `client_message_id`, `type`,
            `content`, `payload_fingerprint`, `created_at`, `updated_at`
        ) VALUES (
            current_conversation_id,
            current_target_id,
            UUID(),
            'TEXT',
            CONCAT('Xin chào, mình muốn trao đổi về bài viết số ', conversation_no, '.'),
            SHA2(CONCAT('demo-message-a-', conversation_no), 256),
            seed_time_value,
            seed_time_value
        );
        SET first_message_id = LAST_INSERT_ID();

        INSERT INTO `messages` (
            `conversation_id`, `sender_id`, `client_message_id`, `type`,
            `content`, `payload_fingerprint`, `created_at`, `updated_at`
        ) VALUES (
            current_conversation_id,
            2,
            UUID(),
            'TEXT',
            'Chào bạn, mình sẵn sàng trao đổi thêm nhé!',
            SHA2(CONCAT('demo-message-b-', conversation_no), 256),
            seed_time_value + INTERVAL 1 MINUTE,
            seed_time_value + INTERVAL 1 MINUTE
        );
        SET second_message_id = LAST_INSERT_ID();

        UPDATE `conversations`
        SET `last_message_id` = second_message_id,
            `last_message_at` = seed_time_value + INTERVAL 1 MINUTE,
            `updated_at` = seed_time_value + INTERVAL 1 MINUTE
        WHERE `id` = current_conversation_id;

        UPDATE `conversation_members`
        SET `last_read_message_id` = IF(`user_id` = 2, first_message_id, second_message_id),
            `last_read_at` = seed_time_value + INTERVAL 1 MINUTE
        WHERE `conversation_id` = current_conversation_id;

        SET conversation_no = conversation_no + 1;
    END WHILE;

    -- Du lieu activity phuc vu dashboard analytics.
    SET user_no = 1;
    WHILE user_no <= 500 DO
        INSERT INTO `user_daily_activities`
            (`user_id`, `activity_date`, `first_active_at`, `last_active_at`, `activity_count`)
        VALUES (
            user_no,
            DATE('2026-08-01') + INTERVAL MOD(user_no, 7) DAY,
            TIMESTAMP('2026-08-01 07:00:00') + INTERVAL MOD(user_no, 7) DAY,
            TIMESTAMP('2026-08-01 09:00:00') + INTERVAL MOD(user_no, 7) DAY,
            1 + MOD(user_no, 20)
        );
        SET user_no = user_no + 1;
    END WHILE;

    COMMIT;
END$$
DELIMITER ;

CALL `seed_website_cases`();
DROP PROCEDURE `seed_website_cases`;

-- Ket qua mong doi: 1.000 users, 1.000 profiles, 1.000 posts va 2.500 post media.
SELECT
    (SELECT COUNT(*) FROM `users`) AS users_count,
    (SELECT COUNT(*) FROM `user_profiles`) AS profiles_count,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `username` IS NOT NULL) AS completed_usernames,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `profile_completed_at` IS NULL) AS onboarding_profiles,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `school_id` IS NOT NULL) AS academic_profiles,
    (SELECT COUNT(*) FROM `user_interests`) AS user_interests_count,
    (SELECT COUNT(*) FROM `posts`) AS posts_count,
    (SELECT COUNT(*) FROM `post_media`) AS post_media_count;

-- Moi dong vi pham phai tra ve 0; giu verify cung seed de thu muc database gon hon.
SELECT 'invalid_demo_counts' AS check_name, COUNT(*) AS violations
FROM (SELECT 1) AS expected
WHERE (SELECT COUNT(*) FROM `users`) <> 1000
   OR (SELECT COUNT(*) FROM `user_profiles`) <> 1000
   OR (SELECT COUNT(*) FROM `posts`) <> 1000
   OR (SELECT COUNT(*) FROM `user_profiles` WHERE `profile_completed_at` IS NULL) <> 10
UNION ALL
SELECT 'invalid_academic_hierarchy', COUNT(*)
FROM `user_profiles` profile
LEFT JOIN `faculties` faculty ON faculty.id = profile.faculty_id
LEFT JOIN `majors` major ON major.id = profile.major_id
WHERE profile.school_id IS NOT NULL
  AND (faculty.school_id <> profile.school_id OR major.faculty_id <> profile.faculty_id)
UNION ALL
SELECT 'counter_mismatch', COUNT(*)
FROM `posts` post
WHERE post.like_count <> (SELECT COUNT(*) FROM `post_likes` item WHERE item.post_id = post.id)
   OR post.comment_count <> (SELECT COUNT(*) FROM `comments` item WHERE item.post_id = post.id AND item.status <> 'DELETED')
   OR post.repost_count <> (SELECT COUNT(*) FROM `post_reposts` item WHERE item.post_id = post.id);

SELECT `status`, COUNT(*) AS total
FROM `posts`
GROUP BY `status`
ORDER BY `status`;
