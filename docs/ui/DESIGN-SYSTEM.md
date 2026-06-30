# Design System

Tài liệu này tổng hợp quy chuẩn thiết kế quan sát được từ ảnh trong `docs/ui/screens/` và `UI-DEMO.html`. Các giá trị chưa đo được chính xác được mô tả định tính và đánh dấu `CẦN XÁC NHẬN`.

## 1. Bố cục tổng thể

- Giao diện người dùng chính theo kiểu mạng xã hội tối giản: sidebar trái cố định, vùng nội dung chính ở giữa, nền sáng.
- Vùng Feed/Profile/Post Detail có chiều rộng trung tâm vừa phải để tập trung vào nội dung bài viết.
- Các modal xuất hiện trên nền mờ hoặc nền trung tính, ưu tiên thao tác ngắn và rõ ràng.
- Admin dùng layout dashboard riêng: sidebar trái, vùng nội dung rộng, bảng dữ liệu và thẻ thống kê.
- `UI-DEMO.html` dùng grid desktop gồm sidebar trái, feed giữa và panel phải; ảnh Stitch hiện tại chủ yếu thể hiện sidebar trái và nội dung giữa.

## 2. Sidebar người dùng

- Brand `UniShare` đặt ở góc trên trái.
- Danh mục chính quan sát được: Dành cho bạn/Trang chủ, Tạo bài viết, Tìm kiếm, Hoạt động, Trang cá nhân, Bài viết đã lưu, Xem thêm.
- Item active có nền xám nhạt, bo góc mềm, icon ở bên trái và text ở bên phải.
- Các mục thuộc MVP: Feed, Tạo bài viết, Tìm kiếm, Trang cá nhân, Bài viết đã lưu.
- Mục `Hoạt động` và một số mục trong `Xem thêm` CẦN XÁC NHẬN, không đưa vào MVP nếu không có đặc tả.

## 3. Sidebar Admin

- Sidebar admin dùng brand `UniShare`, nhãn khu vực "Trang quản trị" và danh mục quản trị.
- Danh mục quan sát được: Tổng quan, Người dùng, Bài viết, Báo cáo, Cài đặt, Quay lại ứng dụng.
- Các mục thuộc MVP: Người dùng, Bài viết, Báo cáo, Quay lại ứng dụng.
- `Tổng quan` chỉ nên hiểu là dashboard tóm tắt đơn giản nếu cần; dashboard nâng cao ngoài MVP.
- `Cài đặt` CẦN XÁC NHẬN và không nằm trong luồng MVP đã chốt.

## 4. Vùng nội dung

- Feed dùng tab ngang ở đầu vùng nội dung cho `Dành cho bạn` và `Đang theo dõi`.
- Composer tạo bài nhanh nằm phía trên danh sách bài trong Feed.
- Profile dùng header thông tin cá nhân, chỉ số follower/following, nút hành động và tab nội dung.
- Search dùng thanh tìm kiếm lớn ở đầu, bên dưới là nhóm gợi ý/tìm kiếm phổ biến.
- Admin dùng tiêu đề trang lớn, mô tả ngắn, bộ lọc/tìm kiếm và bảng dữ liệu.

## 5. PostCard

- PostCard gồm avatar, tên hiển thị, thời gian, menu ba chấm, nội dung text, hashtag, media và thanh action.
- PostCard không hiển thị username, handle hoặc định danh công khai tương tự trong MVP.
- Tác giả bài viết được liên kết nội bộ bằng `authorId`/`userId`; khi bấm tên hoặc avatar điều hướng đến `/profile/:userId`.
- Media ảnh thường bo góc nhẹ, chiếm gần hết chiều rộng card.
- Action quan sát được: like, comment, repost/share-like icon, chia sẻ/lưu/menu tùy màn hình.
- Repost không thuộc MVP; nếu icon xuất hiện trong ảnh thì chỉ ghi nhận visual, không triển khai nghiệp vụ repost.
- Bài có thể hiển thị trạng thái đã chỉnh sửa hoặc số liệu tương tác; vị trí và format CẦN XÁC NHẬN khi có API.

## 6. Modal

- Modal có header rõ, nút đóng hoặc hành động hủy, nội dung chính ở giữa và footer hành động.
- Modal xác nhận xóa dùng thông điệp cảnh báo, nút hủy trung tính và nút xóa màu cảnh báo.
- Modal báo cáo dùng danh sách radio toàn hàng, sau đó màn hình nhập mô tả.
- Modal thành công dùng icon check lớn, tiêu đề ngắn, mô tả và nút chính màu đen.
- Modal phiên hết hạn dùng icon đồng hồ, mô tả ngắn và nút đăng nhập lại.

## 7. Button

