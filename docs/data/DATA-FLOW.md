# Luồng dữ liệu

## 1. Đăng ký

```text
Register Form
→ Nhập identifier, password, confirmPassword
→ POST /api/v1/auth/register
→ AuthController
→ AuthService
→ Chuẩn hóa email hoặc số điện thoại
→ Kiểm tra đúng một phương thức định danh và không trùng
→ Tạo users và user_profiles rỗng trong cùng transaction
→ Trả kết quả đăng ký
→ Frontend điều hướng đến onboarding hồ sơ
```

## 2. Hoàn tất hồ sơ

```text
Onboarding Page
→ GET /api/v1/users/me/onboarding
→ Nhập tên hiển thị bắt buộc, avatar/ngày sinh/bio tùy chọn
→ PUT /api/v1/users/me/onboarding/profile
→ POST /api/v1/users/me/onboarding/complete
→ Backend cập nhật user_profiles.profile_completed_at
→ Frontend cho phép vào Feed
```

Nếu `profile_completed_at` còn `NULL`, các API mạng xã hội chính trả `PROFILE_NOT_COMPLETED` và Frontend điều hướng về onboarding.

## 3. Đăng nhập

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

## 4. Refresh Token

```text
Axios nhận 401
→ POST /api/v1/auth/refresh-token
→ Kiểm tra Refresh Token
→ Kiểm tra revoked và expiry
→ Cấp Access Token mới
→ Gửi lại request cũ
```

## 5. Tạo bài

```text
Create Post Form
→ Kiểm tra profile_completed_at khác NULL
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

## 6. Follow

```text
Profile
→ POST /api/v1/users/{userId}/follow
→ Kiểm tra profile_completed_at khác NULL
→ Kiểm tra không Follow chính mình
→ Kiểm tra không trùng
→ Lưu follows
→ Trả trạng thái mới
```

## 7. Like

```text
PostCard
→ POST /api/v1/posts/{postId}/likes
→ Kiểm tra profile_completed_at khác NULL
→ Kiểm tra post PUBLISHED
→ Kiểm tra chưa Like
→ Lưu post_likes
→ Trả likeCount mới
```

## 8. Comment

```text
Comment Form
→ POST /api/v1/posts/{postId}/comments
→ Kiểm tra profile_completed_at khác NULL
→ Kiểm tra post hợp lệ
→ Validate content
→ Lưu comments
→ Trả CommentResponse
```

## 9. Feed Following

```text
GET /api/v1/feeds/following
→ Kiểm tra profile_completed_at khác NULL
→ Lấy following IDs
→ Lấy post PUBLISHED
→ created_at DESC
→ Phân trang
→ Trả Page<PostResponse>
```

## 10. Feed For You

```text
GET /api/v1/feeds/for-you
→ Kiểm tra profile_completed_at khác NULL
→ Lấy post PUBLISHED
→ Tính điểm độ mới + Like + Comment
→ Hạn chế lặp tác giả
→ Phân trang
→ Trả kết quả
```

## 11. Search

```text
GET /api/v1/search/users?q=
→ Kiểm tra profile_completed_at khác NULL
→ MySQL LIKE hoặc full-text phù hợp
→ Loại user BLOCKED
→ Phân trang
```

```text
GET /api/v1/search/posts?q=
→ Kiểm tra profile_completed_at khác NULL
→ Tìm content/hashtag
→ Chỉ PUBLISHED
→ Phân trang
```

## 12. Report

```text
Report Modal
→ POST /api/v1/posts/{postId}/reports
→ Kiểm tra profile_completed_at khác NULL
→ Kiểm tra report PENDING trùng
→ Tạo report
→ Trả thành công
```

## 13. Admin xử lý Report

```text
Admin Report Detail
→ PATCH /api/v1/admin/reports/{reportId}
→ Kiểm tra ADMIN
→ Chuyển RESOLVED hoặc REJECTED
→ Có thể ẩn post
→ Trả kết quả
```
