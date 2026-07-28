# Trạng thái triển khai hiện tại

> Cập nhật ngày 28/07/2026. `README.md` vẫn là nguồn sự thật cao nhất về phạm vi và nghiệp vụ.
> File này chỉ ghi nhận mức độ triển khai thực tế để chuẩn bị kế hoạch phát triển tiếp theo.

## Quy ước trạng thái

- `IMPLEMENTED`: đã có source code tương ứng.
- `TESTED`: đã có kiểm thử tự động tương ứng trong repository.
- `INTEGRATED`: Frontend đã gọi API Backend trong luồng hiện tại.
- `PARTIAL`: mới hoàn thành một phần hoặc còn phụ thuộc cấu hình/môi trường.
- `PLANNED`: có trong phạm vi nhưng chưa thấy implementation đầy đủ.

## Ma trận chức năng

| Nhóm chức năng | Backend | Frontend | Test | Trạng thái tổng hợp | Ghi chú |
|---|---|---|---|---|---|
| Đăng ký local, OTP, phục hồi pending | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Có lifecycle, resend, cancel/status và transaction tạo user/profile. |
| Đăng nhập local, JWT, Refresh Token, Logout | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Refresh Token lưu hash và có luồng thu hồi. |
| Google/Facebook Auth và xử lý conflict | IMPLEMENTED | IMPLEMENTED | TESTED | PARTIAL | Kiểm thử credential thật còn phụ thuộc cấu hình provider. |
| Quản lý/link/unlink phương thức đăng nhập | IMPLEMENTED | IMPLEMENTED | TESTED | PARTIAL | Backend đầy đủ; một số màn hình vẫn không có ảnh thiết kế riêng. |
| Password Recovery bằng OTP | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Có decoy challenge, verify/resend/complete và thu hồi Refresh Token. |
| Onboarding, hồ sơ và avatar | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Bắt buộc displayName, dateOfBirth và đủ 18 tuổi. |
| Follow/Unfollow và danh sách follow | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách vẫn dùng `PageResponse`. |
| Restrict/Unrestrict và danh sách đã hạn chế | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Restrict một chiều; suppress Like/Comment/Reply trước khi lưu Notification, không ảnh hưởng tương tác. |
| CRUD bài viết, ảnh và một hashtag | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Schema bảng nối chưa tự enforce tối đa một hashtag; Service đang enforce. |
| Like/Unlike và danh sách bài đã thích | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách `/liked` dùng Cursor Pagination. |
| Bình luận và reply một cấp | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Reply một cấp đã có endpoint dù README xếp ưu tiên P2. |
| Save/Unsave và danh sách bài đã lưu | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách dùng Cursor Pagination. |
| Feed For You/Following | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Cursor Pagination và Infinite Scroll. |
| Bài viết trên hồ sơ | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | `GET /api/v1/users/{userId}/posts` dùng Cursor Pagination. |
| Tìm kiếm user/post/hashtag | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Tiếp tục dùng `PageResponse`. |
| Báo cáo bài viết | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Chống trùng report `PENDING`. |
| Admin user/post/report/action history | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Có phân quyền và test security/controller/repository/service. |
| Thông báo đơn giản | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Có danh sách, unread count, read/read-all và delete. |

## Thay đổi đang có trong worktree

- Năm danh sách bài viết đã chuyển sang Cursor Pagination: Feed For You, Feed Following, bài trên
  hồ sơ, bài đã lưu và bài đã thích.
- Frontend có component dùng chung cho Infinite Scroll, lazy route và trang `/liked`.
- SQL bổ sung index phục vụ danh sách bài đã thích và thêm dữ liệu demo.
- README, API contract, PRD, Architecture, Project Rules, Data Flow và tài liệu UI đã được đối
  chiếu lại với thay đổi trên.
- README, SQL tổng và DBML đã bổ sung `user_restrictions`; Backend/Frontend đã tích hợp API Restrict,
  danh sách cài đặt và quan hệ `restrictedByMe`.

## Điểm chưa đồng bộ hoặc cần xác nhận

1. `post_hashtags` vẫn là bảng nối nhiều-nhiều ở mức schema; giới hạn một hashtag hiện do Service
   kiểm soát, chưa có constraint database bảo đảm tuyệt đối.
2. SQL demo có media `VIDEO`, trong khi README MVP chỉ quy định tối đa bốn ảnh và xếp video ngoài
   phạm vi. Cần quyết định bỏ demo video hoặc chính thức thay đổi phạm vi trước khi phát triển tiếp.
3. Frontend hiện chỉ có Node test cho utility, chưa có component test runner để tự động hóa đầy đủ
   modal/menu Restrict; lint và production build đã pass.

## Thứ tự đề xuất trước chức năng tiếp theo

1. Chốt quyết định về video trong Post và đồng bộ SQL/source/tài liệu theo một hướng duy nhất.
2. Khôi phục hoặc tạo DBML từ SQL hiện hành, sau đó thêm kiểm tra schema contract.
3. Hoàn tất quality gate cho đợt Cursor Pagination: Backend full test, Frontend lint/build và kiểm
   thử thủ công Infinite Scroll với dữ liệu trùng timestamp.
4. Sau khi ba mục trên ổn định, chọn chức năng tiếp theo từ phần chưa hoàn thiện về UX hoặc ngoài
   phạm vi MVP; không mở rộng nghiệp vụ trước khi cập nhật README.
