# Workflow tạo API

## Bước 1: Phân tích contract

Xác định:

- Method.
- Endpoint.
- Actor.
- Request.
- Response.
- Validation.
- HTTP status.
- Quyền truy cập.
- Lỗi nghiệp vụ.

Với API Auth MVP:

- Register nhận `identifier`, `password` và `confirmPassword`.
- `identifier` là email hoặc số điện thoại; request chỉ nhận đúng một phương thức định danh tại thời điểm đăng ký.
- Login nhận `identifier` là email hoặc số điện thoại và `password`.
- Không thiết kế request/response phụ thuộc `username`.
- Không nhận `displayName` trong request đăng ký; tên hiển thị thuộc onboarding hồ sơ.
- Không tạo trường `gmail`; dùng `email`.
- Email hoặc số điện thoại phải được chuẩn hóa ở Backend trước khi kiểm tra trùng và lưu.
- Đăng ký phải tạo `users` và `user_profiles` trong cùng transaction.
- Onboarding phải có API đọc trạng thái, cập nhật hồ sơ và xác nhận hoàn tất.
- API mạng xã hội chính phải chặn người chưa hoàn tất hồ sơ bằng `PROFILE_NOT_COMPLETED`.
- MVP chưa triển khai xác minh email hoặc SMS OTP; tài khoản mới được tạo `ACTIVE`.

## Bước 2: Xác định tầng

- DTO.
- Controller.
- Service.
- Repository.
- Mapper.
- Exception.
- Entity hoặc migration nếu cần.

Với Auth/Profile, phải đối chiếu database trước khi thiết kế DTO để xác nhận `users.email`, `users.phone_number`, `user_profiles.display_name` và `user_profiles.profile_completed_at`.

## Bước 3: Triển khai

- Controller mỏng.
- Service xử lý nghiệp vụ.
- Repository chỉ truy cập dữ liệu.
- Dùng transaction khi cần.
- Không trả Entity.
- Không lộ dữ liệu nhạy cảm.

## Bước 4: Kiểm thử

- Thành công.
- Dữ liệu không hợp lệ.
- Không đăng nhập.
- Không có quyền.
- Không tìm thấy.
- Trùng dữ liệu.
- Phân trang.