- Primary button: nền đen, chữ trắng, bo tròn lớn; dùng cho đăng nhập, đăng ký, đăng bài, lưu thay đổi, tiếp tục, xác nhận.
- Secondary button: nền trắng hoặc xám rất nhạt, viền mảnh, chữ đen; dùng cho hủy, theo dõi phụ, điều hướng phụ.
- Destructive action: chữ đỏ hoặc nút cảnh báo trong modal xóa.
- Button trong bảng admin nhỏ gọn hơn, ưu tiên thao tác nhanh.
- Kích thước cụ thể CẦN XÁC NHẬN; không tự chốt pixel khi chưa đo từ thiết kế gốc.

## 8. Input

- Auth form dùng input bo góc, cao vừa phải, viền xám nhạt, placeholder rõ.
- Search input có icon kính lúp và nền xám nhạt.
- Textarea tạo/sửa bài chiếm chiều rộng lớn, hỗ trợ đếm ký tự.
- Radio trong báo cáo đặt bên phải mỗi lựa chọn.
- Focus, error và disabled state cần có nhưng màu/độ dày viền CẦN XÁC NHẬN.

## 9. Typography

- Font sans-serif, phong cách sạch và dễ đọc.
- Brand và heading dùng weight đậm.
- Nội dung bài viết dùng cỡ chữ trung bình, line-height thoáng.
- Metadata như thời gian, mô tả phụ, thống kê dùng màu xám và cỡ nhỏ hơn.
- Hồ sơ, Search, danh sách follow và gợi ý người dùng hiển thị `displayName`.
- Không dùng `displayName` làm khóa liên kết vì có thể trùng hoặc thay đổi.
- Không hiển thị `userId` như thông tin nổi bật trên UI; `userId` chỉ dùng cho điều hướng và liên kết nội bộ.
- Không tự chốt font-size pixel; CẦN XÁC NHẬN từ file thiết kế gốc nếu cần triển khai chính xác.

## 9.1 Mention

- Mention không thuộc MVP hiện tại; nếu xuất hiện trong ảnh hoặc nội dung mẫu thì đánh dấu `FUTURE_DEVELOPMENT`.
- Mention hiển thị bằng `@displayName` sau khi người dùng chọn một tài khoản từ danh sách gợi ý.
- Mention phải lưu liên kết bằng `mentionedUserId`, không lưu bằng displayName.
- Khi render mention, tìm user theo `mentionedUserId`, hiển thị displayName hiện tại và điều hướng đến `/profile/:userId`.
- Gợi ý mention hiển thị avatar, displayName và thông tin phụ như bio ngắn nếu cần; không hiển thị email, số điện thoại hoặc dữ liệu xác thực.

## 10. Màu sắc

- Nền chính: trắng hoặc xám rất nhạt.
- Text chính: gần đen.
- Text phụ, border, divider: các sắc xám nhạt đến xám trung tính.
- Primary action: đen/trắng.
- Link/brand `UniShare`: tím/xanh tím trong một số màn hình người dùng; CẦN XÁC NHẬN mã màu.
- Trạng thái admin: xanh cho active/published, vàng cho pending/chờ xử lý, đỏ cho blocked/ẩn/xóa; mã màu CẦN XÁC NHẬN.

## 11. Border, radius, spacing

- Card, input, modal và media đều dùng bo góc mềm.
- Divider mảnh phân tách bài viết, item menu, bảng và vùng thông tin.
- Khoảng cách tổng thể thoáng, nhiều whitespace; sidebar và content có khoảng cách rõ.
- `UI-DEMO.html` tham chiếu spacing 12px, 16px, 24px và radius 10px-16px, nhưng ảnh Stitch có thể khác; xem đây là tham khảo, không phải token bắt buộc.

## 12. Responsive dự kiến

- Desktop: sidebar trái hiển thị cố định, content ở giữa.
- Tablet: có thể thu hẹp sidebar hoặc ẩn panel phụ nếu có.
- Mobile: CẦN XÁC NHẬN vì bộ ảnh hiện tại chủ yếu là desktop/tablet; dự kiến chuyển sidebar thành top/bottom navigation hoặc drawer.
- Modal trên mobile cần full-width gần mép màn hình, đảm bảo nút chính không bị che và text không tràn.

## 13. Note
- Toàn hệ thống chỉ dùng một logo chính thức.
- Ảnh Stitch chỉ là tài liệu tham chiếu, không phải tiêu chuẩn tuyệt đối.
- Nếu ảnh Stitch mâu thuẫn với tài liệu đã chốt thì ưu tiên tài liệu.
- Sidebar người dùng phải đồng nhất giữa các màn hình.
- Sidebar active mục "Trang cá nhân" chỉ khi route là `/profile/me`; route `/profile/:userId` không active mục này.
- Sidebar Admin phải đồng nhất giữa các màn hình quản trị.
- Các chi tiết ngoài MVP trong ảnh được giữ làm thiết kế `FUTURE_DEVELOPMENT`, không đưa vào demo MVP.
- Các thành phần sau có quy chuẩn dùng chung:

    - PostCard
    - Modal
    - Button
    - Input
    - Bảng Admin
    - Badge trạng thái
