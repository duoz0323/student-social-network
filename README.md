# 🎓 Student Social Network

> Website mạng xã hội tinh gọn hướng đến sinh viên, được phát triển trong khuôn khổ luận văn tốt nghiệp ngành Công nghệ thông tin.

---

## 📖 Giới thiệu

**Student Social Network** là một nền tảng mạng xã hội tinh gọn hướng đến cộng đồng sinh viên.

Hệ thống cung cấp các chức năng cốt lõi của một mạng xã hội như:

* Đăng ký và đăng nhập tài khoản.
* Quản lý hồ sơ cá nhân.
* Theo dõi người dùng khác.
* Đăng tải và quản lý bài viết ngắn.
* Tương tác thông qua lượt thích và bình luận.
* Lưu bài viết.
* Xem bảng tin cá nhân.
* Tìm kiếm người dùng và bài viết.
* Báo cáo nội dung vi phạm.
* Quản trị người dùng, bài viết và báo cáo.

Dự án được xây dựng theo mô hình **Client – Server**, trong đó Frontend và Backend được phát triển độc lập và giao tiếp với nhau thông qua RESTful API.

Phiên bản hiện tại tập trung vào phạm vi **Minimum Viable Product – MVP**, nhằm hoàn thiện một luồng sử dụng mạng xã hội xuyên suốt, ổn định và có thể kiểm thử trong thời gian thực hiện luận văn.

---

## 🎯 Mục tiêu dự án

Dự án hướng đến các mục tiêu chính sau:

* Xây dựng một website mạng xã hội tinh gọn dành cho sinh viên.
* Tạo môi trường để sinh viên chia sẻ nội dung và kết nối với nhau.
* Xây dựng đầy đủ luồng xác thực bằng JWT Access Token và Refresh Token.
* Triển khai các chức năng cốt lõi của mạng xã hội.
* Áp dụng kiến trúc Frontend và Backend tách biệt.
* Đảm bảo khả năng mở rộng, bảo trì và kiểm thử.
* Áp dụng các nguyên tắc bảo mật trong quá trình xây dựng hệ thống.
* Hoàn thiện sản phẩm phục vụ báo cáo và bảo vệ luận văn tốt nghiệp.

---

## 👨‍🎓 Nhóm thực hiện

### Thành viên 1

* **Họ và tên:** Nguyễn Hoàng Dương
* **MSSV:** DH52200548
* **Vai trò:**

  * Phân tích yêu cầu nghiệp vụ.
  * Thiết kế cơ sở dữ liệu.
  * Phát triển Backend.
  * Xây dựng RESTful API.
  * Tích hợp bảo mật JWT.
  * Kiểm thử API.

### Thành viên 2

* **Họ và tên:** Đậu Quốc Khánh
* **MSSV:** DH52200867
* **Vai trò:**

  * Thiết kế giao diện người dùng.
  * Phát triển Frontend.
  * Tích hợp Frontend với RESTful API.
  * Kiểm thử giao diện và luồng người dùng.

### Giảng viên hướng dẫn

* **Giảng viên:** Hoàng Công Quang Huy

---

## 👥 Đối tượng sử dụng

Hệ thống có ba nhóm đối tượng sử dụng chính.

### 1. Khách chưa đăng nhập

Khách chưa đăng nhập có thể:

* Đăng ký tài khoản.
* Đăng nhập.
* Yêu cầu đặt lại mật khẩu nếu chức năng được triển khai.

Khách chưa đăng nhập không được truy cập các chức năng mạng xã hội.

### 2. Người dùng

Người dùng đã đăng nhập và có tài khoản đang hoạt động có thể:

* Quản lý hồ sơ cá nhân.
* Theo dõi người dùng khác.
* Đăng và quản lý bài viết.
* Like và bình luận bài viết.
* Lưu bài viết.
* Xem Feed.
* Tìm kiếm.
* Báo cáo bài viết vi phạm.

### 3. Quản trị viên

Quản trị viên có thể:

* Quản lý người dùng.
* Khóa và mở khóa tài khoản.
* Quản lý bài viết.
* Ẩn và khôi phục bài viết.
* Xem và xử lý báo cáo vi phạm.

---

## ✨ Phạm vi chức năng MVP

### 🔐 1. Xác thực, đăng ký và hoàn tất hồ sơ

