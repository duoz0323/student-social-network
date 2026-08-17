# 🎓 Student Social Network

> Website mạng xã hội tinh gọn hướng đến sinh viên, được phát triển trong khuôn khổ luận văn tốt nghiệp ngành Công nghệ thông tin.

---

## 📖 Giới thiệu

**Student Social Network** là nền tảng mạng xã hội tinh gọn hướng đến cộng đồng sinh viên, cho phép người dùng chia sẻ nội dung ngắn, kết nối, tương tác và khám phá các trải nghiệm thực tế liên quan đến học tập, ăn uống, vui chơi và đời sống sinh viên.

Hệ thống cung cấp các nhóm chức năng chính:

- Đăng ký và đăng nhập bằng email, Google hoặc Facebook.
- Xác minh email bằng mã OTP.
- Quản lý và liên kết nhiều phương thức đăng nhập trên cùng một tài khoản.
- Quản lý hồ sơ cá nhân.
- Theo dõi người dùng khác.
- Đăng tải và quản lý bài viết ngắn.
- Tương tác thông qua lượt thích, bình luận và lưu bài viết.
- Xem bảng tin cá nhân.
- Tìm kiếm người dùng và bài viết.
- Báo cáo nội dung vi phạm.
- Quản trị người dùng, bài viết và báo cáo.

Dự án được xây dựng theo mô hình **Client – Server**. Frontend và Backend được phát triển độc lập, giao tiếp thông qua RESTful API và sử dụng JWT để xác thực các request.

Phiên bản hiện tại tập trung hoàn thiện phạm vi **Minimum Viable Product – MVP**, ưu tiên một luồng sử dụng xuyên suốt, ổn định, có thể kiểm thử và trình bày trong luận văn.

---

## 🎯 Mục tiêu dự án

- Xây dựng một website mạng xã hội tinh gọn dành cho sinh viên.
- Tạo môi trường để sinh viên chia sẻ nội dung và kết nối với nhau.
- Hỗ trợ xác thực đa phương thức bằng email, Google và Facebook.
- Xây dựng đầy đủ luồng JWT Access Token và Refresh Token.
- Triển khai các chức năng cốt lõi của mạng xã hội.
- Áp dụng kiến trúc Frontend và Backend tách biệt.
- Đảm bảo khả năng mở rộng, bảo trì và kiểm thử.
- Áp dụng các nguyên tắc bảo mật trong quá trình xây dựng hệ thống.
- Hoàn thiện sản phẩm phục vụ báo cáo và bảo vệ luận văn tốt nghiệp.

---

## 👨‍🎓 Nhóm thực hiện

### Thành viên 1

- **Họ và tên:** Nguyễn Hoàng Dương
- **MSSV:** DH52200548
- **Vai trò:**
  - Phân tích yêu cầu nghiệp vụ.
  - Thiết kế cơ sở dữ liệu.
  - Phát triển Backend.
  - Xây dựng RESTful API.
  - Tích hợp bảo mật JWT.
  - Kiểm thử API.

### Thành viên 2

- **Họ và tên:** Đậu Quốc Khánh
- **MSSV:** DH52200867
- **Vai trò:**
  - Thiết kế giao diện người dùng.
  - Phát triển Frontend.
  - Tích hợp Frontend với RESTful API.
  - Kiểm thử giao diện và luồng người dùng.

### Giảng viên hướng dẫn

- **Giảng viên:** Hoàng Công Quang Huy

---

## 👥 Đối tượng sử dụng

### 1. Khách chưa đăng nhập

Khách chưa đăng nhập có thể:

- Bắt đầu đăng ký bằng email.
- Xác minh đăng ký bằng OTP.
- Đăng ký hoặc đăng nhập bằng Google/Facebook.
- Đăng nhập bằng email đã xác minh.
- Gửi lại mã xác minh hoặc tiếp tục đăng ký đang chờ.
- Khôi phục mật khẩu bằng OTP đối với tài khoản local đủ điều kiện; tài khoản social-only không dùng luồng này để tạo mật khẩu lần đầu.

Khách chưa đăng nhập không được truy cập các chức năng mạng xã hội.

### 2. Người dùng

Người dùng đã đăng nhập và có tài khoản đang hoạt động có thể:

- Hoàn tất và quản lý hồ sơ cá nhân.
- Quản lý các phương thức đăng nhập đã liên kết.
- Theo dõi người dùng khác.
- Chặn, bỏ chặn, hạn chế và bỏ hạn chế người dùng khác.
- Xem danh sách tài khoản đã chặn và đã hạn chế.
- Đăng và quản lý bài viết.
- Like, bình luận và lưu bài viết.
- Xem Feed.
- Tìm kiếm.
- Báo cáo bài viết hoặc trang cá nhân của người dùng khác vi phạm.

### 3. Quản trị viên

Quản trị viên có thể:

- Quản lý người dùng.
- Xem và chỉnh sửa ảnh đại diện, tên hiển thị, ngày sinh, phần giới thiệu của hồ sơ USER.
- Sau khi chỉnh sửa hồ sơ, gửi thông báo hệ thống cho USER rằng nội dung đã bị điều chỉnh do vi phạm Tiêu chuẩn hệ thống.
- Khóa và mở khóa tài khoản.
- Cấp lại mật khẩu cho tài khoản quản trị viên hỗ trợ khi được yêu cầu; Master Admin chỉ tự đổi mật khẩu trong hồ sơ.
- Xem, chỉnh sửa hồ sơ quản trị viên của chính mình và đổi mật khẩu bằng mật khẩu hiện tại.
- Quản lý bài viết.
- Xem, tìm kiếm, tạo, đổi tên và xóa hashtag; đổi tên giữ nguyên liên kết bài viết, còn xóa khiến bài liên quan không còn hashtag.
- Ẩn và khôi phục bài viết.
- Xem và xử lý báo cáo vi phạm.
- Xem lịch sử thao tác quản trị khi chức năng tương ứng được triển khai.

---

## ✨ Phạm vi chức năng MVP

### 🔐 1. Xác thực, đăng ký và hoàn tất hồ sơ

#### Phương thức xác thực

Hệ thống hỗ trợ ba phương thức:

- Email và mật khẩu, xác minh bằng OTP email.
- Google.
- Facebook.

Hệ thống áp dụng mô hình:

```text
Một tài khoản nội bộ
        +
Nhiều phương thức xác thực
```

Mọi phương thức đã được xác minh và liên kết hợp lệ đều ánh xạ về cùng một `users.id`. Sau khi xác thực thành công, Backend cấp JWT Access Token và Refresh Token của hệ thống.

#### Đăng ký local bằng email

- Người dùng cung cấp một địa chỉ email tại mỗi yêu cầu đăng ký.
- Form đăng ký gồm email, mật khẩu và xác nhận mật khẩu.
- Backend loại bỏ khoảng trắng thừa và chuẩn hóa email về chữ thường trước khi kiểm tra.
- Hệ thống chưa tạo `users` và `user_profiles` ngay khi người dùng gửi form đăng ký.
- Backend tạo bản ghi `pending_registrations`, sinh OTP, lưu OTP và flow token dưới dạng hash rồi gửi mã xác minh.
- Chỉ sau khi OTP hợp lệ, hệ thống mới tạo đồng thời `users` và `user_profiles` trong cùng transaction.
- Nếu tạo `user_profiles` thất bại, toàn bộ transaction phải rollback.
- Sau khi tài khoản được tạo, Backend cấp Access Token và Refresh Token rồi chuyển người dùng đến onboarding.

#### Vòng đời đăng ký tạm

Trạng thái của `pending_registrations`:

- `PENDING`: đang chờ xác minh.
- `COMPLETED`: đã hoàn tất và tạo tài khoản thật.
- `CANCELLED`: người dùng hủy hoặc thay thế phương thức đăng ký.
- `EXPIRED`: đăng ký tạm đã hết thời hạn.

Chính sách mặc định:

- OTP có hiệu lực 10 phút.
- Chỉ cho gửi lại mã sau 60 giây.
- Tối đa 5 lần nhập sai cho mỗi OTP.
- Đăng ký tạm có hiệu lực 24 giờ.
- Dữ liệu `CANCELLED` và `EXPIRED` được lưu tối đa 7 ngày trước khi xóa hoặc ẩn danh.

#### Khôi phục luồng đăng ký

- Khi mất mạng, đóng tab hoặc rời màn hình xác minh, đăng ký tạm vẫn được giữ trong thời hạn.
- Người dùng có thể tiếp tục xác minh hoặc gửi lại mã.
- Nếu Frontend mất flow token, người dùng nhập lại email; Backend phát hiện đăng ký đang chờ và không tạo bản ghi trùng.
- OTP cũ phải mất hiệu lực khi mã mới được phát hành.

#### Đăng ký và đăng nhập bằng Google/Facebook

- Frontend nhận credential/token từ Google hoặc Facebook và gửi về Backend.
- Backend phải xác minh token với nhà cung cấp trước khi tin cậy thông tin người dùng.
- Google/Facebook token chỉ dùng tại endpoint xác thực chuyên biệt.
- Các API nghiệp vụ khác chỉ chấp nhận JWT do Backend phát hành.
- Nếu provider đã được liên kết, Backend đăng nhập đúng tài khoản hiện có.
- Nếu provider chưa được liên kết và thông tin hợp lệ chưa thuộc tài khoản nào, Backend tạo tài khoản nội bộ mới.
- Nếu social email trùng một tài khoản `ACTIVE` nhưng provider chưa được liên kết, hệ thống không tự động gộp chỉ dựa trên email.
- `provider_user_id` là định danh social chính; provider email chỉ là metadata đã được Backend xác minh và không được dùng để chọn `users.id` đích.
- Facebook có thể không trả email. Account Facebook-only hợp lệ giữ `users.email`, `email_verified_at` và `password_hash` bằng `NULL`; email Facebook nếu có chỉ lưu tại `user_auth_providers` và không tự tạo phương thức đăng nhập EMAIL.
- Google identity vẫn được xác thực bằng `sub` khi không có verified email. Khi Google trả email đã verified và không có conflict, Backend được phép populate `users.email` cùng `email_verified_at`; `password_hash` vẫn `NULL`, nên local email login chưa khả dụng cho đến khi Set Password thành công.

#### Chuyển từ đăng ký OTP sang social

- Email đang chờ xác minh và social trả cùng email đã xác minh: được phép hoàn tất đăng ký tạm, giữ phương thức local và liên kết social vào cùng tài khoản.
- Email đang chờ nhưng social trả email khác: người dùng phải chọn tiếp tục OTP hoặc hủy đăng ký tạm để dùng social.
- Khi email social khác email đang chờ, social không được xem là bằng chứng xác minh email đang chờ; người dùng phải xác nhận hủy đăng ký email trước khi tiếp tục.
- Chỉ hủy hoặc hoàn tất đăng ký tạm sau khi Backend xác minh social token thành công.

#### Đăng nhập local

- Người dùng đăng nhập bằng email và mật khẩu.
- Email chỉ được dùng đăng nhập khi `email_verified_at` khác `NULL`.
- Tài khoản chỉ dùng social có thể có `password_hash = NULL` và chưa thể đăng nhập local.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.

#### Liên kết phương thức đăng nhập

Người dùng đã đăng nhập có thể liên kết thêm:

- Email.
- Google.
- Facebook.

Quy tắc:

- Email login chỉ usable khi đồng thời có `users.email`, `email_verified_at` và `password_hash`.
- Link Email là một flow gồm nhập email, xác minh OTP và thiết lập mật khẩu ban đầu; ba giá trị email verified, password hash và challenge `COMPLETED` được ghi nguyên tử.
- Google/Facebook phải được xác minh trong phiên đang đăng nhập.
- `user_id` đích được lấy từ JWT hiện tại, không suy ra chỉ bằng email social.
- Email hoặc provider đã thuộc tài khoản khác phải bị từ chối.
- Không tự động gộp hai tài khoản đang hoạt động.
- Không được gỡ phương thức đăng nhập cuối cùng.
- Tài khoản social muốn dùng đăng nhập local phải thiết lập mật khẩu và xác minh email tương ứng.
- Trạng thái EMAIL authoritative là `NOT_LINKED`, `VERIFIED_NO_PASSWORD` hoặc `READY`; Frontend không suy luận trạng thái chỉ từ việc email có tồn tại.
- Set Password chỉ dành cho tài khoản có verified email nhưng chưa có password, yêu cầu reauthentication ngắn hạn/single-use bằng provider đã liên kết. Change Password yêu cầu current password và là nghiệp vụ khác Forgot Password.
- Phương thức usable được tính bằng EMAIL `READY`, provider GOOGLE đã liên kết và provider FACEBOOK đã liên kết. `canUnlink` luôn được Backend tính theo invariant còn ít nhất một phương thức usable.
- Set Password và Change Password thu hồi toàn bộ Refresh Token của account; Frontend xóa phiên local và yêu cầu đăng nhập lại. Access Token stateless đã phát hành vẫn có thể còn hiệu lực tới expiry vì hệ thống chưa dùng `tokenVersion`/blacklist.

Trạng thái triển khai Login Methods/Link Email/Set Password/Change Password: Backend, Database/API và Frontend `IMPLEMENTED`; automated unit test mục tiêu, Frontend test/lint/build `TESTED`; chưa `INTEGRATED` cho đến khi hoàn tất manual E2E bằng credential Google/Facebook và SMTP thật.

#### Khôi phục mật khẩu

- Chỉ tài khoản `ACTIVE` đã có `password_hash` và EMAIL tương ứng đã verified mới đủ điều kiện.
- Tài khoản social-only không dùng Forgot Password để tạo mật khẩu lần đầu.
- Identifier không tồn tại, chưa verified, social-only hoặc `BLOCKED` đều nhận response start trung tính và một decoy challenge có lifecycle thật.
- Challenge có TTL 15 phút; OTP 10 phút và không vượt challenge expiry; cooldown 60 giây; tối đa 5 lần sai; reset-authorized token 5 phút.
- OTP và token chỉ lưu dạng HMAC hash. Recovery flow token và reset-authorized token chỉ truyền qua `X-Auth-Flow-Token`.
- OTP delivery chạy bất đồng bộ sau commit, không enqueue cho decoy và không giữ transaction khi gọi Gmail SMTP.
- Complete đổi mật khẩu, đánh dấu token single-use và thu hồi toàn bộ Refresh Token trong cùng transaction; không tự đăng nhập hoặc cấp JWT mới.
- Access Token stateless đã phát hành còn hiệu lực tới expiry vì hệ thống hiện chưa dùng `tokenVersion`.
- `password_reset_tokens` là bảng legacy được giữ nguyên để audit; implementation mới không đọc hoặc ghi bảng này.

#### JWT và quản lý phiên

- Xác thực bằng JWT Access Token.
- Làm mới Access Token bằng Refresh Token.
- Refresh Token được lưu dưới dạng hash.
- Thu hồi Refresh Token khi đăng xuất.
- `users.role` tiếp tục phân tách loại tài khoản `USER` và `ADMIN`; tài khoản `ADMIN` được phân quyền chi tiết bằng RBAC.
- Access Token của ADMIN chứa snapshot `adminRoles` và hợp quyền `permissions` đã được Backend ký. Backend chỉ tin claims đã ký, không nhận role/permission từ header hoặc payload do Client tự khai báo.
- Khi gán/thu hồi role hoặc vô hiệu hóa admin, toàn bộ Refresh Token còn hiệu lực của tài khoản đích bị thu hồi; Access Token stateless đã phát hành tiếp tục có hiệu lực tới expiry ngắn hạn.

#### Hoàn tất hồ sơ ban đầu

- Sau khi tài khoản thật được tạo, Frontend điều hướng đến onboarding.
- Username, tên hiển thị và ngày sinh là thông tin bắt buộc.
- Username là định danh public duy nhất, dài 3–30 ký tự, chỉ gồm `a-z`, `0-9`, `_`, `.`, được trim và chuẩn hóa lowercase trước khi lưu; ký tự `@` chỉ do Frontend thêm khi hiển thị.
- Người dùng phải đủ 18 tuổi tại thời điểm Backend xử lý.
- Ảnh đại diện và giới thiệu cá nhân là tùy chọn.
- Hồ sơ chỉ được đánh dấu hoàn tất khi username, tên hiển thị và ngày sinh hợp lệ, đồng thời người dùng xác nhận.
- Trạng thái hoàn tất được lưu tại `user_profiles.profile_completed_at`.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép các API xác thực, token, đăng xuất và onboarding.

