# Checklist Manual E2E — User Block

Trạng thái: **CHƯA CHẠY**.

`FRONTEND COMPONENT TEST NOT AVAILABLE`: Frontend hiện không có Vitest/Jest/React Testing Library hoặc DOM test environment. Ba Node test hiện có chỉ kiểm tra utility/cache, không được tính là component test.

## Chuẩn bị

| Mục | Dữ liệu chuẩn bị | Kết quả thực tế | PASS/FAIL | Ảnh/Ghi chú |
|---:|---|---|---|---|
| P1 | Có ba tài khoản ACTIVE, hoàn tất hồ sơ: User A, User B và User C | Chưa chạy | — | |
| P2 | A Follow B và B Follow A | Chưa chạy | — | |
| P3 | B có ít nhất một bài PUBLISHED | Chưa chạy | — | |
| P4 | A đã Like, Comment và Save bài của B | Chưa chạy | — | |
| P5 | B đã tạo ít nhất một Notification cho A | Chưa chạy | — | |

## Luồng Block

| # | Thao tác | Kết quả mong đợi | Kết quả thực tế | PASS/FAIL | Ảnh/Ghi chú |
|---:|---|---|---|---|---|
| 1 | A mở Profile B | Hồ sơ B hiển thị và có action Block | Chưa chạy | — | |
| 2 | A chọn Block B | Modal xác nhận mở | Chưa chạy | — | |
| 3 | Kiểm tra nội dung modal | Cảnh báo mất quyền xem/tương tác và Follow hai chiều bị xóa | Chưa chạy | — | |
| 4 | A xác nhận Block | PUT Block thành công, không gửi request trùng khi double click | Chưa chạy | — | |
| 5 | Sau Block | A thấy toast và bị điều hướng khỏi Profile B | Chưa chạy | — | |
| 6 | A Search User B | Không tìm thấy B | Chưa chạy | — | |
| 7 | B Search User A | Không tìm thấy A | Chưa chạy | — | |
| 8 | A Search Post của B | Không thấy bài B; total không tính bài B | Chưa chạy | — | |
| 9 | A mở Feed For You | Không thấy bài B | Chưa chạy | — | |
| 10 | A mở Feed Following | Không thấy bài B | Chưa chạy | — | |
| 11 | B xem nội dung của A | Không thấy hồ sơ/bài của A | Chưa chạy | — | |
| 12 | A mở Post Detail của B | Bị từ chối mà không làm lộ nội dung | Chưa chạy | — | |
| 13 | A Like bài B | Bị từ chối | Chưa chạy | — | |
| 14 | A Unlike bài B | Bị từ chối | Chưa chạy | — | |
| 15 | A Comment hoặc Reply bài B | Bị từ chối | Chưa chạy | — | |
| 16 | A Save hoặc Unsave bài B | Bị từ chối | Chưa chạy | — | |
| 17 | A đọc comment thuộc bài B | Bị từ chối | Chưa chạy | — | |
| 18 | Kiểm tra Follow A↔B | Cả A→B và B→A đều không còn | Chưa chạy | — | |
| 19 | A mở Notification list | Notification cũ có actor B không hiển thị | Chưa chạy | — | |
| 20 | Kiểm tra unread count của A | Không tính Notification có actor B | Chưa chạy | — | |
| 21 | User C xem/tương tác A và B | Hoạt động bình thường, không bị ảnh hưởng | Chưa chạy | — | |

## Bảo toàn tương tác lịch sử

| # | Thao tác | Kết quả mong đợi | Kết quả thực tế | PASS/FAIL | Ảnh/Ghi chú |
|---:|---|---|---|---|---|
| H1 | Sau Block, kiểm tra Like cũ của A trên bài B bằng dữ liệu/API phù hợp | Like lịch sử vẫn tồn tại; `like_count` bài B không giảm | Chưa chạy | — | |
| H2 | Sau Block, B mở danh sách Comment trên bài do B sở hữu | B không còn thấy Comment lịch sử A đã tạo trước khi Block | Chưa chạy | — | |
| H3 | Comment cha của A có Reply từ C; B mở danh sách Comment/Reply | Comment cha A và toàn bộ nhánh Reply của cha đều bị ẩn, không có Reply mồ côi | Chưa chạy | — | |
| H4 | A thử đọc danh sách Comment của bài B | A bị từ chối vì không còn quyền truy cập bài B | Chưa chạy | — | |
| H5 | Sau Unblock, kiểm tra lại Like/Comment và bộ đếm của bài B | Like, Comment và các bộ đếm lịch sử vẫn giữ nguyên; Comment hợp lệ hiển thị lại | Chưa chạy | — | |
| H6 | Comment cha của C có Reply từ A; B xem trong thời gian Block | Comment cha C vẫn hiển thị nhưng Reply của A bị ẩn và reply count giảm tương ứng | Chưa chạy | — | |

## Danh sách Block và Unblock

| # | Thao tác | Kết quả mong đợi | Kết quả thực tế | PASS/FAIL | Ảnh/Ghi chú |
|---:|---|---|---|---|---|
| 22 | A vào Xem thêm → Tài khoản đã chặn | Trang Blocked Users mở | Chưa chạy | — | |
| 23 | Kiểm tra danh sách | B xuất hiện đúng một lần | Chưa chạy | — | |
| 24 | Kiểm tra dữ liệu B | Chỉ có display name/avatar/thời gian, không có email | Chưa chạy | — | |
| 25 | A chọn Unblock B | Modal Unblock mở và DELETE đúng endpoint | Chưa chạy | — | |
| 26 | A xác nhận Unblock | B bị loại khỏi danh sách và có toast | Chưa chạy | — | |
| 27 | Kiểm tra Follow A↔B | Không tự khôi phục Follow | Chưa chạy | — | |
| 28 | A và B Search User | Hai bên có thể tìm thấy nhau trở lại | Chưa chạy | — | |
| 29 | A xem nội dung PUBLIC của B | Nội dung xuất hiện lại theo quyền bình thường | Chưa chạy | — | |
| 30 | A mở Feed Following | Chưa có bài B vì A chưa Follow lại | Chưa chạy | — | |
| 31 | A Follow lại B | Follow thành công | Chưa chạy | — | |
| 32 | A tải lại Feed Following | Bài PUBLISHED của B xuất hiện | Chưa chạy | — | |

## Cách ghi nhận

- Điền chính xác kết quả thực tế.
- Chỉ đánh `PASS` khi đã thao tác trên UI và đối chiếu network/API cần thiết.
- Nếu `FAIL`, chụp màn hình, ghi HTTP status, response code và bước tái hiện.
- Không đổi trạng thái thành READY FOR RELEASE khi checklist còn mục chưa chạy.
