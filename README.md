# student-social-network
# 🎓 Student Social Network

> Nền tảng mạng xã hội dành cho sinh viên được phát triển trong khuôn khổ luận văn tốt nghiệp.

## 📖 Giới thiệu

Student Social Network là một nền tảng mạng xã hội được xây dựng nhằm tạo môi trường kết nối, chia sẻ thông tin và tương tác giữa sinh viên trong trường đại học.

Hệ thống cho phép người dùng đăng tải bài viết, tương tác với cộng đồng thông qua bình luận, lượt thích, theo dõi người dùng khác, tham gia khảo sát và chia sẻ các hoạt động học tập cũng như đời sống sinh viên.

Dự án được phát triển theo mô hình Client-Server với Frontend và Backend tách biệt nhằm đảm bảo khả năng mở rộng, bảo trì và phát triển lâu dài.

---

# 👨‍🎓 Nhóm thực hiện

## Thành viên 1

* Họ và tên: Nguyễn Hoàng Dương
* MSSV: DH52200548
* Vai trò:

  * Phân tích yêu cầu
  * Thiết kế cơ sở dữ liệu
  * Phát triển Backend
  * Xây dựng API

## Thành viên 2

* Họ và tên: Đậu Quốc Khánh
* MSSV: DH52200867
* Vai trò:

  * Thiết kế giao diện người dùng
  * Phát triển Frontend
  * Tích hợp API
  * Kiểm thử hệ thống

## Giảng viên hướng dẫn

* Hoàng Công Quang Huy
---

# 🎯 Mục tiêu dự án

* Xây dựng nền tảng mạng xã hội dành cho sinh viên.
* Tạo môi trường kết nối và chia sẻ thông tin.
* Hỗ trợ tương tác giữa người dùng thông qua bài viết và bình luận.
* Áp dụng các công nghệ phát triển web hiện đại.
* Nâng cao kỹ năng thiết kế và phát triển phần mềm thực tế.

---

# ✨ Chức năng chính

## Quản lý tài khoản

* Đăng ký
* Đăng nhập
* Đăng xuất
* Cập nhật hồ sơ cá nhân
* Đổi mật khẩu
* Cập nhật ảnh đại diện và ảnh bìa

## Quản lý bài viết

* Tạo bài viết
* Chỉnh sửa bài viết
* Xóa bài viết
* Đính kèm hình ảnh
* Đính kèm tệp tin
* Đính kèm địa điểm
* Đính kèm khảo sát

## Tương tác

* Thích bài viết
* Bình luận
* Trả lời bình luận
* Chia sẻ bài viết
* Báo cáo nội dung

## Kết nối người dùng

* Theo dõi người dùng
* Hủy theo dõi
* Danh sách người theo dõi
* Danh sách đang theo dõi

## Tìm kiếm

* Tìm kiếm người dùng
* Tìm kiếm bài viết
* Tìm kiếm hashtag

## Thông báo

* Thông báo tương tác
* Thông báo theo dõi
* Thông báo hệ thống

---

# 🛠️ Công nghệ sử dụng

## Frontend

* ReactJS
* JavaScript (ES6+)
* React Router
* Axios
* Tailwind CSS

## Backend

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA

## Database

* SQL Server

## Công cụ phát triển

* Git
* GitHub
* Postman
* Docker

---

# 🏗️ Kiến trúc hệ thống

```text
Client Browser
      │
      ▼
 ReactJS Frontend
      │
 RESTful API
      │
Spring Boot Backend
      │
 Spring Data JPA
      │
 SQL Server
```

---

# 📂 Cấu trúc dự án

```text
student-social-network
│
├── frontend/
│   ├── src/
│   ├── public/
│   └── package.json
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── application.yml
│
├── database/
│
├── docs/
│
└── README.md
```

---

# 🚀 Hướng dẫn cài đặt

## Clone Repository

```bash
git clone https://github.com/your-username/student-social-network.git
```

## Frontend

```bash
cd frontend
npm install
npm start
```

Ứng dụng sẽ chạy tại:

```text
http://localhost:3000
```

## Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

API sẽ chạy tại:

```text
http://localhost:8080
```

---

# 📊 Cơ sở dữ liệu

Hệ thống được xây dựng trên SQL Server với các nhóm bảng chính:

* Users
* Posts
* Comments
* Reactions
* Follows
* Notifications
* Polls
* Attachments
* Locations
* Hashtags

---

# 📌 Định hướng phát triển

* Chat thời gian thực
* Gợi ý bạn bè
* Gợi ý nội dung bằng AI
* Tích hợp hệ thống sự kiện sinh viên
* Mobile Application

---

# 📄 Giấy phép

Dự án được phát triển phục vụ mục đích nghiên cứu và thực hiện luận văn tốt nghiệp.

© 2026 - Đại học Công nghệ Sài Gòn
