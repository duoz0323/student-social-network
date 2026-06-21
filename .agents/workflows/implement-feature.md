# Workflow triển khai chức năng

## Bước 1: Đọc tài liệu

Đọc:

- `docs/PRD.md`
- `docs/ARCHITECTURE.md`
- `docs/PROJECT-RULES.md`
- Tài liệu UI liên quan.
- Tài liệu Data liên quan.
- Rule và skill phù hợp.

## Bước 2: Phân tích

Trình bày:

- Mục tiêu chức năng.
- Actor.
- Tiền điều kiện.
- Luồng chính.
- Luồng ngoại lệ.
- Bảng dữ liệu liên quan.
- API liên quan.
- Màn hình liên quan.

## Bước 3: Lập kế hoạch

Liệt kê:

- File tạo mới.
- File cần sửa.
- Migration nếu có.
- API request/response.
- Test cần viết.

## Bước 4: Triển khai

- Tuân thủ kiến trúc.
- Không mở rộng ngoài MVP.
- Comment tiếng Việt.
- Không cài thư viện nếu chưa cần.
- Không sửa module không liên quan.

## Bước 5: Kiểm tra

- Build/compile.
- Test.
- Validation.
- Phân quyền.
- Loading/Empty/Error đối với Frontend.
- Không lộ dữ liệu nhạy cảm.

## Bước 6: Báo cáo

- Đã làm gì.
- File thay đổi.
- Cách chạy.
- Cách kiểm thử.
- Phần chưa hoàn thành.
