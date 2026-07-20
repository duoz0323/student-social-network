# 🎓 Student Social Network

> Website mạng xã hội tinh gọn hướng đến sinh viên, được phát triển trong khuôn khổ luận văn tốt nghiệp ngành Công nghệ thông tin.

---

## 📖 Giới thiệu

**Student Social Network** là nền tảng mạng xã hội tinh gọn hướng đến cộng đồng sinh viên, cho phép người dùng chia sẻ nội dung ngắn, kết nối, tương tác và khám phá các trải nghiệm thực tế liên quan đến học tập, ăn uống, vui chơi và đời sống sinh viên.

Hệ thống cung cấp các nhóm chức năng chính:

- Đăng ký và đăng nhập bằng email, số điện thoại, Google hoặc Facebook.
- Xác minh email và số điện thoại bằng mã OTP.
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
- Hỗ trợ xác thực đa phương thức bằng email, số điện thoại, Google và Facebook.
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

- Bắt đầu đăng ký bằng email hoặc số điện thoại.
- Xác minh đăng ký bằng OTP.
- Đăng ký hoặc đăng nhập bằng Google/Facebook.
- Đăng nhập bằng email hoặc số điện thoại đã xác minh.
- Gửi lại mã xác minh hoặc tiếp tục đăng ký đang chờ.
- Yêu cầu đặt lại mật khẩu nếu chức năng được triển khai.

Khách chưa đăng nhập không được truy cập các chức năng mạng xã hội.

### 2. Người dùng

Người dùng đã đăng nhập và có tài khoản đang hoạt động có thể:

- Hoàn tất và quản lý hồ sơ cá nhân.
- Quản lý các phương thức đăng nhập đã liên kết.
- Theo dõi người dùng khác.
- Đăng và quản lý bài viết.
- Like, bình luận và lưu bài viết.
- Xem Feed.
- Tìm kiếm.
- Báo cáo bài viết vi phạm.

### 3. Quản trị viên

Quản trị viên có thể:

- Quản lý người dùng.
- Khóa và mở khóa tài khoản.
- Quản lý bài viết.
- Ẩn và khôi phục bài viết.
- Xem và xử lý báo cáo vi phạm.
- Xem lịch sử thao tác quản trị khi chức năng tương ứng được triển khai.

---

## ✨ Phạm vi chức năng MVP

### 🔐 1. Xác thực, đăng ký và hoàn tất hồ sơ

#### Phương thức xác thực

Hệ thống hỗ trợ bốn phương thức:

- Email và mật khẩu, xác minh bằng OTP email.
- Số điện thoại và mật khẩu, xác minh bằng OTP SMS.
- Google.
- Facebook.

Hệ thống áp dụng mô hình:

```text
Một tài khoản nội bộ
        +
Nhiều phương thức xác thực
```

Mọi phương thức đã được xác minh và liên kết hợp lệ đều ánh xạ về cùng một `users.id`. Sau khi xác thực thành công, Backend cấp JWT Access Token và Refresh Token của hệ thống.

#### Đăng ký local bằng email hoặc số điện thoại

- Người dùng chỉ cung cấp một định danh tại một thời điểm: email hoặc số điện thoại.
- Form đăng ký gồm định danh, mật khẩu và xác nhận mật khẩu.
- Backend chuẩn hóa email về chữ thường và chuẩn hóa số điện thoại trước khi kiểm tra.
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
- Nếu Frontend mất flow token, người dùng nhập lại email hoặc số điện thoại; Backend phát hiện đăng ký đang chờ và không tạo bản ghi trùng.
- OTP cũ phải mất hiệu lực khi mã mới được phát hành.

#### Đăng ký và đăng nhập bằng Google/Facebook

- Frontend nhận credential/token từ Google hoặc Facebook và gửi về Backend.
- Backend phải xác minh token với nhà cung cấp trước khi tin cậy thông tin người dùng.
- Google/Facebook token chỉ dùng tại endpoint xác thực chuyên biệt.
- Các API nghiệp vụ khác chỉ chấp nhận JWT do Backend phát hành.
- Nếu provider đã được liên kết, Backend đăng nhập đúng tài khoản hiện có.
- Nếu provider chưa được liên kết và thông tin hợp lệ chưa thuộc tài khoản nào, Backend tạo tài khoản nội bộ mới.
- Nếu social email trùng một tài khoản `ACTIVE` nhưng provider chưa được liên kết, hệ thống không tự động gộp chỉ dựa trên email.

#### Chuyển từ đăng ký OTP sang social