Trạng thái feature username: Backend và Frontend `IMPLEMENTED`; unit test, lint và build `TESTED`. Manual local đã xác minh availability, validation, race-condition, legacy hydration và hiển thị profile current/other. Chưa đánh dấu `INTEGRATED` cho toàn bộ feature cho đến khi hoàn tất manual E2E đăng ký local qua OTP và tạo user mới bằng Google/Facebook với credential thật.
- Các API mạng xã hội chính trả lỗi `PROFILE_NOT_COMPLETED` nếu hồ sơ chưa hoàn tất.

### 👤 2. Hồ sơ người dùng

- Hoàn tất hồ sơ ban đầu sau khi tạo tài khoản.
- Xem hồ sơ cá nhân.
- Xem hồ sơ người dùng khác.
- Cập nhật tên hiển thị.
- Cập nhật ảnh đại diện.
- Cập nhật phần giới thiệu cá nhân.
- Cập nhật ngày sinh.
- Xem số lượng người theo dõi.
- Xem số lượng người đang theo dõi.
- Xem danh sách bài viết đã đăng.

Tất cả hồ sơ trong MVP đều công khai. Email và dữ liệu xác thực không được trả trong API hồ sơ công khai.
Username được trả không kèm ký tự `@`, không được trùng giữa các hồ sơ và không được dùng thay `users.id` trong quan hệ nội bộ.

#### Academic Profile & Interests

Academic Profile & Interests có trạng thái Backend/Database/API/Frontend `IMPLEMENTED`, test tự động `TESTED` và đã manual test end-to-end thành công cho Onboarding, Edit Profile cùng Profile Display. Admin Academic Management V1 cũng đã tích hợp từ API đến giao diện quản trị.

- `schools`, `faculties`, `majors` và `interest_categories` là master data chuẩn hóa; profile chỉ lưu khóa ngoại, không lưu tên trường/khoa/ngành hoặc sở thích dạng text tự do.
- `user_profiles` có `school_id`, `faculty_id`, `major_id`, `entry_year` nullable; `user_interests` dùng khóa chính kép để chống trùng.
- School → Faculty → Major phải đúng hierarchy và toàn bộ master data được chọn phải ở trạng thái `ACTIVE`; School inactive ẩn toàn bộ Faculty/Major con khỏi public API, Faculty inactive ẩn toàn bộ Major con mà không cascade trạng thái xuống child.
- `entry_year` nếu có phải từ 1900 đến năm hiện tại theo UTC. Mỗi profile chọn tối đa 10 sở thích ACTIVE hợp lệ.
- Academic data vẫn là tùy chọn, không thay đổi username hoặc điều kiện cập nhật `profile_completed_at`.
- Admin Academic Management V1 cho phép ADMIN list/search/create/update/chuyển `ACTIVE`/`INACTIVE` đối với School, Faculty, Major và Interest Category; không hard delete và không làm mất reference hiện có của profile/user interests.
- Đây là nền dữ liệu cho Student Recommendation V1 dạng rule-based. AI/ML Recommendation, Location Recommendation và School Suggestion chưa được triển khai.

#### Student Recommendation V1 – “Có thể bạn biết”

Student Recommendation V1 có trạng thái Backend/API/Frontend `IMPLEMENTED`, test tự động `TESTED` và manual E2E `INTEGRATED`. Đây là thuật toán deterministic rule-based, không phải AI/ML.

- `GET /api/v1/recommendations/students?page=0&size=10` chỉ dành cho tài khoản role `USER`, `ACTIVE`, đã hoàn tất profile; current user luôn lấy từ JWT.
- Candidate phải là USER `ACTIVE`, đã hoàn tất profile, không phải current user, chưa được current user Follow và không có Block ở bất kỳ chiều nào. Restrict không loại candidate.
- Signal Academic dùng cùng School `+40`, Faculty `+25`, Major `+20`, Entry Year `+10`; chỉ master data `ACTIVE` tạo signal và việc so khớp dùng ID ổn định.
- Mỗi Interest `ACTIVE` chung cộng `+5`, tối đa `+25`. Mỗi tài khoản `ACTIVE` đã hoàn tất profile mà cả hai cùng Follow cộng `+3`, tối đa `+15`.
- Candidate phải có `score > 0`; không có random fallback. Thứ tự ổn định là score giảm dần, số Interest chung giảm dần, số kết nối chung giảm dần rồi `userId` tăng dần.
- Lọc, tính điểm, sắp xếp và phân trang chạy tại MySQL bằng projection; không lưu recommendation, score hoặc history vào database.
- Frontend hiển thị rail “Có thể bạn biết” dạng card nhỏ cố định bên phải trên các trang Feed ở desktop rộng, mặc định 4 candidate. Nút “Xem thêm” mở thêm tối đa 3 candidate trong vùng danh sách cuộn ẩn scrollbar mà không làm tăng chiều cao card; sau khi mở, nút đổi thành “Thu gọn” để trở về 4 candidate. Mỗi candidate hiển thị lý do semantic nổi bật, không hiển thị raw score; Follow thành công loại candidate khỏi danh sách hiện tại và lần fetch tiếp theo. Feed, Search, Notification, Profile và Saved Posts dùng chung một trục bắt đầu nội dung sau sidebar.

### 👥 3. Theo dõi người dùng

- Theo dõi người dùng.
- Bỏ theo dõi người dùng.
- Xem danh sách người theo dõi.
- Xem danh sách đang theo dõi.

Quy tắc:

- Không được theo dõi chính mình.
- Một người chỉ được theo dõi một tài khoản tối đa một lần.
- Quan hệ theo dõi có hiệu lực ngay.
- MVP chưa triển khai hồ sơ riêng tư và Follow Request.

### 🚫 3.1. Chặn người dùng

Trạng thái nghiệp vụ: `INTEGRATED` và `TESTED`. Quy tắc hiển thị bình luận lịch sử đã được triển khai thống nhất ở Backend/Frontend. Manual E2E tiếp tục thực hiện theo checklist trước khi phát hành.

- User Block là quan hệ có hướng: A chặn B được lưu khác với B chặn A.
- Hiệu lực về khả năng nhìn thấy và tương tác là hai chiều: chỉ cần tồn tại Block theo một trong hai hướng thì cả A và B không được xem hồ sơ, bài viết hoặc nội dung do đối phương tạo trong các luồng dành cho người dùng.
- User Block khác hoàn toàn với `users.status = BLOCKED`; trạng thái này do Admin khóa tài khoản ở cấp hệ thống.
- Không cho phép người dùng chặn chính mình; Block và Unblock phải có tính idempotent.
- Block xóa cả Follow A → B và Follow B → A trong cùng transaction.
- Trong thời gian tồn tại Block, hai bên không thể tạo Follow, Like/Unlike, Comment/Reply, Save/Unsave hoặc Notification mới liên quan đến nhau.
- Block không xóa Like hoặc Comment đã tồn tại trước thời điểm chặn khỏi cơ sở dữ liệu và không làm giảm `like_count` hoặc `comment_count`; các bản ghi này được giữ để bảo toàn lịch sử tương tác.
- Trong thời gian tồn tại Block, các bình luận và câu trả lời bình luận do một bên tạo phải bị ẩn khỏi bên còn lại, kể cả khi bình luận được viết trên bài do bên còn lại sở hữu.
- Việc ẩn bình luận do Block là kiểm soát hiển thị động tại query hoặc policy truy cập; không chuyển bình luận sang trạng thái `DELETED`, không xóa mềm và không tạo trạng thái ẩn vĩnh viễn cho bình luận.
- Nếu bình luận cha bị ẩn do Block thì các câu trả lời thuộc nhánh bình luận đó cũng phải bị ẩn để không xuất hiện câu trả lời mồ côi trên giao diện.
- Người thực hiện Block và người bị Block không còn nhìn thấy bài viết, Post Detail, Feed, Search, Liked, Saved, bình luận hoặc Notification của nhau trong các luồng dành cho người dùng.
- Khi Unblock, các Like và Comment lịch sử vẫn được giữ nguyên; bình luận được hiển thị trở lại nếu vẫn còn ở trạng thái hợp lệ và người xem có quyền truy cập bài viết.
- Unblock không tự khôi phục Follow, không tạo Notification và không tự tạo lại bất kỳ quan hệ xã hội nào đã bị xóa khi Block.
- Feed, hồ sơ, Post Detail, Search User/Post, follower/following, Liked, Saved, comment và notification phải áp dụng chính sách Block hai chiều ngay tại Backend; các danh sách phân trang phải lọc tại database thay vì lấy dữ liệu rồi lọc bằng Java.
- Private Account và Follow Request chưa được triển khai.

### 🔕 3.2. Hạn chế người dùng

Trạng thái nghiệp vụ: `INTEGRATED` và `TESTED`. Restrict là quan hệ một chiều; A hạn chế B không đồng nghĩa B hạn chế A.

- A và B vẫn tìm kiếm, xem hồ sơ, bài viết, Feed và tương tác với nhau theo quyền truy cập bình thường. Restrict không được dùng để lọc Feed hoặc Search.
- Restrict không hủy Follow, không thay đổi số follower/following và không suppress thông báo Follow.
- Restrict không xóa Like, Save, Comment, Reply, Follow hoặc Notification đã tồn tại trước thời điểm hạn chế.
- Like/Unlike, Save/Unsave, Comment và Reply vẫn hoạt động bình thường; Comment và Reply tiếp tục có trạng thái `PUBLISHED` và vẫn được tính vào bộ đếm.
- Khi A đã hạn chế B, Backend không tạo Notification `POST_LIKE`, `POST_COMMENT` hoặc `COMMENT_REPLY` do B gửi tới A. Vì không tạo bản ghi nên unread count, badge và WebSocket/event tương ứng cũng không tăng hoặc phát.
- Các thông báo quản trị, báo cáo, bảo mật, hệ thống và Follow không bị suppress bởi Restrict.
- B không được thông báo và API công khai không được trả bất kỳ trường nào cho phép B biết A đã hạn chế mình. Response quan hệ chỉ được phép trả `restrictedByMe` cho chính người đang xem.
- Block có độ ưu tiên cao hơn Restrict. Không được tạo Restrict khi có Block ở một trong hai chiều.
- Khi A Block B, quan hệ Restrict A → B bị xóa nhưng không tự ý xóa Restrict B → A. Unblock không tự khôi phục Restrict A → B.
- Unrestrict chỉ xóa quan hệ do chính current user tạo, không tạo Notification và không tạo bù các Notification đã bị bỏ qua.
- Restrict trong MVP không thay đổi nghiệp vụ nhắn tin và không triển khai Hidden Message Request hoặc cơ chế duyệt bình luận.

### 💬 3.3. Nhắn tin trực tiếp một-một

Trạng thái nghiệp vụ: Database, REST Core, realtime WebSocket, giao diện text, gửi/hiển thị ảnh, chia sẻ bài viết V1 và Typing Indicator Giai đoạn 1D `TESTED`. Text/ảnh/typing đã `INTEGRATED`; chia sẻ bài viết có migration MySQL thủ công nhưng chưa chạy trên database thật hoặc smoke test E2E hai trình duyệt.

- Conversation một-một hỗ trợ `TEXT`, chỉ ảnh hoặc ảnh kèm chú thích, và `POST_SHARE` tham chiếu bài viết kèm lời nhắn tùy chọn. Nội dung/chú thích tối đa 2.000 Unicode code point; mỗi message ảnh có tối đa 5 ảnh JPG/JPEG/PNG/WEBP, mỗi ảnh tối đa 10 MB; video và tài liệu chưa hỗ trợ.
- Chỉ tài khoản `USER`, `ACTIVE`, `account_type = NORMAL` và đã hoàn tất hồ sơ ở cả hai phía được dùng Messaging; `ADMIN` và Managed Social Identity không dùng Messaging như tài khoản xã hội, đồng thời người dùng không được nhắn chính mình.
- Một cặp người dùng chỉ có một conversation logic, chuẩn hóa bằng `participant_low_id` và `participant_high_id`; conversation và đúng hai member được tạo trong cùng transaction.
- A chỉ được mở hoặc bắt đầu conversation từ hồ sơ của B khi A và B đang Follow lẫn nhau. Conversation rỗng không xuất hiện trong Inbox và phải kiểm tra lại điều kiện Follow hai chiều khi gửi tin đầu tiên; sau tin đầu tiên, Unfollow không đóng conversation hoặc xóa lịch sử đã có.
- Database gồm `conversations`, `conversation_members`, `messages`, `message_attachments` và `media_cleanup_tasks`; `messages.shared_post_id` là tham chiếu nullable tới bài viết, dùng `ON DELETE SET NULL` để giữ lịch sử message khi bài bị xóa cứng. Cặp participant, `(sender_id, client_message_id)`, thứ tự attachment và định danh storage là duy nhất. `last_message_id` và `last_read_message_id` phải thuộc đúng conversation và được Service kiểm soát ngoài foreign key.
- REST Core dùng cùng POST JSON để gửi text hoặc `POST_SHARE` với `clientMessageId`, `content` tùy chọn và `sharedPostId`; Backend tự xác định type, kiểm tra khả năng xem bài của cả sender/recipient và áp dụng fingerprint idempotency gồm conversation, post và content. Cùng path POST với `multipart/form-data` nhận `images` tối đa 5. URL ảnh chat ngắn hạn chỉ được cấp qua `GET /api/v1/message-attachments/{attachmentId}/access` sau khi kiểm tra lại quyền.
- `GET /api/v1/conversations/share-recipients` trả danh sách phân trang gồm conversation đã có message và follower đủ điều kiện; loại self, Block hai chiều, tài khoản không phải `USER`/không `ACTIVE`/chưa onboarding. Restrict không ảnh hưởng danh sách.
- Danh sách conversation và lịch sử message dùng cursor Base64URL opaque, keyset pagination, không truy vấn tổng số. Page lịch sử được truy vấn mới nhất trước nhưng trả nội dung theo thời gian tăng dần trong từng page.
- Gửi message lấy sender từ JWT, nhận UUID v4 `clientMessageId` và Backend tự xác định `TEXT`/`IMAGE`. Payload ảnh có fingerprint gồm conversation, content, số ảnh, MIME, size và SHA-256 từng file; replay chính xác không upload/phát event lại, còn tái sử dụng key với payload khác trả `IDEMPOTENCY_KEY_REUSED`.
- Mark read chỉ tiến lên, không thay đổi `last_read_at` khi marker cũ được gửi lại. Unread được tính trực tiếp từ message của participant còn lại, không dùng counter denormalized.
- Client gửi message bằng REST API; WebSocket/STOMP chỉ dùng để nhận event realtime theo mô hình best-effort.
- REST API và MySQL là nguồn dữ liệu chuẩn; khi mất kết nối realtime, Frontend phải reconciliation lại bằng REST.
- Notification và Messaging dùng chung đúng một native WebSocket/STOMP connection trên mỗi tab tại endpoint `/ws`.
- Frontend subscribe Notification tại `/user/queue/notifications` và Messaging tại `/user/queue/messaging` trên cùng connection.
- JWT tiếp tục được xác thực tại STOMP `CONNECT`; principal name là chuỗi `users.id`. Client chỉ được `SEND` đúng destination `/app/messaging/typing`; mọi destination khác hoặc frame không có destination đều bị từ chối.
- User Block theo một trong hai chiều phải ẩn conversation, chặn history/send/read, loại khỏi unread và chặn nhận realtime; Unblock làm dữ liệu cũ xuất hiện lại. Open/send và Block khóa cặp user theo thứ tự ID ổn định để tuần tự hóa race; Restrict không ảnh hưởng Messaging.
- Message không tạo bản ghi Notification. `MESSAGE_CREATED` và `MESSAGES_READ` chỉ phát `AFTER_COMMIT`; payload message bổ sung metadata `attachments` nhưng không có URL/storage ID. `POST_SHARE` trả snapshot được hydrate theo từng viewer; khi viewer không còn quyền xem hoặc bài không còn tồn tại, payload chỉ đánh dấu `sharedPostUnavailable` và không làm hỏng history. Broker lỗi không rollback REST/MySQL đã commit và không thay đổi contract Notification.
- Upload ảnh dùng Cloudinary `authenticated` ngoài transaction MySQL dài. Khi transaction ngắn lưu message/attachment thất bại, Backend xóa bù ngay; lỗi xóa được ghi durable cleanup task và scheduler retry.
- Media chat không lưu hoặc trả URL công khai vĩnh viễn. Endpoint access chống IDOR, kiểm tra USER/ACTIVE/onboarding/membership/Block và cấp signed URL với TTL cấu hình, mặc định 5 phút.
- Typing Indicator Giai đoạn 1D đã hoàn thành qua `TYPING_STARTED`/`TYPING_STOPPED`, không lưu database, không replay và không thay đổi unread.
- Nút Chia sẻ bài viết mở modal chọn một người nhận, hỗ trợ tìm kiếm/phân trang, gửi qua DM, sao chép URL canonical và Facebook Share Dialog/Web Sharer; không yêu cầu hoặc lưu Facebook Access Token. Chia sẻ không tạo Notification và không tăng Repost count.
- Tài liệu, video, Message Request, Hidden Message Request, online status, delivered status, recall, xóa conversation phía cá nhân và report message chưa thuộc phạm vi.