* Đăng ký tài khoản bằng một trong hai phương thức: email hoặc số điện thoại.
* Form đăng ký chỉ gồm email hoặc số điện thoại, mật khẩu và xác nhận mật khẩu.
* Đăng nhập bằng email hoặc số điện thoại.
* Hệ thống không sử dụng username làm thông tin đăng ký hoặc đăng nhập trong phạm vi MVP.
* Email và số điện thoại được phép bổ sung đồng thời sau này, nhưng tài khoản luôn phải có ít nhất một phương thức định danh.
* Sau khi đăng ký, hệ thống tạo đồng thời `users` và một `user_profiles` rỗng trong cùng transaction.
* Người dùng bắt buộc hoàn tất hồ sơ ban đầu trước khi truy cập Feed và các chức năng mạng xã hội.
* Đăng xuất khỏi phiên hiện tại.
* Xác thực bằng JWT Access Token.
* Làm mới Access Token bằng Refresh Token.
* Thu hồi Refresh Token khi đăng xuất.
* Phân quyền người dùng và quản trị viên.
* Khôi phục mật khẩu nếu đảm bảo tiến độ.

Quy tắc đăng ký:

* Request đăng ký chỉ nhận một giá trị định danh tại một thời điểm: email hoặc số điện thoại.
* Khi đăng ký bằng email, `phone_number` được lưu `NULL`.
* Khi đăng ký bằng số điện thoại, `email` được lưu `NULL`.
* Backend chuẩn hóa email về chữ thường và chuẩn hóa số điện thoại trước khi kiểm tra trùng.
* Database cho phép người dùng bổ sung phương thức còn thiếu trong tương lai.
* MVP tạo tài khoản ở trạng thái `ACTIVE` và chưa triển khai xác minh email/SMS OTP.
* Hai trường `email_verified_at` và `phone_verified_at` được chuẩn bị cho hướng phát triển.
* Email và số điện thoại không được trả trong API hồ sơ công khai.
* Nếu tạo `user_profiles` thất bại thì transaction phải rollback bản ghi `users`.

Quy trình hoàn tất hồ sơ:

* Sau đăng ký, Frontend điều hướng đến màn hình onboarding hồ sơ.
* Tên hiển thị là thông tin bắt buộc.
* Ảnh đại diện, ngày sinh và giới thiệu cá nhân là thông tin tùy chọn, có thể bỏ qua.
* Hồ sơ chỉ được đánh dấu hoàn tất khi tên hiển thị hợp lệ đã được lưu và người dùng xác nhận hoàn tất.
* Trạng thái hoàn tất được lưu tại `user_profiles.profile_completed_at`.
* `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không đồng nghĩa hồ sơ đã hoàn tất.
* Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép các API xác thực, làm mới token, đăng xuất và onboarding.
* Các API mạng xã hội chính phải trả lỗi `PROFILE_NOT_COMPLETED` cho tài khoản chưa hoàn tất hồ sơ.


### 👤 2. Hồ sơ người dùng

* Hoàn tất hồ sơ ban đầu sau khi đăng ký.
* Xem hồ sơ cá nhân.
* Xem hồ sơ người dùng khác.
* Cập nhật tên hiển thị.
* Cập nhật ảnh đại diện.
* Cập nhật phần giới thiệu cá nhân.
* Cập nhật ngày sinh.
* Xem số lượng người theo dõi.
* Xem số lượng người đang theo dõi.
* Xem danh sách bài viết đã đăng.

Trong phiên bản MVP, tất cả hồ sơ người dùng đều được đặt ở chế độ công khai. Tên hiển thị là thông tin bắt buộc để hoàn tất hồ sơ; ảnh đại diện, ngày sinh và giới thiệu cá nhân là tùy chọn.

### 👥 3. Theo dõi người dùng

* Theo dõi người dùng.
* Bỏ theo dõi người dùng.
* Xem danh sách người theo dõi.
* Xem danh sách người đang theo dõi.

Quy tắc nghiệp vụ:

* Người dùng không được theo dõi chính mình.
* Một người chỉ được theo dõi một tài khoản tối đa một lần.
* Quan hệ theo dõi có hiệu lực ngay.
* Phiên bản MVP chưa triển khai hồ sơ riêng tư và yêu cầu theo dõi.

### 📝 4. Quản lý bài viết

* Tạo bài viết.
* Xem chi tiết bài viết.
* Chỉnh sửa bài viết.
* Xóa mềm bài viết.
* Đăng nội dung văn bản tối đa 500 ký tự.
* Tải lên tối đa 4 hình ảnh.
* Gắn nhiều hashtag vào bài viết.

Bài viết phải có ít nhất một trong hai thành phần:

* Nội dung văn bản.
* Hình ảnh.

Trạng thái bài viết:

* `PUBLISHED`: Bài viết đang hoạt động.
* `HIDDEN`: Bài viết bị quản trị viên ẩn.
* `DELETED`: Bài viết đã được tác giả xóa mềm.

### ❤️ 5. Tương tác bài viết

* Like bài viết.
* Unlike bài viết.
* Thêm bình luận.
* Xem danh sách bình luận.
* Xóa bình luận của chính mình.
* Lưu bài viết.
* Bỏ lưu bài viết.
* Xem danh sách bài viết đã lưu.

Quy tắc nghiệp vụ:

* Mỗi người dùng chỉ được Like một bài viết tối đa một lần.
* Mỗi người dùng chỉ được lưu một bài viết tối đa một lần.
* Không được tương tác với bài viết đã bị ẩn hoặc xóa.
* Danh sách bài viết đã lưu chỉ hiển thị với chủ tài khoản.

### 📰 6. Bảng tin

Hệ thống cung cấp hai loại Feed mặc định.

#### Feed Following

* Hiển thị bài viết của những người dùng đang được theo dõi.
* Sắp xếp theo thời gian đăng giảm dần.
* Không hiển thị bài viết bị ẩn hoặc bị xóa.
* Hỗ trợ phân trang.

#### Feed For You

* Hiển thị các bài viết hợp lệ trong hệ thống.
* Ưu tiên bài viết mới.
* Có thể xếp hạng theo số lượt Like và số lượng bình luận.
* Không sử dụng Machine Learning trong phiên bản MVP.
* Hạn chế hiển thị liên tiếp quá nhiều bài viết của cùng một tác giả.

Công thức xếp hạng tham khảo:

```text
Điểm bài viết =
    Điểm độ mới
    + Số lượt Like × Trọng số Like
    + Số bình luận × Trọng số bình luận