- Email đang chờ xác minh và social trả cùng email đã xác minh: được phép hoàn tất đăng ký tạm, giữ phương thức local và liên kết social vào cùng tài khoản.
- Email đang chờ nhưng social trả email khác: người dùng phải chọn tiếp tục OTP hoặc hủy đăng ký tạm để dùng social.
- Số điện thoại đang chờ chuyển sang social: social không được xem là bằng chứng xác minh số điện thoại; người dùng phải xác nhận hủy đăng ký phone trước khi tiếp tục.
- Chỉ hủy hoặc hoàn tất đăng ký tạm sau khi Backend xác minh social token thành công.

#### Đăng nhập local

- Người dùng đăng nhập bằng email hoặc số điện thoại và mật khẩu.
- Email chỉ được dùng đăng nhập khi `email_verified_at` khác `NULL`.
- Số điện thoại chỉ được dùng đăng nhập khi `phone_verified_at` khác `NULL`.
- Tài khoản chỉ dùng social có thể có `password_hash = NULL` và chưa thể đăng nhập local.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.

#### Liên kết phương thức đăng nhập

Người dùng đã đăng nhập có thể liên kết thêm:

- Email.
- Số điện thoại.
- Google.
- Facebook.

Quy tắc:

- Email và số điện thoại phải được xác minh OTP trước khi liên kết.
- Google/Facebook phải được xác minh trong phiên đang đăng nhập.
- `user_id` đích được lấy từ JWT hiện tại, không suy ra chỉ bằng email social.
- Định danh hoặc provider đã thuộc tài khoản khác phải bị từ chối.
- Không tự động gộp hai tài khoản đang hoạt động.
- Không được gỡ phương thức đăng nhập cuối cùng.
- Tài khoản social muốn dùng đăng nhập local phải thiết lập mật khẩu và xác minh định danh tương ứng.

#### JWT và quản lý phiên

- Xác thực bằng JWT Access Token.
- Làm mới Access Token bằng Refresh Token.
- Refresh Token được lưu dưới dạng hash.
- Thu hồi Refresh Token khi đăng xuất.
- Phân quyền `USER` và `ADMIN`.

#### Hoàn tất hồ sơ ban đầu

- Sau khi tài khoản thật được tạo, Frontend điều hướng đến onboarding.
- Tên hiển thị và ngày sinh là thông tin bắt buộc.
- Người dùng phải đủ 18 tuổi tại thời điểm Backend xử lý.
- Ảnh đại diện và giới thiệu cá nhân là tùy chọn.
- Hồ sơ chỉ được đánh dấu hoàn tất khi dữ liệu bắt buộc hợp lệ và người dùng xác nhận.
- Trạng thái hoàn tất được lưu tại `user_profiles.profile_completed_at`.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép các API xác thực, token, đăng xuất và onboarding.
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

Tất cả hồ sơ trong MVP đều công khai. Email, số điện thoại và dữ liệu xác thực không được trả trong API hồ sơ công khai.

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

### 📝 4. Quản lý bài viết

- Tạo bài viết.
- Xem chi tiết bài viết.
- Chỉnh sửa bài viết trong 15 phút sau khi đăng.
- Xóa mềm bài viết.
- Đăng nội dung văn bản tối đa 500 ký tự.
- Tải lên tối đa 4 hình ảnh.
- Gắn tối đa một hashtag.

Bài viết phải có ít nhất một trong hai thành phần:

- Nội dung văn bản.
- Hình ảnh.

Trạng thái bài viết:

- `PUBLISHED`: bài viết đang hoạt động.
- `HIDDEN`: bài viết bị quản trị viên ẩn.
- `DELETED`: bài viết đã được tác giả xóa mềm.

### ❤️ 5. Tương tác bài viết

- Like và Unlike bài viết.
- Thêm bình luận.
- Xem danh sách bình luận.
- Xóa bình luận của chính mình.
- Lưu và bỏ lưu bài viết.
- Xem danh sách bài viết đã lưu.

Quy tắc:

- Mỗi người chỉ được Like một bài tối đa một lần.
- Mỗi người chỉ được lưu một bài tối đa một lần.
- Không được tương tác với bài viết đã bị ẩn hoặc xóa.
- Danh sách bài viết đã lưu chỉ hiển thị với chủ tài khoản.

### 📰 6. Bảng tin

#### Feed Following

- Hiển thị bài viết của những người dùng đang được theo dõi.
- Sắp xếp theo thời gian đăng giảm dần.
- Không hiển thị bài viết bị ẩn hoặc bị xóa.
- Hỗ trợ phân trang.

#### Feed For You

