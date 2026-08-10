-- Kiem tra bo du lieu local/test sau khi chay seed_1000_website_cases.sql.
USE `student_social_network`;
SET NAMES utf8mb4;

SELECT
    (SELECT COUNT(*) FROM `users`) AS users_count,
    (SELECT COUNT(*) FROM `user_profiles`) AS profiles_count,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `username` IS NOT NULL) AS completed_usernames,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `profile_completed_at` IS NULL) AS onboarding_profiles,
    (SELECT COUNT(*) FROM `posts`) AS posts_count,
    (SELECT COUNT(*) FROM `post_media`) AS post_media_count,
    (SELECT COUNT(*) FROM `follows`) AS follows_count,
    (SELECT COUNT(*) FROM `post_likes`) AS likes_count,
    (SELECT COUNT(*) FROM `comments`) AS comments_count,
    (SELECT COUNT(*) FROM `saved_posts`) AS saves_count,
    (SELECT COUNT(*) FROM `post_reposts`) AS reposts_count;

SELECT 'invalid_username' AS check_name, COUNT(*) AS violations
FROM `user_profiles`
WHERE `username` IS NOT NULL
  AND (
      CHAR_LENGTH(`username`) NOT BETWEEN 3 AND 30
      OR `username` <> LOWER(TRIM(`username`))
      OR `username` REGEXP '[^a-z0-9._]'
  )
UNION ALL
SELECT 'duplicate_username', COUNT(*)
FROM (
    SELECT `username`
    FROM `user_profiles`
    WHERE `username` IS NOT NULL
    GROUP BY `username`
    HAVING COUNT(*) > 1
) duplicate_usernames
UNION ALL
SELECT 'invalid_completed_profile', COUNT(*)
FROM `user_profiles`
WHERE `profile_completed_at` IS NOT NULL
  AND (`username` IS NULL OR `display_name` IS NULL OR `date_of_birth` IS NULL)
UNION ALL
SELECT 'invalid_post_state', COUNT(*)
FROM `posts`
WHERE (`status` = 'HIDDEN' AND (`hidden_by` IS NULL OR `hidden_at` IS NULL))
   OR (`status` = 'DELETED' AND `deleted_at` IS NULL)
UNION ALL
SELECT 'post_media_outside_1_4', COUNT(*)
FROM (
    SELECT p.`id`
    FROM `posts` p
    LEFT JOIN `post_media` pm ON pm.`post_id` = p.`id`
    GROUP BY p.`id`
    HAVING COUNT(pm.`id`) NOT BETWEEN 1 AND 4
) invalid_media
UNION ALL
SELECT 'interaction_on_non_published',
    (SELECT COUNT(*) FROM `post_likes` pl JOIN `posts` p ON p.`id` = pl.`post_id` WHERE p.`status` <> 'PUBLISHED')
  + (SELECT COUNT(*) FROM `comments` c JOIN `posts` p ON p.`id` = c.`post_id` WHERE p.`status` <> 'PUBLISHED')
  + (SELECT COUNT(*) FROM `saved_posts` sp JOIN `posts` p ON p.`id` = sp.`post_id` WHERE p.`status` <> 'PUBLISHED')
  + (SELECT COUNT(*) FROM `post_reposts` pr JOIN `posts` p ON p.`id` = pr.`post_id` WHERE p.`status` <> 'PUBLISHED')
UNION ALL
SELECT 'counter_mismatch', COUNT(*)
FROM (
    SELECT p.`id`
    FROM `posts` p
    LEFT JOIN (
        SELECT `post_id`, COUNT(*) AS total FROM `post_likes` GROUP BY `post_id`
    ) likes ON likes.`post_id` = p.`id`
    LEFT JOIN (
        SELECT `post_id`, COUNT(*) AS total
        FROM `comments`
        WHERE `status` = 'PUBLISHED'
        GROUP BY `post_id`
    ) comments ON comments.`post_id` = p.`id`
    LEFT JOIN (
        SELECT `post_id`, COUNT(*) AS total FROM `post_reposts` GROUP BY `post_id`
    ) reposts ON reposts.`post_id` = p.`id`
    WHERE p.`like_count` <> COALESCE(likes.total, 0)
       OR p.`comment_count` <> COALESCE(comments.total, 0)
       OR p.`repost_count` <> COALESCE(reposts.total, 0)
) invalid_counters
UNION ALL
SELECT 'auth_invariant', COUNT(*)
FROM `users` u
WHERE NOT (
    (u.`password_hash` IS NOT NULL AND u.`email` IS NOT NULL AND u.`email_verified_at` IS NOT NULL)
    OR EXISTS (
        SELECT 1 FROM `user_auth_providers` provider WHERE provider.`user_id` = u.`id`
    )
);

SELECT `status`, COUNT(*) AS total
FROM `posts`
GROUP BY `status`
ORDER BY `status`;
