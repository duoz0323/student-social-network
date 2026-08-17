# Quy tắc dự án

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. File này tóm tắt các quy tắc thực thi; khi có khác biệt phải áp dụng README và cập nhật file này, không sửa README để hợp thức hóa implementation cũ.

## 1. Tài khoản

- Email duy nhất nếu có giá trị.
- Username chỉ được nhập trong onboarding, không thuộc request đăng ký và không thay `users.id` trong quan hệ nội bộ.
- Đăng ký bằng đúng một phương thức email: email.
- Form đăng ký chỉ gồm phương thức email, mật khẩu và xác nhận mật khẩu.
- Request đăng ký chỉ nhận một giá trị email tại một thời điểm.
- Nếu đăng ký bằng email thì `email` lưu `NULL`; nếu đăng ký bằng email thì `email` lưu `NULL`.
- Form đăng ký local chỉ tạo `pending_registrations`; chưa tạo `users`, `user_profiles` hoặc JWT.
- OTP email hợp lệ mới tạo `users` và `user_profiles` trong cùng transaction.
- Hệ thống hỗ trợ Google/Facebook và nhiều phương thức xác thực cùng ánh xạ về một `users.id`.
- Provider token chỉ dùng tại Auth endpoint và phải được Backend xác minh.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Liên kết provider lấy user đích từ JWT hiện tại; không được gỡ phương thức đăng nhập cuối cùng.
- `users.password_hash` được phép `NULL` với social-only account.
- Đăng nhập bằng email.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Trạng thái MVP: ACTIVE, BLOCKED.
- User `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.
- Role: USER, ADMIN.
- Email chỉ đăng nhập local được sau khi trường verified timestamp tương ứng có giá trị.

## 2. Hồ sơ

- Hồ sơ công khai.
- Email không hiển thị công khai.
- Chỉ chủ tài khoản cập nhật hồ sơ.
- Backend chỉ tạo `user_profiles` cùng `users` sau khi OTP hoặc social identity hợp lệ theo README.
- `user_profiles.username`, `user_profiles.display_name`, `user_profiles.date_of_birth` và `user_profiles.profile_completed_at` ban đầu phải `NULL` trong hồ sơ rỗng.
- Tên hiển thị thuộc `user_profiles`, không thuộc `users`.
- Username duy nhất, tên hiển thị và ngày sinh bắt buộc để hoàn tất hồ sơ ban đầu.
- Username dài 3–30 ký tự, chỉ gồm `a-z`, `0-9`, `_`, `.`, normalize lowercase và không lưu ký tự `@`.
- Người dùng phải đủ 18 tuổi tại ngày Backend xử lý onboarding hoặc cập nhật hồ sơ.
- Avatar và bio là tùy chọn; username, tên hiển thị và ngày sinh không được bỏ qua khi hoàn tất onboarding.
- Hồ sơ chỉ hoàn tất khi username duy nhất, tên hiển thị hợp lệ và ngày sinh hợp lệ của người dùng đủ 18 tuổi đã được lưu, người dùng xác nhận hoàn tất và Backend cập nhật `profile_completed_at`.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không đồng nghĩa hồ sơ đã hoàn tất.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép API xác thực cần thiết, Refresh Token, đăng xuất và onboarding.
- API mạng xã hội chính phải trả lỗi nghiệp vụ `PROFILE_NOT_COMPLETED` nếu hồ sơ chưa hoàn tất.

## 3. Follow

- Không Follow chính mình.
- Không Follow trùng.
- Follow có hiệu lực ngay.
- Không có Follow Request.

## 4. Bài viết

- Tối đa 500 ký tự.
- Tối đa 4 media, trong đó tối đa một video.
- Phải có nội dung hoặc media.
- Ảnh: JPG, JPEG, PNG, WEBP; video: MP4, WebM.
- Chỉ tác giả sửa/xóa.
- Trong 15 phút, tác giả có thể giữ/gỡ media cũ hoặc thêm ảnh/video mới.
- Trạng thái: PUBLISHED, HIDDEN, DELETED.

## 5. Like

- Một user Like một post một lần.
- Không Like bài HIDDEN/DELETED.
- Danh sách bài đã Like chỉ chủ tài khoản xem.

## 6. Comment

- Chỉ user đăng nhập được comment.
- Chỉ tác giả comment được xóa comment.
- Không comment bài HIDDEN/DELETED.
- Text Comment/Reply phải qua pipeline moderation dùng chung sau kiểm tra quyền và trước persist.

### AI-assisted Content Moderation V1

- Áp dụng text-only cho Post create, Post update khi text thay đổi, Comment và Reply.
- Backend quyết định `ALLOW/WARNING/BLOCK`; WARNING/BLOCK và provider unavailable đều không persist.
- Không giữ transaction database trong external inference call.
- Rejected content không tạo counter side effect, Notification/realtime, Report, Moderation Case hoặc penalty.
- Không tự khóa user và không thay đổi Block/Restrict/Report/Admin semantics hiện tại.
- Provider duy nhất là local FastAPI/PhoBERT; không được bổ sung lại adapter hoặc API key AI trả phí khi chưa có quyết định nghiệp vụ mới.
- Local label phải map cố định `CLEAN→ALLOW`, `OFFENSIVE→WARNING`, `HATE→BLOCK`; không thêm threshold phức tạp cho model local khi chưa có yêu cầu mới.
- Không log raw text inference. Nội dung vượt context model phải chunk theo token và lấy severity cao nhất, không truncation âm thầm.
- Scope V1 chỉ là toxic/offensive/hate speech tiếng Việt; không tuyên bố bao phủ media hoặc mọi chính sách an toàn.

## 7. Save

- Một user Save một post một lần.
- Saved list chỉ chủ tài khoản xem.

## 8. Feed

### Repost

- Một user Repost một Post tối đa một lần; PUT và DELETE đều idempotent.
- Không Repost bài của chính mình hoặc bài không còn `PUBLISHED`.
- `post_reposts` chỉ lưu quan hệ; `posts.repost_count` được cập nhật atomic bằng trigger.
- Profile Repost và Following Feed không dùng OFFSET hoặc COUNT tổng.

- Following: bài của người đang Follow, mới nhất trước.
- For You: Personalized V1 rule-based tại database từ recency, capped engagement, Follow, Academic/Interest ACTIVE, author history và exact hashtag-ID history; Academic/Interest không phải hard filter.
- Cursor For You phải giữ `rankingAt`, `score`, `publishedAt`, `postId`; không score Java, không OFFSET/COUNT tổng và không đưa Repost activity vào For You.
- Không hiển thị HIDDEN/DELETED.
- Feed và các danh sách bài dùng Infinite Scroll phải dùng Cursor Pagination theo API contract.

## 9. Search

- MySQL.
- Tìm user theo tên hiển thị.
- Không tìm user theo username.
- Có phân trang.
- Không hiển thị user BLOCKED.
- Không hiển thị post HIDDEN/DELETED.

## 10. Report

- Report hỗ trợ Post và trang cá nhân; hai loại dùng bảng và workflow riêng.
- Trạng thái PENDING, RESOLVED, REJECTED.
- Mỗi Report giữ riêng reporter, reason, description và snapshot; không gộp chuỗi/JSON thay quan hệ.
- Report phải thuộc Moderation Case; không có nhiều report đang hiệu lực cùng user và post.
- Một post chỉ có một Moderation Case `OPEN`; case dùng `OPEN`, `RESOLVED_NO_VIOLATION`, `RESOLVED_ACTION_TAKEN`.
- Report không tự động ẩn post.
- Mỗi transition case bài viết `OPEN → RESOLVED_ACTION_TAKEN` thành công tính một strike; strike 1 gửi warning `1/3`, strike 2 gửi final warning `2/3`, strike 3 tự động khóa USER và thu hồi phiên.
- Account Standing chỉ đếm Moderation Case bài viết `RESOLVED_ACTION_TAKEN`; không đếm Report, `report_count`, Profile Report hoặc Notification và không reset khi mở khóa.
- Auth tài khoản bị khóa trả `ACCOUNT_BLOCKED` cùng dữ liệu public-safe; không lộ reporter, Admin hoặc internal note.
- Profile Report không cho tự báo cáo, không trùng `PENDING` theo reporter/target; tất cả lượt cùng target thuộc một Profile Report Case. Admin được chọn khóa USER ngay khi xác nhận vi phạm.

## 11. Admin

- Admin Academic V1 chỉ hỗ trợ list/search/create/update và `ACTIVE`/`INACTIVE` cho School, Faculty, Major, Interest Category; không hard delete.
- Mutation lấy actor từ JWT, không nhận `adminId`, và phải ghi `AdminAction` với target Academic.
- Parent inactive không cascade status child nhưng public Academic API phải lọc toàn bộ ancestor; reference Academic/Interest của hồ sơ cũ được bảo toàn.

- Chỉ ADMIN truy cập API quản trị.
- Chỉ ADMIN đang hoạt động được cập nhật hoặc xóa avatar của tài khoản role USER; file dùng cùng validation với avatar cá nhân.
- Có thể khóa/mở user.
- Có thể ẩn/khôi phục post.
- Quản lý hashtag dành cho ADMIN có tìm kiếm, phân trang, tạo, đổi tên và xóa; tên mới dùng pipeline chuẩn hóa chung và không được trùng `normalized_name`.
- Đổi tên cập nhật chính bản ghi `hashtags`, giữ nguyên `id`, `post_count` và toàn bộ quan hệ `post_hashtags`.
- Xóa hashtag phải khóa target, xóa quan hệ `post_hashtags` trước vì FK `RESTRICT`, rồi xóa `hashtags` trong cùng transaction; không xóa Post.
- Xử lý trực tiếp Moderation Case từ `OPEN` sang một kết quả cuối; không có bước tiếp nhận.

## 12. API

- Feed For You, Feed Following, bài trên hồ sơ, bài đã lưu và bài đã thích dùng Cursor Pagination.
- Search, bình luận, Follow và Admin dùng `PageResponse`.
- HTTP status đúng.
- Không trả Entity.
- Không trả dữ liệu nhạy cảm.
- Không trả stack trace.

## 13. Cấu trúc enum Backend

- Mỗi module Backend có entity sử dụng status, role, type, reason hoặc nhóm giá trị cố định phải đặt enum trong thư mục `enums/` của chính module đó.
- Không gom enum nghiệp vụ của nhiều module vào một package chung nếu enum chỉ thuộc về một module.
- `UserStatus` và `UserRole` thuộc `user/enums`.
- `PostStatus` thuộc `post/enums`.
- `CommentStatus` thuộc `interaction/enums`.
- `ReportStatus` và `ReportReason` thuộc `report/enums`.
- Enum dùng chung thật sự cho nhiều module chỉ được đặt trong `common/enums` khi có lý do rõ ràng.

