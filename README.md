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

### 🔐 1. Xác thực và tài khoản

* Đăng ký tài khoản bằng email, username và mật khẩu.
* Đăng nhập bằng email hoặc username.
* Đăng xuất khỏi phiên hiện tại.
* Xác thực bằng JWT Access Token.
* Làm mới Access Token bằng Refresh Token.
* Thu hồi Refresh Token khi đăng xuất.
* Phân quyền người dùng và quản trị viên.
* Khôi phục mật khẩu nếu đảm bảo tiến độ.

### 👤 2. Hồ sơ người dùng

* Xem hồ sơ cá nhân.
* Xem hồ sơ người dùng khác.
* Cập nhật tên hiển thị.
* Cập nhật ảnh đại diện.
* Cập nhật phần giới thiệu cá nhân.
* Xem số lượng người theo dõi.
* Xem số lượng người đang theo dõi.
* Xem danh sách bài viết đã đăng.

Trong phiên bản MVP, tất cả hồ sơ người dùng đều được đặt ở chế độ công khai.

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

* Tìm kiếm người dùng theo username.
* Tìm kiếm người dùng theo tên hiển thị.
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

Cấu trúc tổng thể có thể thay đổi theo tiến độ triển khai. Riêng Frontend hiện tại sử dụng đúng cấu trúc được mô tả ở phần dưới.


## 🧩 Cấu trúc Frontend thực tế

Frontend hiện tại được tổ chức theo cấu trúc phẳng, gọn và phù hợp với tiến độ MVP:

```text
FrontEnd/
├── node_modules/
├── public/
├── src/
│   ├── assets/
│   ├── components/
│   ├── config/
│   ├── contexts/
│   ├── features/
│   ├── hooks/
│   ├── router/
│   ├── utils/
│   ├── App.css
│   ├── App.jsx
│   ├── index.css
│   └── main.jsx
├── .env
├── .gitignore
├── eslint.config.js
├── index.html
├── package-lock.json
├── package.json
├── README.md
└── vite.config.js
```

### Vai trò từng thư mục

#### `src/assets/`

Chứa tài nguyên tĩnh được import vào mã nguồn:

* Hình ảnh.
* Icon.
* Logo.
* Font hoặc file media nội bộ nếu có.

#### `src/components/`

Chứa component dùng chung cho nhiều tính năng:

* Button.
* Input.
* Modal.
* Loading.
* Empty State.
* Error State.
* Layout dùng chung.
* Header.
* Sidebar.

Không đặt component chỉ dùng riêng cho một nghiệp vụ vào đây nếu component đó phù hợp hơn với `features/`.

#### `src/config/`

Chứa cấu hình dùng chung:

* Axios instance.
* Axios Interceptor.
* Biến môi trường.
* Hằng số API.
* Cấu hình ứng dụng.

Ví dụ dự kiến:

```text
src/config/
├── axios.js
├── env.js
└── constants.js
```

#### `src/contexts/`

Chứa React Context dùng toàn ứng dụng:

* Auth Context.
* Theme Context nếu có.
* Toast/Notification Context nếu cần.

Chỉ tạo Context khi dữ liệu thực sự cần dùng ở nhiều khu vực. Không đưa mọi state vào Context.

#### `src/features/`

Chứa các module nghiệp vụ chính của hệ thống:

```text
src/features/
├── auth/
├── profile/
├── follow/
├── post/
├── interaction/
├── feed/
├── search/
├── report/
└── admin/
```

Mỗi feature có thể tổ chức linh hoạt:

```text
src/features/post/
├── components/
├── pages/
├── services/
├── hooks/
├── utils/
└── validation/
```

Không bắt buộc feature nào cũng phải có đầy đủ mọi thư mục con. Chỉ tạo thư mục khi thực sự cần.

#### `src/hooks/`

Chứa custom hook dùng chung cho nhiều feature:

* `useAuth`.
* `useDebounce`.
* `usePagination`.
* `useOutsideClick`.

Hook chỉ dùng riêng một feature nên đặt trong chính feature đó.

#### `src/router/`

Chứa cấu hình điều hướng:

* Router chính.
* Public Route.
* Protected Route.
* Admin Route.
* Danh sách route.

Tên thư mục chính thức của dự án là `router/`, không dùng `routes/`.