- Hiển thị các bài viết hợp lệ trong hệ thống.
- Ưu tiên bài viết mới.
- Có thể xếp hạng theo số lượt Like và số lượng bình luận.
- Không sử dụng Machine Learning trong MVP.
- Hạn chế hiển thị liên tiếp quá nhiều bài viết của cùng một tác giả.

Công thức xếp hạng tham khảo:

```text
Điểm bài viết =
    Điểm độ mới
    + Số lượt Like × Trọng số Like
    + Số bình luận × Trọng số bình luận
```

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
- Hỗ trợ phân trang kết quả.
- MVP sử dụng MySQL làm nguồn tìm kiếm.
- Elasticsearch được đưa vào hướng phát triển sau khi hệ thống cốt lõi ổn định.

### 🚩 9. Báo cáo vi phạm

- Báo cáo bài viết.
- Chọn lý do báo cáo.
- Nhập mô tả bổ sung.
- Theo dõi trạng thái xử lý.

Trạng thái báo cáo:

- `PENDING`.
- `RESOLVED`.
- `REJECTED`.

Gửi báo cáo không tự động làm ẩn bài viết. Quản trị viên là người đưa ra quyết định xử lý cuối cùng.

### 🛡️ 10. Quản trị hệ thống

#### Quản lý người dùng

- Xem danh sách người dùng.
- Tìm kiếm người dùng.
- Khóa và mở khóa tài khoản.
- Lưu lý do thay đổi trạng thái tài khoản.
- Thu hồi Refresh Token còn hiệu lực khi khóa tài khoản.

#### Quản lý bài viết

- Xem danh sách và chi tiết bài viết.
- Ẩn và khôi phục bài viết.
- Không khôi phục bài viết đã bị tác giả xóa mềm.

#### Quản lý báo cáo

- Xem danh sách và chi tiết báo cáo.
- Xác nhận hoặc từ chối báo cáo.
- Ẩn bài viết vi phạm.

#### Hoạt động quản trị

- Ghi lại các hành động quản trị quan trọng.
- Hỗ trợ hiển thị lịch sử hoạt động của quản trị viên.

---

## 📌 Mức độ ưu tiên triển khai

### P0 – Bắt buộc hoàn thành

- Đăng ký email/số điện thoại có OTP.
- Đăng ký và đăng nhập Google/Facebook.
- Đăng nhập local.
- Hoàn tất hồ sơ ban đầu.
- Đăng xuất.
- JWT Access Token và Refresh Token.
- Phân quyền `USER` và `ADMIN`.
- Quản lý hồ sơ.
- Follow và Unfollow.
- CRUD bài viết.
- Upload hình ảnh.
- Like và Unlike.
- Bình luận.
- Feed Following.
- Feed For You.

### P1 – Cần hoàn thành

- Liên kết và quản lý nhiều phương thức đăng nhập.
- Save và Unsave.
- Hashtag và gợi ý hashtag.
- Tìm kiếm người dùng.
- Tìm kiếm bài viết.
- Báo cáo bài viết.
- Admin khóa và mở khóa tài khoản.
- Admin ẩn và khôi phục bài viết.
- Hiển thị hoạt động quản trị cơ bản.

### P2 – Thực hiện nếu đủ tiến độ

- Quên mật khẩu.
- Đặt lại mật khẩu.
- Trả lời bình luận một cấp.
- Thông báo đơn giản.

---

## 🛠️ Công nghệ sử dụng

### Frontend

- ReactJS.
- JavaScript ES6+.
- Vite.
- React Router DOM.
- Axios.
- Tailwind CSS.
- Google Identity Services.
- Facebook SDK for JavaScript.
- Context API hoặc Zustand.
- ESLint.

### Backend

- Java.
- Spring Boot.
- Spring Web.
- Spring Security.
- Spring Data JPA.
- Hibernate.
- Bean Validation.
- JWT Authentication.
- Google token verification.
- Facebook access-token verification.
- Java Mail hoặc dịch vụ email tương đương.
- Nhà cung cấp SMS OTP.
- Maven.
- Lombok.
- MapStruct hoặc mapper thủ công.

### Database

- MySQL 8.
- MySQL Workbench.

### Lưu trữ media

- Cloudinary.
- Firebase Storage.
- Amazon S3.

Database chỉ lưu URL và metadata của media, không lưu dữ liệu BLOB.

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
│ Main Data    │  │ Email/SMS/OAuth  │
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
│   ├── student_social_network_db.sql
│   ├── student_social_network_db.dbml
│   └── seed_data.sql
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
- `provider`: tích hợp email, SMS, Google và Facebook.
- `mapper`: chuyển đổi Entity và DTO.
- `common/security`: JWT và Spring Security.
- `common/exception`: xử lý lỗi tập trung.
- `common/api`: chuẩn hóa response.