### 📝 4. Quản lý bài viết

- Tạo bài viết.
- Xem chi tiết bài viết.
- Chỉnh sửa bài viết trong 15 phút sau khi đăng.
- Frontend hiển thị countdown thời gian sửa còn lại cạnh hành động chỉnh sửa và tự ẩn hành động khi hết 15 phút; Backend tính deadline bằng UTC và vẫn là nơi quyết định cuối cùng.
- Xóa mềm bài viết.
- Đăng nội dung văn bản tối đa 500 ký tự.
- Tải lên tối đa 4 media cho một bài viết, gồm ảnh và video.
- Mỗi bài có tối đa 4 ảnh hoặc tối đa 1 video trong tổng giới hạn 4 media.
- Ảnh hỗ trợ JPG, JPEG, PNG, WEBP và tối đa 10 MB mỗi file.
- Video hỗ trợ MP4, WebM, tối đa 100 MB và dài không quá 3 phút.
- Gắn tối đa một hashtag.
- Tùy chọn gắn tối đa một địa điểm Google Places đã được Frontend chọn.

Bài viết phải có ít nhất một trong hai thành phần:

- Nội dung văn bản.
- Hình ảnh.

Trạng thái bài viết:

- `PUBLISHED`: bài viết đang hoạt động.
- `HIDDEN`: bài viết bị quản trị viên ẩn.
- `DELETED`: bài viết đã được tác giả xóa mềm.

#### Địa điểm gắn với bài viết – P1

- Quan hệ dữ liệu là `Location 1 — N Post`: một địa điểm có thể được nhiều bài viết sử dụng; một bài viết có thể không có hoặc có tối đa một địa điểm.
- Frontend gửi object Location tùy chọn gồm `placeId`, `displayName`, `formattedAddress`, `latitude` và `longitude`.
- Backend validate dữ liệu, chuẩn hóa chuỗi và chỉ dùng `placeId` làm natural unique key. Backend không dùng tên hoặc tọa độ để xác định trùng và chưa gọi Google Places API để xác minh trong giai đoạn này.
- Nếu `google_place_id` đã tồn tại, Backend dùng lại `locations`; nếu chưa tồn tại, Backend tạo một bản ghi mới. Các bài dùng cùng Google Place ID không được tạo Location riêng.
- `posts.location_id` cho phép `NULL`, không có unique constraint và tham chiếu `locations.id` bằng khóa ngoại `ON DELETE SET NULL`.
- Entity `Post` ánh xạ Location bằng `@ManyToOne(fetch = FetchType.LAZY)` và `@JoinColumn(name = "location_id")`; không dùng `@OneToOne`, `CascadeType.REMOVE` hoặc `orphanRemoval` cho quan hệ này. Mapping `authorProfile` hiện tại không thuộc thay đổi này.
- Khi cập nhật bài trong giới hạn 15 phút và đúng quyền tác giả, Location hỗ trợ ba hành động: `KEEP` giữ nguyên, `REPLACE` resolve theo Place ID rồi thay thế, và `REMOVE` gỡ quan hệ bằng cách đặt `location_id = NULL`.
- Khi cập nhật bài trong cùng giới hạn quyền và 15 phút, người dùng có thể giữ, gỡ media cũ hoặc thêm ảnh/video mới; tổng media sau cập nhật vẫn phải tuân thủ các giới hạn media của Post.
- Soft delete hoặc hard delete Post không xóa Location. Không cascade remove, không dùng `orphanRemoval` và không tự động xóa Location không còn được tham chiếu.
- Location hoặc `null` phải xuất hiện nhất quán trong response tạo bài, chi tiết bài, Feed For You, Feed Following, bài trên hồ sơ, bài đã lưu, bài đã thích, kết quả tìm kiếm Post và chi tiết Post dành cho Admin.
- Admin Post Detail hiển thị Location hiện tại. Report snapshot chưa lưu Location trong đợt này.
- Nearby Discovery V1 và Discovery Map V1 dùng dữ liệu Location hiện có để khám phá Post; Feed tùy chỉnh theo Location, trang Location, địa điểm phổ biến, quản trị Location và đồng bộ định kỳ với Google Places vẫn ngoài phạm vi.

### ❤️ 5. Tương tác bài viết

- Like và Unlike bài viết.
- Xem danh sách bài viết đã thích của chính mình.
- Thêm bình luận.
- Xem danh sách bình luận.
- Xóa bình luận của chính mình.
- Lưu và bỏ lưu bài viết.
- Xem danh sách bài viết đã lưu.
- Repost và Unrepost bài viết của người khác.
- Xem tab bài đã đăng lại trên hồ sơ.

Quy tắc:

- Mỗi người chỉ được Like một bài tối đa một lần.
- Mỗi người chỉ được lưu một bài tối đa một lần.
- Mỗi người chỉ được Repost một bài tối đa một lần; Repost và Unrepost là idempotent.
- Chỉ được Repost bài `PUBLISHED` có quyền xem và không được Repost bài của chính mình.
- Repost chỉ lưu quan hệ tham chiếu đến bài gốc, không sao chép content, media, hashtag hoặc Location.
- Bài gốc `HIDDEN` hoặc `DELETED` không được hiển thị qua Repost.
- Không được tương tác với bài viết đã bị ẩn hoặc xóa.
- Danh sách bài viết đã lưu chỉ hiển thị với chủ tài khoản.
- Danh sách bài viết đã thích chỉ hiển thị với chủ tài khoản.
- Frontend cập nhật ngay trạng thái và bộ đếm Like, Comment, Save, Repost từ response REST; các tab cùng trình duyệt đồng bộ qua `BroadcastChannel` và vô hiệu hóa đúng cache danh sách liên quan.
- Khi tab trở lại foreground, Frontend reconciliation danh sách đang hiển thị bằng REST. Notification WebSocket của Like, Comment, Reply và Repost kích hoạt reconciliation Post cho người nhận; MySQL và REST API vẫn là nguồn dữ liệu chuẩn.

#### AI-assisted Content Moderation V1

Trạng thái: Backend/Frontend/local AI service `IMPLEMENTED`, automated test và smoke inference thật `TESTED`; chưa `INTEGRATED` vì chưa chạy toàn bộ manual E2E trên giao diện với local service thật.

- Áp dụng synchronous pre-publication moderation cho text khi tạo Post, sửa text Post, tạo Comment và tạo Reply. Post update chỉ gọi lại provider khi text sau chuẩn hóa thực sự thay đổi.
- V1 chỉ kiểm tra text; không kiểm tra image, video, avatar, bio, message, hashtag, Location hoặc search query.
- AI provider chỉ phân loại/chấm điểm. `ModerationPolicy` của Backend mới quyết định đúng ba kết quả `ALLOW`, `WARNING`, `BLOCK`; Post/Comment service chỉ phụ thuộc abstraction dùng chung `ContentModerationService`.
- `ALLOW` tiếp tục transaction lưu nội dung. `WARNING` và `BLOCK` đều không lưu nội dung; V1 không có lựa chọn đăng bất chấp cảnh báo.
- Timeout, provider unavailable, cấu hình thiếu hoặc response không hợp lệ trả `CONTENT_MODERATION_UNAVAILABLE` và fail closed: nội dung chưa được kiểm tra không được publish.
- Error contract dùng `CONTENT_MODERATION_WARNING`, `CONTENT_POLICY_VIOLATION`, `CONTENT_MODERATION_UNAVAILABLE`; response chỉ có decision/category nội bộ an toàn, không expose confidence, model hoặc raw provider response.
- Frontend chờ response Backend trước khi thêm Post/Comment/Reply, giữ nguyên draft và media/hashtag/Location khi bị từ chối, đồng thời không tăng bộ đếm optimistic cho nội dung rejected.
- External inference và upload media nằm ngoài transaction MySQL dài. Sau khi moderation cho phép, Backend mở transaction ngắn, recheck quyền/trạng thái/deadline và persist atomically.
- Rejected content không tạo Post/Comment/Reply, không làm trigger tăng `comment_count`, không tạo Notification hoặc realtime event.
- V1 không tự khóa user, không tạo Report/Moderation Case, không ẩn nội dung cũ và không thay thế quy trình Report/Admin human moderation hiện tại.
- Provider duy nhất là FastAPI local chạy checkpoint tiếng Việt `visolex/phobert-v2-hsd`; text không được gửi tới dịch vụ AI tính phí bên ngoài. Checkpoint chỉ phân loại `CLEAN/OFFENSIVE/HATE`, tương ứng `ALLOW/WARNING/BLOCK`; đây không phải bộ kiểm duyệt an toàn tổng quát cho media hoặc mọi nhóm rủi ro.
- Input vượt context 256 token được chia theo token và tổng hợp theo mức nghiêm trọng nhất để không bỏ mất vi phạm ở phần đuôi. Model nạp một lần khi FastAPI khởi động; `/health` kiểm tra process và `/ready` chỉ thành công khi model sẵn sàng.
- URL local và timeout đều lấy từ environment; Post/Comment service tiếp tục chỉ phụ thuộc abstraction moderation dùng chung.

Checklist kiểm thử thủ công: [`docs/testing/AI-CONTENT-MODERATION-V1-E2E-CHECKLIST.md`](docs/testing/AI-CONTENT-MODERATION-V1-E2E-CHECKLIST.md).
Bộ dữ liệu E2E local: [`docs/testing/LOCAL-AI-CONTENT-MODERATION-V1-DATASET.md`](docs/testing/LOCAL-AI-CONTENT-MODERATION-V1-DATASET.md).

### 📰 6. Bảng tin

#### Feed Following

- Hiển thị activity bài gốc và Repost của những người dùng đang được theo dõi.
- Item `ORIGINAL` chứa bài gốc; item `REPOST` chứa `activityAt`, `repostedAt`, `repostedBy` và projection `post` của bài gốc.
- Sắp xếp giảm dần theo thời điểm activity với khóa tổng ổn định `activityAt`, `itemRank`, `actorId`, `postId`.
- Không hiển thị bài viết bị ẩn hoặc bị xóa.
- Dùng Cursor Pagination để cuộn vô hạn; Frontend chỉ gửi lại nguyên cursor opaque do Backend phát hành.

#### Feed For You

- Personalized For You V1 là ranking deterministic rule-based, trạng thái Backend `IMPLEMENTED`, automated test `TESTED`; chưa đánh dấu `INTEGRATED` cho đến khi hoàn tất manual E2E.
- Candidate là bài `PUBLISHED` của tác giả `ACTIVE`, đã hoàn tất profile và không có Block ở bất kỳ chiều nào với viewer. Restrict không lọc Feed; bài của chính viewer tiếp tục giữ behavior hiện hành.
- Feed xếp hạng theo độ mới, engagement của Post, quan hệ Follow, Academic affinity, common active Interests, lịch sử viewer tương tác với các bài khác của cùng tác giả và lịch sử tương tác với đúng cùng hashtag ID.
- Academic và Interest chỉ là ranking bonus, không phải hard filter. Cold start vẫn nhận Feed bằng độ mới và engagement.
- Chỉ master data Academic/Interest `ACTIVE` tạo positive signal; so khớp Academic bằng ID ổn định, không bằng tên.
- For You chỉ rank Post gốc; Repost activity tiếp tục chỉ thuộc Following Feed.
- Không sử dụng AI, Machine Learning, embedding, collaborative filtering, cache recommendation hoặc background recommendation job.

Công thức V1:

```text
FeedScore = RecencyScore
          + min(likeCount, 20)
          + min(commentCount, 10) × 2
          + min(repostCount, 10) × 2
          + FollowingAuthorScore
          + AcademicAffinityScore
          + CommonInterestScore
          + HistoricalAuthorAffinityScore
          + ExactHashtagBehaviorAffinityScore
```

Recency dùng các bucket `60/50/35/20/10/0` tại các mốc `6 giờ/1 ngày/3 ngày/7 ngày/14 ngày`; Follow cộng `30`; Academic tối đa `28`; common Interests tối đa `10`; historical author tối đa `31`; exact hashtag behavior tối đa `17`. Candidate Post hiện tại bị loại khỏi lịch sử của chính nó.

#### Location-aware Discovery V1 – Nearby

Trạng thái Backend và Frontend `IMPLEMENTED`, automated test `TESTED`; chưa đánh dấu `INTEGRATED` cho đến khi hoàn tất manual E2E với MySQL, quyền Geolocation trình duyệt và dữ liệu Location thực tế.

- Endpoint `GET /api/v1/discovery/nearby` chỉ dành cho USER `ACTIVE`, đã hoàn tất profile và luôn lấy viewer từ JWT.
- Query bắt buộc `latitude`, `longitude`; `radiusKm` mặc định 5 và chỉ nhận `1`, `3`, `5`, `10`, `20`; `limit` mặc định 10, từ 1 đến 20; `cursor` là tùy chọn.
- Candidate là Post gốc `PUBLISHED` có Location, tác giả role `USER`, trạng thái `ACTIVE`, đã hoàn tất profile và không có Block ở bất kỳ chiều nào với viewer. Restrict không loại candidate; Repost không tạo item riêng.
- MySQL thực hiện bounding-box pre-filter, Haversine exact-radius filter, sắp xếp và keyset pagination; không dùng OFFSET, COUNT hoặc Java post-filter.
- Khoảng cách exact quyết định inside/outside radius. `distanceMeters = ROUND(exactDistanceMeters)` được dùng thống nhất cho response, ORDER BY và cursor, sau đó tie-break bằng `publishedAt DESC`, `postId DESC`.
- Cursor Base64URL versioned giữ `distanceMeters`, `publishedAt`, `postId` và SHA-256 fingerprint của tọa độ chuẩn hóa cùng radius; cursor khác tọa độ/radius hoặc malformed trả `INVALID_CURSOR`.
- Tọa độ viewer chỉ tồn tại trong request/query processing; không lưu database, analytics payload, Notification, WebSocket hoặc log nghiệp vụ.
- Nearby độc lập với Feed For You và không thay đổi score, candidate, `rankingAt` hoặc cursor của Personalized For You V1.
- Frontend tích hợp tab `Gần bạn` trong Feed, chỉ gọi Geolocation khi mở tab hoặc bấm cập nhật vị trí; tọa độ chỉ giữ trong memory runtime. UI hỗ trợ năm radius đã khóa, state tường minh, AbortController/chống response cũ và Infinite Scroll giữ nguyên cursor opaque.
- Canonical demo seed dành mười Post `PUBLISHED`, chia đều cho năm địa điểm công cộng quanh đường Cao Lỗ, Quận 8. Có thể dùng tọa độ Trường Đại học Công nghệ Sài Gòn trong seed (`10.7387550`, `106.6777880`) để kiểm thử radius 1/3/5 km; các `google_place_id` bắt đầu bằng `demo-caolo-` và tọa độ chỉ là dữ liệu demo gần đúng, không giả làm định danh Google chính thức.

