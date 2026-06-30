# API Contract MVP

## 1. Auth

### POST `/api/v1/auth/register`

Request:

```json
{
  "email": "minh@example.com",
  "phoneNumber": "+84901234567",
  "password": "Password123!"
}
```

Response 201:

```json
{
  "userId": "user-001",
  "displayName": "Nguyễn Hoàng Minh",
  "role": "USER",
  "status": "ACTIVE"
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

### POST `/api/v1/auth/refresh`

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
