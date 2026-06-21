# PRODUCT REQUIREMENTS DOCUMENT

## 1. Thông tin sản phẩm

**Tên dự án:** Mạng xã hội tinh gọn hướng đến sinh viên.

**Loại sản phẩm:** Website mạng xã hội.

**Mục tiêu:** Cho phép sinh viên tạo tài khoản, xây dựng hồ sơ, theo dõi người dùng khác, đăng nội dung ngắn, xem bảng tin và thực hiện các tương tác cơ bản.

## 2. Mục tiêu MVP

MVP phải hoàn thiện một luồng xuyên suốt:

Đăng ký
→ Đăng nhập
→ Quản lý hồ sơ
→ Theo dõi
→ Đăng bài
→ Xem Feed
→ Tương tác
→ Tìm kiếm
→ Báo cáo
→ Quản trị.

## 3. Actor

### 3.1 Khách chưa đăng nhập

Được phép:

- Đăng ký.
- Đăng nhập.
- Yêu cầu đặt lại mật khẩu nếu triển khai P2.

Không được sử dụng chức năng mạng xã hội.

### 3.2 Người dùng

Điều kiện:

- Đã đăng nhập.
- Tài khoản ở trạng thái ACTIVE.

Được phép:

- Xem và cập nhật hồ sơ.
- Xem hồ sơ người khác.
- Follow/Unfollow.
- Tạo, xem, sửa và xóa bài.
- Like/Unlike.
- Bình luận và xóa bình luận của mình.
- Lưu/Bỏ lưu bài.
- Xem Feed For You.
- Xem Feed Following.
- Tìm kiếm.
- Báo cáo bài viết.

### 3.3 Quản trị viên

Có role `ADMIN`.

Ngoài quyền người dùng, Admin được phép:

- Xem và tìm kiếm người dùng.
- Khóa/Mở khóa tài khoản.
- Xem danh sách bài viết.
- Xem bài bị báo cáo.
- Ẩn/Khôi phục bài viết.
- Xử lý báo cáo.

## 4. Phạm vi chức năng

### 4.1 Xác thực

- Đăng ký bằng email, username, mật khẩu và tên hiển thị.
- Email duy nhất.
- Username duy nhất.
- Mật khẩu tối thiểu 8 ký tự.
- Tài khoản mới ở trạng thái ACTIVE.
- Đăng nhập bằng email hoặc username.
- Cấp Access Token và Refresh Token.
- Đăng xuất thu hồi Refresh Token.
- Tài khoản BLOCKED không đăng nhập được.

### 4.2 Hồ sơ

Người dùng có thể:

- Xem hồ sơ cá nhân.
- Xem hồ sơ người khác.
- Cập nhật tên hiển thị.
- Cập nhật avatar.
- Cập nhật bio.
- Xem số follower.
- Xem số following.
- Xem bài đã đăng.

Tất cả hồ sơ công khai trong MVP.

### 4.3 Theo dõi

- Follow có hiệu lực ngay.
- Không có Follow Request.
- Không được Follow chính mình.
- Không được tạo Follow trùng.
- Có thể Unfollow.
- Có thể xem follower/following.

### 4.4 Bài viết

Bài viết gồm:

- Nội dung tối đa 500 ký tự.
- Tối đa 4 ảnh.
- Nhiều hashtag.

Quy tắc:

- Phải có nội dung hoặc ít nhất một ảnh.
- Chỉ hỗ trợ JPG, JPEG, PNG, WEBP.
- Chỉ tác giả được sửa/xóa.
- Sau khi đăng không chỉnh sửa ảnh.
- Chỉ sửa nội dung và hashtag.
- Trạng thái: PUBLISHED, HIDDEN, DELETED.
- Xóa bài là xóa mềm.

### 4.5 Tương tác

#### Like

- Like/Unlike.
- Một người Like một bài tối đa một lần.
- Không Like bài HIDDEN hoặc DELETED.