#### Discovery Map V1

Trạng thái Backend và Frontend `IMPLEMENTED`; automated unit/controller/security/query-contract test, MySQL `EXPLAIN` read-only, Frontend test/lint/build `TESTED`. Manual E2E ngày 2026-08-14 với JWT, MySQL và Google Maps thật đã đạt cho render/explicit viewport search, marker/count, cluster click, Location Posts load-more 8 trang, marker và viewport race, Geolocation denied, Block, Restrict, HIDDEN, Repost, desktop/mobile cùng hồi quy Nearby/For You/Following/Places. Geolocation success chưa thể thực thi vì browser tích hợp từ chối location và Chrome/Edge DevTools không khả dụng; DELETED giữ automated-only do không có fixture phá hủy có thể phục hồi an toàn. TIMEOUT/POSITION_UNAVAILABLE và `truncated = true` cũng chỉ có automated coverage theo phạm vi chấp nhận, nên toàn feature vẫn `NOT INTEGRATED`. Các MySQL integration test có mutation chỉ chạy khi có database test riêng qua `AUTH_TEST_DB_*`.

- `GET /api/v1/discovery/map/locations` nhận viewport `north`, `south`, `east`, `west`; V1 yêu cầu `south < north`, `west < east` và chưa hỗ trợ viewport vượt anti-meridian.
- Marker đại diện cho Location, được MySQL aggregation từ Post gốc `PUBLISHED` của author role `USER`, trạng thái `ACTIVE`, đã hoàn tất profile và không có Block ở bất kỳ chiều nào với viewer. Restrict không lọc; Repost không sinh candidate riêng.
- `postCount` và `latestPostAt` chỉ tính trên Post viewer thực sự có quyền xem. Marker sắp xếp theo `latestPostAt DESC`, `locationId DESC`.
- Marker endpoint không phân trang, lấy tối đa 200 Location bằng chiến lược fetch 201 rồi trả `truncated`; không dùng truy vấn tổng `COUNT(*)`.
- `GET /api/v1/discovery/map/locations/{locationId}/posts` trả PostCard chuẩn bằng keyset `publishedAt DESC`, `postId DESC`, `limit` mặc định 10 và tối đa 20.
- Cursor Location Posts là Base64URL opaque, versioned, chứa `locationId`, `publishedAt`, `postId`; cursor dùng sai Location hoặc malformed trả `INVALID_CURSOR`.
- Location Posts tái sử dụng `FeedPostBatchLoader`, batch-load theo ID và khôi phục thứ tự query; không dùng OFFSET, COUNT tổng hoặc query theo từng Post.
- Map chỉ nhận viewport; không lưu vị trí hiện tại của viewer/author, không tạo lịch sử vị trí và không phát tọa độ qua Notification, WebSocket, Messaging hoặc analytics payload.
- Frontend giữ route Nearby `/feed/nearby` và bổ sung `/feed/map` dưới hai chế độ `Gần bạn`/`Bản đồ`; Nearby không đổi contract hoặc thời điểm xin Geolocation.
- Google Places picker và Map dùng chung một Maps JavaScript SDK loader từ `VITE_GOOGLE_MAPS_API_KEY`; marker được gom cụm phía client bằng `@googlemaps/markerclusterer` và cluster click chỉ zoom/fit viewport.
- Map mở ở tâm canonical seed nhưng không tự gọi marker API. Pan/zoom chỉ đánh dấu viewport mới; API marker chỉ chạy khi người dùng bấm `Tìm trong khu vực này`, có AbortController/request ID chống response cũ và cảnh báo khi `truncated = true`.
- Marker click mở panel desktop hoặc bottom sheet mobile, hiển thị `postCount` authoritative và PostCard chuẩn. Đổi marker reset cursor/danh sách, hủy request cũ; Location Posts gửi lại nguyên cursor opaque và khử trùng Post theo ID.
- Nút `Vị trí của tôi` mới gọi một lần `getCurrentPosition`; tọa độ chỉ giữ trong React state runtime, tạo marker riêng và không tự tìm marker cho đến khi người dùng xác nhận tìm trong viewport mới.

Các danh sách bài viết dùng Infinite Scroll gồm Feed For You, Feed Following, bài trên hồ sơ,
bài đã lưu, bài đã thích và kết quả Search bài viết/hashtag. Các API này dùng Cursor Pagination:

- Request đầu: `?limit=10`.
- Request tiếp theo: `?limit=10&cursor=<opaque-cursor>`.
- `limit` mặc định 10, từ 1 đến 20.
- Response dữ liệu: `{ "content": [], "nextCursor": null, "hasNext": false }`.
- Cursor do Backend tạo dưới dạng Base64URL opaque; Client không tự tạo hoặc sửa cursor.
- Feed For You dùng cursor versioned giữ `rankingAt`, `score`, `publishedAt`, `postId`; mọi trang trong cùng phiên cuộn tái sử dụng `rankingAt` để recency không đổi bucket. Các danh sách theo thời gian
  giữ `createdAt`, `postId`.
- Feed Following giữ đủ khóa activity `activityAt`, `itemRank`, `actorId`, `postId`.
- Backend dùng keyset và lấy `limit + 1`, không dùng `page`, `offset` hoặc truy vấn tổng `COUNT(*)`.
- Cursor không hợp lệ trả mã nghiệp vụ `INVALID_CURSOR`.

Các endpoint đã chuyển:

- `GET /api/v1/feeds/for-you`.
- `GET /api/v1/feeds/following`.
- `GET /api/v1/users/{userId}/posts`.
- `GET /api/v1/users/{userId}/reposts`.
- `GET /api/v1/posts/saved`.
- `GET /api/v1/posts/liked`.
- `GET /api/v1/search/posts`.

Search người dùng, bình luận, Follow và Admin vẫn dùng `PageResponse` vì giao diện hiện tại
không dùng Infinite Scroll hoặc cần metadata tổng số/trang.

### #️⃣ 7. Hashtag

- Mỗi bài viết có tối đa một hashtag.
- Xóa toàn bộ ký tự `#` ở đầu.
- Trim khoảng trắng Unicode.
- Gộp nhiều khoảng trắng liên tiếp thành một.
- Chuẩn hóa Unicode NFC.
- Chuyển về chữ thường.
- Hỗ trợ tiếng Việt có dấu và hashtag gồm nhiều từ.
- Giới hạn tối đa 100 code point.
- Hashtag không được tính là nội dung bài viết.
- Có API gợi ý hashtag liên quan, không phân trang.
- Người dùng có thể xem danh sách bài viết theo hashtag.

### 🔍 8. Tìm kiếm

- Tìm kiếm người dùng theo tên hiển thị.
- Tìm kiếm bài viết theo nội dung.
- Tìm kiếm bài viết theo hashtag.
- Search người dùng dùng `PageResponse`; Search bài viết/hashtag dùng Cursor Pagination và Infinite Scroll.
- MVP sử dụng MySQL làm nguồn tìm kiếm.
- Elasticsearch được đưa vào hướng phát triển sau khi hệ thống cốt lõi ổn định.

### 🚩 9. Báo cáo vi phạm

- Mỗi lần báo cáo bài viết tạo một dòng `reports` độc lập, giữ riêng reporter, reason, description và snapshot bằng chứng.
- Mỗi Report thuộc một `Moderation Case`; nhiều Report cùng bài được gắn vào case `OPEN` hiện tại.
- Một bài chỉ có tối đa một case `OPEN` tại một thời điểm. Case mới chỉ được tạo khi không còn case mở.
- Cùng một người không được báo cáo lại bài đang có Report thuộc case `OPEN`; sau khi case cũ được giải quyết có thể báo cáo lại nếu bài vẫn đủ điều kiện.

Báo cáo trang cá nhân:

- Chỉ được báo cáo trang cá nhân của USER khác; không được tự báo cáo hoặc báo cáo khi hai tài khoản đang có quan hệ Block.
- Sáu lý do cố định: nội dung không được phép, giả mạo, chưa đủ tuổi tối thiểu, lừa đảo/gian lận, thông tin sai sự thật, bạo lực/tổ chức nguy hiểm.
- Mỗi reporter chỉ có tối đa một báo cáo `PENDING` cho cùng một trang cá nhân.
- Mỗi trang cá nhân chỉ có một `profile_report_cases`; mọi `profile_reports` từ nhiều reporter được gom vào case này.
- `profile_reports` giữ từng reporter, lý do và snapshot; Admin đồng thời xem hồ sơ hiện tại, danh sách người báo cáo và các bài viết hiện tại của target.
- Báo cáo mới mở lại case đã kết luận; lịch sử kết luận vẫn được giữ trong `admin_actions`.
- Admin kết luận đồng thời toàn bộ lượt `PENDING` trong case thành `RESOLVED` hoặc `REJECTED`.
- Khi xác nhận trang cá nhân vi phạm, Admin có quyền khóa ngay tài khoản target trong cùng transaction; nếu không chọn khóa thì chỉ kết luận case.

Trạng thái báo cáo:

- `PENDING`.
- `RESOLVED`.
- `REJECTED`.

Trạng thái Moderation Case:

- `OPEN`: đang chờ Admin đưa ra quyết định và tiếp tục nhận Report hợp lệ mới.
- `RESOLVED_NO_VIOLATION`: Admin kết luận bài không vi phạm; các Report trong case chuyển `REJECTED`.
- `RESOLVED_ACTION_TAKEN`: Admin kết luận có vi phạm và đã áp dụng hành động; các Report chuyển `RESOLVED`.

#### Community Standards & Account Standing — Backend/Frontend `IMPLEMENTED`, automated test `TESTED`

- `/policies/community-standards` là trang chính sách sử dụng nội bộ public cho Guest, USER và ADMIN; đây không phải tuyên bố về một bộ điều khoản pháp lý đầy đủ. Login, Register và Settings đều có entry point tới trang này.
- Mỗi Moderation Case chuyển thành công từ `OPEN` sang `RESOLVED_ACTION_TAKEN` được tính đúng một lần vi phạm bài viết cho tác giả, không phụ thuộc số Report/reporter trong case. `reports`, `report_count`, Notification, Profile Report và state Frontend không phải nguồn đếm.
- Lần vi phạm thứ nhất tạo system Notification cảnh báo `1/3`; lần thứ hai tạo cảnh báo cuối `2/3`. Hai Notification dùng type riêng để nội dung lịch sử không thay đổi theo số đếm mới và deep-link tới `/settings/account-status`.
- Khi tổng số case vi phạm đạt từ 3, Backend không gửi thông điệp “còn 0 lần” mà tự động khóa với lý do `REPEATED_VIOLATION`, thu hồi Refresh Token, ghi Account Status History, Admin Action và system Notification.
- `GET /api/v1/account/standing` chỉ đọc tài khoản USER hiện tại từ JWT và trả `status`, `confirmedViolationCount`, `violationThreshold`, `remainingBeforeBlock`; không nhận `userId` và không trả reporter, ghi chú nội bộ hoặc metadata kiểm duyệt.
- `/settings/account-status` hiển thị dữ liệu authoritative từ API, gồm loading, error/retry và link về Tiêu chuẩn cộng đồng. Frontend không suy ra số vi phạm từ Notification.
- Khóa thủ công vẫn dùng lý do hiện hành, lịch sử trạng thái, Admin Action, thu hồi Refresh Token và Notification. Auth trả `ACCOUNT_BLOCKED` kèm `reasonCode`, `blockedAt`, thông điệp public-safe; màn hình Login hiển thị dedicated blocked state, không lộ Admin, reporter hoặc internal note.
- Ẩn/khôi phục Post, chỉnh hồ sơ USER, khóa/mở khóa tài khoản đều tạo system Notification bằng hạ tầng MySQL + realtime best-effort `AFTER_COMMIT` hiện có. Mở khóa không xóa hoặc đặt lại lịch sử vi phạm đã xác nhận.
- Admin UI yêu cầu xác nhận cho cả khóa và mở khóa. User Block A→B vẫn độc lập hoàn toàn với Account Block bằng `users.status = BLOCKED`.

Admin xử lý trực tiếp từ `OPEN` sang một trong hai kết quả cuối. Không có bước tiếp nhận, trạng thái
`IN_REVIEW`, trạng thái `CLOSED`, `assigned_admin_id` hoặc `closed_at`. Case đã giải quyết không nhận
Report mới và không được xử lý lại.

Gửi báo cáo không tự động làm ẩn bài viết. Quản trị viên là người đưa ra quyết định xử lý cuối cùng.
Luồng tạo Report khóa bản ghi Post bằng `PESSIMISTIC_WRITE`, sau đó tìm/tạo case, tạo Report và cập
nhật `report_count` trong cùng transaction. Unique generated key trên case là lớp bảo vệ database để
không tồn tại hai case `OPEN` cho cùng bài.

### 🛡️ 10. Quản trị hệ thống

#### RBAC quản trị — Backend/Database/Frontend `IMPLEMENTED`, automated test `TESTED`

