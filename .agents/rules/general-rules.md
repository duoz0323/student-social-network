# Quy tắc chung của dự án

## 1. Tổng quan

Dự án xây dựng một website mạng xã hội tinh gọn hướng đến sinh viên.

Mục tiêu của MVP là hoàn thiện luồng mạng xã hội cốt lõi, ổn định, có thể kiểm thử và trình bày trong luận văn.

## 2. Luồng nghiệp vụ chính

Đăng ký
→ Đăng nhập
→ Quản lý hồ sơ
→ Follow
→ Đăng bài
→ Xem Feed
→ Like
→ Bình luận
→ Lưu bài
→ Tìm kiếm
→ Báo cáo
→ Quản trị.

## 3. Quy tắc tài khoản và đăng ký

- Đăng ký MVP dùng `identifier`, `password` và `confirmPassword`.
- Không dùng `username` hoặc tên định danh công khai riêng trong phạm vi MVP.
- `identifier` là email hoặc số điện thoại; tại thời điểm đăng ký chỉ được cung cấp đúng một phương thức định danh.
- Không bắt buộc người dùng nhập đồng thời email và số điện thoại.
- Email phải đúng định dạng, được trim, chuẩn hóa chữ thường và không trùng nếu được cung cấp.
- Số điện thoại phải đúng định dạng, được chuẩn hóa thống nhất và không trùng nếu được cung cấp.
- Nếu đăng ký bằng email thì `phone_number` lưu `NULL`; nếu đăng ký bằng số điện thoại thì `email` lưu `NULL`.
- Database cho phép bổ sung phương thức còn thiếu trong tương lai, nhưng tài khoản luôn phải có ít nhất email hoặc số điện thoại.
- Backend chuẩn hóa email về chữ thường trước khi kiểm tra và lưu.
- Backend chuẩn hóa số điện thoại về một định dạng thống nhất trước khi kiểm tra và lưu.
- Mật khẩu tối thiểu 8 ký tự, bao gồm chữ, số và ký tự đặc biệt.
- Tài khoản mới có trạng thái `ACTIVE`.
- Đăng nhập bằng email hoặc số điện thoại kèm mật khẩu.
- Email, số điện thoại và dữ liệu xác thực không hiển thị công khai trên hồ sơ.
- Tên hiển thị là dữ liệu hồ sơ, không nhập trong form đăng ký và được lưu trong `user_profiles` ở bước onboarding.
- Sau đăng ký, Backend tạo `users` và `user_profiles` rỗng trong cùng transaction.
- Nếu tạo `user_profiles` thất bại thì phải rollback `users`.
- `user_profiles.display_name` và `user_profiles.profile_completed_at` ban đầu là `NULL`.
- Sau đăng ký, Frontend điều hướng người dùng đến onboarding hồ sơ.
- Hồ sơ chỉ hoàn tất khi tên hiển thị hợp lệ đã được lưu và người dùng xác nhận hoàn tất.
- Avatar, ngày sinh và bio là tùy chọn trong onboarding.
- `users.status = ACTIVE` không đồng nghĩa hồ sơ đã hoàn tất.
- Khi `profile_completed_at` còn `NULL`, người dùng chỉ được dùng API xác thực cần thiết, Refresh Token, đăng xuất và onboarding.
- API mạng xã hội chính phải trả lỗi `PROFILE_NOT_COMPLETED` nếu hồ sơ chưa hoàn tất.
- MVP chưa triển khai xác minh email hoặc SMS OTP; `email_verified_at` và `phone_verified_at` để `NULL` nếu chưa xác minh.
- Không tạo trường hoặc thuật ngữ `gmail`; Gmail chỉ là một nhà cung cấp email.
- Ngày sinh thuộc hồ sơ người dùng, là thông tin tùy chọn khi cập nhật hồ sơ và không bắt buộc trong form đăng ký.

## 4. Nguyên tắc làm việc

Agent phải:

- Đọc tài liệu trước khi code.
- Không suy đoán nghiệp vụ khi tài liệu đã quy định.
- Không bổ sung chức năng ngoài MVP.
- Không thay đổi kiến trúc nếu chưa có lý do rõ ràng.
- Không sửa nhiều module không liên quan trong cùng một nhiệm vụ.
- Luôn ưu tiên thay đổi nhỏ, an toàn và có thể kiểm thử.
- Giữ code và tài liệu đồng nhất.

## 5. Nguyên tắc đặt tên

- Tên file và thư mục: tiếng Anh, dạng kebab-case hoặc theo convention framework.
- Java class: PascalCase.
- Java method và field: camelCase.
- React component: PascalCase.
- Hook React: bắt đầu bằng `use`.
- Constant: UPPER_SNAKE_CASE.
- Endpoint REST: danh từ số nhiều, chữ thường, dùng dấu gạch ngang khi cần.

## 6. Quy tắc comment

- Comment bằng tiếng Việt.
- Giải thích mục đích, quy tắc nghiệp vụ hoặc lý do kỹ thuật.
- Không comment lại chính xác điều câu lệnh đã thể hiện.
- Không để comment sai lệch với code.

## 7. Quy tắc Git

- Không commit secret.
- Không commit file build.
- Không commit `node_modules`.
- Không commit cấu hình cá nhân IDE.
- Mỗi commit chỉ nên phục vụ một mục tiêu rõ ràng.
- Không đổi tên hoặc xóa hàng loạt file nếu không thật sự cần thiết.

## 8. Quy tắc tài liệu

Khi thay đổi nghiệp vụ hoặc API:

1. Cập nhật tài liệu liên quan.
2. Cập nhật mock data nếu cần.
3. Cập nhật Frontend.
4. Cập nhật Backend.
5. Cập nhật test.
