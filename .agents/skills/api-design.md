# Kỹ năng thiết kế API

## 1. Nguyên tắc REST

- Endpoint dùng danh từ.
- Dùng số nhiều.
- Dùng HTTP method đúng mục đích.
- Dùng HTTP status phù hợp.
- API danh sách có phân trang.
- Response thống nhất.

## 2. Endpoint tham khảo

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh-token
POST   /api/v1/auth/logout

GET    /api/v1/users/me/onboarding
PUT    /api/v1/users/me/onboarding/profile
POST   /api/v1/users/me/onboarding/complete

GET    /api/v1/users/{userId}
PATCH  /api/v1/users/me/profile

POST   /api/v1/users/{userId}/follow
DELETE /api/v1/users/{userId}/follow

GET    /api/v1/posts/{postId}
POST   /api/v1/posts
PATCH  /api/v1/posts/{postId}
DELETE /api/v1/posts/{postId}

POST   /api/v1/posts/{postId}/likes
DELETE /api/v1/posts/{postId}/likes

POST   /api/v1/posts/{postId}/comments
GET    /api/v1/posts/{postId}/comments
DELETE /api/v1/comments/{commentId}

POST   /api/v1/posts/{postId}/save
DELETE /api/v1/posts/{postId}/save

GET    /api/v1/feeds/for-you
GET    /api/v1/feeds/following

GET    /api/v1/search/users
GET    /api/v1/search/posts

POST   /api/v1/posts/{postId}/reports

GET    /api/v1/admin/users
PATCH  /api/v1/admin/users/{userId}/status
GET    /api/v1/admin/reports
PATCH  /api/v1/admin/reports/{reportId}
```

## 3. Contract Auth MVP

### Đăng ký

`POST /api/v1/auth/register`

Request bắt buộc:

- `identifier`: email hoặc số điện thoại.
- `password`
- `confirmPassword`

Quy tắc:

- Không nhận `username` trong đăng ký MVP.
- Không nhận `displayName`, avatar, ngày sinh, bio, trường, khoa hoặc ngành trong đăng ký MVP.
- Request đăng ký chỉ nhận đúng một phương thức định danh tại thời điểm tạo tài khoản.
- Nếu `identifier` là email, Backend lưu `email` đã chuẩn hóa và lưu `phone_number` là `NULL`.
- Nếu `identifier` là số điện thoại, Backend lưu `phone_number` đã chuẩn hóa và lưu `email` là `NULL`.
- Email phải đúng định dạng, không trùng và được Backend chuẩn hóa về chữ thường nếu được cung cấp.
- Số điện thoại phải đúng định dạng, không trùng và được Backend chuẩn hóa thống nhất nếu được cung cấp.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Tài khoản mới có trạng thái `ACTIVE` và role mặc định `USER`.
- Backend tạo `users` và `user_profiles` rỗng trong cùng transaction.
- `user_profiles.display_name` và `user_profiles.profile_completed_at` ban đầu là `NULL`.
- Sau đăng ký, người dùng tiếp tục onboarding hồ sơ.
- MVP chưa triển khai xác minh email hoặc SMS OTP; `email_verified_at` và `phone_verified_at` để `NULL` nếu chưa xác minh.
- Response không trả password hash, refresh token, email hoặc số điện thoại nếu response được dùng cho hồ sơ công khai.

### Đăng nhập

`POST /api/v1/auth/login`

Request bắt buộc:

- `identifier`: email hoặc số điện thoại.
- `password`.

Backend tự xác định `identifier` là email hay số điện thoại để truy vấn đúng trường.

### Onboarding hồ sơ

- API onboarding chỉ dành cho người đã đăng nhập.
- `GET /api/v1/users/me/onboarding` trả trạng thái hoàn tất hồ sơ.
- `PUT /api/v1/users/me/onboarding/profile` cập nhật tên hiển thị, avatar, ngày sinh và bio.
- `POST /api/v1/users/me/onboarding/complete` xác nhận hoàn tất và cập nhật `profile_completed_at`.
- Tên hiển thị là bắt buộc để hoàn tất; avatar, ngày sinh và bio là tùy chọn.
- Ngày sinh nếu có không được nằm trong tương lai.
- API mạng xã hội chính trả `PROFILE_NOT_COMPLETED` khi `profile_completed_at` còn `NULL`.

### Hồ sơ và tìm kiếm

- Hồ sơ người dùng khác nên truy cập bằng `userId` hoặc định danh nội bộ phù hợp, không dùng `username`.
- Tìm kiếm người dùng trong MVP theo tên hiển thị; không tìm theo `username`.
- API, DTO và UI dùng thuật ngữ `email`, không dùng `gmail`.

## 4. Response lỗi

Response lỗi nên gồm:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors` nếu có validation.

Không trả stack trace.
