# Workflow tạo API

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Workflow này chỉ mô tả cách thiết kế API; contract nghiệp vụ phải lấy từ README và tài liệu API đã được đồng bộ.

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

- Đọc toàn bộ contract tham chiếu và tiêu chí nghiệm thu trong README.
- Đối chiếu tài liệu API hiện tại; đánh dấu endpoint hoặc response cũ, không kế thừa mặc định.
- Mô hình hóa state transition, actor, authentication, authorization và error code cho từng nhánh.
- Xác định rõ token nào được nhận, phát hành, lưu hash, thu hồi và được phép dùng ở endpoint nào.
- Xác định transaction, external call, idempotency, concurrency và recovery trước khi thiết kế DTO.
- Với username, ghi rõ availability không giữ chỗ; complete onboarding phải kiểm tra lại và map race unique thành lỗi nghiệp vụ ổn định.

## Bước 2: Xác định tầng

- DTO.
- Controller.
- Service.
- Repository.
- Mapper.
- Exception.
- Entity hoặc migration nếu cần.

Với Auth/Profile, phải đối chiếu README với SQL, DBML, Entity và Repository trước khi thiết kế DTO; database baseline không được dùng để ghi đè trạng thái đích.

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
