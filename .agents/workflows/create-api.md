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
