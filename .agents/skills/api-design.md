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
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout

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

- `email`
- `phoneNumber`
- `password`
- `displayName`

Quy tắc:

- Không nhận `username` trong đăng ký MVP.
- Email phải đúng định dạng, không trùng và được Backend chuẩn hóa về chữ thường.
- Số điện thoại phải đúng định dạng, không trùng và được Backend chuẩn hóa thống nhất.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Tài khoản mới có trạng thái `ACTIVE` và role mặc định `USER`.
- Response không trả password hash, refresh token, email hoặc số điện thoại nếu response được dùng cho hồ sơ công khai.

### Đăng nhập

`POST /api/v1/auth/login`

Request bắt buộc:

- `identifier`: email hoặc số điện thoại.
- `password`.

Backend tự xác định `identifier` là email hay số điện thoại để truy vấn đúng trường.

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
