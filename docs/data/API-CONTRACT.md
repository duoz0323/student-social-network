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

Actor:

- Người dùng đã đăng nhập, có tài khoản `ACTIVE` và đã hoàn tất hồ sơ.

Quy tắc:

- Chỉ tìm theo một phần `display_name`; không tìm email, số điện thoại hoặc username.
- `q` được trim, bắt buộc có giá trị và tối đa 100 ký tự.
- `page` mặc định `0` và không được âm.
- `size` mặc định `20`, nhận giá trị từ `1` đến `100`.
- Chỉ trả tài khoản `ACTIVE` có `profile_completed_at` khác `NULL`.
- Ưu tiên tên bắt đầu bằng từ khóa, sau đó tên chứa từ khóa và `userId` giảm dần.

Response 200:

```json
{
  "success": true,
  "message": "Tìm kiếm người dùng thành công",
  "data": {
    "content": [
      {
        "userId": 12,
        "displayName": "Nguyễn Minh",
        "avatarUrl": "https://example.com/avatar.jpg",
        "bio": "Sinh viên CNTT"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-13T00:00:00"
}
```

Danh sách không có kết quả vẫn trả HTTP 200 với `content` rỗng.

### GET `/api/v1/search/posts?q=java&type=CONTENT&page=0&size=20`

Actor:

- Người dùng đã đăng nhập, có tài khoản `ACTIVE` và đã hoàn tất hồ sơ.

Query parameter:

- `q`: bắt buộc, được trim và tối đa 100 ký tự.
- `type`: bắt buộc, chỉ nhận `CONTENT` hoặc `HASHTAG`.
- `page`: mặc định `0`, không được âm.
- `size`: mặc định `20`, từ `1` đến `100`.

Quy tắc:

- `CONTENT` dùng FULLTEXT MySQL trên `posts.content`.
- `HASHTAG` bỏ các ký tự `#` liên tiếp ở đầu, chuyển lowercase và tìm exact `normalized_name`.
- Chỉ trả bài `PUBLISHED` của tác giả `ACTIVE` đã hoàn tất hồ sơ.
- Media, hashtag, Like và Save được tải theo batch cho cả trang.
- Danh sách không có kết quả trả HTTP 200 với `content` rỗng.

Ví dụ tìm hashtag:

```http
GET /api/v1/search/posts?q=%23SinhVien&type=HASHTAG&page=0&size=20
```

Response 200:

```json
{
  "success": true,
  "message": "Tìm kiếm bài viết thành công",
  "data": {
    "content": [
      {
        "postId": 100,
        "content": "Học Java cùng sinh viên CNTT",
        "isEdited": false,
        "likeCount": 3,
        "commentCount": 2,
        "publishedAt": "2026-07-13T01:00:00",
        "author": {
          "id": 20,
          "displayName": "Nguyễn Minh",
          "avatarUrl": "https://example.com/avatar.jpg"
        },
        "media": [],
        "hashtags": ["java", "sinhvien"],
        "likedByCurrentUser": true,
        "savedByCurrentUser": false
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-13T01:00:00"
}
```

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

### GET `/api/v1/admin/users?keyword=&status=&page=0&size=20`

- Chỉ `ADMIN` đang `ACTIVE` được truy cập.
- `keyword` tùy chọn, tìm theo email, số điện thoại hoặc tên hiển thị; blank sau trim được xem như không truyền.
- `status` tùy chọn: `ACTIVE` hoặc `BLOCKED`.
- `size` mặc định 20, tối đa 100.
- Chỉ trả tài khoản role `USER`, sắp xếp `createdAt DESC, userId DESC`.

Mỗi phần tử gồm `userId`, `displayName`, `avatarUrl`, `email`, `phoneNumber`, `status`,
`profileCompleted`, `createdAt`.

### GET `/api/v1/admin/users/{userId}`

- Chỉ cho xem target role `USER`.
- Không tồn tại trả `ADMIN_USER_NOT_FOUND`.
- Target role `ADMIN` trả `ADMIN_USER_MANAGEMENT_FORBIDDEN`.
- Response không chứa `passwordHash`, token hoặc `avatarPublicId`.

Response data gồm `userId`, `displayName`, `avatarUrl`, `bio`, `email`, `phoneNumber`, `status`,
`profileCompleted`, `profileCompletedAt`, `blockedAt`, `blockedReason`, `createdAt`, `updatedAt`.

### PATCH `/api/v1/admin/users/{userId}/block`

Request chỉ gồm mã lý do cố định:

```json
{
  "reasonCode": "SPAM"
}
```

`reasonCode` nhận một trong: `SPAM`, `HARASSMENT`, `HARMFUL_CONTENT`, `FAKE_ACCOUNT`,
`REPEATED_VIOLATION`, `OTHER`. Không nhận `note` hoặc reason tự do.

Thao tác chuyển `ACTIVE → BLOCKED`, thu hồi Refresh Token còn hiệu lực và ghi đồng thời
`account_status_histories`, `admin_actions` trong một transaction.

### PATCH `/api/v1/admin/users/{userId}/unblock`

- Không có request body.
- Chuyển `BLOCKED → ACTIVE`, xóa `blockedAt`/`blockedReason` hiện tại.
- Không khôi phục Refresh Token cũ.
- Lịch sử và action dùng reason/note `ADMIN_UNBLOCK`.

Response hai API gồm `userId`, `status`, `blockedAt`, `blockedReason`, `updatedAt`.

Mã lỗi nghiệp vụ: `ADMIN_USER_NOT_FOUND`, `ADMIN_USER_MANAGEMENT_FORBIDDEN`,
`ADMIN_SELF_ACTION_FORBIDDEN`, `ADMIN_USER_ALREADY_BLOCKED`, `ADMIN_USER_ALREADY_ACTIVE`,
`ADMIN_BLOCK_REASON_REQUIRED`.

Giai đoạn này chưa cung cấp API xem lịch sử trạng thái hoặc danh sách admin action.

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
