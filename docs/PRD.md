# PRODUCT REQUIREMENTS DOCUMENT

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. PRD này tóm tắt yêu cầu sản phẩm; nếu có khác biệt, phải áp dụng `README.md` và cập nhật trực tiếp mục tương ứng trong PRD.

## 1. Thông tin sản phẩm

**Tên dự án:** Mạng xã hội tinh gọn hướng đến sinh viên.

**Loại sản phẩm:** Website mạng xã hội.

**Mục tiêu:** Cho phép sinh viên tạo tài khoản, xây dựng hồ sơ, theo dõi người dùng khác, đăng nội dung ngắn, xem bảng tin và thực hiện các tương tác cơ bản.

## 2. Mục tiêu MVP

MVP phải hoàn thiện một luồng xuyên suốt:

Đăng ký local bằng email/số điện thoại hoặc đăng ký bằng Google/Facebook
→ Xác minh OTP nếu đăng ký local
→ Đăng nhập và nhận JWT của hệ thống
→ Hoàn tất hồ sơ ban đầu
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
- Xác minh hoặc tiếp tục đăng ký local đang chờ.
- Gửi lại OTP theo giới hạn tần suất.
- Đăng ký hoặc đăng nhập bằng Google/Facebook.
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

Hệ thống áp dụng mô hình một tài khoản nội bộ có nhiều phương thức xác thực. Email, số điện thoại, Google và Facebook sau khi được xác minh và liên kết hợp lệ đều ánh xạ về cùng một `users.id`.

Đăng ký local:

- Request chỉ nhận một `identifier` là email hoặc số điện thoại, `password` và `confirmPassword`; không nhận username hoặc tên hiển thị.
- Backend chuẩn hóa identifier và băm mật khẩu trước khi lưu.
- Khi nhận form hợp lệ, Backend chỉ tạo `pending_registrations`; chưa tạo `users`, `user_profiles`, Access Token hoặc Refresh Token.
- OTP email và OTP SMS có hiệu lực 10 phút, resend sau 60 giây và tối đa 5 lần nhập sai cho mỗi OTP.
- Pending có hiệu lực 24 giờ; OTP mới làm OTP cũ mất hiệu lực; không được có hai pending còn hiệu lực cho cùng identifier.
- Dữ liệu `CANCELLED` và `EXPIRED` được giữ tối đa 7 ngày trước khi xóa hoặc ẩn danh.
- Chỉ OTP hợp lệ mới tạo `users` và `user_profiles` trong cùng transaction. Nếu tạo profile thất bại, toàn bộ transaction phải rollback.
- Sau khi tài khoản thật được tạo, Backend cấp Access Token và Refresh Token rồi điều hướng tới onboarding.

Google/Facebook và liên kết phương thức:

- Backend phải tự xác minh provider token; không tin provider ID, email hoặc trạng thái verified do Frontend khai báo.
- Provider token chỉ dùng tại endpoint Auth; API nghiệp vụ chỉ chấp nhận JWT của hệ thống.
- Provider đã liên kết phải đăng nhập về đúng `users.id`.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Khi liên kết provider, tài khoản đích phải lấy từ JWT hiện tại.
- Email/phone phải được xác minh OTP trước khi liên kết; không được gỡ phương thức đăng nhập cuối cùng.
- `users.password_hash` được phép `NULL` với social-only account; tài khoản này không đăng nhập local được cho đến khi thiết lập mật khẩu và xác minh định danh local.

Đăng nhập và phiên:

- Login local dùng email hoặc số điện thoại và mật khẩu; chỉ định danh có trường `*_verified_at` khác `NULL` mới được dùng.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.
- Access Token có thời hạn ngắn; Refresh Token chỉ lưu dạng hash, được rotate khi refresh và bị thu hồi khi logout hoặc theo nghiệp vụ khóa tài khoản.
- Người chưa hoàn tất hồ sơ chỉ được dùng API Auth cần thiết, Refresh Token, logout, onboarding và API quản lý phương thức xác thực theo contract; API mạng xã hội chính trả `PROFILE_NOT_COMPLETED`.
- Email, số điện thoại và dữ liệu xác thực không được trả trong API hồ sơ công khai.

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

- Tên hiển thị và ngày sinh là bắt buộc.
- Người dùng phải đủ 18 tuổi tại ngày hoàn tất hoặc cập nhật hồ sơ.
- Avatar và bio là tùy chọn.
- Hồ sơ chỉ hoàn tất sau khi tên hiển thị hợp lệ và ngày sinh hợp lệ của người dùng đủ 18 tuổi đã được lưu, sau đó người dùng xác nhận hoàn tất.
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

- Đăng ký local bằng email/số điện thoại và xác minh OTP.
- Đăng ký, đăng nhập Google/Facebook.
- Đăng nhập local và đăng xuất.
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

- Liên kết và quản lý nhiều phương thức đăng nhập.
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

- Đăng ký local chỉ tạo `pending_registrations`, chưa tạo `users`.
- OTP email hoặc OTP SMS hợp lệ mới tạo `users` và `user_profiles` trong cùng transaction.
- OTP hết hạn, đã dùng, bị hủy hoặc vượt số lần thử không thể sử dụng.
- Không tồn tại hai pending còn hiệu lực cho cùng identifier.
- Mất mạng hoặc đóng tab vẫn có thể tiếp tục đăng ký trong thời hạn.
- Google/Facebook token được Backend xác minh trước khi dùng.
- Provider đã liên kết luôn đăng nhập về đúng `users.id`.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Liên kết provider lấy tài khoản đích từ JWT hiện tại.
- Không cho gỡ phương thức đăng nhập cuối cùng.
- Social-only account chưa có mật khẩu không đăng nhập local được.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
- Người dùng phải hoàn tất hồ sơ bằng tên hiển thị và ngày sinh hợp lệ, đồng thời đủ 18 tuổi, trước khi dùng Feed và các chức năng mạng xã hội.
- Backend trả `PROFILE_NOT_COMPLETED` khi tài khoản chưa hoàn tất hồ sơ gọi API mạng xã hội chính.
- Token hoạt động đúng.
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