```

Các trọng số được cấu hình trong Backend và có thể điều chỉnh trong quá trình kiểm thử.

### #️⃣ 7. Hashtag

* Gắn hashtag vào bài viết.
* Chuẩn hóa hashtag về chữ thường.
* Một bài viết có thể có nhiều hashtag.
* Xem danh sách bài viết theo hashtag.

Phiên bản MVP không quản lý chủ đề nội dung riêng biệt.

### 🔍 8. Tìm kiếm

* Tìm kiếm người dùng theo tên hiển thị.
* Có thể lọc hoặc mở hồ sơ bằng mã người dùng nội bộ; không tìm kiếm bằng username trong MVP.
* Tìm kiếm bài viết theo nội dung.
* Tìm kiếm bài viết theo hashtag.
* Hỗ trợ phân trang kết quả.

Phiên bản MVP sử dụng MySQL để tìm kiếm. Elasticsearch được chuyển sang hướng phát triển sau khi phiên bản cốt lõi đã ổn định.

### 🚩 9. Báo cáo vi phạm

* Báo cáo bài viết.
* Chọn lý do báo cáo.
* Nhập mô tả bổ sung.
* Theo dõi trạng thái xử lý báo cáo.

Trạng thái báo cáo:

* `PENDING`: Đang chờ xử lý.
* `RESOLVED`: Báo cáo hợp lệ và đã được xử lý.
* `REJECTED`: Báo cáo không hợp lệ.

Việc gửi báo cáo không tự động làm ẩn bài viết. Quản trị viên là người đưa ra quyết định xử lý cuối cùng.

### 🛡️ 10. Quản trị hệ thống

#### Quản lý người dùng

* Xem danh sách người dùng.
* Tìm kiếm người dùng.
* Khóa tài khoản.
* Mở khóa tài khoản.

#### Quản lý bài viết

* Xem danh sách bài viết.
* Xem bài viết bị báo cáo.
* Ẩn bài viết.
* Khôi phục bài viết.

#### Quản lý báo cáo

* Xem danh sách báo cáo.
* Xem chi tiết báo cáo.
* Xác nhận báo cáo hợp lệ.
* Từ chối báo cáo.
* Ẩn bài viết vi phạm.

---

## 📌 Mức độ ưu tiên triển khai

### P0 – Bắt buộc hoàn thành

* Đăng ký.
* Hoàn tất hồ sơ ban đầu.
* Đăng nhập.
* Đăng xuất.
* JWT Access Token và Refresh Token.
* Phân quyền `USER` và `ADMIN`.
* Quản lý hồ sơ.
* Follow và Unfollow.
* Tạo, xem, chỉnh sửa và xóa bài viết.
* Upload hình ảnh.
* Like và Unlike.
* Bình luận.
* Feed Following.
* Feed For You.

### P1 – Cần hoàn thành

* Save và Unsave.
* Hashtag.
* Tìm kiếm người dùng.
* Tìm kiếm bài viết.
* Báo cáo bài viết.
* Admin khóa và mở khóa tài khoản.
* Admin ẩn và khôi phục bài viết.

### P2 – Thực hiện nếu đủ tiến độ

* Quên mật khẩu.
* Đặt lại mật khẩu.
* Trả lời bình luận một cấp.
* Thông báo đơn giản.
* Lịch sử thao tác quản trị cơ bản.

---

## 🛠️ Công nghệ sử dụng

### Frontend

* ReactJS.
* JavaScript ES6+.
* Vite.
* React Router DOM.
* Axios.
* Tailwind CSS.
* Context API hoặc Zustand tùy nhu cầu quản lý trạng thái.
* ESLint.

### Backend

* Java.
* Spring Boot.
* Spring Web.
* Spring Security.
* Spring Data JPA.
* Hibernate.
* Bean Validation.
* JWT Authentication.
* Maven.
* Lombok.
* MapStruct hoặc mapper thủ công.

### Database

* MySQL 8.
* MySQL Workbench.

### Lưu trữ hình ảnh

Có thể sử dụng một trong các dịch vụ:

* Cloudinary.
* Firebase Storage.
* Amazon S3.

Database chỉ lưu URL và metadata của hình ảnh, không lưu trực tiếp dữ liệu ảnh dạng BLOB.

### Công cụ phát triển

* Git.
* GitHub.
* Postman.
* IntelliJ IDEA.
* Visual Studio Code.
* MySQL Workbench.
* Docker tùy chọn.
* Figma tùy chọn.

---

## 🏗️ Kiến trúc tổng thể

```text
┌──────────────────────────┐
│      Client Browser      │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│    ReactJS Frontend      │
│   Vite + Tailwind CSS    │
└─────────────┬────────────┘
              │
              │ RESTful API / JSON
              │ JWT Access Token
              ▼