#### `src/utils/`

Chứa hàm tiện ích dùng chung:

* Format thời gian.
* Xử lý chuỗi.
* Chuẩn hóa hashtag.
* Kiểm tra file.
* Xử lý lỗi API.

Không đặt logic nghiệp vụ phức tạp trong `utils/`.

#### `App.jsx`

Component gốc của ứng dụng.

Nên chịu trách nhiệm:

* Gắn Router.
* Gắn Provider.
* Khởi tạo layout cấp cao.

Không đặt toàn bộ giao diện và nghiệp vụ trong `App.jsx`.

#### `main.jsx`

Điểm khởi tạo React:

* Render ứng dụng.
* Gắn StrictMode.
* Import CSS toàn cục.
* Gắn Provider cấp cao nếu cần.

#### `App.css` và `index.css`

* `index.css`: style toàn cục, Tailwind directives và reset CSS.
* `App.css`: chỉ giữ style cấp ứng dụng nếu thật sự cần.

Không để toàn bộ style của mọi component trong hai file này.

### Nguyên tắc tổ chức Frontend

* Dự án sử dụng cấu trúc `features/` theo nghiệp vụ.
* `components/` chỉ chứa component dùng chung.
* `contexts/` chỉ chứa state toàn cục thực sự cần thiết.
* `hooks/` chỉ chứa hook dùng chung.
* `router/` là nơi duy nhất quản lý route.
* `config/` quản lý Axios và cấu hình môi trường.
* `utils/` không chứa component hoặc logic gọi API.
* Không gọi Axios trực tiếp trong JSX.
* API của từng feature đặt trong `features/<feature>/services/`.
* Component riêng của feature đặt trong `features/<feature>/components/`.
* Page riêng của feature đặt trong `features/<feature>/pages/`.
* Mọi màn hình gọi API phải có trạng thái loading, empty và error.

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

* `users`
* `user_profiles`
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

GET    /api/v1/users/{username}
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
      "field": "username",
      "message": "Username đã tồn tại"
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

1. Người dùng đăng ký và đăng nhập thành công.
2. Access Token và Refresh Token hoạt động đúng.
3. Refresh Token bị thu hồi không thể cấp Access Token mới.
4. Tài khoản bị khóa không thể đăng nhập.
5. Người dùng cập nhật và xem hồ sơ thành công.
6. Người dùng Follow và Unfollow thành công.
7. Không tồn tại hai quan hệ Follow trùng nhau.
8. Người dùng tạo được bài viết có văn bản hoặc hình ảnh.
9. Chỉ tác giả được chỉnh sửa hoặc xóa bài viết.
10. Bài viết bị ẩn hoặc xóa không xuất hiện trên Feed.
11. Người dùng Like và Unlike bài viết.
12. Một người không thể Like một bài hai lần.
13. Người dùng bình luận và xóa bình luận của mình.
14. Người dùng lưu và bỏ lưu bài viết.
15. Feed Following hiển thị đúng bài của người đang theo dõi.
16. Feed For You hiển thị các bài viết hợp lệ.
17. Người dùng tìm kiếm được tài khoản và bài viết.
18. Người dùng gửi được báo cáo bài viết.
19. Admin quản lý được người dùng, bài viết và báo cáo.
20. Các API danh sách đều hỗ trợ phân trang.
21. Backend từ chối thao tác khi người dùng không có quyền.
22. Mật khẩu không được lưu dưới dạng văn bản thuần.
23. Hình ảnh không hợp lệ bị Backend từ chối.
24. Backend không trả stack trace hoặc thông tin nhạy cảm cho Client.

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


## 🤖 Tài liệu dành cho AI Coding Agent

Dự án sử dụng bộ tài liệu hỗ trợ Codex, Antigravity và các AI Coding Agent khác.

### Cấu trúc tài liệu AI

