# Workflow tạo UI

## Bước 1: Đọc tài liệu

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
