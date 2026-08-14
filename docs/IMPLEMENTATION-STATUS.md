# Trạng thái triển khai hiện tại

> Cập nhật ngày 11/08/2026. `README.md` vẫn là nguồn sự thật cao nhất về phạm vi và nghiệp vụ.
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
| Academic Profile & Interests foundation | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Schema/master/profile API, onboarding wizard, Edit/Profile Display đã có và đã manual test end-to-end thành công. |
| Student Recommendation V1 (rule-based) | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | “Có thể bạn biết” dùng Academic ACTIVE, Interest ACTIVE và common-follow; automated test và manual E2E đã thành công. |
| Admin Academic Management V1 | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | School/Faculty/Major/Interest có list/search/create/update/status, active hierarchy public, audit log và UI `/admin/academic`; không hard delete. |
| Follow/Unfollow và danh sách follow | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách vẫn dùng `PageResponse`. |
| Block/Unblock và danh sách đã chặn | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Block hai chiều ở lớp truy cập; đồng bộ Feed, Search, Profile, Comment và Messaging. |
| Restrict/Unrestrict và danh sách đã hạn chế | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Restrict một chiều; suppress Like/Comment/Reply trước khi lưu Notification, không ảnh hưởng tương tác. |
| CRUD bài viết, media và một hashtag | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Tạo/sửa hỗ trợ ảnh và video; form tạo/sửa dùng chung gợi ý hashtag. Schema bảng nối chưa tự enforce tối đa một hashtag; Service đang enforce. |
| Gắn, thay đổi và gỡ Location tùy chọn trên Post | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Create/update hỗ trợ Location tùy chọn, resolve dùng chung theo Place ID, mọi Post response và Admin detail trả Location; Frontend dùng Google Places picker. MySQL integration test cần database test riêng. |
| Like/Unlike và danh sách bài đã thích | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách `/liked` dùng Cursor Pagination. |
| Bình luận và reply một cấp | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Reply một cấp đã có endpoint dù README xếp ưu tiên P2. |
| Save/Unsave và danh sách bài đã lưu | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Danh sách dùng Cursor Pagination. |
| Repost/Unrepost, Profile Repost và Following activity | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Luồng chính đã tích hợp cùng Block/Restrict; MySQL concurrency test cần `REPOST_TEST_DB_URL`. |
| Personalized For You V1 (rule-based) | IMPLEMENTED | IMPLEMENTED | TESTED | TESTED | Database-side ranking từ recency/engagement/Follow/Academic/Interest/history/exact hashtag ID; cursor freeze `rankingAt`. Chờ manual E2E để nâng `INTEGRATED`. |
| Location-aware Discovery V1 – Nearby | IMPLEMENTED | IMPLEMENTED | TESTED | TESTED | Tab `Gần bạn`, Geolocation chủ động, radius 1/3/5/10/20, cursor Infinite Scroll và chống stale request đã có; chờ manual E2E MySQL/Geolocation để nâng `INTEGRATED`. |
| Feed Following | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | ORIGINAL/REPOST activity và cursor riêng không thay đổi bởi Personalized For You V1. |
| Bài viết trên hồ sơ | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | `GET /api/v1/users/{userId}/posts` dùng Cursor Pagination. |
| Tìm kiếm user/post/hashtag | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Tiếp tục dùng `PageResponse`. |
| Báo cáo bài viết và Moderation Case | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Report độc lập; một case OPEN mỗi Post; chống trùng theo reporter/Post/case OPEN. |
| Admin user/post/moderation/action history | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Admin xem một dòng mỗi case, chi tiết mọi Report và giải quyết trực tiếp có phân quyền/locking. |
| Admin cập nhật hồ sơ người dùng | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Cập nhật hồ sơ qua API quản trị và ghi nhận action history. |
| Analytics mức độ hoạt động người dùng | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Tracking activity theo ngày và tổng hợp báo cáo tháng cho Admin; integration test MySQL phụ thuộc cấu hình test DB. |
| Thông báo REST và realtime Giai đoạn 1 | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | REST/MySQL là nguồn sự thật; STOMP native phát `NOTIFICATION_CREATED` after-commit, có reconcile và polling fallback. |
| Nhắn tin trực tiếp Giai đoạn 1C | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Database/REST + after-commit realtime + responsive UI text đã có; E2E hai browser chưa chạy. |
| Ảnh trong Messaging một-một | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Multipart/private media/durable cleanup, UI gửi/hiển thị ảnh và signed access URL đã tích hợp. |
| Typing Indicator Messaging Giai đoạn 1D | IMPLEMENTED | IMPLEMENTED | TESTED | INTEGRATED | Ephemeral STOMP SEND exact allowlist, recipient-only events và UI timeout đã có; không DB, MySQL test hay E2E hai browser. |

## Thay đổi đang có trong worktree

- Sáu danh sách bài viết/activity dùng Cursor Pagination: Feed For You, Feed Following, bài trên
  hồ sơ, tab Repost, bài đã lưu và bài đã thích.
- Frontend có component dùng chung cho Infinite Scroll, lazy route và trang `/liked`.
- SQL bổ sung index phục vụ danh sách bài đã thích và thêm dữ liệu demo.
- README, API contract, PRD, Architecture, Project Rules, Data Flow và tài liệu UI đã được đối
  chiếu lại với thay đổi trên.
- README, SQL tổng và DBML đã bổ sung `user_restrictions`; Backend/Frontend đã tích hợp API Restrict,
  danh sách cài đặt và quan hệ `restrictedByMe`.
