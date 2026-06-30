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

- Register nhận `email`, `phoneNumber` và `password`.
- Login nhận `identifier` là email hoặc số điện thoại và `password`.
- Không thiết kế request/response phụ thuộc `username`.
- Không tạo trường `gmail`; dùng `email`.
- Email và số điện thoại phải được chuẩn hóa ở Backend trước khi kiểm tra trùng và lưu.

## Bước 2: Xác định tầng

- DTO.
- Controller.
- Service.
- Repository.
- Mapper.
- Exception.
- Entity hoặc migration nếu cần.

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