#### Bình luận

- Thêm bình luận.
- Xem bình luận.
- Xóa bình luận của mình.
- Reply một cấp thuộc P2.

#### Lưu bài

- Save/Unsave.
- Một người Save một bài tối đa một lần.
- Danh sách Save chỉ chủ tài khoản xem.

### 4.6 Feed

#### Following

- Chỉ bài của tài khoản đang Follow.
- Sắp xếp thời gian giảm dần.
- Không gồm bài HIDDEN hoặc DELETED.
- Có phân trang.

#### For You

- Gồm bài PUBLISHED.
- Xếp hạng cơ bản theo:
  - Độ mới.
  - Số Like.
  - Số bình luận.
- Không dùng Machine Learning.
- Hạn chế lặp liên tiếp cùng tác giả.

### 4.7 Hashtag

- Chuẩn hóa chữ thường.
- Một bài có nhiều hashtag.
- Xem bài theo hashtag.

### 4.8 Tìm kiếm

- Tìm user theo username.
- Tìm user theo tên hiển thị.
- Tìm post theo nội dung.
- Tìm post theo hashtag.
- Dùng MySQL trong MVP.
- Có phân trang.
- Không hiển thị user BLOCKED.
- Không hiển thị post HIDDEN/DELETED.

### 4.9 Báo cáo

Chỉ báo cáo bài viết.

Thông tin:

- Post.
- Người báo cáo.
- Lý do.
- Mô tả.
- Thời gian.
- Trạng thái.

Trạng thái:

- PENDING.
- RESOLVED.
- REJECTED.

Một người không được có nhiều report PENDING cho cùng một bài.

### 4.10 Quản trị

#### Người dùng

- Danh sách.
- Tìm kiếm.
- Khóa.
- Mở khóa.

#### Bài viết

- Danh sách.
- Bài bị báo cáo.
- Ẩn.
- Khôi phục.

#### Báo cáo

- Danh sách.
- Chi tiết.
- Xác nhận hợp lệ.
- Từ chối.
- Có thể ẩn bài khi vi phạm.

## 5. Ưu tiên

### P0

- Đăng ký, đăng nhập, đăng xuất.
- JWT/Refresh Token.
- Hồ sơ.
- Follow/Unfollow.
- CRUD bài.
- Upload ảnh.
- Like/Unlike.
- Bình luận.
- Feed Following.
- Feed For You.
- USER/ADMIN.

### P1

- Save/Unsave.
- Hashtag.
- Search.
- Report.
- Admin khóa tài khoản.
- Admin ẩn/khôi phục bài.

### P2

- Quên mật khẩu.
- Reply bình luận một cấp.
- Thông báo đơn giản.
- Lịch sử thao tác quản trị đơn giản.

## 6. Ngoài phạm vi

- Xác thực email.
- OAuth.
- Hồ sơ riêng tư.
- Follow Request.
- Block/Restrict.
- Video/tài liệu.
- Bản nháp.
- Mention.
- Repost.
- Quote Post.
- Chủ đề.
- Địa điểm.
- Discovery Map.
- Feed tùy chỉnh.
- Elasticsearch.
- Nhắn tin.
- Thông báo realtime.
- Dashboard nâng cao.
- Moderation Case.
- Audit Log chi tiết.

## 7. Tiêu chí nghiệm thu

- Đăng ký và đăng nhập thành công.
- Token hoạt động đúng.
- Tài khoản BLOCKED không đăng nhập được.
- Cập nhật hồ sơ.
- Follow/Unfollow không trùng.
- CRUD bài đúng quyền.
- Upload ảnh hợp lệ.
- Like không trùng.
- Bình luận đúng quyền.
- Save không trùng.
- Feed đúng nguồn.
- Search có phân trang.
- Report không trùng PENDING.
- Admin quản lý được user, post, report.
- API từ chối khi không có quyền.
- Password không lưu plain text.
