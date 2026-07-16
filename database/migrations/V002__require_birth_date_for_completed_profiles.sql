-- =============================================================================
-- Mục đích: bắt buộc hồ sơ đã hoàn tất phải có ngày sinh.
-- Phạm vi: MySQL 8.0+, áp dụng cho database đã được tạo từ schema trước nghiệp vụ đủ 18 tuổi.
-- =============================================================================

-- Kiểm tra dữ liệu trước khi migrate. Nếu truy vấn trả về bản ghi, cần bổ sung ngày sinh thật
-- và xác minh người dùng đủ 18 tuổi trước khi chạy lệnh ALTER TABLE bên dưới.
SELECT user_id, display_name, profile_completed_at
FROM user_profiles
WHERE profile_completed_at IS NOT NULL
  AND date_of_birth IS NULL;

-- Không tự sinh ngày sinh giả. MySQL sẽ từ chối thêm CHECK nếu vẫn còn dữ liệu vi phạm.
-- Constraint cũ về display_name được giữ lại; constraint mới bổ sung điều kiện date_of_birth.
ALTER TABLE user_profiles
    ADD CONSTRAINT chk_user_profiles_completion_requires_birth_date CHECK (
        profile_completed_at IS NULL OR date_of_birth IS NOT NULL
    );

-- Tuổi phụ thuộc ngày xử lý nên không dùng CHECK cố định trong database.
-- Backend tiếp tục chịu trách nhiệm từ chối ngày tương lai và người dùng chưa đủ 18 tuổi.