Nguyên tắc:

- Controller không trực tiếp truy cập Repository.
- Entity không được trả trực tiếp ra API.
- Mọi kiểm tra quyền phải thực hiện tại Backend.
- Các thao tác nhiều bước phải dùng transaction khi cần.
- Không giữ transaction database trong lúc chờ SMTP, SMS hoặc provider bên ngoài.
- Không lưu mật khẩu, OTP, flow token hoặc Refresh Token dưới dạng văn bản thuần.

---

## 🗄️ Các bảng dữ liệu chính

### Tài khoản và xác thực

- `users`: tài khoản nội bộ, email/số điện thoại, mật khẩu băm, trạng thái xác minh, vai trò và trạng thái tài khoản.
- `user_profiles`: hồ sơ người dùng và trạng thái onboarding.
- `pending_registrations`: đăng ký local đang chờ OTP.
- `user_auth_providers`: liên kết Google/Facebook với cùng một `users.id`.
- `refresh_tokens`: phiên làm mới JWT.
- `password_reset_tokens`: token đặt lại mật khẩu.

### Quan hệ theo dõi

- `follows`.

### Bài viết

- `posts`.
- `post_media`.
- `hashtags`.
- `post_hashtags`.

### Tương tác

- `post_likes`.
- `comments`.
- `saved_posts`.

### Báo cáo và quản trị

- `reports`.
- `account_status_histories`.
- `admin_actions`.

Ràng buộc Auth quan trọng:

- `users.email` là duy nhất khi có giá trị.
- `users.phone_number` là duy nhất khi có giá trị.
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
POST   /api/v1/users/me/auth-providers/phone
POST   /api/v1/users/me/auth-providers/google
POST   /api/v1/users/me/auth-providers/facebook
DELETE /api/v1/users/me/auth-providers/{provider}
```

### Các nhóm API khác

```text
/api/v1/users
/api/v1/follows
/api/v1/posts
/api/v1/feeds
/api/v1/search
/api/v1/reports
/api/v1/admin
```

Ví dụ bắt đầu đăng ký:

```json
{
  "identifier": "student@example.com",
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

Ví dụ Google authentication:

```json
{
  "idToken": "<google-id-token>",
  "registrationFlowToken": "<optional>"
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

```bash
mysql -u root -p
```

```sql
CREATE DATABASE student_social_network
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

```bash
mysql -u root -p student_social_network < database/student_social_network_db.sql
```

### 4. Cấu hình Backend

Ví dụ biến môi trường:

```env
DB_URL=jdbc:mysql://localhost:3306/student_social_network?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

JWT_ACCESS_SECRET=your_access_token_secret
JWT_REFRESH_SECRET=your_refresh_token_secret
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=2592000000

MAIL_USERNAME=your_mail_username
MAIL_PASSWORD=your_mail_app_password

GOOGLE_CLIENT_ID=your_google_client_id
FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_APP_SECRET=your_facebook_app_secret

SMS_PROVIDER_API_KEY=your_sms_provider_api_key
```

Không đưa thông tin bí mật lên GitHub.

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
```

```bash
npm run dev
```

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
- Đăng ký số điện thoại và xác minh OTP.
- Gửi lại OTP và giới hạn tần suất.
- OTP sai, hết hạn, đã dùng hoặc vượt số lần thử.
- Tiếp tục đăng ký sau khi đóng tab hoặc mất mạng.
- Đăng ký/đăng nhập Google.
- Đăng ký/đăng nhập Facebook.
- Email pending chuyển sang social cùng email.
- Phone pending chuyển sang social.
- Provider đã liên kết đăng nhập đúng `users.id`.
- Liên kết và gỡ phương thức đăng nhập.
- Từ chối gỡ phương thức cuối cùng.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.

---

## ✅ Tiêu chí nghiệm thu MVP

### Xác thực và tài khoản

1. Đăng ký local chỉ tạo `pending_registrations`, chưa tạo `users`.
2. OTP hợp lệ mới tạo `users` và `user_profiles`.
3. `users` và `user_profiles` được tạo trong cùng transaction.
4. OTP hết hạn, đã dùng, bị hủy hoặc vượt số lần thử không thể sử dụng.
5. Không tồn tại hai đăng ký tạm còn hiệu lực cho cùng một định danh.
6. Mất mạng hoặc đóng tab vẫn có thể tiếp tục trong thời hạn.
7. Email pending cùng social email đã xác minh hoàn tất đúng một tài khoản.
8. Phone pending chuyển social không mang theo số điện thoại chưa xác minh.
9. Provider đã liên kết luôn đăng nhập đúng `users.id`.
10. Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
11. Không cho gỡ phương thức đăng nhập cuối cùng.
12. Tài khoản social chưa có mật khẩu không đăng nhập local được.
13. Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
14. Access Token và Refresh Token hoạt động đúng.
15. Refresh Token bị thu hồi không thể cấp Access Token mới.

### Hồ sơ và chức năng mạng xã hội

16. Người dùng hoàn tất onboarding hợp lệ trước khi truy cập chức năng chính.
17. Người dùng cập nhật và xem hồ sơ thành công.
18. Follow và Unfollow hoạt động đúng, không tạo quan hệ trùng.
19. Người dùng tạo bài có nội dung hoặc hình ảnh.
20. Chỉ tác giả được sửa hoặc xóa bài của mình.
21. Bài bị ẩn hoặc xóa không xuất hiện trên Feed.
22. Like, bình luận và Save hoạt động đúng.
23. Feed Following và Feed For You trả đúng dữ liệu hợp lệ.
24. Tìm kiếm người dùng và bài viết hoạt động đúng.
25. Người dùng gửi được báo cáo.
26. Admin quản lý được người dùng, bài viết và báo cáo.
27. Backend từ chối thao tác không có quyền.
28. Mật khẩu, OTP, flow token và Refresh Token không lưu dạng văn bản thuần.
29. Backend không trả stack trace hoặc thông tin nhạy cảm cho Client.

---

## 🔒 Yêu cầu bảo mật

- Mật khẩu được băm bằng BCrypt.
- OTP, flow token và Refresh Token chỉ lưu dạng hash.
- Access Token có thời hạn ngắn.
- Refresh Token có khả năng thu hồi.
- Rate limit cho đăng nhập, gửi OTP, gửi lại OTP và social authentication.
- Token Google/Facebook phải được xác minh tại Backend.
- Không tin email, provider ID hoặc trạng thái verified do Frontend tự gửi.
- API Admin chỉ dành cho vai trò `ADMIN`.
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
- API danh sách hỗ trợ phân trang.
- Kích thước trang mặc định là 20.
- Hệ thống hướng đến 30–50 người dùng đồng thời trong môi trường kiểm thử.
- Tránh truy vấn N+1.
- Không tải toàn bộ Entity Relationship không cần thiết.
- Việc gửi email/SMS không giữ transaction database mở quá lâu.
- Các tác vụ dọn đăng ký hết hạn có thể chạy theo lịch.

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
- Block và Restrict giữa người dùng.
- Video và tài liệu trong bài viết.
- Bài viết nháp.
- Mention.
- Repost.
- Trích dẫn bài viết.
- Chủ đề nội dung do Admin quản lý.
- Địa điểm và Discovery Map.
- Feed cá nhân hóa do người dùng tạo.
- Feed công khai và lưu Feed.
- Elasticsearch.
- Nhắn tin trực tiếp.
- Message Request và Hidden Message Request.
- Thông báo thời gian thực.
- Quản lý trường, khoa và ngành.
- Dashboard thống kê nâng cao.
- Moderation Case.
- Machine Learning cho hệ thống gợi ý.
- Ứng dụng mobile native.
- Group Chat.
- Gọi thoại.
- Gọi video.
- Livestream.

---

## 🔮 Định hướng phát triển

- Hồ sơ riêng tư và Follow Request.
- Block và Restrict.
- Repost và trích dẫn bài viết.
- Video và tài liệu học tập.
- Bài viết nháp.
- Mention người dùng.
- Feed cá nhân hóa.
- Elasticsearch.
- Khám phá nội dung theo địa điểm.
- Discovery Map.
- Nhắn tin trực tiếp bằng WebSocket.
- Message Request.
- Thông báo thời gian thực.
- Quản lý trường, khoa và ngành.
- Dashboard thống kê.
- Audit Log chi tiết.
- Moderation Case.
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

---

## 📄 Giấy phép

Dự án được phát triển nhằm phục vụ mục đích học tập, nghiên cứu và thực hiện luận văn tốt nghiệp.

Không sử dụng dự án cho mục đích thương mại khi chưa có sự đồng ý của nhóm phát triển.

---

## © Bản quyền

**© 2026 – Trường Đại học Công nghệ Sài Gòn**

**Khoa Công nghệ Thông tin**

**Đề tài: Xây dựng hệ thống mạng xã hội tinh gọn hướng đến sinh viên**
