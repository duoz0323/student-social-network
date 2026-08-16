-- Hotfix: Managed Social Identity là thương hiệu nội dung nên không có ngày sinh.
-- User NORMAL vẫn bị trigger từ chối nếu hoàn tất hồ sơ mà thiếu date_of_birth.
START TRANSACTION;

ALTER TABLE `user_profiles`
  DROP CHECK `chk_user_profiles_completion_consistency`,
  DROP CHECK `chk_user_profiles_completion_requires_birth_date`,
  ADD CONSTRAINT `chk_user_profiles_completion_consistency` CHECK (
    `profile_completed_at` IS NULL OR (`username` IS NOT NULL AND `display_name` IS NOT NULL)
  );

COMMIT;

DROP TRIGGER IF EXISTS `trg_user_profiles_completion_birth_insert`;
DROP TRIGGER IF EXISTS `trg_user_profiles_completion_birth_update`;

DELIMITER $$
CREATE TRIGGER `trg_user_profiles_completion_birth_insert`
BEFORE INSERT ON `user_profiles`
FOR EACH ROW
BEGIN
  DECLARE owner_account_type varchar(16);
  IF NEW.profile_completed_at IS NOT NULL THEN
    SELECT account_type INTO owner_account_type FROM users WHERE id = NEW.user_id;
    IF owner_account_type IS NULL OR (owner_account_type <> 'MANAGED' AND NEW.date_of_birth IS NULL) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed normal profile requires date_of_birth';
    END IF;
  END IF;
END$$

CREATE TRIGGER `trg_user_profiles_completion_birth_update`
BEFORE UPDATE ON `user_profiles`
FOR EACH ROW
BEGIN
  DECLARE owner_account_type varchar(16);
  IF NEW.profile_completed_at IS NOT NULL THEN
    SELECT account_type INTO owner_account_type FROM users WHERE id = NEW.user_id;
    IF owner_account_type IS NULL OR (owner_account_type <> 'MANAGED' AND NEW.date_of_birth IS NULL) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Completed normal profile requires date_of_birth';
    END IF;
  END IF;
END$$
DELIMITER ;
