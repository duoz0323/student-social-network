# Quy tắc bảo mật

## 1. Xác thực

- Mật khẩu phải được băm một chiều.
- Không lưu mật khẩu dạng văn bản thuần.
- Đăng ký phải kiểm tra request chỉ có đúng một phương thức định danh: email hoặc số điện thoại.
- Email hoặc số điện thoại được cung cấp phải đúng định dạng, được chuẩn hóa và duy nhất.
- Mỗi tài khoản luôn phải có ít nhất email hoặc số điện thoại.
- MVP chưa yêu cầu xác minh email hoặc SMS OTP trước khi tạo tài khoản `ACTIVE`.
- Backend phải chuẩn hóa email về chữ thường trước khi kiểm tra và lưu.
- Backend phải chuẩn hóa số điện thoại về một định dạng thống nhất trước khi kiểm tra và lưu.
- Mật khẩu đăng ký tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Access Token có thời hạn ngắn.
- Refresh Token có thời hạn dài hơn và có thể bị thu hồi.
- Khi đăng xuất, refresh token của phiên hiện tại phải bị vô hiệu hóa.
- Tài khoản `BLOCKED` không được đăng nhập hoặc sử dụng hệ thống.
- Backend phải kiểm tra `user_profiles.profile_completed_at` trước khi cho dùng API mạng xã hội chính.
- Nếu hồ sơ chưa hoàn tất, Backend trả lỗi nghiệp vụ `PROFILE_NOT_COMPLETED`.

## 2. Phân quyền

- Backend kiểm tra quyền cho mọi API nhạy cảm.
- API Admin yêu cầu role `ADMIN`.
- Chỉ tác giả được chỉnh sửa hoặc xóa nội dung của mình.
- Không dựa vào việc ẩn nút trên Frontend để bảo vệ tài nguyên.

## 3. Dữ liệu nhạy cảm

Không trả qua API công khai:

- Password hash.
- Email và số điện thoại trên hồ sơ công khai.
- Refresh Token.
- Secret.
- Stack trace.
- Thông tin cấu hình nội bộ.
- Dữ liệu xác thực không cần thiết.

## 4. Upload ảnh

Backend phải kiểm tra:

- Phần mở rộng.
- MIME type thực tế.
- Kích thước file.
- Số lượng file.
- Tên file an toàn.
- URL hoặc metadata sau khi upload.

## 5. Input

- Validate DTO.
- Giới hạn độ dài chuỗi.
- Không nối chuỗi SQL thủ công.
- Không render HTML chưa được làm sạch.
- Kiểm soát dữ liệu tìm kiếm.
- Chống request lặp khi cần.

## 6. Token trên Frontend

- Không ghi token ra console.
- Không đưa token vào URL.
- Không chia sẻ token giữa người dùng.
- Khi refresh thất bại phải xóa phiên đăng nhập.
- Không để nhiều request refresh chạy đồng thời nếu có thể kiểm soát.

## 7. Secret

- Secret lấy từ biến môi trường.
- Không commit `.env` chứa secret.
- Cung cấp `.env.example` không có giá trị thật.
