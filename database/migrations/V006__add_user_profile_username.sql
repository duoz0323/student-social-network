-- Thêm định danh public cho profile nhưng giữ NULL trong thời gian onboarding.
ALTER TABLE `user_profiles`
  ADD COLUMN `username` varchar(30) NULL AFTER `user_id`;

-- Profile legacy chưa có username phải quay lại onboarding; không tự sinh định danh thay người dùng.
UPDATE `user_profiles`
SET `profile_completed_at` = NULL
WHERE `username` IS NULL
  AND `profile_completed_at` IS NOT NULL;

ALTER TABLE `user_profiles`
  DROP CHECK `chk_user_profiles_completion_consistency`,
  ADD CONSTRAINT `uq_user_profiles_username` UNIQUE (`username`),
  ADD CONSTRAINT `chk_user_profiles_completion_consistency`
    CHECK ((`profile_completed_at` IS NULL)
      OR (`username` IS NOT NULL AND `display_name` IS NOT NULL AND `date_of_birth` IS NOT NULL));
