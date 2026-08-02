# Kiến trúc hệ thống

## 1. Tổng quan

Hệ thống gồm:

- Frontend ReactJS.
- Backend Spring Boot.
- MySQL.
- Cloud Storage cho ảnh.
- JWT Access Token.
- Refresh Token.

## 2. Luồng tổng thể

```text
Browser
→ ReactJS
→ Axios
→ REST API
→ Spring Security
→ Controller
→ Service
→ Repository
→ MySQL
```

## 3. Frontend

Cấu trúc Frontend chính thức hiện tại:

```text
FrontEnd/
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

### Quy tắc tổ chức

- `components/`: component dùng chung.
- `config/`: Axios, interceptor và cấu hình.
- `contexts/`: Context toàn ứng dụng.
- `features/`: module nghiệp vụ.
- `hooks/`: hook dùng chung.
- `router/`: cấu hình route.
- `utils/`: hàm tiện ích.
- Không tự ý thêm `app/`, `shared/`, `layouts/`, `routes/` hoặc `store/`.

### Feature-Based Structure

```text
src/features/<feature>/
├── components/
├── pages/
├── services/
├── hooks/
├── utils/
└── validation/
```

Chỉ tạo thư mục con khi thực sự cần.

## 4. Backend

Cấu trúc package theo feature:

```text
backend/src/main/java/com/example/socialnetwork/
├── common/
├── config/
├── security/
├── auth/
├── user/
├── follow/
├── post/
├── interaction/
├── feed/
├── search/
├── report/
└── admin/
```

Trong mỗi feature:

```text
post/
├── controller/
├── service/
├── repository/
├── entity/
├── enums/
├── dto/
├── mapper/
└── exception/
```

Quy tắc enum trong Backend:

- Mỗi feature/module có thể tạo thư mục `enums/` khi entity của module có trạng thái, vai trò, loại, lý do hoặc nhóm giá trị cố định.
- Enum phải nằm trong module sở hữu nghiệp vụ, không đặt chung ở package toàn cục nếu chỉ phù hợp với một module.
- Ví dụ: `user/enums/UserStatus.java`, `user/enums/UserRole.java`, `post/enums/PostStatus.java`, `interaction/enums/CommentStatus.java`, `report/enums/ReportStatus.java`.
- Entity, DTO, mapper, service và repository phải import enum từ package `enums` của module tương ứng.

## 5. Tầng xử lý

### Controller

- Nhận request.
- Validation cơ bản.
- Gọi Service.
- Trả response.

### Service

- Nghiệp vụ.
- Phân quyền.
- Transaction.
- Điều phối dữ liệu.

### Repository

- Truy vấn dữ liệu.

### DTO

- Request/Response.

### Mapper

- Entity ↔ DTO.

### Security

- JWT filter.
- Authentication.
- Authorization.
- Password encoder.
- Refresh token.

## 6. Dữ liệu

MySQL là nguồn dữ liệu chuẩn.

Timestamp do MySQL/API quản lý được quy ước theo UTC. `Clock` của Backend phải dùng UTC để deadline nghiệp vụ, gồm giới hạn sửa Post 15 phút, không phụ thuộc múi giờ máy chạy; Frontend chuyển UTC sang múi giờ hiển thị của người dùng.

Media:

- Lưu trên Cloud Storage.
- Database lưu URL và metadata.
- Không lưu BLOB.

## 7. Feed

### Following

- Truy vấn bài của người đang Follow.
- Chỉ PUBLISHED.
- Sắp xếp ổn định `published_at DESC, id DESC`.
- Dùng Cursor Pagination và keyset query.

### For You

MVP dùng điểm cơ bản:

```text
score = freshnessScore
      + likeCount × likeWeight
      + commentCount × commentWeight
```

Không sử dụng Machine Learning.

### Cursor Pagination cho danh sách bài viết

- Feed For You, Feed Following, bài trên hồ sơ, tab Repost, bài đã lưu và bài đã thích dùng Cursor Pagination.
- Request đầu truyền `limit`; request sau truyền nguyên `nextCursor` opaque do Backend trả về.
- `limit` mặc định 10, tối đa 20; Backend lấy `limit + 1` để xác định `hasNext`.
- Feed For You dùng cursor gồm `score`, `publishedAt`, `postId`; danh sách theo thời gian dùng
  `createdAt`, `postId`.
- Following Feed hợp nhất `ORIGINAL` và `REPOST` bằng `UNION ALL`, dùng cursor
  `activityAt`, `itemRank`, `actorId`, `postId`; dữ liệu PostCard được batch-load để tránh N+1.
- Search, bình luận, Follow và Admin tiếp tục dùng `PageResponse`.

## 8. Bảo mật

- Mật khẩu băm.
- Access Token ngắn hạn.
- Refresh Token thu hồi được.
- API Admin yêu cầu ADMIN.
- Backend kiểm tra quyền.
- Không trả Entity trực tiếp.
- Không trả stack trace.

## 9. Phân trang

- Danh sách bài viết phục vụ Infinite Scroll dùng Cursor Pagination, mặc định 10 và tối đa 20.
- Các danh sách cần số trang/tổng số tiếp tục dùng `PageResponse` theo contract từng endpoint.
- Không dùng `page`, `offset` hoặc `COUNT(*)` cho các endpoint Cursor Pagination.