┌──────────────────────────┐
│ Spring Boot Backend      │
│                          │
│ Controller               │
│ Service                  │
│ Repository               │
│ Security                 │
│ Validation               │
└───────┬───────────┬──────┘
        │           │
        ▼           ▼
┌──────────────┐  ┌────────────────┐
│   MySQL 8    │  │ Cloud Storage  │
│ Main Data    │  │ Post Images    │
└──────────────┘  └────────────────┘
```

---

## 📂 Cấu trúc thư mục dự án

```text
student-social-network/
│
├── frontend/
│   ├── public/
│   │
│   ├── src/
│   │   ├── assets/
│   │   │   ├── icons/
│   │   │   └── images/
│   │   │
│   │   ├── components/
│   │   │   ├── common/
│   │   │   └── layout/
│   │   │
│   │   ├── config/
│   │   │   ├── axios.js
│   │   │   └── routes.js
│   │   │
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   │   ├── components/
│   │   │   │   ├── pages/
│   │   │   │   ├── services/
│   │   │   │   └── validation/
│   │   │   │
│   │   │   ├── profile/
│   │   │   │   ├── components/
│   │   │   │   ├── pages/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── follow/
│   │   │   │   ├── components/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── post/
│   │   │   │   ├── components/
│   │   │   │   ├── pages/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── feed/
│   │   │   │   ├── components/
│   │   │   │   ├── pages/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── search/
│   │   │   │   ├── components/
│   │   │   │   ├── pages/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── report/
│   │   │   │   ├── components/
│   │   │   │   └── services/
│   │   │   │
│   │   │   └── admin/
│   │   │       ├── components/
│   │   │       ├── pages/
│   │   │       └── services/
│   │   │
│   │   ├── hooks/
│   │   ├── routes/
│   │   ├── store/
│   │   ├── utils/
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   │
│   ├── .env.example
│   ├── eslint.config.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── studentsocialnetwork/
│   │   │   │           ├── common/
│   │   │   │           │   ├── config/
│   │   │   │           │   ├── exception/
│   │   │   │           │   ├── response/
│   │   │   │           │   ├── security/
│   │   │   │           │   └── util/
│   │   │   │           │
│   │   │   │           ├── auth/
│   │   │   │           │   ├── controller/
│   │   │   │           │   ├── dto/
│   │   │   │           │   ├── entity/
│   │   │   │           │   ├── mapper/
│   │   │   │           │   ├── repository/
│   │   │   │           │   └── service/
│   │   │   │           │
│   │   │   │           ├── user/
│   │   │   │           ├── follow/
│   │   │   │           ├── post/
│   │   │   │           ├── comment/
│   │   │   │           ├── reaction/
│   │   │   │           ├── savedpost/
│   │   │   │           ├── feed/
│   │   │   │           ├── search/
│   │   │   │           ├── report/
│   │   │   │           ├── admin/
│   │   │   │           └── StudentSocialNetworkApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   │
│   │   └── test/
│   │
│   ├── .env.example
│   └── pom.xml
│
├── database/
│   ├── student_social_network_db.sql
│   ├── seed_data.sql
│   └── erd/
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

Mỗi chức năng nghiệp vụ được đặt trong một thư mục riêng bên trong `features/`.

Ví dụ:

```text
features/post/
├── components/
│   ├── CreatePostForm.jsx
│   ├── PostCard.jsx
│   └── PostList.jsx
├── pages/
│   ├── PostDetailPage.jsx
│   └── SavedPostsPage.jsx
├── services/
│   └── postApi.js
└── validation/
    └── postSchema.js
```

Nguyên tắc:

