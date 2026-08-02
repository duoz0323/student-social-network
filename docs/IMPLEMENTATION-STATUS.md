# Trạng thái triển khai hiện tại

> Cập nhật ngày 01/08/2026. `README.md` vẫn là nguồn sự thật cao nhất về phạm vi và nghiệp vụ.
> File này chỉ ghi nhận mức độ triển khai thực tế để chuẩn bị kế hoạch phát triển tiếp theo.

## Quy ước trạng thái

- `IMPLEMENTED`: đã có source code tương ứng.
- `TESTED`: đã có kiểm thử tự động tương ứng trong repository.
- `INTEGRATED`: Frontend đã gọi API Backend trong luồng hiện tại.
- `DESIGNED`: nghiệp vụ và contract đã được chốt trong tài liệu nhưng production code, database hoặc test chưa được triển khai.
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
| CRUD bài viết, media và một hashtag | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Tạo/sửa hỗ trợ ảnh và video; form tạo/sửa dùng chung gợi ý hashtag. Schema bảng nối chưa tự enforce tối đa một hashtag; Service đang enforce. |
| Gắn, thay đổi và gỡ Location tùy chọn trên Post | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Create/update hỗ trợ Location tùy chọn, resolve dùng chung theo Place ID, mọi Post response và Admin detail trả Location; Frontend dùng Google Places picker. MySQL integration test cần database test riêng. |
| Like/Unlike và danh sách bài đã thích | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách `/liked` dùng Cursor Pagination. |
| Bình luận và reply một cấp | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Reply một cấp đã có endpoint dù README xếp ưu tiên P2. |
| Save/Unsave và danh sách bài đã lưu | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách dùng Cursor Pagination. |
| Repost/Unrepost, Profile Repost và Following activity | IMPLEMENTED | IMPLEMENTED | TESTED | PARTIAL | Luồng chính đã tích hợp; MySQL concurrency test cần `REPOST_TEST_DB_URL`, còn Block/Restrict phụ thuộc nhánh riêng chưa có trong worktree này. |
| Feed For You/Following | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Cursor Pagination và Infinite Scroll. |
| Bài viết trên hồ sơ | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | `GET /api/v1/users/{userId}/posts` dùng Cursor Pagination. |
| Tìm kiếm user/post/hashtag | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Tiếp tục dùng `PageResponse`. |
| Báo cáo bài viết và Moderation Case | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Report độc lập; một case OPEN mỗi Post; chống trùng theo reporter/Post/case OPEN. |
| Admin user/post/moderation/action history | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Admin xem một dòng mỗi case, chi tiết mọi Report và giải quyết trực tiếp có phân quyền/locking. |
| Thông báo đơn giản | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Có danh sách, unread count, read/read-all và delete. |

## Thay đổi đang có trong worktree

- Sáu danh sách bài viết/activity dùng Cursor Pagination: Feed For You, Feed Following, bài trên
  hồ sơ, tab Repost, bài đã lưu và bài đã thích.
- Frontend có component dùng chung cho Infinite Scroll, lazy route và trang `/liked`.
- SQL bổ sung index phục vụ danh sách bài đã thích và thêm dữ liệu demo.
- README, API contract, PRD, Architecture, Project Rules, Data Flow và tài liệu UI đã được đối
  chiếu lại với thay đổi trên.
- Location P1 đã hoàn tất từ schema/JPA đến API create/update, batch enrichment response, Admin detail và Frontend Google Places picker. Backend không gọi Google Places để xác minh và không triển khai Discovery Map.

## Điểm chưa đồng bộ hoặc cần xác nhận

1. SQL baseline và DBML đã đồng bộ schema Location. Migration `database/migrations/V001__add_post_locations.sql` là file chạy thủ công vì dự án chưa dùng Flyway/Liquibase; chưa được áp dụng tự động lên local hoặc Aiven.
2. `post_hashtags` vẫn là bảng nối nhiều-nhiều ở mức schema; giới hạn một hashtag hiện do Service
   kiểm soát, chưa có constraint database bảo đảm tuyệt đối.
3. Full Backend suite đã chạy 610 test, pass 563 và skip 47 integration test theo biến môi trường; Frontend pass 15 test cho multipart Location/media, draft edit, validation media, countdown sửa Post và Google Maps URL. MySQL/Google integration thật tiếp tục phụ thuộc cấu hình môi trường riêng.
4. Frontend chưa có framework component test cho tương tác DOM của picker hashtag, Location và trình sửa media.
5. Cần cấu hình `VITE_GOOGLE_MAPS_API_KEY` theo từng môi trường và giới hạn key theo HTTP referrer/API trong Google Cloud Console trước khi kiểm thử Places thật.

## Thứ tự đề xuất trước chức năng tiếp theo

1. Kiểm tra và áp dụng migration Location trên MySQL test/local trước, sau đó chỉ chuyển sang Giai đoạn 3 khi schema và JPA mapping đã được duyệt.
2. Hoàn tất quality gate cho đợt Cursor Pagination: Backend full test, Frontend lint/build và kiểm
   thử thủ công Infinite Scroll với dữ liệu trùng timestamp.
3. Sau khi các mục trên ổn định, chọn chức năng tiếp theo từ phần chưa hoàn thiện về UX hoặc ngoài
   phạm vi MVP; không mở rộng nghiệp vụ trước khi cập nhật README.
