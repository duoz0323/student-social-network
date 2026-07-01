# Luồng giao diện

Tài liệu này mô tả các luồng có thể suy ra từ MVP, ảnh trong `docs/ui/screens/` và điều hướng mẫu trong `UI-DEMO.html`. Không mô tả luồng cho nhắn tin, Discovery Map, Feed tùy chỉnh, Follow Request, Repost, video/tài liệu, Elasticsearch hoặc dashboard nâng cao.

## 1. Luồng MVP hiện tại

### 1.1 Đăng ký

```text
AUTH-02 Đăng ký
→ Nhập email hoặc số điện thoại
→ Nhập mật khẩu và xác nhận mật khẩu
→ Đồng ý điều khoản nếu form yêu cầu
→ Gửi đăng ký
→ AUTH-03 Đăng ký thành công nếu UI cần trạng thái trung gian
→ Onboarding hồ sơ
→ Nhập tên hiển thị bắt buộc
→ Có thể bỏ qua avatar, ngày sinh và bio
→ Xác nhận hoàn tất
→ FEED-01 Feed người dùng
```

Ghi chú: Không dùng username, displayName, Google hoặc Facebook trong form đăng ký MVP. Người dùng chỉ cung cấp đúng một phương thức định danh tại thời điểm đăng ký. Tên hiển thị được lưu trong `user_profiles` ở onboarding và có thể chỉnh sửa sau khi hoàn tất hồ sơ.

Nếu `profile_completed_at` còn `NULL`, route guard chuyển người dùng về onboarding và API mạng xã hội chính trả `PROFILE_NOT_COMPLETED`.

### 1.2 Đăng nhập

```text
AUTH-01 Đăng nhập
→ Nhập email hoặc số điện thoại
→ Nhập mật khẩu
→ Gửi đăng nhập
→ Nhận Access Token và Refresh Token
→ Nếu hồ sơ chưa hoàn tất thì đến onboarding
→ Nếu hồ sơ đã hoàn tất thì đến FEED-01 Feed người dùng
```

Trường hợp phiên hết hạn:

```text
API trả 401
→ Thử refresh token một lần
→ Refresh thất bại
→ SYS-04 Phiên đăng nhập hết hạn
→ AUTH-01 Đăng nhập
```

### 1.3 Feed For You và Following

```text
FEED-01
→ Tab Dành cho bạn tải bài PUBLISHED hợp lệ
→ Tab Đang theo dõi tải bài của người đang follow
→ Hiển thị danh sách PostCard
→ Người dùng cuộn hoặc tải thêm theo phân trang
```

Nếu Feed Following rỗng:

```text
Hiển thị empty state
→ Gợi ý người dùng tìm kiếm hoặc follow tài khoản khác
```

### 1.4 Tạo bài viết

```text
FEED-01 hoặc sidebar Tạo bài viết
→ POST-02 Modal tạo bài viết
→ Nhập nội dung tối đa 500 ký tự hoặc chọn ảnh
→ Có thể gắn hashtag
→ Đăng bài
→ Bài xuất hiện trên Feed/Profile nếu PUBLISHED
```

Quy tắc: bài phải có nội dung hoặc ít nhất một ảnh; tối đa 4 ảnh; chỉ hỗ trợ JPG, JPEG, PNG, WEBP theo MVP.

### 1.5 Xem chi tiết bài viết

```text
PostCard trong Feed/Profile/Saved
→ POST-01 Chi tiết bài viết
→ Xem nội dung, ảnh, số liệu tương tác
→ Xem danh sách bình luận
→ Nhập bình luận mới
```

### 1.6 Chỉnh sửa và xóa bài viết của mình

Chỉnh sửa:

```text
PostCard của chính mình
→ POST-04 Menu thao tác bài viết
→ Chỉnh sửa bài viết
→ POST-03 Modal chỉnh sửa bài viết
→ Sửa nội dung/hashtag
→ Lưu thay đổi
→ Quay lại bài viết/feed/profile
```

Xóa:

```text
PostCard của chính mình
→ POST-04 Menu thao tác bài viết
→ Xóa
→ POST-05 Modal xác nhận xóa
→ Xóa bài viết
→ POST-06 Xóa thành công
→ Bài không còn hiển thị trong Feed/Profile/Search thông thường
```

Quy tắc: chỉ tác giả được sửa/xóa; sau khi đăng không chỉnh sửa ảnh.

### 1.7 Like, bình luận và lưu bài

Like/Unlike:

```text
PostCard hoặc POST-01
→ Bấm biểu tượng thích
→ Cập nhật trạng thái liked/unliked và số lượt thích
```

Bình luận:

```text
POST-01
→ Nhập nội dung bình luận
→ Gửi bình luận
→ Bình luận xuất hiện trong danh sách
→ Chủ bình luận có thể xóa bình luận của mình
```

Lưu/Bỏ lưu:

```text
PostCard hoặc menu bài viết
→ Lưu hoặc Bỏ lưu
→ POST-07/SAVED-01 hiển thị danh sách bài đã lưu của chính người dùng
```

### 1.8 Xem hồ sơ

Hồ sơ của mình:

