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

Request `multipart/form-data`:

- `content`: tùy chọn, tối đa 500 ký tự.
- `images`: tùy chọn, tối đa 4 file ảnh.
- `hashtag`: tùy chọn, một chuỗi scalar.

### GET `/api/v1/posts/{postId}`

### PUT `/api/v1/posts/{postId}`

Request `multipart/form-data`:

- `content`: nội dung sau cập nhật.
- `keepMediaIds`: danh sách ID ảnh cũ cần giữ.
- `newImages`: ảnh mới cần thêm, tổng số ảnh sau cập nhật tối đa 4.
- `hashtag`: field có ba trạng thái được mô tả bên dưới.

`hashtag` là field multipart tùy chọn, tối đa một giá trị và không cần dấu `#`. Backend loại bỏ mọi
dấu `#`, trim Unicode whitespace, gộp mỗi nhóm khoảng trắng bên trong thành một dấu cách, chuẩn hóa
Unicode NFC rồi chuyển chữ thường bằng `Locale.ROOT`. Giá trị sau chuẩn hóa phải khác rỗng, chỉ gồm
chữ Unicode, dấu kết hợp, chữ số, `_` và dấu cách giữa các từ, tối đa 100 code point.

Khi update: không gửi field `hashtag` thì giữ nguyên; gửi raw chỉ gồm Unicode whitespace thì xóa;
gửi giá trị khác thì chuẩn hóa và thay thế. Raw không blank nhưng thành rỗng sau khi bỏ `#` là không hợp lệ.
Bài viết ở cả create và update vẫn phải có content hoặc ít nhất một ảnh; riêng hashtag không đủ.

### DELETE `/api/v1/posts/{postId}`

### GET `/api/v1/hashtags/suggestions?keyword=doan`

Actor:

- Người dùng đã đăng nhập, có tài khoản `ACTIVE` và đã hoàn tất hồ sơ.

Quy tắc:

- `keyword` được trim, bỏ một hoặc nhiều ký tự `#` ở đầu, chuyển chữ thường và chuẩn hóa Unicode NFC.
- Keyword dưới 2 ký tự sau chuẩn hóa trả danh sách rỗng; trên 100 ký tự trả lỗi `HASHTAG_SUGGESTION_KEYWORD_TOO_LONG`.
- API không nhận `page`, `size` hoặc `limit`; Backend giới hạn cố định tối đa 10 kết quả ngay tại database.
- Ưu tiên `normalized_name` bắt đầu bằng keyword, sau đó chứa keyword ở vị trí khác, `post_count DESC`, `id DESC`.
- API chỉ đọc hashtag đã tồn tại, không tạo hashtag, không cập nhật `post_count` và không tạo `post_hashtags`.

Response 200:

```json
{
  "success": true,
  "message": "Lấy gợi ý hashtag thành công",
  "data": {
    "keyword": "Doan",
    "normalizedKeyword": "doan",
    "exactMatch": false,
    "suggestions": [
      {
        "hashtagId": 1,
        "name": "doantruong",
        "postCount": 100
      }
    ],
    "canUseAsNewHashtag": true
  },
  "timestamp": "2026-07-16T10:00:00"
}
```

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
- `HASHTAG` dùng cùng pipeline chuẩn hóa hashtag của create/update và tìm exact `normalized_name`.
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
        "hashtag": "java",
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

### GET `/api/v1/admin/posts`

Query parameter:

- `keyword`: tùy chọn, trim, tối đa 100 ký tự; blank được xem là không truyền; tìm không phân biệt hoa thường theo nội dung hoặc tên tác giả.
- `status`: tùy chọn, nhận `PUBLISHED`, `HIDDEN` hoặc `DELETED`.
- `authorId`: tùy chọn, phải là số dương.
- `reportedOnly`: mặc định `false`; khi `true` chỉ trả bài có ít nhất một report `PENDING`.
- `page`: mặc định `0`, không âm.
- `size`: mặc định `20`, từ `1` đến `100`.

Chỉ `ADMIN` đang `ACTIVE` được truy cập. Danh sách không loại tác giả `BLOCKED` và sắp xếp
`createdAt DESC, postId DESC`.

