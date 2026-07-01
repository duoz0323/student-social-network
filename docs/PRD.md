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
- Hồ sơ đã hoàn tất, tức `user_profiles.profile_completed_at` khác `NULL`.

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

- Đăng ký bằng đúng một phương thức định danh: email hoặc số điện thoại.
- Form đăng ký chỉ gồm phương thức định danh, mật khẩu và xác nhận mật khẩu.
- Tại thời điểm đăng ký, nếu dùng email thì `phone_number` lưu `NULL`; nếu dùng số điện thoại thì `email` lưu `NULL`.
- Database cho phép tài khoản bổ sung phương thức còn thiếu trong tương lai, nhưng mỗi tài khoản luôn phải có ít nhất email hoặc số điện thoại.
- Email duy nhất nếu có giá trị.
- Số điện thoại duy nhất nếu có giá trị.
- Backend chuẩn hóa email và số điện thoại trước khi kiểm tra trùng và lưu.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Tài khoản mới ở trạng thái ACTIVE.
- Khi đăng ký hợp lệ, Backend tạo `users` và một `user_profiles` rỗng trong cùng transaction; nếu tạo hồ sơ thất bại thì rollback tài khoản.
- `user_profiles.display_name` và `user_profiles.profile_completed_at` ban đầu là `NULL`.
- Sau đăng ký, người dùng được điều hướng đến onboarding hồ sơ.
- Cơ chế phiên ngay sau đăng ký cần chốt theo API triển khai: có thể cấp Access Token/Refresh Token ngay hoặc duy trì phiên đăng ký hợp lệ, nhưng không được bỏ qua bước onboarding.
- Đăng nhập bằng email hoặc số điện thoại.
- Cấp Access Token và Refresh Token.
- Đăng xuất thu hồi Refresh Token.
- Tài khoản BLOCKED không đăng nhập được.
- MVP chưa triển khai xác minh email hoặc SMS OTP; `email_verified_at` và `phone_verified_at` để `NULL` nếu chưa xác minh.
- Email và số điện thoại không hiển thị trong API hồ sơ công khai.
- Người chưa hoàn tất hồ sơ chỉ được dùng API xác thực, refresh token, đăng xuất và onboarding; API mạng xã hội chính phải trả lỗi `PROFILE_NOT_COMPLETED`.

### 4.2 Hồ sơ

Người dùng có thể:

- Xem hồ sơ cá nhân.
- Xem hồ sơ người khác.
- Điều hướng hồ sơ bằng userId, trong đó `/profile/me` dành cho hồ sơ cá nhân và `/profile/:userId` dành cho hồ sơ người khác.
- Cập nhật tên hiển thị.
- Cập nhật avatar.
- Cập nhật ngày sinh.
- Cập nhật bio.
- Xem số follower.
- Xem số following.
- Xem bài đã đăng.

Tất cả hồ sơ công khai trong MVP.

Hoàn tất hồ sơ ban đầu:

- Tên hiển thị là bắt buộc.
- Avatar, ngày sinh và bio là tùy chọn.
- Hồ sơ chỉ hoàn tất sau khi tên hiển thị hợp lệ đã được lưu và người dùng xác nhận hoàn tất.
- Backend cập nhật `user_profiles.profile_completed_at` khi hoàn tất.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không đồng nghĩa hồ sơ đã hoàn tất.

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

- Tìm user theo tên hiển thị.
- Tìm post theo nội dung.
- Tìm post theo hashtag.
- Dùng MySQL trong MVP.
- Có phân trang.
- Không hiển thị user BLOCKED.
- Không hiển thị post HIDDEN/DELETED.

### 4.8.1 Mention

- Mention không thuộc phạm vi MVP hiện tại.
- Khi phát triển sau MVP, mention hiển thị bằng tên hiển thị của người được chọn.
- Mention phải liên kết nội bộ bằng userId, không dùng tên hiển thị làm khóa.
- Khi bấm mention, điều hướng đến `/profile/:userId`.

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
- Hoàn tất hồ sơ ban đầu.
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
- Sau đăng ký tạo đồng thời `users` và `user_profiles` trong cùng transaction.
- Hồ sơ ban đầu có `display_name` và `profile_completed_at` là `NULL`.
- Người dùng phải hoàn tất hồ sơ bằng tên hiển thị hợp lệ trước khi dùng Feed và các chức năng mạng xã hội.
- Backend trả `PROFILE_NOT_COMPLETED` khi tài khoản chưa hoàn tất hồ sơ gọi API mạng xã hội chính.
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
