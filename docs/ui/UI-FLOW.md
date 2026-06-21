# Luồng giao diện

## 1. Khách

### Đăng ký

```text
/register
→ Nhập email, username, display name, password
→ Validation
→ Gửi đăng ký
→ Thành công
→ /login
```

### Đăng nhập

```text
/login
→ Nhập email hoặc username
→ Nhập password
→ Gửi đăng nhập
→ Nhận Access Token và Refresh Token
→ Điều hướng /
```

## 2. Người dùng

### Feed For You

```text
/
→ Tải Feed For You
→ Hiển thị PostCard
→ Like / Comment / Save / Report
```

### Feed Following

```text
/following
→ Tải bài của tài khoản đang Follow
→ Sắp xếp mới nhất
```

Nếu rỗng:

```text
Hiển thị Empty State
→ Gợi ý tìm người dùng
```

### Tạo bài

```text
Nhấn Tạo bài
→ Nhập nội dung
→ Chọn tối đa 4 ảnh
→ Gắn hashtag
→ Đăng
→ Bài xuất hiện trên Feed/Profile
```

### Hồ sơ

```text
/profile/:username
→ Xem thông tin
→ Xem follower/following
→ Xem bài đã đăng
→ Follow/Unfollow nếu là người khác
```

### Tìm kiếm

```text
/search
→ Nhập từ khóa
→ Chọn User hoặc Post
→ Xem kết quả có phân trang
```

### Báo cáo

```text
Menu bài viết
→ Báo cáo
→ Chọn lý do
→ Nhập mô tả
→ Gửi
→ Thông báo thành công
```

## 3. Admin

### Quản lý user

```text
/admin/users
→ Tìm kiếm user
→ Mở chi tiết
→ Khóa hoặc mở khóa
```

### Quản lý report

```text
/admin/reports
→ Xem PENDING
→ Mở chi tiết
→ Xác nhận hoặc từ chối
→ Có thể ẩn post
```

## 4. Refresh Token

```text
API trả 401
→ Axios interceptor gọi refresh
→ Thành công: gửi lại request
→ Thất bại: xóa phiên và về /login
```
