# Workflow triển khai chức năng

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Workflow này mô tả trình tự làm việc, không định nghĩa lại nghiệp vụ.

## Bước 1: Đọc tài liệu

Đọc:

- `README.md` trước tiên và đầy đủ.
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

Nếu chức năng thuộc Auth hoặc hồ sơ, phải xác định rõ:

- Các trạng thái, actor và nhánh nghiệp vụ trong README.
- Boundary giữa pending, tài khoản thật, phiên đăng nhập và onboarding.
- Dữ liệu do Backend tự xác minh và dữ liệu không được tin từ Frontend.
- Transaction, external provider call, concurrency, recovery, linking và revocation.
- Khoảng chênh lệch giữa README với SQL/DBML, source và test hiện tại.
- Nếu liên quan username: phân biệt registration, onboarding, profile display, route `userId`, legacy data và các module chưa được mở rộng.

## Bước 3: Lập kế hoạch

Liệt kê:

- File tạo mới.
- File cần sửa.
- Enum của entity, nếu có, và vị trí trong `enums/` của module tương ứng.
- Migration nếu có.
- API request/response.
- Test cần viết.

Với Auth/Profile, kế hoạch phải đi theo thứ tự: chốt trạng thái đích từ README, audit tài liệu và database, chốt API, thiết kế DTO/validation/transaction/security guard, rồi mới đến test và code.

## Bước 4: Triển khai

- Tuân thủ kiến trúc.
- Đặt status, role, type, reason hoặc enum nghiệp vụ của entity trong thư mục `enums/` của module đó.
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

Test tối thiểu cho Auth/Profile MVP:

- Mỗi tiêu chí nghiệm thu Auth/onboarding trong README có ít nhất một test tương ứng.
- Có test thành công, validation, expiry, conflict, concurrency, rollback, authorization và dữ liệu nhạy cảm.
- Có test cho các đường local, social, linking, token và onboarding mà README đưa vào phạm vi.
- Test cũ mâu thuẫn README phải được xác định để thay thế, không được dùng làm bằng chứng thay đổi nghiệp vụ.

## Bước 6: Báo cáo

- Đã làm gì.
- File thay đổi.
- Cách chạy.
- Cách kiểm thử.
- Phần chưa hoàn thành.
