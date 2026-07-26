# Quy tắc dự án

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. File này tóm tắt các quy tắc thực thi; khi có khác biệt phải áp dụng README và cập nhật file này, không sửa README để hợp thức hóa implementation cũ.

## 1. Tài khoản

- Email duy nhất nếu có giá trị.
- Không dùng username hoặc email công khai tương tự trong MVP.
- Đăng ký bằng đúng một phương thức email: email.
- Form đăng ký chỉ gồm phương thức email, mật khẩu và xác nhận mật khẩu.
- Request đăng ký chỉ nhận một giá trị email tại một thời điểm.
- Nếu đăng ký bằng email thì `email` lưu `NULL`; nếu đăng ký bằng email thì `email` lưu `NULL`.
- Form đăng ký local chỉ tạo `pending_registrations`; chưa tạo `users`, `user_profiles` hoặc JWT.
- OTP email hợp lệ mới tạo `users` và `user_profiles` trong cùng transaction.
- Hệ thống hỗ trợ Google/Facebook và nhiều phương thức xác thực cùng ánh xạ về một `users.id`.
- Provider token chỉ dùng tại Auth endpoint và phải được Backend xác minh.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Liên kết provider lấy user đích từ JWT hiện tại; không được gỡ phương thức đăng nhập cuối cùng.
- `users.password_hash` được phép `NULL` với social-only account.
- Đăng nhập bằng email.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Trạng thái MVP: ACTIVE, BLOCKED.
- User `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.
- Role: USER, ADMIN.
- Email chỉ đăng nhập local được sau khi trường verified timestamp tương ứng có giá trị.

## 2. Hồ sơ

- Hồ sơ công khai.
- Email không hiển thị công khai.
- Chỉ chủ tài khoản cập nhật hồ sơ.
- Backend chỉ tạo `user_profiles` cùng `users` sau khi OTP hoặc social identity hợp lệ theo README.
- `user_profiles.display_name`, `user_profiles.date_of_birth` và `user_profiles.profile_completed_at` ban đầu phải `NULL` trong hồ sơ rỗng.
- Tên hiển thị thuộc `user_profiles`, không thuộc `users`.
- Tên hiển thị và ngày sinh bắt buộc để hoàn tất hồ sơ ban đầu.
- Người dùng phải đủ 18 tuổi tại ngày Backend xử lý onboarding hoặc cập nhật hồ sơ.
- Avatar và bio là tùy chọn; tên hiển thị và ngày sinh không được bỏ qua khi hoàn tất onboarding.
- Hồ sơ chỉ hoàn tất khi tên hiển thị hợp lệ và ngày sinh hợp lệ của người dùng đủ 18 tuổi đã được lưu, người dùng xác nhận hoàn tất và Backend cập nhật `profile_completed_at`.
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
- Danh sách bài đã Like chỉ chủ tài khoản xem.

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
- Feed và các danh sách bài dùng Infinite Scroll phải dùng Cursor Pagination theo API contract.

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

- Feed For You, Feed Following, bài trên hồ sơ, bài đã lưu và bài đã thích dùng Cursor Pagination.
- Search, bình luận, Follow và Admin dùng `PageResponse`.
- HTTP status đúng.
- Không trả Entity.
- Không trả dữ liệu nhạy cảm.
- Không trả stack trace.

## 13. Cấu trúc enum Backend

- Mỗi module Backend có entity sử dụng status, role, type, reason hoặc nhóm giá trị cố định phải đặt enum trong thư mục `enums/` của chính module đó.
- Không gom enum nghiệp vụ của nhiều module vào một package chung nếu enum chỉ thuộc về một module.
- `UserStatus` và `UserRole` thuộc `user/enums`.
- `PostStatus` thuộc `post/enums`.
- `CommentStatus` thuộc `interaction/enums`.
- `ReportStatus` và `ReportReason` thuộc `report/enums`.
- Enum dùng chung thật sự cho nhiều module chỉ được đặt trong `common/enums` khi có lý do rõ ràng.

