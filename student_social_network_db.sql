-- =============================================================================
-- TÊN CSDL: student_social_network_mvp
-- HỆ QUẢN TRỊ: MySQL 8.0+
-- PHẠM VI: MVP mạng xã hội tinh gọn dành cho sinh viên
-- GHI CHÚ: Các giới hạn như tối đa 4 ảnh/bài và trả lời bình luận tối đa 1 cấp
--          được kiểm tra tại Service Layer để bảo đảm thông báo lỗi nghiệp vụ rõ ràng.
-- =============================================================================

-- Tạo cơ sở dữ liệu với bộ ký tự utf8mb4 để lưu đầy đủ tiếng Việt và emoji.
CREATE DATABASE IF NOT EXISTS student_social_network_mvp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

-- Chọn cơ sở dữ liệu vừa tạo để thực thi các lệnh tiếp theo.
USE student_social_network_mvp;

-- Tắt kiểm tra khóa ngoại tạm thời để có thể chạy lại script nhiều lần.
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa các bảng theo thứ tự phụ thuộc ngược để tránh lỗi khóa ngoại.
DROP TABLE IF EXISTS admin_actions;
DROP TABLE IF EXISTS account_status_histories;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS saved_posts;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS post_hashtags;
DROP TABLE IF EXISTS hashtags;
DROP TABLE IF EXISTS post_media;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS users;

-- Bật lại kiểm tra khóa ngoại sau khi dọn cấu trúc cũ.
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1. NHÓM TÀI KHOẢN VÀ HỒ SƠ
-- =============================================================================

-- Bảng users lưu thông tin xác thực, vai trò và trạng thái tài khoản.
CREATE TABLE users (
    -- Khóa chính dạng số lớn, thuận lợi cho index và quan hệ khóa ngoại.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Email dùng để đăng ký/đăng nhập; luôn lưu dạng chữ thường tại Backend.
    email VARCHAR(255) NOT NULL,
    -- Username công khai và có tính duy nhất.
    username VARCHAR(50) NOT NULL,
    -- Mật khẩu đã băm một chiều bằng BCrypt hoặc Argon2.
    password_hash VARCHAR(255) NOT NULL,
    -- Vai trò hệ thống trong MVP chỉ gồm USER và ADMIN.
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    -- Trạng thái tài khoản trong MVP chỉ gồm ACTIVE và BLOCKED.
    status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    -- Thời điểm tài khoản bị khóa; NULL khi tài khoản đang hoạt động.
    blocked_at DATETIME(6) NULL,
    -- Lý do khóa tài khoản; chỉ có ý nghĩa khi status = BLOCKED.
    blocked_reason VARCHAR(500) NULL,
    -- Thời điểm tạo bản ghi.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Thời điểm cập nhật gần nhất.
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng users.
    CONSTRAINT pk_users PRIMARY KEY (id),
    -- Không cho phép hai tài khoản dùng cùng email.
    CONSTRAINT uq_users_email UNIQUE (email),
    -- Không cho phép hai tài khoản dùng cùng username.
    CONSTRAINT uq_users_username UNIQUE (username),
    -- Bảo đảm dữ liệu khóa tài khoản nhất quán với trạng thái.
    CONSTRAINT chk_users_blocked_data CHECK (
        (status = 'ACTIVE' AND blocked_at IS NULL)
        OR
        (status = 'BLOCKED' AND blocked_at IS NOT NULL)
    )
) ENGINE=InnoDB;

-- Tạo index phục vụ màn hình quản trị lọc tài khoản theo trạng thái.
CREATE INDEX idx_users_status_created_at
    ON users (status, created_at DESC);

