# Workflow tạo UI

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Workflow này chỉ mô tả cách tạo UI; hành vi và trạng thái đích phải lấy từ README.

## Bước 1: Đọc tài liệu

- `README.md` trước tiên và đầy đủ.
- `docs/ui/UI-FLOW.md`
- `docs/ui/SCREEN-LIST.md`
- `docs/ui/COMPONENTS.md`
- `docs/ui/DESIGN-SYSTEM.md`
- `docs/data/DEMO-DATA.md`
- `docs/data/demo-data.json`

## Bước 2: Xác định màn hình

Ghi rõ:

- Route.
- Actor.
- Mục tiêu.
- Dữ liệu cần hiển thị.
- Hành động người dùng.
- Loading State.
- Empty State.
- Error State.
- Permission State.

Với màn hình Auth MVP:

- Lập danh sách trạng thái và màn hình từ README trước khi dùng mock hoặc ảnh thiết kế cũ.
- Đối chiếu tài liệu UI với README và ghi nhận điểm lỗi thời trước khi triển khai.
- Mô hình hóa pending/recovery/cooldown/conflict, authentication và onboarding theo API contract đã đồng bộ.
- Không tạo session hoặc user mock trước thời điểm contract cho phép.
- Không lưu provider credential lâu dài hoặc dùng provider credential gọi API nghiệp vụ.
- Route guard và xử lý lỗi phải bao phủ các trạng thái README quy định.

## Bước 3: Dựng bằng mock data

- Không hard-code dữ liệu trong component.
- Tách dữ liệu mock khỏi UI.
- Giữ cấu trúc response gần API dự kiến.
- Không gọi Backend khi chưa được yêu cầu.

## Bước 4: Kiểm tra

- Desktop.
- Mobile.
- Loading.
- Empty.
- Error.
- Nội dung dài.
- Ảnh lỗi.
- Không có quyền.