- Chỉ tài khoản được tạo từ cấu hình Bootstrap được giữ `SUPER_ADMIN` và có quyền tạo tài khoản quản trị hỗ trợ, gán/thu hồi role hoặc cấu hình permission. Các tài khoản `ADMIN` hỗ trợ có thể mang nhiều vai trò nghiệp vụ nhưng không được nhận `SUPER_ADMIN` hay quyền phân quyền tiếp.
- `SUPER_ADMIN` nhận toàn bộ permission hiện có và permission bổ sung trong tương lai; `USER_MANAGER` có Dashboard Tổng quan, quản lý người dùng và thống kê người dùng; `MODERATOR` có Dashboard Tổng quan, bài viết, hashtag, báo cáo và quyền duyệt đề xuất kiểm duyệt; `ADS_MANAGER` chỉ có Dashboard Tổng quan vì module Ads chưa được triển khai.
- `COLLABORATOR` là role hệ thống có bộ permission cố định và có khu vực riêng tại `/admin/collaborator/**`; role này không dùng Dashboard Tổng quan và không nhận permission của module quản trị khác. Khi Master Admin gán role, Backend tự tạo hoặc kích hoạt Managed Social Identity trong cùng transaction, nên tài khoản nhận ngay toàn bộ chức năng: tạo, sửa, xóa mềm bài viết của chính danh tính; xem bình luận/reply công khai trên bài `PUBLISHED` của mình; xem analytics thật; khám phá; like/unlike; comment/reply; repost/unrepost; xem/tìm hashtag và gửi/xem đề xuất kiểm duyệt của chính mình.
- Ranh giới actor là bắt buộc: `ADMIN/NORMAL` chỉ đăng nhập và quản trị, không được dùng API xã hội hoặc xuất hiện như hồ sơ công khai; `USER/NORMAL` là người dùng xã hội thông thường; `USER/MANAGED` là danh tính công khai của Collaborator, không đăng nhập trực tiếp và chỉ được điều khiển qua `/api/v1/admin/collaborator/**`. Các truy vấn Search, Profile, Follow và danh sách public phải loại `users.role = ADMIN`.
- Luồng Moderation Suggestion có Backend/Database/Frontend `IMPLEMENTED`, automated test và Frontend lint/build mục tiêu `TESTED`: Cộng tác viên dùng “Khám phá nội dung” để xem hoặc tìm kiếm bài viết theo nội dung và gửi một đề xuất gồm lý do cùng mô tả tùy chọn, sau đó theo dõi tại “Đề xuất của tôi”; Moderator dùng “Đề xuất kiểm duyệt” để xem rõ tên hiển thị, username, avatar và các vai trò hiện tại của người đề xuất, xem chi tiết rồi chấp nhận hoặc từ chối. Sau khi xử lý, chi tiết đề xuất cũng hiển thị người xử lý cùng vai trò hiện tại; API không trả email hoặc dữ liệu xác thực trong các actor này. Tìm kiếm được thực hiện phía Backend với cursor và luôn dùng Managed Social Identity làm viewer. Mỗi cộng tác viên không thể tạo hai đề xuất `PENDING` cho cùng một bài. Chấp nhận đề xuất chỉ ghi nhận kết quả đánh giá, không tự động ẩn bài. Thông báo tạo mới điều hướng Moderator tới chi tiết đề xuất; thông báo kết quả điều hướng Cộng tác viên về đề xuất tương ứng.
- Mỗi Collaborator có đúng một Managed Social Identity. Khi thu hồi role, Backend vô hiệu hóa liên kết nhưng không xóa Social User, bài viết hoặc lịch sử; khi gán lại role, danh tính cũ được kích hoạt lại. Danh tính này là user nội bộ loại `MANAGED`, không đại diện cá nhân nên không lưu ngày sinh, không có thông tin đăng nhập và không được phát hành Refresh Token; hồ sơ vẫn được đánh dấu sẵn sàng để nội dung xuất hiện trong feed. Mọi bài viết/tương tác vẫn đi qua cùng policy, repository, counter và notification của user thường. Client không được gửi `authorId` hoặc `socialUserId` để lựa chọn actor.
- Managed Collaborator Public Identity V1 có trạng thái Backend/API/Frontend `IMPLEMENTED`, automated test mục tiêu, Frontend lint/build `TESTED`; chưa `INTEGRATED` cho đến khi hoàn tất manual E2E với MySQL, hai tài khoản Browser và WebSocket thật. Public identity luôn đọc `username`, `display_name`, avatar và bio đã persist từ `user_profiles` của `admin_social_identities.social_user_id`; username được gán đúng một lần khi tạo danh tính và bất biến về sau. Khu vực `/admin/collaborator/profile` là trang “Hồ sơ cộng tác viên” hợp nhất, chỉ cho chỉnh tên hiển thị, avatar và bio public, đồng thời cho xem thông tin tài khoản đăng nhập và đổi mật khẩu. Tài khoản chỉ có role `COLLABORATOR` không hiển thị thêm mục “Hồ sơ của tôi”; route `/admin/profile` cũ tự chuyển về trang hợp nhất. Backend tự resolve actor từ JWT Admin hiện tại.
- Badge public `COLLABORATOR` do Backend quyết định và chỉ active khi Social User còn là `MANAGED`, liên kết `admin_social_identities` là `ACTIVE`, Admin owner còn `ACTIVE` và vẫn có role `COLLABORATOR`. Thu hồi role không xóa profile, Post hoặc lịch sử nhưng làm badge biến mất. USER thường vẫn Follow/Unfollow Managed Identity qua bảng `follows` và các semantics Feed, Post, Block, Restrict hiện hành không thay đổi.
- Messaging chỉ hỗ trợ `NORMAL USER ACTIVE` đã hoàn tất profile ở cả hai phía. Managed Identity chỉ có action Follow/Unfollow và không có action Nhắn tin. Trên hồ sơ USER thường, action Nhắn tin vẫn hiển thị nhưng chỉ mở được khi hai người đang Follow lẫn nhau; Frontend thông báo rõ điều kiện này và Backend kiểm tra lại để chống bypass. Managed Identity bị Backend từ chối tại open/send/history/read/attachment/typing/realtime/inbox/unread, và bị lọc trực tiếp tại truy vấn `share-recipients` để giữ đúng pagination; conversation legacy có participant `MANAGED` không bị xóa nhưng không được expose qua Messaging thông thường.
- Đề xuất kiểm duyệt có trạng thái `PENDING`, `ACCEPTED`, `REJECTED`; một Collaborator không được có hai đề xuất `PENDING` cho cùng bài. Chấp nhận đề xuất chỉ ghi nhận quyết định và audit, không tự động ẩn bài hay tạo kết luận vi phạm.
- Database dùng `roles`, `permissions`, `role_permissions`, `admin_roles`; unique key trên bảng nối ngăn gán trùng. `users.role` không bị thay thế.
- Bootstrap Admin được gán `SUPER_ADMIN`; nếu tài khoản bootstrap ADMIN đã tồn tại thì bảo đảm role này được gán nhưng không đổi mật khẩu. Các API phân quyền kiểm tra trực tiếp email actor khớp `BOOTSTRAP_ADMIN_EMAIL`, không chỉ tin role/permission trong Access Token.
- `SUPER_ADMIN` không xuất hiện trong danh mục role có thể gán. Không được cấp role này cho tài khoản khác hoặc thu hồi/vô hiệu hóa tài khoản Bootstrap làm hệ thống mất quản trị viên gốc.
- Các permission `ADMIN_CREATE`, `ADMIN_ROLE_ASSIGN`, `ADMIN_ROLE_REVOKE` là quyền không thể ủy quyền: chỉ `SUPER_ADMIN` Bootstrap được giữ và không thể thêm vào role nghiệp vụ khác.
- Thay đổi tài khoản/role admin được ghi `admin_actions` với actor, action, target, role trong note và timestamp. Tài khoản admin hỗ trợ bị vô hiệu hóa bằng trạng thái `BLOCKED`, không xóa cứng; quản trị viên có permission `ADMIN_ENABLE` được mở khóa tài khoản về `ACTIVE`. Master Admin không thể bị vô hiệu hóa.
- Quản trị viên có permission `ADMIN_PASSWORD_RESET` được cấp lại mật khẩu cho một tài khoản ADMIN hỗ trợ. Backend áp dụng policy mật khẩu, chỉ lưu BCrypt hash, không trả hoặc ghi log mật khẩu, ghi action `RESET_ADMIN_PASSWORD` và thu hồi toàn bộ Refresh Token của tài khoản đích trong cùng transaction; trạng thái `ACTIVE`/`BLOCKED` của tài khoản không bị thay đổi. Master Admin không nhận thao tác này từ màn hình quản trị và chỉ tự đổi mật khẩu tại `/admin/profile`.
- ADMIN thường và tài khoản đa vai trò đang hoạt động có trang `/admin/profile` để xem email, username, trạng thái, role/permission và tự sửa tên hiển thị, ngày sinh, bio. Tài khoản chỉ có role `COLLABORATOR` dùng trang hợp nhất `/admin/collaborator/profile`: phần công khai hiển thị username chỉ đọc và cho sửa tên hiển thị, avatar, bio của Managed Public Identity; phần hồ sơ quản trị cho sửa độc lập tên hiển thị, ngày sinh và bio của `ADMIN/NORMAL`; email, username kỹ thuật, trạng thái và role chỉ đọc. Mọi username chỉ được thiết lập khi tạo hoặc onboarding và không được sửa qua API hồ sơ. Managed Public Identity vẫn là nguồn duy nhất cho username, tên hiển thị, avatar và bio trên Dashboard/sidebar Collaborator và mọi bề mặt USER nhìn thấy. Hai bản ghi giữ `users.id` và username database riêng vì `user_profiles.username` là định danh unique toàn hệ thống. Backend luôn lấy cả Admin hiện tại và Managed Identity tương ứng từ JWT, không nhận ID đích từ Client.
- Tự đổi mật khẩu yêu cầu đúng mật khẩu hiện tại; mật khẩu mới phải khớp xác nhận, đạt policy và khác mật khẩu cũ. Backend lưu BCrypt hash, ghi `CHANGE_ADMIN_PASSWORD`, thu hồi toàn bộ Refresh Token và Frontend yêu cầu đăng nhập lại.
- Frontend đọc claims đã ký để bảo vệ route, lọc sidebar và ẩn action không được phép; Backend dùng method security theo từng permission và vẫn là hàng rào cuối cùng.
- Khu vực quản trị tách thành `Quản trị viên` tại `/admin/admins` để xem/tạo/vô hiệu hóa Admin và gán role, cùng `Phân quyền` tại `/admin/permissions` để bật/tắt từng permission theo nhóm chức năng. `DASHBOARD_BASIC_VIEW` bắt buộc cho mọi role; quyền của `SUPER_ADMIN` là bất biến.
- Tại trang Phân quyền, SUPER_ADMIN có thể nhập tên để tạo role tùy chỉnh. Backend chuẩn hóa tên, sinh `code` ASCII uppercase snake case duy nhất, tạo `reserved = false` và gán `DASHBOARD_BASIC_VIEW` mặc định trước khi cho cấu hình thêm permission.
- Cập nhật permission của role thay toàn bộ snapshot `role_permissions` trong transaction, ghi audit và thu hồi Refresh Token của mọi Admin đang mang role đó. Access Token đã phát hành chỉ hết hiệu lực theo TTL ngắn hạn.

#### Admin Realtime Notification V1 — `IMPLEMENTED` + automated test `TESTED`, chưa `INTEGRATED`

- Mọi tài khoản `ADMIN` đang `ACTIVE` có Notification Center dùng domain `admin_notifications` riêng, không thay đổi semantics của bảng `notifications` dành cho USER.
- Audience nghiệp vụ được resolve từ effective permission hiện tại trong MySQL qua `admin_roles`, `roles`, `role_permissions` và `permissions`; custom role tự nhận đúng event khi được gán permission tương ứng. Direct recipient dùng cho sự kiện cá nhân như kết quả Moderation Suggestion; role-holder direct audience chỉ dùng cho thay đổi chính role đó.
- `(recipient_admin_id, event_key)` là hàng rào chống trùng cho retry, admin nhiều role hoặc match nhiều permission. Permission-based self-notification của actor bị loại; chỉ ADMIN `ACTIVE` được nhận event mới.
- MySQL và REST là source of truth. API danh sách dùng cursor Base64URL opaque theo `created_at DESC, id DESC`, mặc định 10 và tối đa 20; hỗ trợ unread count, read one idempotent, read-all theo visibility hiện tại và soft delete.
- List, unread count, read-all và realtime đều kiểm tra lại `required_permission_code` theo RBAC hiện tại; thu hồi permission giữ row lịch sử nhưng ẩn row và loại khỏi badge. Notification không cấp quyền truy cập tài nguyên đích.
- Realtime dùng shared native WebSocket/STOMP `/ws`, không tạo connection hoặc client thứ hai. ADMIN subscribe `/user/queue/admin-notifications`; row được lưu trong business transaction và chỉ phát best-effort tại `AFTER_COMMIT`. Broker lỗi không rollback MySQL.
- Frontend có chuông/badge/dropdown và trang `/admin/notifications`, dedupe theo notification ID, reconcile qua REST khi reconnect, foreground, mở center và sau mutation; các tab đồng bộ mutation qua `BroadcastChannel`.
- Event V1 đã nối với Post/Profile Report, Moderation Suggestion, User/Post/Hashtag management, auto-block repeated violation và Admin/RBAC lifecycle. ADS_MANAGER vẫn có center với empty state và không có Ads event giả.
- Notification kiểm duyệt của USER có modal chi tiết cho `POST_HIDDEN_BY_ADMIN`, `CONTENT_VIOLATION_WARNING` và `CONTENT_VIOLATION_FINAL_WARNING`. Backend chỉ cho chính người nhận đọc, không lộ danh tính Admin, đồng thời lưu snapshot mã lý do và nội dung ngắn để quyết định cũ vẫn giải thích được khi Post được khôi phục hoặc thay đổi. Cảnh báo 1/3–2/3 tham chiếu đúng bài vi phạm; modal hiển thị lý do, nội dung liên quan, thời điểm và mức cảnh báo.
- Automated backend/frontend test, lint và build đã chạy; migration MySQL thật và manual E2E nhiều tài khoản/WebSocket chưa chạy nên trạng thái là `NOT INTEGRATED`.

Checklist manual E2E: [`docs/testing/ADMIN-REALTIME-NOTIFICATION-V1-E2E-CHECKLIST.md`](docs/testing/ADMIN-REALTIME-NOTIFICATION-V1-E2E-CHECKLIST.md).

#### Quản lý người dùng

- Xem danh sách người dùng.
- Tìm kiếm người dùng.
- Chỉnh sửa ảnh đại diện và nội dung hồ sơ của tài khoản USER.
- Khóa và mở khóa tài khoản.
- Lưu lý do thay đổi trạng thái tài khoản.
- Thu hồi Refresh Token còn hiệu lực khi khóa tài khoản.

#### Quản lý bài viết

- Xem danh sách và chi tiết bài viết.
- Ẩn và khôi phục bài viết.
- Không khôi phục bài viết đã bị tác giả xóa mềm.

#### Quản lý hashtag

- Xem và tìm kiếm danh sách hashtag có phân trang.
- Hiển thị tên hashtag, số bài viết đang liên kết, ngày tạo và ngày sử dụng mới nhất.
- Admin có thể tạo hashtag theo cùng quy tắc chuẩn hóa dùng khi tạo bài viết; không cho tạo tên trùng sau chuẩn hóa.
- Admin có thể đổi tên hashtag; toàn bộ bài đang sử dụng tự hiển thị tên mới do quan hệ giữ nguyên `hashtag_id`.
- Khi Admin xóa hashtag, Backend gỡ hashtag khỏi mọi bài liên quan rồi xóa hashtag trong cùng transaction; bài viết, nội dung và media vẫn được giữ nguyên.
- Thao tác tạo/xóa hashtag được ghi vào lịch sử quản trị.

#### Quản lý báo cáo

- Xem danh sách Moderation Case, mỗi case đúng một dòng và phân trang tại database.
- Xem chi tiết toàn bộ Report theo dạng rút gọn gồm reporter, reason, description và thời gian; snapshot vẫn được lưu độc lập làm bằng chứng nhưng không render trong từng Report trên giao diện Admin.
- Kết luận trực tiếp không vi phạm hoặc có vi phạm và ẩn bài.
- Chỉ một Admin được phép giải quyết case `OPEN` khi có thao tác đồng thời.

#### Hoạt động quản trị

- Ghi lại các hành động quản trị quan trọng.
- Hỗ trợ hiển thị lịch sử hoạt động của quản trị viên.

#### Analytics hoạt động người dùng — Backend/Frontend `IMPLEMENTED`, Frontend `TESTED`, test MySQL `CONDITIONAL`

Analytics gồm module quản trị độc lập tại `/admin/user-analytics` và widget Dashboard tại `/admin`. Frontend gọi hai
API monthly/summary qua service tập trung, có bộ lọc khoảng tháng và ngưỡng không hoạt động, bốn KPI, biểu đồ
hai trục cho số người quay lại/tỷ lệ tái kích hoạt và bảng snapshot tháng cuối cùng các trạng thái Loading/Empty/Error. Một hoạt động hợp lệ
là request nghiệp vụ thành công đại diện cho hành vi thực của USER như mở Feed, xem chi tiết bài, tạo hoặc
sửa bài, Like/Save, bình luận hoặc Follow. Refresh Token, Auth/OTP, health check, WebSocket heartbeat,
request Admin và request nền không tạo activity. Ngày hoạt động dùng UTC; mỗi USER có tối đa một dòng
`user_daily_activities` trong một ngày nhờ unique `(user_id, activity_date)` và MySQL UPSERT atomic.

Bảng snapshot tháng hiển thị cây ba cấp: tổng USER đủ điều kiện ở cấp 1; tổng đang hoạt động và tổng không hoạt động
ở cấp 2; mỗi nhóm cấp 2 chứa ba trạng thái chi tiết tương ứng ở cấp 3. Thứ tự và độ thụt lề chỉ mô tả quan hệ
tổng–thành phần, không thay đổi giá trị hoặc công thức do API Analytics trả về.

