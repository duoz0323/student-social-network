# Tài liệu Database MVP

## 1. Danh sách bảng

### users

Lưu:

- Email, có thể `NULL` nếu tài khoản đăng ký bằng số điện thoại.
- Số điện thoại, có thể `NULL` nếu tài khoản đăng ký bằng email.
- Password hash.
- `email_verified_at`.
- `phone_verified_at`.
- Role.
- Status.
- Thời gian tạo/cập nhật.

Quy tắc:

- Mỗi tài khoản luôn phải có ít nhất email hoặc số điện thoại.
- Tại thời điểm đăng ký, người dùng chỉ cung cấp đúng một phương thức định danh.
- Database cho phép bổ sung phương thức còn thiếu trong tương lai.
- Không dùng cột `verified` kiểu chuỗi để lưu trạng thái xác minh.

### user_profiles

Lưu:

- User ID.
- Display name.
- Avatar URL.
- Bio.
- Birth date nếu cần hiển thị/cập nhật hồ sơ.
- `profile_completed_at`.

Quy tắc:

- Bản ghi `user_profiles` được tạo rỗng cùng transaction với `users` ngay sau đăng ký.
- `display_name` ban đầu được phép `NULL`.
- `profile_completed_at` ban đầu phải `NULL`.
- Tên hiển thị bắt buộc để hoàn tất hồ sơ.
- Avatar, ngày sinh và bio là tùy chọn.
- `profile_completed_at` xác định hồ sơ đã hoàn tất; không suy luận từ `users.status`.

### refresh_tokens

Lưu:

- User ID.
- Token hash hoặc định danh token.
- Expiry.
- Revoked.
- Created at.

### password_reset_tokens

P2.

### follows

Lưu quan hệ:

- Follower.
- Following.
- Created at.

### posts

Lưu:

- Author.
- Content.
- Status.
- Edited flag.
- Created at.
- Updated at.
- Deleted at nếu cần.

### post_media

Lưu:

- Post.
- URL.
- MIME type.
- Size.
- Sort order.

### hashtags

Lưu:

- Name.
- Normalized name.

### post_hashtags

Bảng liên kết Post → Hashtag: mỗi Post có tối đa một dòng, một Hashtag có thể được nhiều Post dùng chung.

### post_likes

Lưu User ↔ Post.

### comments

Lưu:

- Post.
- User.
- Content.
- Created at.
- Deleted state nếu cần.

### post_mentions

FUTURE_DEVELOPMENT.

Lưu:

- Post.
- Mentioned user.
- Display name snapshot nếu cần.

### comment_mentions

FUTURE_DEVELOPMENT.

Lưu:

- Comment.
- Mentioned user.
- Display name snapshot nếu cần.

### saved_posts

Lưu User ↔ Post.

### reports

Lưu:

- Post.
- Reporter.
- Reason.
- Description.
- Status.
- Resolved by.
- Resolved at.

### account_status_histories

Lưu lịch sử bất biến cho mỗi lần ADMIN khóa hoặc mở khóa tài khoản USER:

- Trạng thái cũ và mới.
- ADMIN thực hiện thay đổi.
- Lý do chuẩn hóa.
- Thời điểm do database tạo.

Bản ghi được tạo cùng transaction với thay đổi `users`; không sửa hoặc xóa lịch sử cũ khi mở khóa.

### admin_actions

Lưu dấu vết thao tác quản trị. Luồng trạng thái tài khoản dùng `BLOCK_USER` hoặc `UNBLOCK_USER`,
target type `USER`, target ID là tài khoản bị tác động. `old_data` và `new_data` để `NULL`
trong phạm vi này.

## 2. Quan hệ

- users 1-1 user_profiles.
- users 1-N refresh_tokens.
- users N-N users qua follows.
- users 1-N posts.
- posts 1-N post_media.
- posts có tối đa một hashtag qua post_hashtags; UNIQUE(post_id) bảo vệ invariant ở database.
- users N-N posts qua post_likes.
- users 1-N comments.
- posts 1-N comments.
- users N-N posts qua saved_posts.
- users 1-N reports.
- posts 1-N reports.
- posts N-N users qua post_mentions.
- comments N-N users qua comment_mentions.

## 3. Unique Constraint

- users.email nếu có giá trị.
- users.phone_number nếu có giá trị.
- follows(follower_id, following_id).
- post_likes(user_id, post_id).
- saved_posts(user_id, post_id).

## 4. Index

- users(email).
- users(phone_number).
- users(status).
- posts(author_id).
- posts(status, created_at).
- comments(post_id, created_at).
- follows(follower_id).
- follows(following_id).
- post_likes(post_id).
- saved_posts(user_id, created_at).
- reports(status, created_at).

## 5. Xóa mềm

Nên áp dụng cho:

- users.
- posts.
- comments nếu cần lưu lịch sử.
- reports không xóa thông thường.
