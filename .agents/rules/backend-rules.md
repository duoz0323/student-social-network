# Quy tắc Backend

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. File này chỉ quy định cách triển khai Backend; nghiệp vụ, trạng thái đích và API phải lấy từ README.

## 1. Công nghệ

- Java.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- MySQL.
- JWT Access Token.
- Refresh Token.

## 2. Kiến trúc xử lý

Luồng chuẩn:

Controller
→ Service
→ Repository
→ Database.

Trong mỗi module Backend, nếu entity có status, role, type, reason hoặc enum nghiệp vụ khác thì enum phải nằm trong thư mục `enums/` của module đó.

Ví dụ:

```text
user/
├── entity/
├── enums/
│   ├── UserRole.java
│   └── UserStatus.java
└── repository/
```

Không đặt enum của một module vào package chung. Chỉ dùng `common/enums` khi enum thật sự được chia sẻ bởi nhiều module và không thuộc sở hữu nghiệp vụ của module nào.

## 3. Quy tắc Auth và tài khoản

- Trước khi thiết kế DTO hoặc Service, đọc lại các mục Auth, API, Security, Database và tiêu chí nghiệm thu trong README.
- Lập bảng đối chiếu README với SQL/DBML, Entity, Repository, source và test hiện tại.
- Tách rõ thao tác gọi email, SMS hoặc provider bên ngoài khỏi transaction database.
- Xác định transaction boundary, concurrency control, idempotency, dữ liệu nhạy cảm và rollback cho từng luồng.
- Không dùng endpoint, DTO hoặc hành vi Auth cũ làm chuẩn khi khác README.
- Không sao chép nghiệp vụ Auth vào rule này; mọi validation và state transition phải truy ngược được tới README hoặc API contract đã đồng bộ.
- Với username onboarding, normalize/format/reserved/unique phải do Service kiểm tra lại khi submit; availability không thay thế validation cuối hoặc unique constraint database.
- Response onboarding/profile trả username không có `@`; không dùng username thay `users.id` để phân quyền, liên kết hoặc route nội bộ.

## 4. Controller

Controller chỉ:

- Nhận request.
- Gọi Service.
- Trả response.
- Áp dụng annotation validation hoặc phân quyền khi phù hợp.

Controller không:

- Chứa nghiệp vụ chính.
- Truy cập Repository trực tiếp.
- Tạo transaction nghiệp vụ phức tạp.
- Trả Entity trực tiếp.

## 5. Service

Service chịu trách nhiệm:

- Kiểm tra nghiệp vụ.
- Kiểm tra quyền.
- Điều phối Repository.
- Quản lý transaction.
- Chuyển đổi dữ liệu thông qua Mapper.
- Ném exception nghiệp vụ rõ nghĩa.

## 6. Repository

Repository chỉ chịu trách nhiệm truy cập dữ liệu.

- Tên method phải phản ánh điều kiện truy vấn.
- Tránh query N+1.
- Chỉ dùng native query khi thực sự cần.
- Truy vấn danh sách phải có phân trang.
- Dùng index phù hợp cho trường truy vấn thường xuyên.

## 7. DTO và Mapper

- Request và Response dùng DTO.
- Không dùng chung DTO cho mọi ngữ cảnh.
- Không trả password hash, refresh token hoặc dữ liệu nhạy cảm.
- Mapper chịu trách nhiệm chuyển Entity và DTO.
- Tránh mapping vòng lặp quan hệ hai chiều.

## 8. Exception

- Xử lý exception tập trung.
- Trả mã HTTP phù hợp.
- Không trả stack trace.
- Error response phải thống nhất.
- Message gửi Client phải dễ hiểu và không lộ cấu trúc nội bộ.

## 9. Transaction

Cân nhắc `@Transactional` cho:

- Hoàn tất đăng ký và tạo các bản ghi tài khoản liên quan theo transaction được README quy định.
- Tạo bài kèm media và hashtag.
- Follow/Unfollow.
- Like/Unlike.
- Save/Unsave.
- Tạo báo cáo.
- Admin xử lý báo cáo và ẩn bài.
- Thu hồi refresh token.

## 10. Phân quyền

- API người dùng yêu cầu tài khoản hợp lệ.
- API Admin yêu cầu role `ADMIN`.
- Chỉ tác giả được sửa hoặc xóa bài.
- Chỉ tác giả bình luận được xóa bình luận.
- Backend luôn kiểm tra quyền, không tin dữ liệu từ Frontend.

## 11. Hiệu năng

- API danh sách có phân trang.
- Kích thước mặc định 20.
- Không trả dữ liệu thừa.
- Tránh eager loading không cần thiết.
- Kiểm soát query khi lấy Feed.
