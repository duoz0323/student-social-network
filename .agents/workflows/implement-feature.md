# Workflow triển khai chức năng

## Bước 1: Đọc tài liệu

Đọc:

- `docs/PRD.md`
- `docs/ARCHITECTURE.md`
- `docs/PROJECT-RULES.md`
- Tài liệu UI liên quan.
- Tài liệu Data liên quan.
- Rule và skill phù hợp.

## Bước 2: Phân tích

Trình bày:

- Mục tiêu chức năng.
- Actor.
- Tiền điều kiện.
- Luồng chính.
- Luồng ngoại lệ.
- Bảng dữ liệu liên quan.
- API liên quan.
- Màn hình liên quan.

Nếu chức năng thuộc Auth hoặc hồ sơ, phải xác định rõ:

- Đăng ký dùng `identifier`, `password`, `confirmPassword`.
- `identifier` là email hoặc số điện thoại; tại thời điểm đăng ký chỉ nhận đúng một phương thức định danh.
- Đăng nhập dùng email hoặc số điện thoại.
- Không dùng `username` trong MVP.
- Không nhận tên hiển thị trong form/request đăng ký.
- Tạo `users` và `user_profiles` trong cùng transaction.
- Tên hiển thị bắt buộc ở onboarding; avatar, ngày sinh và bio tùy chọn.
- `profile_completed_at` là điều kiện để dùng chức năng mạng xã hội chính.
- Backend trả `PROFILE_NOT_COMPLETED` khi hồ sơ chưa hoàn tất.
- Email/số điện thoại không hiển thị công khai.
- Tài khoản mới được tạo `ACTIVE`; MVP chưa triển khai xác minh email hoặc SMS OTP.
- Ngày sinh chỉ thuộc cập nhật hồ sơ và là tùy chọn.

## Bước 3: Lập kế hoạch

Liệt kê:

- File tạo mới.
- File cần sửa.
- Enum của entity, nếu có, và vị trí trong `enums/` của module tương ứng.
- Migration nếu có.
- API request/response.
- Test cần viết.

Với Auth/Profile, kế hoạch phải đi theo thứ tự: đối chiếu database, thiết kế DTO, validation, transaction đăng ký, onboarding, security guard, rồi mới đến test và code.

## Bước 4: Triển khai

- Tuân thủ kiến trúc.
- Đặt status, role, type, reason hoặc enum nghiệp vụ của entity trong thư mục `enums/` của module đó.
- Không mở rộng ngoài MVP.
- Comment tiếng Việt.
- Không cài thư viện nếu chưa cần.
- Không sửa module không liên quan.

## Bước 5: Kiểm tra

- Build/compile.
- Test.
- Validation.
- Phân quyền.
- Loading/Empty/Error đối với Frontend.
- Không lộ dữ liệu nhạy cảm.

Test tối thiểu cho Auth/Profile MVP:

- Đăng ký email hợp lệ.
- Đăng ký số điện thoại hợp lệ.
- Thiếu cả email và số điện thoại.
- Request chứa đồng thời email và số điện thoại nếu DTO tách trường.
- Email sai định dạng.
- Số điện thoại sai định dạng.
- Email đã tồn tại.
- Số điện thoại đã tồn tại.
- Mật khẩu không đạt yêu cầu.
- Confirm password không khớp.
- Rollback khi tạo `user_profiles` thất bại.
- `display_name` ban đầu `NULL`.
- `profile_completed_at` ban đầu `NULL`.
- Không hoàn tất khi thiếu tên hiển thị.
- Cho phép bỏ qua avatar, ngày sinh và bio.
- Từ chối ngày sinh trong tương lai.
- Chặn Feed khi `profile_completed_at` là `NULL`.
- Trả `PROFILE_NOT_COMPLETED`.
- Cho phép API onboarding khi chưa hoàn tất hồ sơ.
- Cho phép đăng nhập bằng email hoặc số điện thoại.
- Từ chối tài khoản `BLOCKED`.
- Không lộ email hoặc số điện thoại trong API hồ sơ công khai.

## Bước 6: Báo cáo

- Đã làm gì.
- File thay đổi.
- Cách chạy.
- Cách kiểm thử.
- Phần chưa hoàn thành.