Sidebar Admin gom các màn hình thống kê trong nhóm mở rộng “Thống kê”, gồm Người dùng (`/admin/user-analytics`),
Bài viết (`/admin/post-analytics`) và Hashtag (`/admin/hashtag-analytics`). Thống kê bài viết dùng endpoint chỉ đọc
`GET /api/v1/admin/analytics/posts`, yêu cầu `POST_VIEW`, với bộ lọc 7/30/90 ngày, 6 tháng, 1 năm hoặc khoảng ngày
tùy chọn tối đa 366 ngày. Màn hình gồm KPI trạng thái, tăng trưởng so với kỳ liền trước, biểu đồ bài mới, phân bố
trạng thái, tương tác Like/Comment/Save/Repost và top bài nổi bật. Thống kê hashtag dùng endpoint chỉ đọc
`GET /api/v1/admin/analytics/hashtags`, yêu cầu `HASHTAG_VIEW`, với cùng các preset thời gian. Màn hình gồm 6 KPI,
xu hướng bài có hashtag so với tổng bài, top 10 và phân bố sử dụng, tăng trưởng so với kỳ trước, hashtag hoạt động
gần đây và hashtag ít sử dụng. Analytics không chứa thao tác tạo, đổi tên hoặc xóa; các thao tác này vẫn thuộc màn
Quản lý Hashtag. Các mục con chỉ hiển thị khi Admin có permission đọc module tương ứng.

Widget Dashboard gọi `GET /api/v1/admin/analytics/user-engagement/dashboard?days=30` (từ 1 đến 90 ngày) để
hiển thị chuỗi tổng `activity_count` theo từng ngày UTC. Response luôn bù các ngày chưa có dữ liệu bằng `0` và
trả tối đa 5 USER nổi bật của ngày UTC hiện tại. USER nổi bật phải `ACTIVE`, hoàn tất hồ sơ; được xếp theo số bài
`PUBLISHED` tạo trong ngày giảm dần, sau đó theo tổng `activity_count` giảm dần. Dashboard không hiển thị lịch sử
hoạt động chi tiết hoặc khối “Hoạt động gần đây”.

Eligible System User tại `evaluationDate` là tài khoản đã được tạo, có `role = USER`, hồ sơ đã hoàn tất và
ở trạng thái `ACTIVE` tại ngày đánh giá. Trạng thái tháng lịch sử được dựng lại từ
`account_status_histories`. Tại mỗi tháng, toàn bộ tài khoản USER hợp lệ được phân loại vào đúng một trong
sáu nhóm `NEW`, `REGULAR`, `RETURNING`, `RECENTLY_INACTIVE`, `ELIGIBLE_INACTIVE_NOT_RETURNED` hoặc
`NEVER_ACTIVE`. Ba nhóm đầu có hoạt động trong tháng; ba nhóm sau không hoạt động trong tháng. Các nhóm
không chồng lặp và tổng sáu nhóm bằng Eligible System User.

- `NEW`: có activity trong tháng và không có activity trước đầu tháng.
- `REGULAR`: có activity trong tháng, có activity trước tháng và khoảng cách tới activity đầu tháng
  `<= inactiveDays`.
- `RETURNING`: có activity trong tháng, có activity trước tháng và khoảng cách đó `> inactiveDays`.
- `RECENTLY_INACTIVE`: không activity trong tháng, đã từng activity và tại đầu tháng chưa vượt ngưỡng.
- `ELIGIBLE_INACTIVE_NOT_RETURNED`: không activity trong tháng và đã vượt ngưỡng tại đầu tháng.
- `NEVER_ACTIVE`: chưa từng có activity hợp lệ tính đến ngày đánh giá.

Công thức:

```text
activeUserCount = NEW + REGULAR + RETURNING
inactiveUserCount = RECENTLY_INACTIVE + ELIGIBLE_INACTIVE_NOT_RETURNED + NEVER_ACTIVE
eligibleSystemUserCount = activeUserCount + inactiveUserCount
eligibleInactiveUserCount = returningEligibleUserCount + eligibleInactiveNotReturnedUserCount
Monthly Active User Rate = activeUserCount / eligibleSystemUserCount × 100
Regular Active Rate = regularActiveUserCount / activeUserCount × 100
Monthly Reactivation Rate = returningEligibleUserCount / eligibleInactiveUserCount × 100
Never Active Rate = neverActiveUserCount / eligibleSystemUserCount × 100
```

Rate trả `null` khi mẫu số bằng 0. Điều kiện inactive dùng toán tử `>`: đúng 15 ngày là REGULAR/chưa đủ
inactive, từ 16 ngày mới là RETURNING/eligible inactive khi `inactiveDays=15`. Tháng hiện tại dùng ngày UTC
hiện tại làm `evaluationDate`; tháng đã qua dùng ngày cuối tháng.

---

## 📌 Mức độ ưu tiên triển khai

### P0 – Bắt buộc hoàn thành

- Đăng ký email có OTP.
- Đăng ký và đăng nhập Google/Facebook.
- Đăng nhập local.
- Hoàn tất hồ sơ ban đầu.
- Đăng xuất.
- JWT Access Token và Refresh Token.
- Phân quyền `USER` và `ADMIN`.
- Quản lý hồ sơ.
- Follow và Unfollow.
- Block, Unblock, Restrict, Unrestrict và danh sách quan hệ do người dùng thiết lập.
- CRUD bài viết.
- Upload hình ảnh.
- Like, Unlike và xem danh sách bài viết đã thích.
- Bình luận.
- Feed Following.
- Feed For You.

### P1 – Cần hoàn thành

- Liên kết và quản lý nhiều phương thức đăng nhập.
- Save và Unsave.
- Repost, Unrepost, tab Repost trên Profile và activity Repost trong Following Feed.
- Hashtag và gợi ý hashtag.
- Tìm kiếm người dùng.
- Tìm kiếm bài viết.
- Báo cáo bài viết và trang cá nhân.
- Moderation Case và xử lý báo cáo theo nhóm bài viết.
- Gắn, thay đổi và gỡ một Location tùy chọn trên Post.
- Location-aware Discovery V1 – Nearby theo bán kính và Discovery Map V1 theo viewport/Location Posts; cả hai không lưu vị trí người dùng.
- Admin khóa và mở khóa tài khoản.
- Admin ẩn và khôi phục bài viết.
- Hiển thị hoạt động quản trị cơ bản.
- Academic Profile & Interests từ Database/API đến onboarding, Edit Profile và Profile Display.
- Student Recommendation V1 rule-based dựa trên Academic Profile, Interests và kết nối chung; không gồm AI/ML.
- Admin Academic Management V1 cho School, Faculty, Major và Interest Category; không hard delete.

### P2 – Thực hiện nếu đủ tiến độ

- Trả lời bình luận một cấp.
- Thông báo REST và realtime bằng WebSocket/STOMP.
- Nhắn tin trực tiếp một-một bằng text hoặc ảnh; gửi bằng REST và nhận realtime bằng WebSocket/STOMP dùng chung.

---

## 🛠️ Công nghệ sử dụng

### Frontend

- ReactJS.
- JavaScript ES6+.
- Vite.
- React Router DOM.
- Axios.
- STOMP client trên native WebSocket.
- Tailwind CSS.
- Google Identity Services.
- Facebook SDK for JavaScript.
- Context API hoặc Zustand.
- ESLint.

### Backend

- Java.
- Spring Boot.
- Spring Web.
- Spring WebSocket và STOMP.
- Spring Security.
- Spring Data JPA.
- Hibernate.
- Bean Validation.
- JWT Authentication.
- Google token verification.
- Facebook access-token verification.
- Spring JavaMailSender.
- Gmail SMTP cho toàn bộ email OTP.
- Maven.
- Lombok.
- MapStruct hoặc mapper thủ công.

### Database

- MySQL 8.
- MySQL Workbench.
- Bảng `user_blocks` dùng khóa chính kép `(blocker_id, blocked_id)`, khóa ngoại tới `users`, CHECK chống tự Block và index đảo chiều `(blocked_id, blocker_id)`.
- Bảng `user_restrictions` dùng khóa chính kép `(restrictor_id, restricted_id)`, khóa ngoại tới `users`, CHECK chống tự Restrict và index `(restricted_id, restrictor_id)` phục vụ kiểm tra suppress Notification.
- Các bảng RBAC quản trị gồm `roles`, `permissions`, `role_permissions`, `admin_roles`; SQL seed năm role ban đầu và schema cho phép tạo thêm role tùy chỉnh, DBML được đồng bộ cùng schema vật lý.

### Lưu trữ media

- Cloudinary.
- Firebase Storage.
- Amazon S3.

Database không lưu dữ liệu BLOB. Media công khai có thể lưu URL; media chat chỉ lưu storage identifier và metadata, URL truy cập được ký ngắn hạn sau khi kiểm tra quyền.

### Công cụ phát triển

- Git.
- GitHub.
- Postman.
- IntelliJ IDEA.
- Visual Studio Code.
- MySQL Workbench.
- Docker tùy chọn.
- Figma tùy chọn.

---

## 🏗️ Kiến trúc tổng thể

```text
┌────────────────────────────┐
│       Client Browser       │
└──────────────┬─────────────┘
               │
               ▼
┌────────────────────────────┐
│      ReactJS Frontend      │
│ Vite + Tailwind + OAuth UI │
└──────────────┬─────────────┘
               │ RESTful API / JSON
               │ JWT Access Token
               ▼
┌────────────────────────────┐
│    Spring Boot Backend     │
│                            │
│ Controller / Service       │
│ Repository / Security      │
│ OTP / OAuth Verification   │
└───────┬──────────┬─────────┘
        │          │
        ▼          ▼
┌──────────────┐  ┌──────────────────┐
│   MySQL 8    │  │ External Services│
│ Main Data    │  │ Email/OAuth  │
└───────┬──────┘  └──────────────────┘
        │
        ▼
┌──────────────────┐
│  Cloud Storage   │
│  Post Media      │
└──────────────────┘
```

---

## 📂 Cấu trúc thư mục dự án

```text
student-social-network/
├── frontend/
│   ├── public/
│   └── src/
│       ├── assets/
│       ├── components/
│       ├── config/
│       ├── features/
│       │   ├── auth/
│       │   │   ├── components/
│       │   │   ├── pages/
│       │   │   ├── services/
│       │   │   └── validation/
│       │   ├── profile/
│       │   ├── follow/
│       │   ├── post/
│       │   ├── feed/
│       │   ├── search/
│       │   ├── report/
│       │   └── admin/
│       ├── hooks/
│       ├── routes/
│       ├── store/
│       ├── utils/
│       ├── App.jsx
│       └── main.jsx
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/stu/edu/vn/backend/
│   │   │   │   ├── common/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── dto/
│   │   │   │   │   ├── entity/
│   │   │   │   │   ├── enums/
│   │   │   │   │   ├── provider/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── service/
│   │   │   │   ├── user/
│   │   │   │   ├── follow/
│   │   │   │   ├── post/
│   │   │   │   ├── comment/
│   │   │   │   ├── feed/
│   │   │   │   ├── search/
│   │   │   │   ├── report/
│   │   │   │   └── admin/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── database/
│   ├── student_social_network.sql
│   └── student_social_network.dbml
│
├── docs/
│   ├── api/
│   ├── database/
│   ├── diagrams/
│   ├── ui/
│   └── business/
│
├── .gitignore
├── docker-compose.yml
└── README.md
```

---

## 🧩 Nguyên tắc tổ chức Frontend

Frontend được tổ chức theo hướng **Feature-Based Structure**.

- `components/`: component riêng của module.
- `pages/`: các trang được khai báo trong Router.
- `services/`: các hàm gọi RESTful API.
- `validation/`: quy tắc kiểm tra biểu mẫu.
- Component dùng chung đặt trong `src/components/common/`.
- Axios và Interceptor đặt trong `src/config/`.
- Không gọi Axios trực tiếp trong component giao diện.
- Token được quản lý tập trung.
- Credential của Google/Facebook chỉ gửi đến endpoint Auth tương ứng.
- Không dùng provider token để gọi API Feed, Post hoặc các API nghiệp vụ khác.

---

## 🧩 Nguyên tắc tổ chức Backend

Backend được tổ chức theo module nghiệp vụ kết hợp kiến trúc phân lớp.

- `controller`: tiếp nhận request và trả response.
- `dto/request`: nhận và kiểm tra dữ liệu đầu vào.
- `dto/response`: trả dữ liệu cần thiết cho Frontend.
- `entity`: ánh xạ bảng dữ liệu.
- `enums`: nhóm giá trị cố định của module.
- `repository`: truy cập dữ liệu.
- `service`: khai báo nghiệp vụ.
- `service/impl`: triển khai nghiệp vụ và quản lý transaction.
- `provider`: tích hợp email, Google và Facebook.
- `mapper`: chuyển đổi Entity và DTO.
- `common/security`: JWT và Spring Security.
- `common/exception`: xử lý lỗi tập trung.
- `common/api`: chuẩn hóa response.

Nguyên tắc:

- Controller không trực tiếp truy cập Repository.
- Entity không được trả trực tiếp ra API.
- Mọi kiểm tra quyền phải thực hiện tại Backend.
- Các thao tác nhiều bước phải dùng transaction khi cần.
- Không giữ transaction database trong lúc chờ SMTP, email hoặc provider bên ngoài.
- Không lưu mật khẩu, OTP, flow token hoặc Refresh Token dưới dạng văn bản thuần.

---

## 🗄️ Các bảng dữ liệu chính

### Tài khoản và xác thực

- `users`: tài khoản nội bộ, email, mật khẩu băm, trạng thái xác minh, vai trò và trạng thái tài khoản.
- `user_profiles`: hồ sơ người dùng và trạng thái onboarding.
- `pending_registrations`: đăng ký local đang chờ OTP.
- `user_auth_providers`: liên kết Google/Facebook với cùng một `users.id`.
- `refresh_tokens`: phiên làm mới JWT.
- `password_recovery_challenges`: challenge thật/decoy cho Password Recovery, chỉ lưu OTP và token dạng hash.
- `password_reset_tokens`: bảng đặt lại mật khẩu legacy, được giữ nguyên để audit và không được service mới ghi dữ liệu.

### Academic Profile & Interests

- `schools` → `faculties` → `majors`: master data học thuật có hierarchy và trạng thái `ACTIVE`/`INACTIVE`.
- `interest_categories`: danh mục sở thích chuẩn hóa.
- `user_profiles.school_id`, `faculty_id`, `major_id`, `entry_year`: dữ liệu học thuật tùy chọn.
- `user_interests(user_id, interest_id)`: quan hệ N-N, chống trùng bằng khóa chính kép.

### Quan hệ theo dõi

- `follows`.

### Bài viết

- `posts`.
- `locations`: địa điểm Google Places dùng chung giữa nhiều bài viết, duy nhất theo `google_place_id`.
- `post_media`.
- `hashtags`.
- `post_hashtags`.

Quan hệ Location–Post:

- `locations.id` là khóa chính nội bộ.
- `locations.google_place_id` là natural unique key, `NOT NULL` và duy nhất.
- `locations.display_name`, `locations.latitude` và `locations.longitude` là `NOT NULL`; `locations.formatted_address` cho phép `NULL`; bảng có `created_at` và `updated_at`.
- `posts.location_id` cho phép `NULL`, có index, không duy nhất và tham chiếu `locations.id` bằng `ON DELETE SET NULL`.

### Tương tác

- `post_likes`.
- `comments`.
- `saved_posts`.

### Báo cáo và quản trị

- `reports`.
- `moderation_cases`: hồ sơ xử lý chung theo bài viết; duy nhất một case `OPEN` cho mỗi Post.
- `profile_report_cases`: một vụ việc duy nhất cho mỗi trang cá nhân, gom báo cáo từ nhiều người.
- `profile_reports`: từng lượt báo cáo thuộc case, giữ reporter/lý do/snapshot và chống trùng `PENDING` theo reporter/target.
- `account_status_histories`.
- `admin_actions`.

Ràng buộc Auth quan trọng:

- `users.email` là duy nhất khi có giá trị.
- `users.password_hash` được phép `NULL` với tài khoản chỉ dùng social.
- `user_auth_providers(provider, provider_user_id)` là duy nhất.
- OTP, flow token và Refresh Token chỉ lưu dưới dạng hash.

---

## 🔐 Cơ chế xác thực

### Access Token

- Xác thực request đến Backend.
- Có thời hạn ngắn.
- Gửi trong header:

```http
Authorization: Bearer <access_token>
```

### Refresh Token

- Dùng để cấp Access Token mới.
- Có thời hạn dài hơn.
- Được lưu dưới dạng hash.
- Có thể bị thu hồi khi đăng xuất hoặc khi tài khoản bị khóa.

