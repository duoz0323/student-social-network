# Quy tắc Backend

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

## 3. Quy tắc Auth và tài khoản

- API đăng ký nhận `identifier`, `password` và `confirmPassword`.
- Không nhận hoặc lưu `username` trong luồng đăng ký MVP.
- `identifier` là email hoặc số điện thoại; request đăng ký chỉ được chứa đúng một phương thức định danh tại thời điểm tạo tài khoản.
- Backend chuẩn hóa email về chữ thường trước khi kiểm tra trùng và lưu.
- Backend chuẩn hóa số điện thoại về định dạng thống nhất trước khi kiểm tra trùng và lưu.
- Email phải đúng định dạng, được trim, chuẩn hóa chữ thường và duy nhất nếu được cung cấp.
- Số điện thoại phải đúng định dạng, được chuẩn hóa thống nhất và duy nhất nếu được cung cấp.
- Nếu đăng ký bằng email thì `phone_number` lưu `NULL`; nếu đăng ký bằng số điện thoại thì `email` lưu `NULL`.
- Database cho phép tài khoản bổ sung phương thức còn thiếu trong tương lai, nhưng mỗi tài khoản luôn phải có ít nhất email hoặc số điện thoại.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt, sau đó băm một chiều trước khi lưu.
- Tài khoản mới có trạng thái `ACTIVE` và role mặc định `USER`.
- API đăng nhập nhận một định danh email hoặc số điện thoại kèm mật khẩu; Service tự xác định loại định danh để truy vấn đúng trường.
- Không trả email, số điện thoại, password hash, refresh token hoặc dữ liệu xác thực trong response hồ sơ công khai.
- Tên hiển thị là dữ liệu hồ sơ, không nhận trong request đăng ký và chỉ lưu vào `user_profiles` khi onboarding/cập nhật hồ sơ.
- Đăng ký thành công phải tạo `users` và `user_profiles` rỗng trong cùng transaction.
- Nếu tạo `user_profiles` thất bại thì transaction phải rollback `users`.
- `user_profiles.display_name` và `user_profiles.profile_completed_at` ban đầu là `NULL`.
- Hoàn tất hồ sơ yêu cầu tên hiển thị hợp lệ và thao tác xác nhận hoàn tất từ người dùng.
- Khi hoàn tất, Backend cập nhật `user_profiles.profile_completed_at`.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không thay thế kiểm tra `profile_completed_at`.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép API xác thực cần thiết, Refresh Token, đăng xuất và onboarding.
- Các API Feed, đăng bài, Follow, Like, bình luận, lưu bài, tìm kiếm, báo cáo và chức năng mạng xã hội chính phải trả `PROFILE_NOT_COMPLETED`.
- MVP chưa triển khai xác minh email hoặc SMS OTP; `email_verified_at` và `phone_verified_at` để `NULL` nếu chưa xác minh.
- Ngày sinh chỉ xử lý trong cập nhật hồ sơ, là thông tin tùy chọn và không được nằm trong tương lai.

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

- Đăng ký tài khoản kèm tạo `user_profiles`.
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