- Notification đã có `/ws`, JWT STOMP `CONNECT`, user destination riêng, phát sự kiện after-commit,
  Context dùng chung, badge desktop/mobile, reconnect/reconcile và polling fallback.
- Messaging có migration, REST cursor/idempotency/read, `MESSAGE_CREATED`/`MESSAGES_READ` after-commit,
  Inbox/detail/badge/optimistic UI và reconciliation trên connection STOMP dùng chung.
- Messaging ảnh hỗ trợ tối đa 5 ảnh private, multipart cùng path, metadata attachment, signed access URL,
  fingerprint idempotency, durable cleanup và UI gửi/hiển thị ảnh.
- Typing Indicator dùng cùng connection STOMP, chỉ allow `/app/messaging/typing`, kiểm tra membership/account/Block,
  giới hạn 4 frame/user/giây trong từng instance và hiển thị có expiry 5 giây; không lưu DB hay thay đổi unread.
- Location P1 đã hoàn tất từ schema/JPA đến API create/update, batch enrichment response, Admin detail và Frontend Google Places picker. Backend không gọi Google Places để xác minh và không triển khai Discovery Map.
- Repost đã tích hợp với Profile, Following Feed, Notification realtime và các bộ lọc Block/Restrict.
- Moderation Case, chỉnh sửa hồ sơ người dùng bởi Admin và analytics hoạt động người dùng đã tích hợp xuyên suốt Backend, schema và Admin UI.
- Academic Profile & Interests đã có SQL/DBML/JPA, master API autocomplete, profile transaction, seed demo, onboarding wizard, Edit/Profile Display, test tự động và manual E2E thành công.
- Admin Academic Management V1 đã tích hợp Backend/API, audit log và Admin UI cho School/Faculty/Major/Interest; không bao gồm hard delete, cấu hình Recommendation, School Suggestion hoặc AI/ML.
- Student Recommendation V1 đã có API rule-based, database-side ranking/pagination, UI “Có thể bạn biết”, test tự động và manual E2E thành công; trạng thái `INTEGRATED`.
- Personalized For You V1 đã triển khai/test bằng MySQL 8 theo deterministic rule-based score; không tạo bảng/index/cache/job mới, không thay public API và chưa manual E2E nên chưa đánh dấu `INTEGRATED`.
- Location-aware Discovery V1 đã có Backend query và Frontend tab `Gần bạn`; canonical seed có 6 Post tại 5 địa điểm công cộng quanh Bình Trị Đông để manual test radius. Automated test, lint và build đạt, chưa manual E2E với quyền Geolocation/MySQL thực nên chưa `INTEGRATED`.
- AI/ML Feed Recommendation: `NOT IMPLEMENTED`.

## Điểm chưa đồng bộ hoặc cần xác nhận

1. SQL canonical và DBML đã đồng bộ schema Location cùng Academic Profile. Repo demo chỉ giữ full rebuild; không có migration tự động và không tự áp dụng thay đổi lên local hoặc database thật.
2. `post_hashtags` vẫn là bảng nối nhiều-nhiều ở mức schema; giới hạn một hashtag hiện do Service
   kiểm soát, chưa có constraint database bảo đảm tuyệt đối.
3. Source và SQL demo có media `VIDEO`, trong khi README MVP chỉ quy định tối đa bốn ảnh và xếp video ngoài
   phạm vi. Cần quyết định bỏ demo video hoặc chính thức thay đổi phạm vi trước khi phát triển tiếp.
4. Frontend chưa có framework component test để tự động hóa đầy đủ Context, onboarding Academic autocomplete/chips, badge, modal Restrict, picker hashtag/Location và trình sửa media; Node test, lint và production build đã pass.
5. Các test tích hợp MySQL/Google phụ thuộc biến môi trường và database test riêng. Kết nối WebSocket với hai phiên người dùng thật vẫn cần smoke test thủ công ở môi trường tích hợp.
6. Schema Messaging text + ảnh và 7 test MySQL 8.4 cho concurrent open/text retry/image retry,
   read, constraint và race Block-send/Block-open đã chạy thành công trên container test tạm.
7. Cần cấu hình `VITE_GOOGLE_MAPS_API_KEY` theo từng môi trường và giới hạn key theo HTTP referrer/API trong Google Cloud Console trước khi kiểm thử Places thật.

## Kết quả quality gate ngày 10/08/2026

- Backend: `831` test, `0` failure, `0` error, `63` test tích hợp được skip do thiếu biến môi trường/database test; `mvn test` và `mvn clean package` đều pass.
- Frontend: `129` test pass; ESLint pass; production build pass.
- Kiểm tra merge: không còn conflict marker và `git diff --check` không phát hiện lỗi whitespace.

## Thứ tự đề xuất trước chức năng tiếp theo

1. Kiểm tra và áp dụng migration Location trên MySQL test/local trước, sau đó chỉ chuyển sang Giai đoạn 3 khi schema và JPA mapping đã được duyệt.
2. Hoàn tất quality gate cho đợt Cursor Pagination: Backend full test, Frontend lint/build và kiểm
   thử thủ công Infinite Scroll với dữ liệu trùng timestamp.
3. Sau khi các mục trên ổn định, chọn chức năng tiếp theo từ phần chưa hoàn thiện về UX hoặc ngoài
   phạm vi MVP; không mở rộng nghiệp vụ trước khi cập nhật README.