### Provider Token

- Google ID Token và Facebook Access Token chỉ dùng cho endpoint Auth.
- Backend phải xác minh token trước khi xử lý.
- Provider Token không thay thế JWT của hệ thống.

### Hạ tầng realtime dùng chung

- REST API và MySQL tiếp tục là nguồn dữ liệu chuẩn của Notification.
- WebSocket/STOMP chỉ là kênh phân phối realtime theo mô hình best-effort.
- Handshake endpoint là `/ws`, không bật SockJS và chưa dùng Outbox.
- Mỗi tab chỉ tạo một STOMP client và một WebSocket connection dùng chung cho các module realtime.
- Frontend subscribe Notification tại `/user/queue/notifications` và Messaging tại `/user/queue/messaging` mà không tạo client thứ hai.
- Access Token chỉ được gửi trong native header `Authorization: Bearer <access_token>` của STOMP `CONNECT`, không truyền token trong URL.
- Principal của kết nối được Backend xác định từ JWT và có tên bằng chuỗi `users.id`; Backend không tin `userId` do Frontend gửi.
- Event chỉ được phát sau khi transaction tạo Notification commit thành công.
- Giai đoạn đầu chỉ phát event `NOTIFICATION_CREATED`; read, read-all, delete và invalidation vẫn đồng bộ qua REST.
- Messaging phát `MESSAGE_CREATED` và `MESSAGES_READ` sau commit cho cả hai participant; mỗi envelope có unread count authoritative riêng và snapshot `POST_SHARE` được hydrate theo quyền của chính user nhận.
- Client không được gửi mutation bền vững qua STOMP. Chỉ frame typing tạm thời tại `/app/messaging/typing` được phép; Backend tự lấy actor từ principal, kiểm tra membership/account/Block và chỉ phát cho participant còn lại qua `/user/queue/messaging`.
- Typing dùng `TYPING_STARTED`/`TYPING_STOPPED`, giới hạn tối đa 4 frame/user/giây trong bộ nhớ của từng instance, không ghi database, không thay đổi unread và không replay. Frontend gửi START lần đầu, refresh mỗi 3 giây khi còn hoạt động, STOP sau 2 giây idle và tự hết hạn trạng thái nhận sau 5 giây.
- Khi socket gián đoạn, Frontend phải reconcile bằng REST và chỉ polling unread count khi tab đang hiển thị.

Quy trình tổng quát:

```text
Local OTP hoặc Social Provider
            │
            ▼
Backend xác minh danh tính
            │
            ▼
Tìm hoặc tạo users.id
            │
            ▼
Cấp Access Token + Refresh Token
            │
            ▼
Frontend dùng Access Token gọi API
```

---

## 📡 Quy ước RESTful API

Base URL môi trường phát triển:

```text
http://localhost:8080/api/v1
```

### Auth local và OTP

```http
POST /api/v1/auth/registrations
POST /api/v1/auth/registrations/verify
POST /api/v1/auth/registrations/resend
POST /api/v1/auth/registrations/cancel
GET  /api/v1/auth/registrations/status

POST /api/v1/auth/login
POST /api/v1/auth/refresh-token
POST /api/v1/auth/logout
```

### Social authentication

```http
POST /api/v1/auth/oauth/google
POST /api/v1/auth/oauth/facebook
POST /api/v1/auth/registrations/resolve-social-conflict
```

### Quản lý phương thức đăng nhập

```http
GET    /api/v1/users/me/auth-providers
POST   /api/v1/users/me/auth-providers/email
POST   /api/v1/users/me/auth-providers/google
POST   /api/v1/users/me/auth-providers/facebook
DELETE /api/v1/users/me/auth-providers/{provider}
```

### User Block

```http
PUT    /api/v1/users/{targetUserId}/block
DELETE /api/v1/users/{targetUserId}/block
GET    /api/v1/users/me/blocked-users
```

### User Restriction

```http
POST   /api/v1/users/{targetUserId}/restriction
DELETE /api/v1/users/{targetUserId}/restriction
GET    /api/v1/users/me/restricted-users
```

Các API trên yêu cầu JWT, luôn lấy người thực hiện từ Security Context và có tính idempotent. Không có endpoint công khai để xem ai đã hạn chế current user.

### Các nhóm API khác

```text
/api/v1/users
/api/v1/follows
/api/v1/posts
/api/v1/feeds
/api/v1/discovery
/api/v1/search
/api/v1/reports
/api/v1/admin
```

API Nearby Discovery V1 yêu cầu JWT của USER `ACTIVE` đã hoàn tất profile:

```http
GET /api/v1/discovery/nearby?latitude=10.8231&longitude=106.6297&radiusKm=5&limit=10&cursor=<opaque-optional>
```

Response `data` là `CursorPageResponse<NearbyPostItemResponse>`; mỗi item có `post` theo PostCard chuẩn hiện hành và `distanceMeters` kiểu số nguyên. Client phải gửi lại nguyên cursor opaque và reset cursor khi đổi tọa độ hoặc radius.

API Discovery Map V1 Backend yêu cầu cùng điều kiện JWT/USER/ACTIVE/onboarding:

```http
GET /api/v1/discovery/map/locations?north=10.78&south=10.73&east=106.70&west=106.64
GET /api/v1/discovery/map/locations/{locationId}/posts?limit=10&cursor=<opaque-optional>
```

Marker response `data` có `{ "locations": [], "truncated": false }`. Location Posts dùng `CursorPageResponse<FeedPostResponse>` và cursor opaque bind với `locationId`.

API onboarding và kiểm tra username:

```http
GET /api/v1/users/me/onboarding
GET /api/v1/users/me/onboarding/username-availability?username=duoz_03
PUT /api/v1/users/me/onboarding
```

API master data Academic/Profile yêu cầu JWT nhưng được phép gọi trước khi hoàn tất onboarding để phục vụ lựa chọn dữ liệu:

```http
GET /api/v1/academic/schools?keyword=Cong&limit=10
GET /api/v1/academic/schools/{schoolId}/faculties?keyword=Cong&limit=10
GET /api/v1/academic/faculties/{facultyId}/majors?keyword=Cong&limit=10
GET /api/v1/interests
```

Ba API autocomplete chỉ query hierarchy đang selectable (`School ACTIVE AND Faculty ACTIVE AND Major ACTIVE`) tại MySQL, dùng `limit` mặc định 10 và tối đa 20. `PUT /api/v1/users/me/profile` nhận thêm khối `academic` và `interestIds`; bỏ các field này sẽ giữ nguyên dữ liệu hiện có.

API Admin Academic Management V1 chỉ dành cho `ADMIN`:

```http
GET|POST  /api/v1/admin/academic/schools
PUT       /api/v1/admin/academic/schools/{schoolId}
PATCH     /api/v1/admin/academic/schools/{schoolId}/status
GET|POST  /api/v1/admin/academic/schools/{schoolId}/faculties
PUT       /api/v1/admin/academic/faculties/{facultyId}
PATCH     /api/v1/admin/academic/faculties/{facultyId}/status
GET|POST  /api/v1/admin/academic/faculties/{facultyId}/majors
PUT       /api/v1/admin/academic/majors/{majorId}
PATCH     /api/v1/admin/academic/majors/{majorId}/status
GET|POST  /api/v1/admin/academic/interests
PUT       /api/v1/admin/academic/interests/{interestId}
PATCH     /api/v1/admin/academic/interests/{interestId}/status
```

Các API danh sách Admin dùng `keyword`, `page`, `size`; actor mutation lấy từ JWT, mọi create/update/status change đều ghi `admin_actions`. Chuyển inactive không cascade status và không xóa reference hiện hữu.

API Repost đã triển khai:

```http
PUT    /api/v1/posts/{postId}/repost
DELETE /api/v1/posts/{postId}/repost
GET    /api/v1/users/{userId}/reposts?limit=10&cursor=<opaque-cursor>
```

Post response công khai có thêm `repostCount` và `repostedByCurrentUser`. Realtime `POST_REPOST` được đẩy
best-effort tới `/user/queue/notifications` sau transaction commit; REST/MySQL vẫn là nguồn sự thật.

API Moderation Case dành cho Admin:

```http
GET   /api/v1/admin/moderation-cases
GET   /api/v1/admin/moderation-cases/{caseId}
PATCH /api/v1/admin/moderation-cases/{caseId}/resolve-no-violation
PATCH /api/v1/admin/moderation-cases/{caseId}/resolve-action
```

Frontend chỉ gửi hành động nghiệp vụ và kết luận; `status` cùng `resolved_by` do Backend xác định từ
state machine và JWT Principal.

API Account Standing dành cho USER hiện tại:

```http
GET /api/v1/account/standing
```

Current account lấy từ JWT; API không nhận `userId` từ Client.

API quản lý Admin/RBAC:

```http
GET   /api/v1/admin/profile
PUT   /api/v1/admin/profile
PATCH /api/v1/admin/profile/password
GET   /api/v1/admin/admins
GET   /api/v1/admin/admins/{adminId}
POST  /api/v1/admin/admins
PUT   /api/v1/admin/admins/{adminId}
PATCH /api/v1/admin/admins/{adminId}/disable
PATCH /api/v1/admin/admins/{adminId}/enable
PATCH /api/v1/admin/admins/{adminId}/password
POST  /api/v1/admin/admins/{adminId}/roles/{roleCode}
PATCH /api/v1/admin/admins/{adminId}/roles/{roleCode}/revoke
GET   /api/v1/admin/admins/roles/catalog
GET   /api/v1/admin/roles
POST  /api/v1/admin/roles
GET   /api/v1/admin/roles/permissions
PUT   /api/v1/admin/roles/{roleCode}/permissions
```

Ba endpoint hồ sơ tự quản lý dành cho mọi ADMIN đang hoạt động và không nhận `adminId` từ Client. Chỉ actor có email khớp `BOOTSTRAP_ADMIN_EMAIL` mới được tạo Admin hỗ trợ, gán/thu hồi role, tạo role và quản lý ma trận permission; Backend không chỉ dựa vào claim `SUPER_ADMIN`. Endpoint tạo role chỉ nhận `name`; Backend tự sinh `code`, chống trùng và gán quyền Tổng quan mặc định. Không thể gán `SUPER_ADMIN` hoặc thêm `ADMIN_CREATE`, `ADMIN_ROLE_ASSIGN`, `ADMIN_ROLE_REVOKE` vào role khác. Master Admin không nhận thêm/bớt role nghiệp vụ, không được cấp lại mật khẩu qua endpoint quản lý và không thể bị vô hiệu hóa; mật khẩu của tài khoản này chỉ đổi qua API hồ sơ tự quản lý. Các endpoint tài khoản còn lại yêu cầu permission `ADMIN_*` tương ứng. Endpoint cấp lại mật khẩu của Admin hỗ trợ nhận `newPassword` và `confirmPassword`, không trả mật khẩu trong response. Việc thay đổi role hoặc mật khẩu không nhận `userId` actor từ Client mà lấy actor từ JWT Principal hiện tại.

API Analytics độc lập dành cho Admin:

```http
GET /api/v1/admin/analytics/user-engagement/monthly?fromMonth=2026-01&toMonth=2026-06&inactiveDays=15
GET /api/v1/admin/analytics/user-engagement/summary?month=2026-06&inactiveDays=15
GET /api/v1/admin/analytics/user-engagement/dashboard?days=30
GET /api/v1/admin/analytics/posts?range=30D
GET /api/v1/admin/analytics/posts?fromDate=2026-08-01&toDate=2026-08-16
```

Khoảng monthly tối đa 24 tháng. `summary` mặc định lấy tháng hiện tại. Response monthly gồm từng tháng,
hai peak (`peakReturningMonth`, `peakReturnRateMonth`) và `comparisonOperator = GREATER_THAN`.
Dashboard mặc định dùng 30 ngày, cho phép từ 1 đến 90 ngày và trả `dailyInteractions` cùng `featuredUsers`.
Post Analytics mặc định 30 ngày. Tổng bài/công khai/ẩn/xóa là snapshot trạng thái hiện tại; bài mới, tương tác,
top bài và hồ sơ kiểm duyệt dùng cùng khoảng ngày UTC. So sánh dùng khoảng liền trước có cùng số ngày.

Ví dụ bắt đầu đăng ký:

```json
{
  "email": "student@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

Ví dụ xác minh OTP:

```json
{
  "registrationFlowToken": "<temporary-token>",
  "code": "123456"
}
```

Ví dụ Google authentication khi tham gia luồng registration đang chờ:

```http
X-Auth-Flow-Token: <optional-registration-flow-token>
Content-Type: application/json
```

```json
{
  "idToken": "<google-id-token>"
}
```

Cấu trúc response tham khảo:

```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": {},
  "timestamp": "2026-07-19T10:00:00"
}
```

---

## 🚀 Hướng dẫn cài đặt

### 1. Yêu cầu môi trường

- Node.js 20 trở lên.
- npm 10 trở lên.
- Java Development Kit 21.
- Maven 3.9 trở lên.
- MySQL 8.
- Git.
- IntelliJ IDEA hoặc IDE hỗ trợ Spring Boot.
- Visual Studio Code hoặc IDE hỗ trợ ReactJS.

### 2. Clone Repository

```bash
git clone https://github.com/your-username/student-social-network.git
cd student-social-network
```

### 3. Khởi tạo cơ sở dữ liệu

File `student_social_network.sql` là file import duy nhất: tự động drop database test hiện tại, tạo lại toàn bộ schema/trigger và nạp đúng 1.000 users cùng 1.000 posts demo. Từ Command Prompt hoặc Git Bash:

```bash
mysql --default-character-set=utf8mb4 -u root -p < database/student_social_network.sql
```

Từ PowerShell:

```powershell
cmd /c "mysql --default-character-set=utf8mb4 -u root -p < database\student_social_network.sql"
```

Nếu database đã tồn tại và cần giữ dữ liệu, không import lại file canonical. Repository không phân phối migration rời;
phải backup và xây dựng script nâng cấp riêng đã được review cho đúng schema nguồn thực tế. Backend dùng
`ddl-auto: validate` và không tự sửa schema khi khởi động.

### 4. Cấu hình Backend

Ví dụ biến môi trường:

```env
DB_URL=jdbc:mysql://localhost:3306/student_social_network?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

JWT_ACCESS_TOKEN_SECRET=your_access_token_secret
JWT_ACCESS_TOKEN_EXPIRATION_MILLIS=900000
JWT_REFRESH_TOKEN_EXPIRATION_MILLIS=2592000000

AUTH_OTP_HMAC_SECRET=your_independent_otp_hmac_secret
AUTH_FLOW_TOKEN_HMAC_SECRET=your_independent_flow_token_hmac_secret
AUTH_SOCIAL_IDENTITY_FINGERPRINT_SECRET=your_independent_social_fingerprint_secret

GOOGLE_CLIENT_ID=your_google_client_id
FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_APP_SECRET=your_facebook_app_secret

AI_MODERATION_LOCAL_BASE_URL=http://127.0.0.1:8001
AI_MODERATION_CONNECT_TIMEOUT=3s
AI_MODERATION_READ_TIMEOUT=5s

MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_APP_PASSWORD=your_16_character_google_app_password
MAIL_SENDER_NAME=UniShare
```

`MAIL_USERNAME` là tài khoản Gmail gửi OTP. `MAIL_APP_PASSWORD` phải là Google App Password,
không dùng mật khẩu đăng nhập Google thông thường. `MAIL_SENDER_NAME` là tên hiển thị người gửi
và mặc định là `UniShare`. Không đưa thông tin bí mật lên GitHub.

Content moderation chỉ dùng local service và không có API key AI trả phí. Khi local service chưa sẵn sàng, timeout hoặc response sai contract, mutation text fail closed với `CONTENT_MODERATION_UNAVAILABLE`; Backend không tự bypass moderation.

Khởi động local AI service trước Backend (lần đầu tải khoảng 540 MB model vào cache):

```powershell
cd ai-service
py -3.12 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements-dev.txt
python -m pytest
python -m uvicorn app.main:app --host 127.0.0.1 --port 8001
```

Chi tiết CPU/GPU, cache, Linux/macOS, smoke test thật, nguồn model và giấy phép xem tại [`ai-service/README.md`](ai-service/README.md).

Chạy Backend:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 5. Cấu hình Frontend

```bash
cd frontend
npm install
```

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_GOOGLE_CLIENT_ID=your_google_client_id
VITE_FACEBOOK_APP_ID=your_facebook_app_id
VITE_PUBLIC_APP_URL=https://your-public-frontend-domain.example
```

