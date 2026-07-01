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

Với màn hình Auth MVP:

- Đăng ký hiển thị một trường email hoặc số điện thoại, mật khẩu và xác nhận mật khẩu.
- Không yêu cầu nhập đồng thời email và số điện thoại.
- Đăng ký không hiển thị tên người dùng hoặc username.
- Đăng nhập hiển thị một ô email hoặc số điện thoại và một ô mật khẩu.
- Không dùng nhãn Gmail, Google/Facebook login hoặc xác thực email trong MVP.
- Sau đăng ký chuyển sang onboarding hồ sơ.
- Onboarding yêu cầu tên hiển thị; avatar, ngày sinh và bio có thể bỏ qua.
- Route Guard tách trạng thái chưa đăng nhập, đã đăng nhập nhưng chưa hoàn tất hồ sơ, và đã đăng nhập hoàn tất hồ sơ.
- Khi API trả `PROFILE_NOT_COMPLETED`, điều hướng về onboarding.
- Ngày sinh chỉ đặt ở onboarding/cập nhật hồ sơ, không đặt ở đăng ký.

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