* `components/`: Chứa component thuộc riêng module.
* `pages/`: Chứa các trang được khai báo trong Router.
* `services/`: Chứa các hàm gọi RESTful API.
* `validation/`: Chứa quy tắc kiểm tra dữ liệu biểu mẫu.
* Component dùng chung toàn hệ thống đặt trong `src/components/common/`.
* Cấu hình Axios và Interceptor đặt trong `src/config/axios.js`.
* Không gọi Axios trực tiếp bên trong component giao diện.
* Token được quản lý tập trung và tự động gắn vào request bằng Axios Interceptor.

---

## 🧩 Nguyên tắc tổ chức Backend

Backend được tổ chức theo module nghiệp vụ kết hợp với kiến trúc phân lớp.

Mỗi module có thể bao gồm:

```text
post/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── mapper/
├── repository/
└── service/
    └── impl/
```

Trách nhiệm của từng thành phần:

* `controller`: Tiếp nhận HTTP request và trả HTTP response.
* `dto/request`: Nhận và kiểm tra dữ liệu đầu vào.
* `dto/response`: Trả dữ liệu cần thiết cho Frontend.
* `entity`: Ánh xạ thực thể với bảng trong cơ sở dữ liệu.
* `repository`: Truy vấn và thao tác dữ liệu.
* `service`: Khai báo nghiệp vụ.
* `service/impl`: Triển khai nghiệp vụ và quản lý transaction.
* `mapper`: Chuyển đổi giữa Entity và DTO.
* `common/security`: Xử lý JWT và Spring Security.
* `common/exception`: Xử lý lỗi tập trung.
* `common/response`: Chuẩn hóa cấu trúc phản hồi API.

Nguyên tắc:

* Controller không trực tiếp truy cập Repository.
* Entity không được trả trực tiếp ra API.
* Mọi kiểm tra quyền phải thực hiện tại Backend.
* Các thao tác nhiều bước phải sử dụng Transaction khi cần.
* Không đặt toàn bộ nghiệp vụ vào Controller.
* Không lưu mật khẩu hoặc Refresh Token dưới dạng văn bản thuần.

---

## 🗄️ Các bảng dữ liệu chính

Các bảng dữ liệu dự kiến trong phạm vi MVP gồm:

### Tài khoản và hồ sơ

* `users`: lưu email hoặc số điện thoại, mật khẩu băm, trạng thái xác minh, vai trò và trạng thái tài khoản.
* `user_profiles`: lưu tên hiển thị, ảnh đại diện, giới thiệu, ngày sinh và `profile_completed_at`.
  Bản ghi được tạo rỗng ngay sau đăng ký; `display_name` được phép `NULL` cho đến khi hoàn tất onboarding.
* `refresh_tokens`
* `password_reset_tokens`

### Quan hệ theo dõi

* `follows`

### Bài viết

* `posts`
* `post_media`
* `hashtags`
* `post_hashtags`

### Tương tác

* `post_likes`
* `comments`
* `saved_posts`

### Báo cáo và quản trị

* `reports`
* `account_status_histories`
* `admin_actions`

Tên bảng thực tế có thể được điều chỉnh trong quá trình triển khai nhưng phải giữ đúng ý nghĩa và ràng buộc nghiệp vụ đã thống nhất.

---

## 🔐 Cơ chế xác thực

Hệ thống sử dụng hai loại Token.

### Access Token

* Dùng để xác thực các request đến Backend.
* Có thời hạn sử dụng ngắn.
* Được gửi trong HTTP Header:

```http
Authorization: Bearer <access_token>
```

### Refresh Token

* Dùng để yêu cầu cấp Access Token mới.
* Có thời hạn dài hơn Access Token.
* Được lưu trong cơ sở dữ liệu.
* Có thể bị thu hồi khi đăng xuất.
* Không được trả trong các API công khai không liên quan.

Quy trình xác thực:

```text
Đăng nhập
   │
   ▼
Backend kiểm tra tài khoản
   │
   ▼
Cấp Access Token và Refresh Token
   │
   ▼
Frontend dùng Access Token gọi API
   │
   ▼
Access Token hết hạn
   │
   ▼
Frontend gửi Refresh Token
   │
   ▼
Backend cấp Access Token mới
```

---

## 📡 Quy ước RESTful API

Base URL trong môi trường phát triển:

```text
http://localhost:8080/api/v1
```

Một số nhóm API dự kiến:

```text
/api/v1/auth
/api/v1/users
/api/v1/profiles
/api/v1/follows
/api/v1/posts
/api/v1/comments
/api/v1/likes
/api/v1/saved-posts
/api/v1/feeds
/api/v1/search
/api/v1/reports
/api/v1/admin
```

Ví dụ:

```http
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh-token
POST   /api/v1/auth/logout

GET    /api/v1/users/me/onboarding
PUT    /api/v1/users/me/onboarding/profile
POST   /api/v1/users/me/onboarding/complete

GET    /api/v1/users/{userId}
PUT    /api/v1/users/me/profile

POST   /api/v1/users/{userId}/follow
DELETE /api/v1/users/{userId}/follow

GET    /api/v1/posts
POST   /api/v1/posts
GET    /api/v1/posts/{postId}
PUT    /api/v1/posts/{postId}
DELETE /api/v1/posts/{postId}

POST   /api/v1/posts/{postId}/likes
DELETE /api/v1/posts/{postId}/likes

GET    /api/v1/posts/{postId}/comments
POST   /api/v1/posts/{postId}/comments

GET    /api/v1/feeds/following
GET    /api/v1/feeds/for-you
```

Ví dụ request đăng ký:

```json
{
  "identifier": "student@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

Ví dụ cập nhật hồ sơ ban đầu:

```json
{
  "displayName": "Nguyễn Văn A",
  "avatarUrl": null,
  "dateOfBirth": null,
  "bio": null
}
```

Ảnh đại diện, ngày sinh và giới thiệu có thể bỏ qua. Sau khi tên hiển thị hợp lệ, Frontend gọi API hoàn tất onboarding để Backend cập nhật `profile_completed_at`.

Cấu trúc phản hồi API tham khảo:

```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": {},
  "timestamp": "2026-06-21T10:00:00"
}
```

Cấu trúc phản hồi lỗi tham khảo:

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "errors": [
    {
      "field": "emailOrPhone",
      "message": "Email hoặc số điện thoại đã được sử dụng"
    }
  ],
  "timestamp": "2026-06-21T10:00:00"
}
```

---

## 🚀 Hướng dẫn cài đặt

### 1. Yêu cầu môi trường

Cần cài đặt các công cụ sau:

* Node.js 20 trở lên.
* npm 10 trở lên.
* Java Development Kit 21.
* Maven 3.9 trở lên.
* MySQL 8.
* Git.
* IntelliJ IDEA hoặc IDE hỗ trợ Spring Boot.
* Visual Studio Code hoặc IDE hỗ trợ ReactJS.

Kiểm tra phiên bản:

```bash
node --version
npm --version
java --version
mvn --version
mysql --version
git --version
```

---

### 2. Clone Repository

```bash
git clone https://github.com/your-username/student-social-network.git
cd student-social-network
```

Thay `your-username` bằng username GitHub của nhóm.

---

### 3. Khởi tạo cơ sở dữ liệu

Đăng nhập vào MySQL:

```bash
mysql -u root -p
```

Tạo database:

```sql
CREATE DATABASE student_social_network
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Import file SQL:

```bash
mysql -u root -p student_social_network < database/student_social_network_db.sql
```

---

### 4. Cấu hình Backend

Di chuyển vào thư mục Backend:

```bash
cd backend
```

Tạo file cấu hình môi trường dựa trên `.env.example` hoặc chỉnh sửa `application-dev.yml`.

Ví dụ:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/student_social_network?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
    username: root
    password: your_mysql_password

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  access-token-secret: your_access_token_secret
  refresh-token-secret: your_refresh_token_secret
  access-token-expiration: 900000
  refresh-token-expiration: 2592000000

server:
  port: 8080
```

Không đưa mật khẩu database, JWT Secret hoặc thông tin nhạy cảm lên GitHub.

Chạy Backend:

```bash
mvn clean install
mvn spring-boot:run
```

Backend mặc định chạy tại:

```text
http://localhost:8080
```

API Base URL:

```text
http://localhost:8080/api/v1
```

---

### 5. Cấu hình Frontend

Mở terminal mới và di chuyển vào thư mục Frontend:

```bash
cd frontend
```

Cài đặt thư viện:

```bash
npm install
```