-- Bảng user_profiles tách dữ liệu hồ sơ công khai khỏi dữ liệu xác thực.
CREATE TABLE user_profiles (
    -- Khóa chính đồng thời là khóa ngoại tới users để tạo quan hệ 1-1.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Tên hiển thị công khai của người dùng.
    display_name VARCHAR(100) NOT NULL,
    -- Đường dẫn ảnh đại diện lưu trên dịch vụ lưu trữ tệp.
    avatar_url VARCHAR(1000) NULL,
    -- Mã public_id của ảnh trên Cloudinary/S3 để hỗ trợ xóa tệp.
    avatar_public_id VARCHAR(255) NULL,
    -- Nội dung giới thiệu cá nhân.
    bio VARCHAR(500) NULL,
    -- Thời điểm tạo hồ sơ.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Thời điểm cập nhật hồ sơ gần nhất.
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    -- Khóa chính bảo đảm mỗi user chỉ có đúng một hồ sơ.
    CONSTRAINT pk_user_profiles PRIMARY KEY (user_id),
    -- Khóa ngoại liên kết hồ sơ với tài khoản.
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bảng refresh_tokens quản lý phiên đăng nhập và khả năng thu hồi token.
CREATE TABLE refresh_tokens (
    -- Khóa chính của phiên refresh token.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Tài khoản sở hữu phiên đăng nhập.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Chỉ lưu giá trị băm SHA-256 của refresh token, không lưu token thô.
    token_hash CHAR(64) NOT NULL,
    -- Mã định danh thiết bị hoặc phiên do Backend sinh.
    device_id VARCHAR(100) NULL,
    -- Thông tin thiết bị/trình duyệt để người dùng nhận biết phiên.
    device_info VARCHAR(500) NULL,
    -- Địa chỉ IP tại thời điểm tạo phiên.
    ip_address VARCHAR(45) NULL,
    -- Thời điểm token hết hạn.
    expires_at DATETIME(6) NOT NULL,
    -- Thời điểm token bị thu hồi; NULL nghĩa là còn hiệu lực về mặt thu hồi.
    revoked_at DATETIME(6) NULL,
    -- Thời điểm tạo token.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng refresh_tokens.
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    -- Mỗi giá trị băm token chỉ được tồn tại một lần.
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    -- Khóa ngoại liên kết token với người dùng.
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Thời điểm hết hạn phải lớn hơn thời điểm tạo.
    CONSTRAINT chk_refresh_tokens_expiry CHECK (expires_at > created_at),
    -- Thời điểm thu hồi nếu có không được trước thời điểm tạo.
    CONSTRAINT chk_refresh_tokens_revoked_at CHECK (
        revoked_at IS NULL OR revoked_at >= created_at
    )
) ENGINE=InnoDB;

-- Index phục vụ truy vấn danh sách token còn hiệu lực theo người dùng.
CREATE INDEX idx_refresh_tokens_user_expiry
    ON refresh_tokens (user_id, revoked_at, expires_at);

-- Bảng password_reset_tokens hỗ trợ chức năng khôi phục mật khẩu P2.
CREATE TABLE password_reset_tokens (
    -- Khóa chính của yêu cầu đặt lại mật khẩu.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Tài khoản yêu cầu đặt lại mật khẩu.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Giá trị băm của mã hoặc token đặt lại mật khẩu.
    token_hash CHAR(64) NOT NULL,
    -- Thời điểm token hết hạn.
    expires_at DATETIME(6) NOT NULL,
    -- Thời điểm token được sử dụng; NULL nghĩa là chưa sử dụng.
    used_at DATETIME(6) NULL,
    -- Thời điểm tạo token.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng password_reset_tokens.
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    -- Không cho phép trùng giá trị băm token.
    CONSTRAINT uq_password_reset_tokens_hash UNIQUE (token_hash),
    -- Khóa ngoại liên kết token đặt lại mật khẩu với người dùng.
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Thời điểm hết hạn phải lớn hơn thời điểm tạo.
    CONSTRAINT chk_password_reset_tokens_expiry CHECK (expires_at > created_at),
    -- Thời điểm sử dụng nếu có không được trước thời điểm tạo.
    CONSTRAINT chk_password_reset_tokens_used_at CHECK (
        used_at IS NULL OR used_at >= created_at
    )
) ENGINE=InnoDB;

-- Index hỗ trợ kiểm tra token chưa dùng và chưa hết hạn của một tài khoản.
CREATE INDEX idx_password_reset_tokens_user_state
    ON password_reset_tokens (user_id, used_at, expires_at);

-- =============================================================================
-- 2. NHÓM QUAN HỆ THEO DÕI
-- =============================================================================

-- Bảng follows biểu diễn quan hệ N-N tự tham chiếu giữa các người dùng.
CREATE TABLE follows (
    -- Người thực hiện hành động theo dõi.
    follower_id BIGINT UNSIGNED NOT NULL,
    -- Người được theo dõi.
    following_id BIGINT UNSIGNED NOT NULL,
    -- Thời điểm bắt đầu theo dõi.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính kép chống tạo trùng một quan hệ Follow.
    CONSTRAINT pk_follows PRIMARY KEY (follower_id, following_id),
    -- Khóa ngoại tới người theo dõi.
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Khóa ngoại tới người được theo dõi.
    CONSTRAINT fk_follows_following FOREIGN KEY (following_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Không cho phép người dùng tự theo dõi chính mình.
    CONSTRAINT chk_follows_not_self CHECK (follower_id <> following_id)
) ENGINE=InnoDB;

-- Index phục vụ truy vấn danh sách follower của một người dùng.
CREATE INDEX idx_follows_following_created_at
    ON follows (following_id, created_at DESC);

-- Index phục vụ truy vấn danh sách following theo thời gian.
CREATE INDEX idx_follows_follower_created_at
    ON follows (follower_id, created_at DESC);

-- =============================================================================
-- 3. NHÓM BÀI VIẾT, HÌNH ẢNH VÀ HASHTAG
-- =============================================================================

-- Bảng posts lưu bài viết gốc trong phạm vi MVP.
CREATE TABLE posts (
    -- Khóa chính của bài viết.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Tác giả sở hữu bài viết.
    author_id BIGINT UNSIGNED NOT NULL,
    -- Nội dung văn bản tối đa 500 ký tự.
    content VARCHAR(500) NULL,
    -- Trạng thái bài viết: đang hoạt động, bị Admin ẩn hoặc tác giả xóa mềm.
    status ENUM('PUBLISHED', 'HIDDEN', 'DELETED') NOT NULL DEFAULT 'PUBLISHED',
    -- Cờ cho biết bài viết đã từng được chỉnh sửa.
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    -- Bộ đếm Like được lưu dư thừa có kiểm soát để tối ưu Feed For You.
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    -- Bộ đếm bình luận hợp lệ để tối ưu Feed For You.
    comment_count INT UNSIGNED NOT NULL DEFAULT 0,
    -- Thời điểm xuất bản bài viết.
    published_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Admin thực hiện ẩn bài viết; NULL khi chưa bị ẩn.
    hidden_by BIGINT UNSIGNED NULL,
    -- Thời điểm bài viết bị ẩn.
    hidden_at DATETIME(6) NULL,
    -- Lý do ẩn bài viết.
    hidden_reason VARCHAR(500) NULL,
    -- Thời điểm tác giả xóa mềm bài viết.
    deleted_at DATETIME(6) NULL,
    -- Thời điểm tạo bản ghi.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Thời điểm cập nhật gần nhất.
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng posts.
    CONSTRAINT pk_posts PRIMARY KEY (id),
    -- Khóa ngoại liên kết bài viết với tác giả.
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    -- Khóa ngoại liên kết người ẩn bài với tài khoản Admin.
    CONSTRAINT fk_posts_hidden_by FOREIGN KEY (hidden_by)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE SET NULL,
    -- Bảo đảm dữ liệu HIDDEN đi kèm thời điểm và người xử lý.
    CONSTRAINT chk_posts_hidden_state CHECK (
        (status = 'HIDDEN' AND hidden_at IS NOT NULL AND hidden_by IS NOT NULL)
        OR
        (status <> 'HIDDEN')
    ),
    -- Bảo đảm dữ liệu DELETED đi kèm thời điểm xóa mềm.
    CONSTRAINT chk_posts_deleted_state CHECK (
        (status = 'DELETED' AND deleted_at IS NOT NULL)
        OR
        (status <> 'DELETED')
    )
) ENGINE=InnoDB;

-- Index quan trọng cho trang hồ sơ và Feed Following.
CREATE INDEX idx_posts_author_status_published
    ON posts (author_id, status, published_at DESC, id DESC);

-- Index quan trọng cho Feed For You và danh sách bài toàn hệ thống.
CREATE INDEX idx_posts_status_published
    ON posts (status, published_at DESC, id DESC);

-- Index hỗ trợ xếp hạng Feed cơ bản theo tương tác.
CREATE INDEX idx_posts_status_engagement
    ON posts (status, like_count DESC, comment_count DESC, published_at DESC);

-- Bảng post_media lưu metadata ảnh; tệp thật nằm trên Cloud Storage.
CREATE TABLE post_media (
    -- Khóa chính của ảnh bài viết.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Bài viết sở hữu ảnh.
    post_id BIGINT UNSIGNED NOT NULL,
    -- URL truy cập ảnh.
    media_url VARCHAR(1000) NOT NULL,
    -- Public ID hoặc object key để hỗ trợ quản lý/xóa tệp.
    storage_public_id VARCHAR(255) NOT NULL,
    -- MIME type thực tế đã được Backend kiểm tra.
    mime_type ENUM('image/jpeg', 'image/png', 'image/webp') NOT NULL,
    -- Kích thước tệp theo byte.
    file_size_bytes BIGINT UNSIGNED NOT NULL,
    -- Chiều rộng ảnh theo pixel.
    width_px INT UNSIGNED NULL,
    -- Chiều cao ảnh theo pixel.
    height_px INT UNSIGNED NULL,
    -- Thứ tự hiển thị ảnh trong bài, bắt đầu từ 0.
    display_order TINYINT UNSIGNED NOT NULL,
    -- Thời điểm tạo metadata ảnh.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng post_media.
    CONSTRAINT pk_post_media PRIMARY KEY (id),
    -- Mỗi object trên dịch vụ lưu trữ chỉ được tham chiếu một lần.
    CONSTRAINT uq_post_media_storage_public_id UNIQUE (storage_public_id),
    -- Không cho phép trùng vị trí ảnh trong cùng một bài.
    CONSTRAINT uq_post_media_post_order UNIQUE (post_id, display_order),
    -- Khóa ngoại liên kết ảnh với bài viết.
    CONSTRAINT fk_post_media_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Thứ tự hiển thị trong MVP chỉ từ 0 đến 3 vì tối đa 4 ảnh.
    CONSTRAINT chk_post_media_display_order CHECK (display_order <= 3),
    -- Kích thước tệp phải lớn hơn 0.
    CONSTRAINT chk_post_media_file_size CHECK (file_size_bytes > 0)
) ENGINE=InnoDB;

-- Index hỗ trợ tải danh sách ảnh theo bài và đúng thứ tự.
CREATE INDEX idx_post_media_post_order
    ON post_media (post_id, display_order);

-- Bảng hashtags lưu hashtag chuẩn hóa dùng chung cho nhiều bài viết.
CREATE TABLE hashtags (
    -- Khóa chính của hashtag.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Tên chuẩn hóa dạng chữ thường và không chứa ký tự #.
    normalized_name VARCHAR(100) NOT NULL,
    -- Tên hiển thị; trong MVP có thể giống normalized_name.
    display_name VARCHAR(100) NOT NULL,
    -- Bộ đếm số bài đang gắn hashtag để tối ưu truy vấn.
    post_count INT UNSIGNED NOT NULL DEFAULT 0,
    -- Thời điểm tạo hashtag.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Thời điểm cập nhật hashtag gần nhất.
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng hashtags.
    CONSTRAINT pk_hashtags PRIMARY KEY (id),
    -- Không cho phép trùng hashtag sau chuẩn hóa.
    CONSTRAINT uq_hashtags_normalized_name UNIQUE (normalized_name),
    -- Tên hashtag phải có ít nhất một ký tự.
    CONSTRAINT chk_hashtags_not_empty CHECK (CHAR_LENGTH(normalized_name) > 0)
) ENGINE=InnoDB;

-- Bảng post_hashtags là bảng trung gian cho quan hệ N-N giữa posts và hashtags.
CREATE TABLE post_hashtags (
    -- Bài viết được gắn hashtag.
    post_id BIGINT UNSIGNED NOT NULL,
    -- Hashtag được gắn vào bài viết.
    hashtag_id BIGINT UNSIGNED NOT NULL,
    -- Thời điểm tạo quan hệ.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính kép chống gắn trùng hashtag trong cùng bài.
    CONSTRAINT pk_post_hashtags PRIMARY KEY (post_id, hashtag_id),
    -- Khóa ngoại liên kết tới bài viết.
    CONSTRAINT fk_post_hashtags_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Khóa ngoại liên kết tới hashtag.
    CONSTRAINT fk_post_hashtags_hashtag FOREIGN KEY (hashtag_id)
        REFERENCES hashtags (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Index hỗ trợ truy vấn danh sách bài theo hashtag.
CREATE INDEX idx_post_hashtags_hashtag_post
    ON post_hashtags (hashtag_id, post_id);

-- =============================================================================
-- 4. NHÓM TƯƠNG TÁC
-- =============================================================================

-- Bảng post_likes là bảng trung gian N-N giữa users và posts.
CREATE TABLE post_likes (
    -- Người dùng thực hiện Like.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Bài viết được Like.
    post_id BIGINT UNSIGNED NOT NULL,
    -- Thời điểm Like.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính kép bảo đảm mỗi người chỉ Like một bài tối đa một lần.
    CONSTRAINT pk_post_likes PRIMARY KEY (user_id, post_id),
    -- Khóa ngoại liên kết Like với người dùng.
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Khóa ngoại liên kết Like với bài viết.
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index hỗ trợ đếm và liệt kê người Like theo bài.
CREATE INDEX idx_post_likes_post_created
    ON post_likes (post_id, created_at DESC);

-- Bảng comments lưu bình luận và trả lời bình luận tối đa một cấp.
CREATE TABLE comments (
    -- Khóa chính của bình luận.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Bài viết chứa bình luận.
    post_id BIGINT UNSIGNED NOT NULL,
    -- Người tạo bình luận.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Bình luận cha; NULL nghĩa là bình luận cấp gốc.
    parent_comment_id BIGINT UNSIGNED NULL,
    -- Nội dung bình luận.
    content VARCHAR(1000) NOT NULL,
    -- Trạng thái phục vụ xóa mềm bình luận.
    status ENUM('PUBLISHED', 'DELETED') NOT NULL DEFAULT 'PUBLISHED',
    -- Thời điểm xóa mềm.
    deleted_at DATETIME(6) NULL,
    -- Thời điểm tạo bình luận.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Thời điểm cập nhật gần nhất.
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng comments.
    CONSTRAINT pk_comments PRIMARY KEY (id),
    -- Khóa ngoại liên kết bình luận với bài viết.
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Khóa ngoại liên kết bình luận với tác giả.
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    -- Khóa ngoại tự tham chiếu để hỗ trợ trả lời một cấp.
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id)
        REFERENCES comments (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Nội dung bình luận không được rỗng.
    CONSTRAINT chk_comments_content_not_empty CHECK (
        CHAR_LENGTH(TRIM(content)) > 0
    ),
    -- Bình luận DELETED phải có thời điểm xóa mềm.
    CONSTRAINT chk_comments_deleted_state CHECK (
        (status = 'DELETED' AND deleted_at IS NOT NULL)
        OR
        (status = 'PUBLISHED' AND deleted_at IS NULL)
    )
) ENGINE=InnoDB;

-- Index hỗ trợ tải bình luận gốc của một bài theo thời gian.
CREATE INDEX idx_comments_post_parent_created
    ON comments (post_id, parent_comment_id, status, created_at ASC);

-- Index hỗ trợ người dùng quản lý các bình luận của mình.
CREATE INDEX idx_comments_user_created
    ON comments (user_id, created_at DESC);

-- Bảng saved_posts là bảng trung gian N-N giữa users và posts.
CREATE TABLE saved_posts (
    -- Người dùng lưu bài viết.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Bài viết được lưu.
    post_id BIGINT UNSIGNED NOT NULL,
    -- Thời điểm lưu bài.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính kép bảo đảm mỗi người chỉ lưu một bài tối đa một lần.
    CONSTRAINT pk_saved_posts PRIMARY KEY (user_id, post_id),
    -- Khóa ngoại liên kết bản ghi lưu với người dùng.
    CONSTRAINT fk_saved_posts_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    -- Khóa ngoại liên kết bản ghi lưu với bài viết.
    CONSTRAINT fk_saved_posts_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index hỗ trợ truy vấn danh sách người đã lưu một bài nếu Admin cần thống kê.
CREATE INDEX idx_saved_posts_post_created
    ON saved_posts (post_id, created_at DESC);

-- =============================================================================
-- 5. NHÓM BÁO CÁO VÀ QUẢN TRỊ
-- =============================================================================

-- Bảng reports lưu báo cáo bài viết trong phạm vi MVP.
CREATE TABLE reports (
    -- Khóa chính của báo cáo.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Người gửi báo cáo.
    reporter_id BIGINT UNSIGNED NOT NULL,
    -- Bài viết bị báo cáo.
    post_id BIGINT UNSIGNED NOT NULL,
    -- Lý do báo cáo theo danh sách chuẩn hóa.
    reason ENUM(
        'SPAM',
        'HARASSMENT',
        'HARMFUL_CONTENT',
        'VIOLENCE',
        'MISINFORMATION',
        'INAPPROPRIATE',
        'OTHER'
    ) NOT NULL,
    -- Mô tả bổ sung do người báo cáo nhập.
    description VARCHAR(1000) NULL,
    -- Trạng thái xử lý báo cáo trong MVP.
    status ENUM('PENDING', 'RESOLVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    -- Admin xử lý báo cáo.
    resolved_by BIGINT UNSIGNED NULL,
    -- Thời điểm xử lý báo cáo.
    resolved_at DATETIME(6) NULL,
    -- Ghi chú nội bộ của Admin.
    resolution_note VARCHAR(1000) NULL,
    -- Ảnh chụp nội dung bài tại thời điểm báo cáo để bảo toàn bằng chứng cơ bản.
    post_content_snapshot VARCHAR(500) NULL,
    -- Ảnh chụp danh sách URL media dưới dạng JSON.
    post_media_snapshot JSON NULL,
    -- Khóa sinh tự động chỉ có giá trị khi báo cáo đang PENDING.
    pending_report_key VARCHAR(100) GENERATED ALWAYS AS (
        CASE
            WHEN status = 'PENDING'
            THEN CONCAT(reporter_id, ':', post_id)
            ELSE NULL
        END
    ) STORED,
    -- Thời điểm gửi báo cáo.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Thời điểm cập nhật báo cáo gần nhất.
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng reports.
    CONSTRAINT pk_reports PRIMARY KEY (id),
    -- Chống một người gửi nhiều báo cáo PENDING cho cùng một bài.
    CONSTRAINT uq_reports_pending_key UNIQUE (pending_report_key),
    -- Khóa ngoại liên kết báo cáo với người gửi.
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    -- Khóa ngoại liên kết báo cáo với bài viết.
    CONSTRAINT fk_reports_post FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    -- Khóa ngoại liên kết báo cáo với Admin xử lý.
    CONSTRAINT fk_reports_resolved_by FOREIGN KEY (resolved_by)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE SET NULL,
    -- Báo cáo đã xử lý phải có Admin và thời điểm xử lý.
    CONSTRAINT chk_reports_resolution_state CHECK (
        (status = 'PENDING' AND resolved_by IS NULL AND resolved_at IS NULL)
        OR
        (status IN ('RESOLVED', 'REJECTED')
            AND resolved_by IS NOT NULL
            AND resolved_at IS NOT NULL)
    )
) ENGINE=InnoDB;

-- Index phục vụ hàng đợi xử lý báo cáo theo trạng thái và thời gian.
CREATE INDEX idx_reports_status_created
    ON reports (status, created_at ASC, id ASC);

-- Index phục vụ xem các báo cáo của một bài viết.
CREATE INDEX idx_reports_post_status
    ON reports (post_id, status, created_at DESC);

-- Bảng account_status_histories lưu lịch sử khóa/mở khóa tài khoản.
CREATE TABLE account_status_histories (
    -- Khóa chính của bản ghi lịch sử.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Tài khoản bị thay đổi trạng thái.
    user_id BIGINT UNSIGNED NOT NULL,
    -- Trạng thái trước khi thay đổi.
    old_status ENUM('ACTIVE', 'BLOCKED') NOT NULL,
    -- Trạng thái sau khi thay đổi.
    new_status ENUM('ACTIVE', 'BLOCKED') NOT NULL,
    -- Admin thực hiện thay đổi.
    changed_by BIGINT UNSIGNED NOT NULL,
    -- Lý do thay đổi trạng thái.
    reason VARCHAR(500) NOT NULL,
    -- Thời điểm thay đổi trạng thái.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng account_status_histories.
    CONSTRAINT pk_account_status_histories PRIMARY KEY (id),
    -- Khóa ngoại tới tài khoản bị thay đổi.
    CONSTRAINT fk_account_status_histories_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    -- Khóa ngoại tới Admin thực hiện.
    CONSTRAINT fk_account_status_histories_changed_by FOREIGN KEY (changed_by)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    -- Trạng thái mới phải khác trạng thái cũ.
    CONSTRAINT chk_account_status_histories_changed CHECK (
        old_status <> new_status
    )
) ENGINE=InnoDB;

-- Index phục vụ xem lịch sử trạng thái theo tài khoản.
CREATE INDEX idx_account_status_histories_user_created
    ON account_status_histories (user_id, created_at DESC);

-- Bảng admin_actions lưu vết thao tác quản trị cơ bản trong MVP/P2.
CREATE TABLE admin_actions (
    -- Khóa chính của thao tác quản trị.
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- Admin thực hiện thao tác.
    admin_id BIGINT UNSIGNED NOT NULL,
    -- Loại hành động quản trị.
    action_type ENUM(
        'BLOCK_USER',
        'UNBLOCK_USER',
        'HIDE_POST',
        'RESTORE_POST',
        'RESOLVE_REPORT',
        'REJECT_REPORT'
    ) NOT NULL,
    -- Loại đối tượng bị tác động.
    target_type ENUM('USER', 'POST', 'REPORT') NOT NULL,
    -- ID đối tượng bị tác động; kiểm tra tồn tại được thực hiện trong transaction Service.
    target_id BIGINT UNSIGNED NOT NULL,
    -- Ghi chú hoặc lý do thao tác.
    note VARCHAR(1000) NULL,
    -- Dữ liệu trước thay đổi ở dạng JSON.
    old_data JSON NULL,
    -- Dữ liệu sau thay đổi ở dạng JSON.
    new_data JSON NULL,
    -- Thời điểm thực hiện thao tác.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- Khóa chính của bảng admin_actions.
    CONSTRAINT pk_admin_actions PRIMARY KEY (id),
    -- Khóa ngoại liên kết thao tác với tài khoản Admin.
    CONSTRAINT fk_admin_actions_admin FOREIGN KEY (admin_id)
        REFERENCES users (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Index phục vụ truy vết thao tác theo đối tượng.
CREATE INDEX idx_admin_actions_target_created
    ON admin_actions (target_type, target_id, created_at DESC);

-- Index phục vụ truy vết thao tác của một Admin.
CREATE INDEX idx_admin_actions_admin_created
    ON admin_actions (admin_id, created_at DESC);

-- =============================================================================
-- 6. TRIGGER BẢO ĐẢM DỮ LIỆU VÀ TỐI ƯU BỘ ĐẾM
-- =============================================================================

-- Đổi delimiter để khai báo trigger nhiều câu lệnh.
DELIMITER $$

-- Trigger kiểm tra người xử lý khóa/mở khóa phải có vai trò ADMIN.
CREATE TRIGGER trg_account_status_histories_validate_admin
BEFORE INSERT ON account_status_histories
FOR EACH ROW
BEGIN
    -- Khai báo biến lưu vai trò của người thực hiện.
    DECLARE v_role VARCHAR(10);

    -- Lấy vai trò của tài khoản thực hiện thay đổi.
    SELECT role INTO v_role
    FROM users
    WHERE id = NEW.changed_by;

    -- Từ chối nếu người thực hiện không phải ADMIN.
    IF v_role <> 'ADMIN' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Người thay đổi trạng thái tài khoản phải là ADMIN';
    END IF;
END$$

-- Trigger kiểm tra tài khoản hidden_by của bài viết phải có vai trò ADMIN.
CREATE TRIGGER trg_posts_validate_hidden_admin
BEFORE UPDATE ON posts
FOR EACH ROW
BEGIN
    -- Khai báo biến lưu vai trò Admin.
    DECLARE v_role VARCHAR(10);

    -- Chỉ kiểm tra khi bài được chuyển sang trạng thái HIDDEN.
    IF NEW.status = 'HIDDEN' THEN
        -- Lấy vai trò của tài khoản thực hiện ẩn bài.
        SELECT role INTO v_role
        FROM users
        WHERE id = NEW.hidden_by;

        -- Từ chối nếu người thực hiện không phải ADMIN.
        IF v_role <> 'ADMIN' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Chỉ ADMIN được phép ẩn bài viết';
        END IF;
    END IF;
END$$

-- Trigger tăng bộ đếm Like sau khi tạo Like thành công.
CREATE TRIGGER trg_post_likes_after_insert
AFTER INSERT ON post_likes
FOR EACH ROW
BEGIN
    -- Tăng like_count bằng một thao tác nguyên tử.
    UPDATE posts
    SET like_count = like_count + 1
    WHERE id = NEW.post_id;
END$$

-- Trigger giảm bộ đếm Like sau khi Unlike.
CREATE TRIGGER trg_post_likes_after_delete
AFTER DELETE ON post_likes
FOR EACH ROW
BEGIN
    -- Giảm bộ đếm nhưng không cho xuống dưới 0.
    UPDATE posts
    SET like_count = IF(like_count > 0, like_count - 1, 0)
    WHERE id = OLD.post_id;
END$$

-- Trigger tăng bộ đếm bình luận khi thêm bình luận PUBLISHED.
CREATE TRIGGER trg_comments_after_insert
AFTER INSERT ON comments
FOR EACH ROW
BEGIN
    -- Chỉ tăng bộ đếm khi bình luận đang được hiển thị.
    IF NEW.status = 'PUBLISHED' THEN
        UPDATE posts
        SET comment_count = comment_count + 1
        WHERE id = NEW.post_id;
    END IF;
END$$

-- Trigger đồng bộ bộ đếm khi bình luận đổi trạng thái.
CREATE TRIGGER trg_comments_after_update
AFTER UPDATE ON comments
FOR EACH ROW
BEGIN
    -- Giảm bộ đếm khi bình luận chuyển từ PUBLISHED sang DELETED.
    IF OLD.status = 'PUBLISHED' AND NEW.status = 'DELETED' THEN
        UPDATE posts
        SET comment_count = IF(comment_count > 0, comment_count - 1, 0)
        WHERE id = NEW.post_id;
    END IF;

    -- Tăng bộ đếm khi khôi phục từ DELETED sang PUBLISHED.
    IF OLD.status = 'DELETED' AND NEW.status = 'PUBLISHED' THEN
        UPDATE posts
        SET comment_count = comment_count + 1
        WHERE id = NEW.post_id;
    END IF;
END$$

-- Trigger tăng số bài của hashtag khi tạo quan hệ post_hashtags.
CREATE TRIGGER trg_post_hashtags_after_insert
AFTER INSERT ON post_hashtags
FOR EACH ROW
BEGIN
    -- Tăng bộ đếm số bài của hashtag.
    UPDATE hashtags
    SET post_count = post_count + 1
    WHERE id = NEW.hashtag_id;
END$$

-- Trigger giảm số bài của hashtag khi xóa quan hệ post_hashtags.
CREATE TRIGGER trg_post_hashtags_after_delete
AFTER DELETE ON post_hashtags
FOR EACH ROW
BEGIN
    -- Giảm bộ đếm nhưng không cho xuống dưới 0.
    UPDATE hashtags
    SET post_count = IF(post_count > 0, post_count - 1, 0)
    WHERE id = OLD.hashtag_id;
END$$

-- Khôi phục delimiter mặc định.
DELIMITER ;

-- =============================================================================
-- 7. VIEW HỖ TRỢ TRUY VẤN MVP
-- =============================================================================

-- View chỉ trả các bài đang hoạt động cùng thông tin tác giả.
CREATE OR REPLACE VIEW v_active_posts AS
SELECT
    -- ID bài viết.
    p.id AS post_id,
    -- Nội dung bài viết.
    p.content,
    -- Thời điểm xuất bản.
    p.published_at,
    -- Bộ đếm Like.
    p.like_count,
    -- Bộ đếm bình luận.
    p.comment_count,
    -- Cờ đã chỉnh sửa.
    p.is_edited,
    -- ID tác giả.
    u.id AS author_id,
    -- Username tác giả.
    u.username,
    -- Tên hiển thị tác giả.
    up.display_name,
    -- Ảnh đại diện tác giả.
    up.avatar_url
FROM posts p
INNER JOIN users u
    ON u.id = p.author_id
INNER JOIN user_profiles up
    ON up.user_id = u.id
WHERE p.status = 'PUBLISHED'
  AND u.status = 'ACTIVE';

-- =============================================================================
-- 8. DỮ LIỆU ADMIN MẪU TÙY CHỌN
-- =============================================================================

-- Lưu ý: Không chèn sẵn mật khẩu mẫu để tránh tạo thông tin đăng nhập không an toàn.
-- Admin đầu tiên nên được tạo bằng migration riêng với password_hash hợp lệ.
