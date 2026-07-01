# UI-FIX-LIST

## Mục đích

Tài liệu này dùng để theo dõi các điểm giao diện cần sửa khi dựng `UI-DEMO.html` và khi triển khai Frontend ReactJS + Tailwind CSS.

Các ảnh trong thư mục `screens` chỉ được dùng làm tài liệu tham chiếu. Nếu ảnh Stitch có điểm không thống nhất với tài liệu UI đã chốt, ưu tiên quy tắc trong tài liệu dự án.

---

## UI-001 - Chuẩn hóa logo toàn hệ thống

### Hiện trạng

Một số ảnh giao diện do Google Stitch tạo ra đang sử dụng logo không thống nhất.

### Kết quả mong muốn

- Toàn bộ website chỉ sử dụng duy nhất một logo chính thức.
- Không lấy logo riêng từ từng ảnh Stitch.
- Logo phải được tái sử dụng từ một asset dùng chung.
- Logo trên giao diện người dùng và giao diện quản trị phải thống nhất.

### Đường dẫn đề xuất

```text
docs/ui/assets/logo.png
```

Khi triển khai Frontend ReactJS có thể chuyển asset sang:

```text
FrontEnd/src/assets/brand/logo.png
```

### Phạm vi áp dụng

- Trang đăng nhập.
- Trang đăng ký.
- Sidebar người dùng.
- Sidebar quản trị.
- Các trang lỗi.
- Những vị trí khác có hiển thị nhận diện thương hiệu.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-002 - Sidebar active sai tại hồ sơ người dùng khác

### Hiện trạng

Ảnh Stitch của trang hồ sơ người dùng khác đang active mục **Trang cá nhân** trên sidebar.

### Kết quả đúng

- Khi xem hồ sơ của chính mình, mục **Trang cá nhân** được active.
- Khi xem hồ sơ của người dùng khác, không active mục **Trang cá nhân**.
- Không active sai sang bất kỳ mục điều hướng nào khác.

### Quy tắc route dự kiến

```text
/profile/me
```

Kết quả:

```text
Active: Trang cá nhân
```

```text
/profile/:userId
```

Kết quả:

```text
Không active mục Trang cá nhân
```

### Phạm vi áp dụng

- `UI-DEMO.html`.
- Router của Frontend ReactJS.
- Component `Sidebar`.
- Trang hồ sơ cá nhân.
- Trang hồ sơ người dùng khác.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-003 - Đồng bộ sidebar giữa các màn hình người dùng

### Hiện trạng

Các ảnh Stitch có thể có sai lệch nhỏ về:

- Kích thước sidebar.
- Khoảng cách giữa các menu.
- Kích thước icon.
- Trạng thái active.
- Vị trí logo.
- Vị trí nút đăng bài.

### Kết quả mong muốn

- Dùng một component Sidebar chung.
- Cùng chiều rộng trên các màn hình desktop.
- Icon, nhãn và khoảng cách thống nhất.
- Trạng thái active được xác định từ route hiện tại.
- Không dựng sidebar riêng cho từng trang.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-004 - Chuẩn hóa PostCard

### Hiện trạng

PostCard giữa Feed, trang chi tiết bài viết, hồ sơ và danh sách bài đã lưu có thể chưa hoàn toàn đồng nhất.

### Kết quả mong muốn

PostCard dùng chung phải thống nhất:

- Ảnh đại diện.
- Tên hiển thị.
- Thời gian đăng.
- Nội dung bài viết.
- Danh sách hình ảnh.
- Hashtag.
- Số lượt Like.
- Số bình luận.
- Nút Like.
- Nút Bình luận.
- Nút Lưu.
- Menu thao tác.

Các trang chỉ thay đổi chế độ hiển thị hoặc hành động được phép, không tạo nhiều cấu trúc PostCard khác nhau.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-005 - Chuẩn hóa modal và hộp thoại xác nhận

### Phạm vi

- Modal tạo bài viết.
- Modal chỉnh sửa bài viết.
- Modal xóa bài viết.
- Modal báo cáo bài viết.
- ConfirmDialog khóa hoặc mở khóa người dùng.
- ConfirmDialog ẩn hoặc khôi phục bài viết.

### Kết quả mong muốn