Mỗi phần tử gồm `postId`, `contentPreview`, `status`, `authorId`, `authorDisplayName`,
`authorAvatarUrl`, `authorAccountStatus`, `thumbnailUrl`, `mediaCount`, `likeCount`,
`commentCount`, `pendingReportCount`, `createdAt`, `updatedAt`.

### GET `/api/v1/admin/posts/{postId}`

- Xem được bài ở cả ba trạng thái `PUBLISHED`, `HIDDEN`, `DELETED`.
- Không tồn tại trả `ADMIN_POST_NOT_FOUND`.
- Media sắp xếp `sortOrder ASC, mediaId ASC`; `hashtag` là scalar nullable.
- Response không chứa password, token, `avatarPublicId`, `storagePublicId` hoặc metadata cloud nội bộ.

Response data gồm `postId`, `content`, `status`, `author`, `media`, `hashtag`, `likeCount`,
`commentCount`, `pendingReportCount`, `totalReportCount`, `hiddenAt`, `hiddenReason`, `hiddenBy`,
`deletedAt`, `createdAt`, `updatedAt`.

Mã lỗi riêng: `ADMIN_POST_NOT_FOUND`, `ADMIN_POST_KEYWORD_TOO_LONG`.

### PATCH `/api/v1/admin/posts/{postId}/hide`

Request chỉ gồm mã lý do cố định:

```json
{
  "reasonCode": "SPAM"
}
```

`reasonCode` nhận một trong: `SPAM`, `HARASSMENT`, `HARMFUL_CONTENT`, `VIOLENCE`,
`MISINFORMATION`, `SCHOOL_POLICY_VIOLATION`, `INAPPROPRIATE_CONTENT`, `OTHER`.
Không nhận `note`, reason tự do hoặc `adminId`.

Chỉ chuyển `PUBLISHED → HIDDEN`. Backend lưu `hiddenBy`, `hiddenAt`, `hiddenReason` và action
`HIDE_POST` trong cùng transaction.

### PATCH `/api/v1/admin/posts/{postId}/restore`

- Không có request body.
- Chỉ chuyển `HIDDEN → PUBLISHED`.
- Xóa `hiddenBy`, `hiddenAt`, `hiddenReason` hiện tại nhưng không sửa action `HIDE_POST` cũ.
- Ghi action `RESTORE_POST` với note `ADMIN_RESTORE` trong cùng transaction.

Response hai API gồm `postId`, `status`, `hiddenAt`, `hiddenReason`, `hiddenBy`, `updatedAt`.

Mã lỗi mutation: `ADMIN_POST_ALREADY_HIDDEN`, `ADMIN_POST_ALREADY_PUBLISHED`,
`ADMIN_POST_DELETED_ACTION_FORBIDDEN`, `ADMIN_POST_HIDE_REASON_REQUIRED`.

Giai đoạn này chưa xử lý hoặc tự động thay đổi trạng thái Report.

### GET `/api/v1/admin/reports?status=PENDING&page=0&size=20`

### PATCH `/api/v1/admin/reports/{reportId}`

```json
{
  "status": "RESOLVED",
  "hidePost": true
}
```

### GET `/api/v1/admin/actions`

- Chỉ `ADMIN` đang `ACTIVE` được truy cập.
- Filter tùy chọn: `actionType`, `targetType`, `adminId`, `from`, `to`.
- `page` mặc định `0`; `size` mặc định `20` và tối đa `100`.
- Nếu có cả `from` và `to` thì `from` không được sau `to`.
- Sắp xếp cố định `createdAt DESC, actionId DESC` và phân trang tại database.
- Target đã bị xóa vẫn giữ bản ghi lịch sử với `targetAvailable = false`.
- Danh sách không trả `oldData` hoặc `newData`.

Mỗi phần tử gồm `actionId`, `actionType`, `actionLabel`, `admin`, `target`, `note`, `createdAt`.

### GET `/api/v1/admin/actions/{actionId}`

- Trả toàn bộ trường của phần tử danh sách và bổ sung `oldData`, `newData` dạng JSON.
- JSON được lọc đệ quy để không trả password, token, secret hoặc credential.
- Không tồn tại trả `ADMIN_ACTION_NOT_FOUND`.
- Hai API lịch sử chỉ đọc; không có API tạo, sửa hoặc xóa `admin_actions`.