Tạo file `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Chạy Frontend:

```bash
npm run dev
```

Frontend mặc định chạy tại:

```text
http://localhost:5173
```

---

## 🧪 Kiểm thử

### Backend

Chạy Unit Test và Integration Test:

```bash
cd backend
mvn test
```

Build toàn bộ Backend:

```bash
mvn clean package
```

### Frontend

Kiểm tra lỗi ESLint:

```bash
cd frontend
npm run lint
```

Build Frontend:

```bash
npm run build
```

Chạy bản build để kiểm tra:

```bash
npm run preview
```

### Kiểm thử API

Có thể sử dụng Postman để kiểm thử:

* Đăng ký.
* Hoàn tất hồ sơ ban đầu.
* Đăng nhập.
* Refresh Token.
* Tạo bài viết.
* Follow người dùng.
* Like bài viết.
* Bình luận.
* Xem Feed.
* Báo cáo bài viết.
* API quản trị.

---

## ✅ Tiêu chí nghiệm thu MVP

Phiên bản MVP được xem là hoàn thành khi đáp ứng các tiêu chí sau:

1. Người dùng đăng ký thành công bằng email hoặc số điện thoại.
2. Hệ thống từ chối khi thiếu cả hai phương thức, gửi đồng thời cả hai hoặc phương thức đã tồn tại.
3. Hệ thống tạo `users` và `user_profiles` trong cùng một transaction.
4. Nếu tạo hồ sơ thất bại, tài khoản vừa tạo phải được rollback.
5. Sau đăng ký, `display_name` và `profile_completed_at` của hồ sơ bằng `NULL`.
6. Hệ thống điều hướng người dùng đến quy trình hoàn tất hồ sơ.
7. Người dùng bắt buộc nhập tên hiển thị hợp lệ trước khi hoàn tất hồ sơ.
8. Người dùng có thể bỏ qua ảnh đại diện, ngày sinh và giới thiệu cá nhân.
9. Khi hoàn tất hồ sơ, `profile_completed_at` được cập nhật.
10. Tài khoản chưa hoàn tất hồ sơ không thể truy cập Feed hoặc các API mạng xã hội chính.
11. Người dùng đăng nhập thành công bằng email hoặc số điện thoại.
12. Access Token và Refresh Token hoạt động đúng.
13. Refresh Token bị thu hồi không thể cấp Access Token mới.
14. Tài khoản bị khóa không thể đăng nhập.
15. Người dùng cập nhật và xem hồ sơ thành công.
16. Người dùng Follow và Unfollow thành công.
17. Không tồn tại hai quan hệ Follow trùng nhau.
18. Người dùng tạo được bài viết có văn bản hoặc hình ảnh.
19. Chỉ tác giả được chỉnh sửa hoặc xóa bài viết.
20. Bài viết bị ẩn hoặc xóa không xuất hiện trên Feed.
21. Người dùng Like và Unlike bài viết.
22. Một người không thể Like một bài hai lần.
23. Người dùng bình luận và xóa bình luận của mình.
24. Người dùng lưu và bỏ lưu bài viết.
25. Feed Following hiển thị đúng bài của người đang theo dõi.
26. Feed For You hiển thị các bài viết hợp lệ.
27. Người dùng tìm kiếm được tài khoản và bài viết.
28. Người dùng gửi được báo cáo bài viết.
29. Admin quản lý được người dùng, bài viết và báo cáo.
30. Các API danh sách đều hỗ trợ phân trang.
31. Backend từ chối thao tác khi người dùng không có quyền.
32. Mật khẩu không được lưu dưới dạng văn bản thuần.
33. Hình ảnh không hợp lệ bị Backend từ chối.
34. Backend không trả stack trace hoặc thông tin nhạy cảm cho Client.

---


## 🔒 Yêu cầu bảo mật

* Mật khẩu phải được băm bằng thuật toán an toàn như BCrypt.
* Access Token có thời hạn sử dụng ngắn.
* Refresh Token phải có khả năng thu hồi.
* API quản trị chỉ cho phép tài khoản có vai trò `ADMIN`.
* Backend phải kiểm tra quyền truy cập cho mọi thao tác.
* Không tin tưởng dữ liệu hoặc quyền được gửi từ Frontend.
* Kiểm tra MIME type, phần mở rộng và dung lượng ảnh.
* Kiểm tra dữ liệu đầu vào bằng Bean Validation.
* Sử dụng JPA hoặc Prepared Statement để hạn chế SQL Injection.
* Làm sạch nội dung đầu vào để hạn chế XSS.
* Không ghi mật khẩu, Token hoặc dữ liệu nhạy cảm vào log.
* Không trả stack trace cho người dùng.
* Cấu hình CORS chỉ cho phép các nguồn cần thiết.
* Thông tin bí mật phải được lưu trong biến môi trường.

---

## ⚡ Yêu cầu hiệu năng

* API thông thường có thời gian phản hồi trung bình không quá 2 giây trong môi trường kiểm thử.
* Mỗi lần tải Feed trả tối đa 20 bài viết.
* Tất cả API danh sách phải hỗ trợ phân trang.
* Kích thước trang mặc định là 20 bản ghi.
* Không trả toàn bộ dữ liệu không giới hạn.
* Hệ thống hướng đến đáp ứng từ 30 đến 50 người dùng đồng thời trong môi trường kiểm thử luận văn.
* Các trường thường xuyên tìm kiếm phải được đánh index phù hợp.
* Tránh lỗi truy vấn N+1 trong Hibernate.
* Không tải toàn bộ Entity Relationship không cần thiết.

---

## 🌿 Quy ước Git

### Nhánh chính

* `main`: Phiên bản ổn định.
* `develop`: Nhánh tích hợp phát triển.
* `feature/*`: Phát triển chức năng mới.
* `fix/*`: Sửa lỗi.
* `refactor/*`: Tái cấu trúc mã nguồn.
* `docs/*`: Cập nhật tài liệu.

Ví dụ:

```text
feature/auth-login
feature/create-post
feature/follow-user
fix/refresh-token
docs/update-readme
```

### Quy ước Commit

```text
feat: thêm chức năng đăng nhập
fix: sửa lỗi làm mới access token
refactor: tách service xử lý bài viết
docs: cập nhật tài liệu README
test: thêm kiểm thử cho chức năng follow
chore: cập nhật dependency
```

Ví dụ:

```bash
git commit -m "feat: thêm API tạo bài viết"
git commit -m "fix: ngăn người dùng follow chính mình"
git commit -m "docs: cập nhật hướng dẫn cài đặt dự án"
```

---

## 📝 Quy ước mã nguồn

### Frontend

* Component sử dụng PascalCase.
* Hàm và biến sử dụng camelCase.
* Custom Hook bắt đầu bằng `use`.
* Tên file API kết thúc bằng `Api.js`.
* Không gọi API trực tiếp trong JSX.
* Không lặp lại URL API trong nhiều file.
* Xử lý đầy đủ trạng thái loading, empty và error.
* Tách component khi một component có quá nhiều trách nhiệm.

### Backend

* Class sử dụng PascalCase.
* Biến và phương thức sử dụng camelCase.
* Hằng số sử dụng UPPER_SNAKE_CASE.
* DTO Request và DTO Response phải tách biệt.
* Không trả trực tiếp Entity qua Controller.
* Service chịu trách nhiệm xử lý nghiệp vụ.
* Repository chỉ chịu trách nhiệm truy cập dữ liệu.
* Exception được xử lý tập trung.
* Các thao tác quan trọng sử dụng `@Transactional`.
* Không đưa logic nghiệp vụ phức tạp vào Controller.

---

## 🚧 Chức năng ngoài phạm vi MVP

Các chức năng sau chưa triển khai trong phiên bản MVP:

* Xác thực email.
* Đăng nhập bằng Google và Facebook.
* Hồ sơ riêng tư.
* Follow Request.
* Block và Restrict.
* Video và tài liệu trong bài viết.
* Bài viết nháp.
* Mention.
* Repost.
* Trích dẫn bài viết.
* Chủ đề nội dung do Admin quản lý.
* Địa điểm và Discovery Map.
* Feed cá nhân hóa do người dùng tạo.
* Feed công khai và lưu Feed.
* Elasticsearch.
* Nhắn tin trực tiếp.
* Message Request và Hidden Message Request.
* Thông báo thời gian thực.
* Quản lý trường, khoa và ngành.
* Dashboard thống kê nâng cao.
* Moderation Case.
* Audit Log chi tiết.
* Machine Learning cho hệ thống gợi ý.
* Ứng dụng mobile native.
* Group Chat.
* Gọi thoại.
* Gọi video.
* Livestream.

---

## 🔮 Định hướng phát triển

Sau khi phiên bản MVP hoàn thành và ổn định, hệ thống có thể mở rộng:

* Xác thực email.
* Đăng nhập Google và Facebook.
* Hồ sơ riêng tư và Follow Request.
* Block và Restrict.
* Repost và trích dẫn bài viết.
* Bài viết nháp.
* Video và tài liệu học tập.
* Mention người dùng.
* Feed cá nhân hóa.
* Tìm kiếm bằng Elasticsearch.
* Khám phá nội dung theo địa điểm.
* Discovery Map.
* Nhắn tin trực tiếp bằng WebSocket.
* Message Request.
* Thông báo thời gian thực.
* Quản lý trường, khoa và ngành.
* Dashboard thống kê.
* Audit Log.
* Moderation Case.
* Thuật toán gợi ý nội dung nâng cao.
* Ứng dụng trên thiết bị di động.

---

## 📚 Tài liệu dự án

Các tài liệu phân tích và thiết kế được lưu trong thư mục `docs/`, bao gồm:

* Đặc tả nghiệp vụ.
* Sơ đồ phân rã chức năng.
* Use Case tổng quát.
* Use Case chi tiết.
* Activity Diagram.
* Sequence Diagram.
* Entity Relationship Diagram.
* Tài liệu thiết kế database.
* Tài liệu API.
* Thiết kế giao diện.
* Kịch bản kiểm thử.
* Hướng dẫn sử dụng hệ thống.

---

## 📄 Giấy phép

Dự án được phát triển nhằm phục vụ mục đích học tập, nghiên cứu và thực hiện luận văn tốt nghiệp.

Không sử dụng dự án cho mục đích thương mại khi chưa có sự đồng ý của nhóm phát triển.

---

## © Bản quyền

**© 2026 – Trường Đại học Công nghệ Sài Gòn**

**Khoa Công nghệ Thông tin**

**Đề tài: Xây dựng hệ thống mạng xã hội tinh gọn hướng đến sinh viên**