```text
.agents/
├── rules/
│   ├── general-rules.md
│   ├── frontend-rules.md
│   ├── backend-rules.md
│   ├── database-rules.md
│   └── security-rules.md
│
├── skills/
│   ├── frontend-development.md
│   ├── backend-development.md
│   ├── database-design.md
│   ├── api-design.md
│   └── bug-fixing.md
│
└── workflows/
    ├── implement-feature.md
    ├── create-ui.md
    ├── create-api.md
    ├── fix-bug.md
    └── review-code.md

docs/
├── data/
│   ├── DATABASE.md
│   ├── DATA-FLOW.md
│   ├── API-CONTRACT.md
│   ├── DEMO-DATA.md
│   ├── demo-data.json
│   └── demo-credentials.md
│
├── ui/
│   ├── UI-DEMO.html
│   ├── UI-FLOW.md
│   ├── SCREEN-LIST.md
│   ├── COMPONENTS.md
│   └── DESIGN-SYSTEM.md
│
├── ARCHITECTURE.md
├── PRD.md
└── PROJECT-RULES.md

AGENTS.md
```

### Vai trò của `AGENTS.md`

`AGENTS.md` là file điều hướng chính mà AI Agent phải đọc trước khi code.

File này quy định:

* Thứ tự đọc tài liệu.
* Phạm vi MVP.
* Quy tắc bắt buộc.
* Cách lập kế hoạch.
* Cách báo cáo kết quả.
* Các hành động Agent không được tự ý thực hiện.

### Vai trò của `.agents/rules/`

Chứa các quy tắc bắt buộc theo phạm vi:

* Quy tắc chung.
* Quy tắc Frontend.
* Quy tắc Backend.
* Quy tắc Database.
* Quy tắc bảo mật.

### Vai trò của `.agents/skills/`

Chứa hướng dẫn chuyên môn để Agent triển khai đúng cách:

* Phát triển Frontend.
* Phát triển Backend.
* Thiết kế Database.
* Thiết kế API.
* Phân tích và sửa lỗi.

### Vai trò của `.agents/workflows/`

Chứa trình tự làm việc bắt buộc:

* Triển khai chức năng.
* Tạo giao diện.
* Tạo API.
* Sửa lỗi.
* Review code.

### Vai trò của `docs/ui/`

Chứa tài liệu giúp AI hiểu giao diện và luồng sử dụng:

* Danh sách màn hình.
* Luồng giao diện.
* Danh sách component.
* Design System.
* File HTML demo.

### Vai trò của `docs/data/`

Chứa tài liệu giúp AI hiểu dữ liệu và API:

* Mô tả database.
* Luồng dữ liệu.
* API Contract.
* Mock data.
* Tài khoản demo.

### Thứ tự AI Agent phải đọc

1. `AGENTS.md`
2. `docs/PRD.md`
3. `docs/ARCHITECTURE.md`
4. `docs/PROJECT-RULES.md`
5. Rule phù hợp trong `.agents/rules/`
6. Skill phù hợp trong `.agents/skills/`
7. Workflow phù hợp trong `.agents/workflows/`
8. Tài liệu UI hoặc Data liên quan.

### Prompt khởi đầu khuyến nghị

```text
Hãy đọc AGENTS.md và toàn bộ tài liệu liên quan đến nhiệm vụ.
Chưa viết code.

Hãy:
1. Tóm tắt yêu cầu.
2. Xác định phạm vi MVP.
3. Liệt kê các file dự kiến tạo hoặc sửa.
4. Đề xuất kế hoạch triển khai.
5. Chỉ ra các rủi ro hoặc điểm chưa thống nhất.
```

### Nguyên tắc sử dụng AI Agent

* Chỉ giao một chức năng nhỏ trong mỗi nhiệm vụ.
* Yêu cầu Agent lập kế hoạch trước khi code.
* Không cho Agent tự ý mở rộng ngoài MVP.
* Không cho Agent tự ý đổi cấu trúc dự án.
* Không cho Agent tự ý cài thư viện.
* Không cho Agent tự ý thay đổi database.
* Yêu cầu Agent báo cáo file đã tạo và sửa.
* Luôn review code trước khi merge.


---

## 📄 Giấy phép

Dự án được phát triển nhằm phục vụ mục đích học tập, nghiên cứu và thực hiện luận văn tốt nghiệp.

Không sử dụng dự án cho mục đích thương mại khi chưa có sự đồng ý của nhóm phát triển.

---

## © Bản quyền

**© 2026 – Trường Đại học Công nghệ Sài Gòn**

**Khoa Công nghệ Thông tin**

**Đề tài: Xây dựng hệ thống mạng xã hội tinh gọn hướng đến sinh viên**
