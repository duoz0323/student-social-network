# API Contract MVP

## 1. Auth

### POST `/api/v1/auth/register`

Request:

```json
{
  "identifier": "minh@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

Hoặc:

```json
{
  "identifier": "0901234567",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

Quy tắc:

- `identifier` là email hoặc số điện thoại.
- Request đăng ký chỉ nhận đúng một phương thức định danh tại một thời điểm.
- Request đăng ký không nhận `username`, `displayName`, avatar, ngày sinh hoặc bio.
- Nếu đăng ký bằng email thì `phone_number` lưu `NULL`.
- Nếu đăng ký bằng số điện thoại thì `email` lưu `NULL`.
- Backend chuẩn hóa email hoặc số điện thoại trước khi kiểm tra trùng và lưu.
- Backend tạo `users` và `user_profiles` rỗng trong cùng transaction.
- `user_profiles.display_name` và `user_profiles.profile_completed_at` ban đầu là `NULL`.
- Sau đăng ký, Frontend điều hướng đến onboarding hồ sơ.
- Contract hiện chưa chốt đăng ký có cấp Access Token/Refresh Token ngay hay dùng phiên đăng ký hợp lệ; cần xác nhận khi triển khai, nhưng luồng vẫn phải đi đến onboarding.
- MVP chưa triển khai xác minh email hoặc SMS OTP; tài khoản mới có trạng thái `ACTIVE`.

Response 201:

```json
{
  "userId": "user-001",
  "role": "USER",
  "status": "ACTIVE",
  "profileCompleted": false
}
```

### POST `/api/v1/auth/login`

Request:

```json
{
  "identifier": "minh@example.com",
  "password": "Password123!"
}
```

`identifier` là email hoặc số điện thoại. Backend tự xác định loại định danh để truy vấn đúng trường.

Ví dụ đăng nhập bằng số điện thoại:

```json
{
  "identifier": "0901234567",
  "password": "Password123!"
}
```

Response 200:

```json
{
  "accessToken": "demo-access-token",
  "refreshToken": "demo-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "user-001",
    "displayName": "Nguyễn Hoàng Minh",
    "role": "USER"
  }
}
```

### POST `/api/v1/auth/refresh-token`

Request:

```json
{
  "refreshToken": "demo-refresh-token"
}
```

### POST `/api/v1/auth/logout`

Request:

```json
{
  "refreshToken": "demo-refresh-token"
}
```

## 2. User

### GET `/api/v1/users/me/onboarding`

Response 200:

```json
{
  "profileCompleted": false,
  "displayName": null,
  "avatarUrl": null,
  "dateOfBirth": null,
  "bio": null
}
```

### PUT `/api/v1/users/me/onboarding/profile`

Request:

```json
{
  "displayName": "Nguyễn Hoàng Minh",
  "avatarUrl": null,
  "dateOfBirth": null,
  "bio": null
}
```

Quy tắc:

- `displayName` bắt buộc để hoàn tất hồ sơ.
- `avatarUrl`, `dateOfBirth` và `bio` là tùy chọn.
- `dateOfBirth` nếu có không được nằm trong tương lai.

### POST `/api/v1/users/me/onboarding/complete`

Quy tắc:

- Backend chỉ cập nhật `profile_completed_at` khi tên hiển thị hợp lệ đã được lưu.
- `users.status = ACTIVE` không đồng nghĩa hồ sơ đã hoàn tất.
- API mạng xã hội chính phải trả lỗi `PROFILE_NOT_COMPLETED` khi `profile_completed_at` còn `NULL`.

Ví dụ lỗi:

```json
{
  "success": false,
  "code": "PROFILE_NOT_COMPLETED",
  "message": "Bạn cần hoàn tất hồ sơ trước khi sử dụng chức năng này",
  "timestamp": "2026-06-21T10:00:00"
}
```

### GET `/api/v1/users/{userId}`

### PATCH `/api/v1/users/me/profile`

Request:

```json
{
  "displayName": "Nguyễn Hoàng Minh",
  "bio": "Sinh viên CNTT",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

Route UI tương ứng:

- Hồ sơ cá nhân: `/profile/me`.
- Hồ sơ người dùng khác: `/profile/:userId`.

## 3. Follow

### POST `/api/v1/users/{userId}/follow`

### DELETE `/api/v1/users/{userId}/follow`

### GET `/api/v1/users/{userId}/followers?page=0&size=20`

### GET `/api/v1/users/{userId}/following?page=0&size=20`

## 4. Post

### POST `/api/v1/posts`

Request:

```json
{
  "content": "Nội dung bài viết",
  "imageUrls": [
    "https://example.com/image-1.jpg"
  ],
  "hashtags": [
    "sinhvien",
    "hoctap"
  ]
}
```

### GET `/api/v1/posts/{postId}`

### PATCH `/api/v1/posts/{postId}`

Request:

```json
{
  "content": "Nội dung đã sửa",
  "hashtags": [
    "doan"
  ]
}
```

### DELETE `/api/v1/posts/{postId}`

## 5. Interaction

### POST `/api/v1/posts/{postId}/likes`

### DELETE `/api/v1/posts/{postId}/likes`

### POST `/api/v1/posts/{postId}/comments`

Request:

```json
{
  "content": "Bình luận mẫu"
}
```

### GET `/api/v1/posts/{postId}/comments?page=0&size=20`

### DELETE `/api/v1/comments/{commentId}`

### POST `/api/v1/posts/{postId}/save`

### DELETE `/api/v1/posts/{postId}/save`

### GET `/api/v1/users/me/saved-posts?page=0&size=20`

## 6. Feed

### GET `/api/v1/feeds/for-you?page=0&size=20`

### GET `/api/v1/feeds/following?page=0&size=20`

## 7. Search

### GET `/api/v1/search/users?q=minh&page=0&size=20`

### GET `/api/v1/search/posts?q=hoctap&page=0&size=20`

## 8. Report

### POST `/api/v1/posts/{postId}/reports`

Request:

```json
{
  "reason": "SPAM",
  "description": "Nội dung quảng cáo lặp lại."
}
```

## 9. Admin

### GET `/api/v1/admin/users?page=0&size=20`

### PATCH `/api/v1/admin/users/{userId}/status`

```json
{
  "status": "BLOCKED"
}
```

### GET `/api/v1/admin/posts?page=0&size=20`

### PATCH `/api/v1/admin/posts/{postId}/status`

```json
{
  "status": "HIDDEN"
}
```

### GET `/api/v1/admin/reports?status=PENDING&page=0&size=20`

### PATCH `/api/v1/admin/reports/{reportId}`

```json
{
  "status": "RESOLVED",
  "hidePost": true
}
```