- Overlay thống nhất.
- Bo góc, khoảng cách và tiêu đề thống nhất.
- Nút chính và nút hủy dùng chung quy chuẩn.
- Thao tác nguy hiểm có nút và cảnh báo rõ ràng.
- Có thể đóng bằng nút đóng, nút hủy hoặc phím Escape khi phù hợp.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-006 - Bổ sung trạng thái giao diện khi code

Các trạng thái sau không cần quay lại Google Stitch để thiết kế riêng nhưng phải được bổ sung khi dựng demo hoặc Frontend:

- Loading.
- Skeleton.
- Empty.
- Error.
- Disabled.
- Validation form.
- Toast thành công.
- Toast thất bại.
- Phiên đăng nhập hết hạn.
- Không có quyền truy cập.
- Trang không tồn tại.
- Lỗi hệ thống.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-007 - Không triển khai chức năng ngoài MVP trong bản demo

Không đưa vào `UI-DEMO.html` và Frontend MVP hiện tại:

- Nhắn tin.
- Discovery Map.
- Feed tùy chỉnh.
- Follow Request.
- Repost.
- Trích dẫn bài viết.
- Video hoặc tài liệu trong bài viết.
- Elasticsearch.
- Dashboard nâng cao.
- Các chức năng ngoài tài liệu MVP đã chốt.

### Trạng thái

- [ ] Đã kiểm tra phạm vi bản demo.
- [ ] Đã kiểm tra phạm vi Frontend ReactJS.

---

## UI-008 - Loại bỏ username khỏi MVP

### Hiện trạng

Một số ảnh Stitch và tài liệu cũ còn dùng tên người dùng hoặc chuỗi dạng `@...`.

### Kết quả mong muốn

- Đăng ký không dùng username.
- Đăng ký không nhập tên hiển thị.
- Đăng nhập không dùng username.
- Search không tìm theo username.
- Profile và PostCard hiển thị displayName.
- Quan hệ dữ liệu dùng userId.
- Không tạo trường thay thế như handle, userSlug hoặc publicUsername.

### Ghi chú onboarding

- Sau đăng ký phải chuyển người dùng đến onboarding hồ sơ.
- Onboarding yêu cầu tên hiển thị; avatar, ngày sinh và bio có thể bỏ qua.
- Người chưa hoàn tất hồ sơ không được vào Feed hoặc các chức năng mạng xã hội chính.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-009 - Chuẩn hóa route hồ sơ

### Kết quả mong muốn

- `/profile/me` cho hồ sơ cá nhân.
- `/profile/:userId` cho hồ sơ người dùng khác.
- Không sử dụng `/profile/:username`.
- Không sử dụng `/users/:username`.
- Không sử dụng `/@username`, `/u/:username` hoặc `/profile/@username`.
- Profile người khác không active mục Trang cá nhân.

### Trạng thái

- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-010 - Phân loại màn hình ngoài MVP

### Kết quả mong muốn

- Không xóa ảnh.
- Giữ các màn hình tương lai.
- Đánh dấu `FUTURE_DEVELOPMENT`.
- Không triển khai trong demo MVP hiện tại.
- Màn hình thuộc MVP đánh dấu `MVP_CURRENT`.

### Trạng thái

- [x] Đã cập nhật phân loại trong tài liệu UI.
- [ ] Chưa xử lý trong `UI-DEMO.html`.
- [ ] Chưa xử lý trong Frontend ReactJS.

---

## UI-011 - Chuẩn hóa quy tắc Mention

### Kết quả mong muốn

- Hiển thị mention bằng displayName.
- Người dùng chọn tài khoản từ danh sách gợi ý.
- Lưu liên kết bằng `mentionedUserId`.
- Khi nhấn mention, điều hướng `/profile/:userId`.
- Không dùng `@username`.
- Không dùng displayName làm khóa liên kết.

### Trạng thái

- [x] Đã chuẩn hóa quy tắc trong tài liệu.
- [ ] Chưa triển khai trong MVP.

---

## Quy tắc cập nhật tài liệu

Khi phát hiện thêm sai lệch giao diện:

1. Tạo mã mới theo thứ tự `UI-008`, `UI-009`, ...
2. Ghi rõ hiện trạng.
3. Ghi rõ kết quả mong muốn.
4. Ghi phạm vi ảnh hưởng.
5. Theo dõi riêng trạng thái xử lý ở demo và Frontend.
6. Không sửa trực tiếp ảnh Stitch chỉ vì sai lệch nhỏ.
