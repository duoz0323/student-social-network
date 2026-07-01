# Kỹ năng phát triển Backend

## 1. Mục tiêu

Hướng dẫn Agent xây dựng API Spring Boot theo layered architecture và package theo feature.

## 2. Cấu trúc feature

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

Thư mục `enums/` chỉ tạo khi module có enum nghiệp vụ. Các trạng thái hoặc nhóm giá trị cố định của entity phải đặt tại đây, ví dụ `user/enums/UserStatus.java`, `user/enums/UserRole.java`, `post/enums/PostStatus.java`.

## 3. Quy trình triển khai

1. Đọc PRD và rule nghiệp vụ.
2. Xác định Entity liên quan.
3. Xác định enum của entity và đặt trong `enums/` của module.
4. Xác định request DTO.
5. Xác định response DTO.
6. Viết Repository.
7. Viết Service.
8. Viết Mapper.
9. Viết Controller.
10. Bổ sung validation.
11. Bổ sung phân quyền.
12. Viết test.

## 4. Nguyên tắc

- Không trả Entity.
- Không để Controller chứa logic.
- Không bỏ qua transaction khi có nhiều thao tác dữ liệu.
- Không bỏ qua kiểm tra quyền.
- Exception phải rõ nghĩa.
- Response không chứa dữ liệu nhạy cảm.
- Enum nghiệp vụ thuộc module nào thì đặt trong `enums/` của module đó; không đặt chung ngoài module khi không cần chia sẻ.
- Luồng đăng ký dùng `identifier`, `password`, `confirmPassword`; không dùng `username` hoặc `displayName`.
- `identifier` là email hoặc số điện thoại; request đăng ký chỉ nhận đúng một phương thức định danh tại thời điểm tạo tài khoản.
- Service Auth phải tự xác định `identifier`, chuẩn hóa email hoặc số điện thoại trước khi kiểm tra trùng và lưu.
- Nếu đăng ký bằng email thì `phone_number` lưu `NULL`; nếu đăng ký bằng số điện thoại thì `email` lưu `NULL`.
- Đăng ký tạo `users` và `user_profiles` rỗng trong cùng transaction.
- Nếu tạo `user_profiles` thất bại thì rollback `users`.
- Tên hiển thị được lưu trong `user_profiles` ở bước onboarding hoặc cập nhật hồ sơ.
- `profile_completed_at` là điều kiện để sử dụng chức năng mạng xã hội chính.
- Service phải trả `PROFILE_NOT_COMPLETED` khi người chưa hoàn tất hồ sơ gọi API Feed, đăng bài, Follow, Like, bình luận, lưu bài, tìm kiếm hoặc báo cáo.
- Đăng nhập dùng một định danh email hoặc số điện thoại kèm mật khẩu.
- MVP chưa triển khai xác minh email hoặc SMS OTP; tài khoản mới được tạo `ACTIVE`.
- Ngày sinh chỉ xử lý ở hồ sơ, không bắt buộc khi đăng ký.

## 5. Kiểm tra

- Compile thành công.
- Test liên quan chạy thành công.
- Validation hoạt động.
- Phân quyền hoạt động.
- HTTP status đúng.
- Không lộ stack trace.
