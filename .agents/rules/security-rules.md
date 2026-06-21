# Quy tắc bảo mật

## 1. Xác thực

- Mật khẩu phải được băm một chiều.
- Không lưu mật khẩu dạng văn bản thuần.
- Access Token có thời hạn ngắn.
- Refresh Token có thời hạn dài hơn và có thể bị thu hồi.
- Khi đăng xuất, refresh token của phiên hiện tại phải bị vô hiệu hóa.

## 2. Phân quyền

- Backend kiểm tra quyền cho mọi API nhạy cảm.
- API Admin yêu cầu role `ADMIN`.
- Chỉ tác giả được chỉnh sửa hoặc xóa nội dung của mình.
- Không dựa vào việc ẩn nút trên Frontend để bảo vệ tài nguyên.

## 3. Dữ liệu nhạy cảm

Không trả qua API công khai:

- Password hash.
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