```text
Sidebar Trang cá nhân
→ /profile/me
→ PROFILE-01 Hồ sơ của mình
→ Xem thông tin cá nhân, số follower/following, bài đã đăng
→ Chọn Chỉnh sửa trang cá nhân
→ PROFILE-03 Modal chỉnh sửa hồ sơ
→ Lưu thay đổi
```

Hồ sơ người khác:

```text
Chọn avatar/tên hiển thị từ Feed/Search/Comment
→ /profile/:userId
→ PROFILE-02 Hồ sơ người khác
→ Xem thông tin công khai và bài viết
→ Follow hoặc Unfollow
```

Quy tắc: `/profile/me` active mục Trang cá nhân. `/profile/:userId` không active mục Trang cá nhân. Nút Nhắn tin xuất hiện trong ảnh nhưng thuộc FUTURE_DEVELOPMENT.

### 1.9 Follow và Unfollow

```text
PROFILE-02 hoặc PROFILE-04
→ Bấm Theo dõi
→ Trạng thái chuyển thành Đang theo dõi
→ Bài của người đó có thể xuất hiện trong Feed Following
→ Bấm Đang theo dõi/Bỏ theo dõi
→ Hủy quan hệ follow
```

Quy tắc: không follow chính mình, không tạo follow trùng, không có Follow Request.

### 1.10 Tìm kiếm

```text
Sidebar Tìm kiếm
→ SEARCH-01
→ Nhập từ khóa theo displayName, nội dung bài viết hoặc hashtag
→ Xem gợi ý/tìm kiếm phổ biến hoặc kết quả
→ Chọn user để đến /profile/:userId
→ Chọn bài viết/hashtag để xem danh sách bài hoặc POST-01
```

MVP dùng MySQL và phân trang; không dùng username hoặc Elasticsearch.

### 1.11 Báo cáo bài viết

```text
PostCard của người khác hoặc POST-01
→ POST-04 Menu thao tác bài viết
→ Báo cáo
→ POST-08 Chọn lý do báo cáo
→ POST-09 Nhập mô tả bổ sung
→ Gửi báo cáo
→ POST-10 Gửi báo cáo thành công
```

Quy tắc: chỉ báo cáo bài viết; một người không có nhiều report PENDING cho cùng một bài; gửi report không tự động ẩn bài.

### 1.12 Admin quản lý người dùng

```text
Admin đăng nhập
→ ADMIN-02 Quản lý người dùng
→ Tìm kiếm/lọc người dùng
→ ADMIN-03 Menu thao tác người dùng
→ Khóa hoặc mở khóa tài khoản
→ Danh sách cập nhật trạng thái ACTIVE/BLOCKED
```

Tài khoản BLOCKED không được đăng nhập hoặc dùng chức năng hệ thống.

### 1.13 Admin quản lý bài viết

```text
Admin đăng nhập
→ ADMIN-04 Quản lý bài viết
→ Tìm kiếm/lọc bài viết
→ Xem trạng thái bài
→ Ẩn hoặc khôi phục bài viết khi cần
```

Chỉ xử lý trạng thái phục vụ MVP: PUBLISHED, HIDDEN, DELETED.

### 1.14 Admin xử lý báo cáo

```text
Admin đăng nhập
→ ADMIN-05 Quản lý báo cáo
→ Mở ADMIN-06 Chi tiết báo cáo
→ Xem bài bị báo cáo và thông tin báo cáo
→ Từ chối báo cáo hoặc xác nhận vi phạm
→ Nếu vi phạm, admin có thể ẩn bài
```

Lịch sử xử lý trong ảnh chỉ nên là lịch sử đơn giản; audit log chi tiết ngoài MVP.

### 1.15 Trạng thái hệ thống

```text
Không có quyền → SYS-01
Không tìm thấy route/tài nguyên → SYS-02
Lỗi server/API → SYS-03
Phiên đăng nhập hết hạn → SYS-04
```

## 2. Luồng phát triển tương lai

Các luồng sau có thể đã có ảnh thiết kế nhưng thuộc `FUTURE_DEVELOPMENT`, không triển khai trong bản demo và Frontend MVP hiện tại.

### 2.1 Quên mật khẩu

```text
AUTH-01 Đăng nhập
→ Chọn Quên mật khẩu
→ AUTH-04 Nhập mã xác minh
→ AUTH-05 Đặt lại mật khẩu
→ AUTH-06 Đặt lại mật khẩu thành công
→ AUTH-01 Đăng nhập
```

### 2.2 Mention

```text
Người dùng nhập @ trong bài viết hoặc bình luận
→ Hệ thống gợi ý người dùng theo displayName
→ Người dùng chọn một tài khoản cụ thể
→ Lưu mentionedUserId
→ Render mention bằng @displayName
→ Bấm mention điều hướng /profile/:userId
```

Quy tắc: Không dùng @username, không dùng displayName làm khóa liên kết, không điều hướng theo tên hiển thị.

### 2.3 Các chức năng tương lai khác

- Nhắn tin.
- Discovery Map.
- Feed tùy chỉnh.
- Follow Request.
- Repost.
- Trích dẫn bài viết.
- Video hoặc tài liệu trong bài viết.
- Elasticsearch.
- Dashboard nâng cao.
- Thông báo thời gian thực.