`VITE_PUBLIC_APP_URL` là origin HTTPS công khai dùng để tạo canonical link khi chia sẻ Post ra Facebook.
Facebook không thể crawl `localhost`, địa chỉ loopback hoặc IP mạng nội bộ; khi phát triển local cần dùng
deployment/tunnel public và cấu hình origin tương ứng, sau đó khởi động lại Vite.

```bash
npm run dev
```

### 6. Deploy Render và Aiven

- Repository dùng [`render.yaml`](render.yaml) để cố định cấu hình Spring Web Service và React Static Site, gồm health check `/health`, SPA rewrite và cache asset.
- Backend production vẫn dùng `ddl-auto: validate`; deploy không tự sửa schema Aiven. Nếu Aiven còn schema cũ, phải backup và chạy migration nâng cấp đã review trước khi deploy code mới.
- JDBC URL Aiven phải dùng TLS (`sslMode=REQUIRED`), đúng hostname, port và database trong Aiven Console.
- Render Free có cold start và giới hạn 512 MB RAM; Frontend production dùng timeout 75 giây, còn Backend giới hạn JVM, Tomcat thread và Hikari pool phù hợp một instance demo.
- AI moderation PhoBERT không phù hợp RAM của Render Free. `AI_MODERATION_LOCAL_BASE_URL` phải trỏ đến một AI service đủ RAM; nếu provider không sẵn sàng, Post/Comment/Reply có text tiếp tục fail closed đúng contract.
- Checklist biến môi trường, nâng schema an toàn và xử lý lỗi xem tại [`docs/deployment/RENDER-AIVEN.md`](docs/deployment/RENDER-AIVEN.md).

---

## 🧪 Kiểm thử

### Backend

```bash
cd backend
mvn test
mvn clean package
```

### Frontend

```bash
cd frontend
npm run lint
npm run build
npm run preview
```

### Luồng Auth cần kiểm thử

- Đăng ký email và xác minh OTP.
- Gửi lại OTP và giới hạn tần suất.
- OTP sai, hết hạn, đã dùng hoặc vượt số lần thử.
- Tiếp tục đăng ký sau khi đóng tab hoặc mất mạng.
- Đăng ký/đăng nhập Google.
- Đăng ký/đăng nhập Facebook.
- Email pending chuyển sang social cùng email.
- Provider đã liên kết đăng nhập đúng `users.id`.
- Liên kết và gỡ phương thức đăng nhập.
- Từ chối gỡ phương thức cuối cùng.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
- Report đầu tiên tạo case `OPEN`; các Report hợp lệ tiếp theo cùng bài dùng lại đúng case đó.
- Hai request báo cáo đồng thời không tạo hai case `OPEN`, không mất Report và không làm sai `report_count`.
- Admin giải quyết case `OPEN` đúng một lần; thao tác cạnh tranh còn lại nhận lỗi conflict.
- Kết luận không vi phạm không ẩn bài và chuyển Report sang `REJECTED`; kết luận có vi phạm ẩn bài và chuyển Report sang `RESOLVED`.
- Danh sách Admin hiển thị một dòng mỗi case; chi tiết giữ riêng reporter, reason, description và snapshot của từng Report.

---

## ✅ Tiêu chí nghiệm thu MVP

### Xác thực và tài khoản

1. Đăng ký local chỉ tạo `pending_registrations`, chưa tạo `users`.
2. OTP hợp lệ mới tạo `users` và `user_profiles`.
3. `users` và `user_profiles` được tạo trong cùng transaction.
4. OTP hết hạn, đã dùng, bị hủy hoặc vượt số lần thử không thể sử dụng.
5. Không tồn tại hai đăng ký tạm còn hiệu lực cho cùng một email.
6. Mất mạng hoặc đóng tab vẫn có thể tiếp tục trong thời hạn.
7. Email pending cùng social email đã xác minh hoàn tất đúng một tài khoản.
9. Provider đã liên kết luôn đăng nhập đúng `users.id`.
10. Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
11. Không cho gỡ phương thức đăng nhập cuối cùng.
12. Tài khoản social chưa có mật khẩu không đăng nhập local được.
13. Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
14. Access Token và Refresh Token hoạt động đúng.
15. Refresh Token bị thu hồi không thể cấp Access Token mới.

### Hồ sơ và chức năng mạng xã hội

16. Người dùng hoàn tất onboarding bằng username duy nhất, tên hiển thị và ngày sinh hợp lệ trước khi truy cập chức năng chính.
17. Người dùng cập nhật và xem hồ sơ thành công.
17a. Người dùng cập nhật Academic Profile đúng hierarchy, năm nhập học hợp lệ và tối đa 10 sở thích không trùng; các field nullable không làm thay đổi trạng thái hoàn tất profile.
17b. Public Academic API chỉ trả hierarchy selectable; School/Faculty/Interest inactive không xuất hiện trong lựa chọn mới nhưng reference Academic/Interest của hồ sơ cũ vẫn được bảo toàn.
18. Follow và Unfollow hoạt động đúng, không tạo quan hệ trùng.
18a. Block hoạt động idempotent, xóa Follow hai chiều; Unblock không khôi phục Follow.
18b. Các query dành cho người dùng lọc Block hai chiều trực tiếp tại database mà không làm sai phân trang/cursor.
18c. Block không xóa Like hoặc Comment lịch sử và không làm giảm `like_count` hoặc `comment_count`.
18d. Trong thời gian Block, hai bên không nhìn thấy bình luận hoặc câu trả lời bình luận do đối phương tạo; sau Unblock, nội dung lịch sử hiển thị trở lại nếu vẫn hợp lệ và còn quyền truy cập.
18e. Bình luận cha bị ẩn do Block không được để lộ các câu trả lời con dưới dạng nội dung mồ côi.
18f. Restrict/Unrestrict hoạt động một chiều và idempotent; không tạo quan hệ trùng, không cho tự Restrict hoặc Restrict khi có Block hai chiều.
18g. Restrict không thay đổi Follow, Feed, Search, Like, Save, Comment, Reply hoặc các bộ đếm; Comment/Reply vẫn `PUBLISHED`.
18h. Notification Like, Comment và Reply từ người bị hạn chế tới người hạn chế bị chặn trước khi lưu, không tăng unread count và không phát WebSocket/event; Notification Follow vẫn hoạt động.
18i. API chỉ trả `restrictedByMe` cho current user, không tiết lộ chiều ngược lại; danh sách restricted users chỉ chứa quan hệ do current user tạo.
18j. Block sau Restrict xóa đúng quan hệ Restrict cùng chiều; Unblock không tự khôi phục Restrict và Unrestrict không tạo bù Notification.
19. Người dùng tạo bài có nội dung hoặc hình ảnh.
20. Chỉ tác giả được sửa hoặc xóa bài của mình.
21. Bài bị ẩn hoặc xóa không xuất hiện trên Feed.
19. Người dùng tạo bài có nội dung hoặc hình ảnh và có thể tùy chọn gắn tối đa một Location; các bài dùng cùng Google Place ID phải dùng chung một bản ghi `locations`.
20. Chỉ tác giả được sửa hoặc xóa bài của mình; trong giới hạn chỉnh sửa 15 phút, tác giả có thể cập nhật nội dung/hashtag, giữ/gỡ/thêm ảnh hoặc video và giữ/thay đổi/gỡ Location bằng `KEEP`, `REPLACE`, `REMOVE`.
21. Bài bị ẩn hoặc xóa không xuất hiện trên Feed; xóa Post không xóa Location dùng chung.
22. Like, Unlike, danh sách bài viết đã thích, bình luận và Save hoạt động đúng.
23. Repost và Unrepost idempotent; tab Repost và Following Feed chỉ hiển thị quan hệ có bài gốc còn `PUBLISHED`.
24. Feed Following và Feed For You trả đúng dữ liệu hợp lệ, gồm Location object hoặc `null` cho từng Post.
25. Tìm kiếm người dùng và bài viết hoạt động đúng; Search Post và Admin Post Detail trả Location nhất quán với các Post response khác.
26. Người dùng gửi được báo cáo.
27. Admin quản lý được người dùng, chỉnh sửa nội dung hồ sơ USER và USER nhận được thông báo hệ thống sau khi chỉnh sửa, quản lý bài viết, báo cáo và Academic master data V1; mọi mutation Academic được ghi lịch sử quản trị.
28. Backend từ chối thao tác không có quyền.
29. Mật khẩu, OTP, flow token và Refresh Token không lưu dạng văn bản thuần.
30. Backend không trả stack trace hoặc thông tin nhạy cảm cho Client.

---

## 🔒 Yêu cầu bảo mật

- Mật khẩu được băm bằng BCrypt.
- OTP, flow token và Refresh Token chỉ lưu dạng hash.
- Access Token có thời hạn ngắn.
- Refresh Token có khả năng thu hồi.
- Rate limit cho đăng nhập, gửi OTP, gửi lại OTP và social authentication.
- Token Google/Facebook phải được xác minh tại Backend.
- Không tin email, provider ID hoặc trạng thái verified do Frontend tự gửi.
- API Admin trước hết yêu cầu `users.role = ADMIN`, sau đó kiểm tra permission RBAC hoặc `ADMIN_ROLE_SUPER_ADMIN` phù hợp tại Backend.
- Backend kiểm tra quyền cho mọi thao tác.
- Không ghi mật khẩu, OTP hoặc Token vào log.
- Bean Validation cho dữ liệu đầu vào.
- Kiểm tra MIME type, phần mở rộng và dung lượng media.
- Hạn chế XSS, SQL Injection và CSRF theo kiến trúc.
- CORS chỉ cho phép các nguồn cần thiết.
- Bí mật ứng dụng được lưu trong biến môi trường.

---

## ⚡ Yêu cầu hiệu năng

- API thông thường phản hồi trung bình không quá 2 giây trong môi trường kiểm thử.
- Feed trả tối đa 20 bài viết mỗi lần tải.
- API danh sách hỗ trợ phân trang theo contract của từng module.
- Danh sách bài viết Infinite Scroll dùng cursor với `limit` mặc định 10 và tối đa 20; các danh
  sách còn dùng `PageResponse` giữ kích thước trang theo endpoint hiện tại.
- Hệ thống hướng đến 30–50 người dùng đồng thời trong môi trường kiểm thử.
- Tránh truy vấn N+1.
- Không tải toàn bộ Entity Relationship không cần thiết.
- Việc gửi email không giữ transaction database mở quá lâu.
- Các tác vụ dọn đăng ký hết hạn có thể chạy theo lịch.
- Autocomplete School/Faculty/Major truy vấn trực tiếp tại MySQL theo prefix, trạng thái và hierarchy; mặc định 10, tối đa 20 kết quả.

---

## 🌿 Quy ước Git

### Nhánh

- `main`: phiên bản ổn định.
- `develop`: nhánh tích hợp.
- `feature/*`: chức năng mới.
- `fix/*`: sửa lỗi.
- `refactor/*`: tái cấu trúc.
- `docs/*`: cập nhật tài liệu.

Ví dụ:

```text
feature/auth-registration-otp
feature/auth-google
feature/auth-facebook
feature/auth-provider-linking
docs/update-readme
```

### Commit

```text
feat: thêm đăng ký email bằng OTP
feat: thêm đăng nhập Google
fix: ngăn liên kết provider đã thuộc tài khoản khác
docs: cập nhật README module xác thực
test: thêm kiểm thử luồng đăng ký tạm
```

---

## 📝 Quy ước mã nguồn

### Frontend

- Component dùng PascalCase.
- Hàm và biến dùng camelCase.
- Custom Hook bắt đầu bằng `use`.
- Tên file API kết thúc bằng `Api.js`.
- Không gọi API trực tiếp trong JSX.
- Xử lý đầy đủ trạng thái loading, empty và error.
- Không lưu provider token lâu hơn mức cần thiết.

### Backend

- Class dùng PascalCase.
- Biến và phương thức dùng camelCase.
- Hằng số dùng UPPER_SNAKE_CASE.
- DTO Request và Response tách biệt.
- Không trả trực tiếp Entity.
- Service chịu trách nhiệm nghiệp vụ.
- Repository chỉ truy cập dữ liệu.
- Exception xử lý tập trung.
- Dùng `@Transactional` cho thao tác quan trọng.
- Không đặt logic nghiệp vụ phức tạp trong Controller.

---

## 🚧 Chức năng ngoài phạm vi MVP

- Hồ sơ riêng tư.
- Follow Request.
- Video và tài liệu trong bài viết.
- Bài viết nháp.
- Mention.
- Trích dẫn bài viết.
- Chủ đề nội dung do Admin quản lý.
- Các chức năng khám phá nâng cao theo địa điểm ngoài marker Map V1, gồm tìm kiếm địa điểm trên Map, trang Location và địa điểm phổ biến.
- Feed cá nhân hóa do người dùng tạo.
- Feed công khai và lưu Feed.
- Elasticsearch.
- Message Request và Hidden Message Request.
- Tài liệu, video, online status, recall và report message trong Nhắn tin.
- School Suggestion và import/sync dữ liệu học thuật từ nguồn bên ngoài.
- AI/ML Recommendation, collaborative filtering và Smart Student Match nâng cao dựa trên hành vi.
- Dashboard thống kê nâng cao.
- Machine Learning cho hệ thống gợi ý.
- Ứng dụng mobile native.
- Group Chat.
- Gọi thoại.
- Gọi video.
- Livestream.

---

## 🔮 Định hướng phát triển

- Hồ sơ riêng tư và Follow Request.
- Trích dẫn bài viết và Quote Post.
- Video và tài liệu học tập.
- Bài viết nháp.
- Mention người dùng.
- Feed cá nhân hóa.
- Elasticsearch.
- Khám phá nội dung theo địa điểm.
- Manual E2E Discovery Map với tài khoản, Google Maps key, MySQL và quyền Geolocation thật.
- Backend xác minh và đồng bộ định kỳ dữ liệu với Google Places.
- Message Request.
- Tài liệu, video, online status, recall và report message trong Nhắn tin.
- School Suggestion và import/sync dữ liệu học thuật từ nguồn bên ngoài.
- AI/ML Recommendation, collaborative filtering và thuật toán gợi ý nâng cao dựa trên hành vi.
- Dashboard thống kê nâng cao.
- Audit Log chi tiết.
- Thuật toán gợi ý nâng cao.
- Ứng dụng di động.

---

## 📚 Tài liệu dự án

Các tài liệu phân tích và thiết kế được lưu trong thư mục `docs/`:

- Đặc tả nghiệp vụ.
- Sơ đồ phân rã chức năng.
- Use Case tổng quát và chi tiết.
- Activity Diagram.
- Sequence Diagram.
- Entity Relationship Diagram.
- Tài liệu thiết kế database.
- Tài liệu API.
- Thiết kế giao diện.
- Kịch bản kiểm thử.
- Hướng dẫn sử dụng hệ thống.
- Ma trận trạng thái triển khai: [`docs/IMPLEMENTATION-STATUS.md`](docs/IMPLEMENTATION-STATUS.md).

---

## 📄 Giấy phép

Dự án được phát triển nhằm phục vụ mục đích học tập, nghiên cứu và thực hiện luận văn tốt nghiệp.

Không sử dụng dự án cho mục đích thương mại khi chưa có sự đồng ý của nhóm phát triển.

---

## © Bản quyền

**© 2026 – Trường Đại học Công nghệ Sài Gòn**

**Khoa Công nghệ Thông tin**

**Đề tài: Xây dựng hệ thống mạng xã hội tinh gọn hướng đến sinh viên**
