# Quy tắc dự án

## 1. Tài khoản

- Email duy nhất nếu có giá trị.
- Số điện thoại duy nhất nếu có giá trị.
- Không dùng username hoặc định danh công khai tương tự trong MVP.
- Đăng ký bằng đúng một phương thức định danh: email hoặc số điện thoại.
- Form đăng ký chỉ gồm phương thức định danh, mật khẩu và xác nhận mật khẩu.
- Request đăng ký chỉ nhận một giá trị định danh tại một thời điểm.
- Nếu đăng ký bằng email thì `phone_number` lưu `NULL`; nếu đăng ký bằng số điện thoại thì `email` lưu `NULL`.
- Database cho phép bổ sung phương thức còn thiếu trong tương lai, nhưng tài khoản luôn phải có ít nhất email hoặc số điện thoại.
- Đăng nhập bằng email hoặc số điện thoại.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Trạng thái MVP: ACTIVE, BLOCKED.
- User BLOCKED không đăng nhập được.
- Role: USER, ADMIN.
- MVP chưa triển khai xác minh email hoặc SMS OTP; dùng `email_verified_at` và `phone_verified_at` cho hướng phát triển.

## 2. Hồ sơ

- Hồ sơ công khai.
- Email và số điện thoại không hiển thị công khai.
- Chỉ chủ tài khoản cập nhật hồ sơ.
- Khi đăng ký hợp lệ, Backend phải tạo ngay `user_profiles` rỗng trong cùng transaction với `users`.
- `user_profiles.display_name` ban đầu được phép `NULL`.
- `user_profiles.profile_completed_at` ban đầu phải `NULL`.
- Tên hiển thị thuộc `user_profiles`, không thuộc `users`.
- Tên hiển thị bắt buộc để hoàn tất hồ sơ ban đầu.
- Avatar, ngày sinh và bio là tùy chọn và có thể bỏ qua.
- Hồ sơ chỉ hoàn tất khi tên hiển thị hợp lệ đã được lưu, người dùng xác nhận hoàn tất và Backend cập nhật `profile_completed_at`.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không đồng nghĩa hồ sơ đã hoàn tất.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép API xác thực cần thiết, Refresh Token, đăng xuất và onboarding.
- API mạng xã hội chính phải trả lỗi nghiệp vụ `PROFILE_NOT_COMPLETED` nếu hồ sơ chưa hoàn tất.

## 3. Follow

- Không Follow chính mình.
- Không Follow trùng.
- Follow có hiệu lực ngay.
- Không có Follow Request.

## 4. Bài viết

- Tối đa 500 ký tự.
- Tối đa 4 ảnh.
- Phải có nội dung hoặc ảnh.
- Ảnh: JPG, JPEG, PNG, WEBP.
- Chỉ tác giả sửa/xóa.
- Không sửa ảnh sau khi đăng.
- Trạng thái: PUBLISHED, HIDDEN, DELETED.

## 5. Like

- Một user Like một post một lần.
- Không Like bài HIDDEN/DELETED.

## 6. Comment

- Chỉ user đăng nhập được comment.
- Chỉ tác giả comment được xóa comment.
- Không comment bài HIDDEN/DELETED.

## 7. Save

- Một user Save một post một lần.
- Saved list chỉ chủ tài khoản xem.

## 8. Feed

- Following: bài của người đang Follow, mới nhất trước.
- For You: bài hợp lệ, điểm cơ bản.
- Không hiển thị HIDDEN/DELETED.
- Có phân trang.

## 9. Search

- MySQL.
- Tìm user theo tên hiển thị.
- Không tìm user theo username.
- Có phân trang.
- Không hiển thị user BLOCKED.
- Không hiển thị post HIDDEN/DELETED.

## 10. Report

- Chỉ report post.
- Trạng thái PENDING, RESOLVED, REJECTED.
- Không có nhiều report PENDING cùng user và post.
- Report không tự động ẩn post.

## 11. Admin

- Chỉ ADMIN truy cập API quản trị.
- Có thể khóa/mở user.
- Có thể ẩn/khôi phục post.
- Có thể xử lý report.

## 12. API

- Danh sách có phân trang.
- HTTP status đúng.
- Không trả Entity.
- Không trả dữ liệu nhạy cảm.
- Không trả stack trace.
