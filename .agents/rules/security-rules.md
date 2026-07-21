# Quy tắc bảo mật

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. File này mô tả cách kiểm tra bảo mật; contract và nghiệp vụ Auth phải lấy từ README.

## 1. Xác thực

- Đọc các mục Auth, JWT, Security, onboarding và tiêu chí nghiệm thu trong README trước khi review hoặc triển khai.
- Lập threat model cho mật khẩu, OTP, flow token, provider token, Access Token và Refresh Token.
- Kiểm tra hash-at-rest, expiry, revocation, replay, rate limit, concurrency và log redaction theo README.
- Backend phải tự xác minh dữ liệu danh tính từ provider; không tin dữ liệu do Frontend tự khai báo.
- Kiểm tra trạng thái tài khoản và trạng thái onboarding tại Backend cho mọi đường xác thực và API liên quan.
- Không dùng hành vi source cũ để nới lỏng yêu cầu bảo mật đã được README chốt.

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
