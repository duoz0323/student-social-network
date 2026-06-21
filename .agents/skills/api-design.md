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

GET    /api/v1/users/{username}
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

## 3. Response lỗi

Response lỗi nên gồm:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors` nếu có validation.

Không trả stack trace.
