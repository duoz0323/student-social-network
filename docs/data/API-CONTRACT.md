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

Actor:

- Khách chưa đăng nhập hoặc người dùng có phiên đã hết hạn Access Token.

Request:

```json
{
  "identifier": "minh@example.com",
  "password": "Password123!",
  "deviceId": "optional-device-id",
  "deviceInfo": "optional-browser-information"
}
```

Quy tắc:

- `identifier` là email hoặc số điện thoại. Backend tự xác định loại định danh để truy vấn đúng trường.
- Email được trim và chuẩn hóa chữ thường trước khi truy vấn.
- Số điện thoại được chuẩn hóa theo utility hiện có của Backend trước khi truy vấn.
- `deviceId` và `deviceInfo` là tùy chọn, dùng để ghi nhận thông tin phiên nếu Client cung cấp.
- Chỉ tài khoản `ACTIVE` được đăng nhập.
- Tài khoản `BLOCKED` bị từ chối đăng nhập.
- Mật khẩu được kiểm tra bằng `PasswordEncoder`, không so sánh chuỗi thô.
- Lỗi sai identifier hoặc sai mật khẩu phải dùng cùng một mã lỗi để không tiết lộ tài khoản có tồn tại hay không.
- Người dùng chưa hoàn tất hồ sơ vẫn được đăng nhập; response phải trả `profileCompleted` để Frontend điều hướng.
- Không trả `password_hash`, `token_hash`, email, số điện thoại hoặc dữ liệu nhạy cảm.

Ví dụ đăng nhập bằng số điện thoại:

```json
{
  "identifier": "0901234567",
  "password": "Password123!",
  "deviceId": "optional-device-id",
  "deviceInfo": "Chrome on Windows"
}
```

Response 200:

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "demo-access-token",
    "refreshToken": "demo-refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "refreshTokenExpiresIn": 2592000,
    "profileCompleted": false,
    "user": {
      "id": 1,
      "role": "USER"
    }
  },
  "timestamp": "2026-06-21T10:00:00"
}
```

Điều hướng Frontend:

- `profileCompleted = false`: chuyển đến onboarding hồ sơ.
- `profileCompleted = true`: chuyển đến Feed.

Error:

| HTTP status | Code | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Request thiếu `identifier`, thiếu `password` hoặc dữ liệu không hợp lệ. |
| 401 | `INVALID_CREDENTIALS` | Identifier không tồn tại hoặc mật khẩu không đúng. |
| 403 | `USER_BLOCKED` | Tài khoản tồn tại, mật khẩu đúng nhưng tài khoản bị khóa. |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống ngoài dự kiến. |

### POST `/api/v1/auth/refresh-token`

Actor:

- Người dùng có Refresh Token còn hiệu lực.

Request:

```json
{
  "refreshToken": "demo-refresh-token"
}
```

Quy tắc:

- Refresh Token phải là token đúng chữ ký, đúng loại token và chưa hết hạn.
- Backend chỉ lưu và truy vấn SHA-256 hash của Refresh Token, không lưu token thô.
- Refresh Token đã bị thu hồi hoặc hết hạn không được cấp Access Token mới.
- Tài khoản sở hữu token phải còn `ACTIVE`.
- Người dùng chưa hoàn tất hồ sơ vẫn được refresh token.
- Response chỉ cấp Access Token mới; không tự rotate Refresh Token trong contract MVP này.

Response 200:

```json
{
  "success": true,
  "message": "Làm mới Access Token thành công",
  "data": {
    "accessToken": "new-demo-access-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "profileCompleted": false
  },
  "timestamp": "2026-06-21T10:00:00"
}
```

Error:

| HTTP status | Code | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Request thiếu `refreshToken`. |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh Token sai định dạng, sai chữ ký, không tồn tại trong database hoặc không khớp user. |
| 401 | `REFRESH_TOKEN_EXPIRED` | Refresh Token đã hết hạn. |
| 401 | `REFRESH_TOKEN_REVOKED` | Refresh Token đã bị thu hồi. |
| 403 | `USER_BLOCKED` | Tài khoản sở hữu token đã bị khóa. |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống ngoài dự kiến. |

### POST `/api/v1/auth/logout`

Actor:

- Người dùng muốn đăng xuất khỏi phiên hiện tại.

Request:

```json
{
  "refreshToken": "demo-refresh-token"
}
```

Quy tắc:

- Backend hash Refresh Token thô bằng SHA-256 rồi tìm bản ghi tương ứng.
- Chỉ Refresh Token của phiên hiện tại bị thu hồi.
- Không thu hồi toàn bộ phiên khác của cùng người dùng.
- Không yêu cầu hồ sơ đã hoàn tất.
- Không trả token hoặc token hash trong response.
- Có thể trả thành công theo hướng idempotent để không tiết lộ trạng thái tồn tại của token.

Response 200:

```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": {
    "loggedOut": true
  },
  "timestamp": "2026-06-21T10:00:00"
}
```

Error:

| HTTP status | Code | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Request thiếu `refreshToken`. |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh Token sai định dạng hoặc sai chữ ký. |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống ngoài dự kiến. |

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
