-- Mở rộng reauthentication cho SET_PASSWORD; không tạo bảng/challenge framework mới.
ALTER TABLE `reauthentication_challenges`
    DROP CHECK `chk_reauth_scope`,
    ADD CONSTRAINT `chk_reauth_scope`
        CHECK (`scope` IN ('UNLINK_AUTH_METHOD', 'SET_PASSWORD'));

ALTER TABLE `auth_method_link_challenges`
    ADD COLUMN `otp_verified_at` datetime(6) NULL AFTER `otp_hash`;
