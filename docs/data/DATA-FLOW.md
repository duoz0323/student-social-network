# Luồng dữ liệu

## 1. Đăng nhập

```text
Login Form
→ POST /api/v1/auth/login
→ AuthController
→ AuthService
→ UserRepository
→ PasswordEncoder
→ JwtService
→ RefreshTokenService
→ Access Token + Refresh Token
→ Frontend
```

## 2. Refresh Token

```text
Axios nhận 401
→ POST /api/v1/auth/refresh
→ Kiểm tra Refresh Token
→ Kiểm tra revoked và expiry
→ Cấp Access Token mới
→ Gửi lại request cũ
```

## 3. Tạo bài

```text
Create Post Form
→ Kiểm tra nội dung và ảnh
→ Upload ảnh
→ Nhận URL
→ POST /api/v1/posts
→ PostService
→ Lưu posts
→ Lưu post_media
→ Lưu hashtag/post_hashtags
→ Trả PostResponse
```

## 4. Follow

```text
Profile
→ POST /api/v1/users/{userId}/follow
→ Kiểm tra không Follow chính mình
→ Kiểm tra không trùng
→ Lưu follows
→ Trả trạng thái mới
```

## 5. Like

```text
PostCard
→ POST /api/v1/posts/{postId}/likes
→ Kiểm tra post PUBLISHED
→ Kiểm tra chưa Like
→ Lưu post_likes
→ Trả likeCount mới
```

## 6. Comment

```text
Comment Form
→ POST /api/v1/posts/{postId}/comments
→ Kiểm tra post hợp lệ
→ Validate content
→ Lưu comments
→ Trả CommentResponse
```

## 7. Feed Following

```text
GET /api/v1/feeds/following
→ Lấy following IDs
→ Lấy post PUBLISHED
→ created_at DESC
→ Phân trang
→ Trả Page<PostResponse>
```

## 8. Feed For You

```text
GET /api/v1/feeds/for-you
→ Lấy post PUBLISHED
→ Tính điểm độ mới + Like + Comment
→ Hạn chế lặp tác giả
→ Phân trang
→ Trả kết quả
```

## 9. Search

```text
GET /api/v1/search/users?q=
→ MySQL LIKE hoặc full-text phù hợp
→ Loại user BLOCKED
→ Phân trang
```

```text
GET /api/v1/search/posts?q=
→ Tìm content/hashtag
→ Chỉ PUBLISHED
→ Phân trang
```

## 10. Report

```text
Report Modal
→ POST /api/v1/posts/{postId}/reports
→ Kiểm tra report PENDING trùng
→ Tạo report
→ Trả thành công
```

## 11. Admin xử lý Report

```text
Admin Report Detail
→ PATCH /api/v1/admin/reports/{reportId}
→ Kiểm tra ADMIN
→ Chuyển RESOLVED hoặc REJECTED
→ Có thể ẩn post
→ Trả kết quả
```
