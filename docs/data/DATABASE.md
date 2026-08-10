# Tài liệu Database MVP

`README.md` là nguồn sự thật cao nhất. Schema vật lý được mô tả tại `database/student_social_network.sql`; DBML tương ứng nằm tại `database/student_social_network.dbml`. SQL và DBML phải được cập nhật đồng thời.

## 0. Messaging trực tiếp và ảnh

- `conversations`: cặp participant low/high duy nhất, last message nullable và index
  `(last_message_at DESC, id DESC)` cho Inbox.
- `conversation_members`: khóa chính `(conversation_id, user_id)`, marker đọc nullable và index theo user.
- `messages`: `TEXT|IMAGE`, content nullable cho message chỉ có ảnh, fingerprint SHA-256 canonical,
  UUID client dùng collation `ascii_bin`, unique `(sender_id, client_message_id)` và index
  `(conversation_id, id DESC)`.
- `message_attachments`: quan hệ N-1 tới message, chỉ `IMAGE`/`CLOUDINARY`, lưu public ID nội bộ,
  MIME, size, dimensions và thứ tự 0..4; unique `(message_id, display_order)` và
  `(storage_provider, storage_public_id)`.
- `media_cleanup_tasks`: hàng đợi bền vững `PENDING|PROCESSING|COMPLETED|FAILED`, lưu lần thử và thời
  điểm retry cho file upload đã thành rác; scheduler khóa/xử lý từng task trong transaction riêng.
- Composite FK bảo đảm sender là member. Service bảo đảm hai member khớp participant và các marker
  last/read thuộc đúng conversation.
- Schema Messaging hiện hành nằm trực tiếp trong SQL canonical và DBML; dự án không tự chạy migration khi Backend khởi động.

## Repost

- `post_reposts` có composite primary key `(user_id, post_id)` để chống trùng tại database.
- Quan hệ chỉ lưu bài gốc và `created_at`, không sao chép content, media, hashtag hoặc Location.
- Trigger insert/delete cập nhật atomic `posts.repost_count`; trigger delete dùng `GREATEST(..., 0)`.
- Index `(user_id, created_at DESC, post_id DESC)` phục vụ Profile Repost keyset.

## Hashtag và danh sách quản trị

- Không bổ sung bảng hoặc cột nghiệp vụ hashtag; `V009` thêm audit tạo/xóa và target `HASHTAG`, còn `V010__add_admin_hashtag_update_action.sql` thêm `UPDATE_HASHTAG` mà không sửa migration cũ.
- `hashtags.post_count` là bộ đếm số quan hệ hiện tại và được trigger insert/delete trên `post_hashtags` duy trì atomic.
- Ngày sử dụng mới nhất được đọc bằng `MAX(post_hashtags.created_at)`; hashtag không còn quan hệ có giá trị `NULL`.
- API quản trị dùng projection tổng hợp và phân trang tại database, không tải collection bài viết nên không phát sinh N+1.
- Do FK `post_hashtags.hashtag_id` dùng `ON DELETE RESTRICT`, service phải xóa quan hệ trước hashtag trong cùng transaction. Trigger delete hiện có giảm `hashtags.post_count`; Post không bị xóa.
- Đổi tên chỉ cập nhật `hashtags.normalized_name` và `display_name`; khóa chính cùng quan hệ bài viết được giữ nguyên.

## 1. Trạng thái thiết kế Auth 0E

Auth dùng bốn bảng challenge riêng:

- `pending_registrations`.
- `auth_method_link_challenges`.
- `social_auth_challenges`.
- `reauthentication_challenges`.

Không tạo bảng `auth_challenges` tổng quát. Các type/status Auth trong database dùng `VARCHAR + CHECK`; source Java dự kiến dùng enum với `EnumType.STRING`.

## 2. Users và phương thức đăng nhập

### users

- `email`, `email` và `password_hash` đều có thể `NULL`.
- Social-only user có thể không có email; không tạo placeholder.
- Đã bỏ `chk_users_contact_required`.
- `password_hash` chỉ được phép khác `NULL` khi có email verified hoặc email verified.
- `email_verified_at` chỉ có giá trị khi `email` khác `NULL`.

Invariant cuối cùng:

```text
Local method hợp lệ
= password_hash tồn tại
  và có email verified hoặc email verified

HOẶC

Social method hợp lệ
= có user_auth_providers hợp lệ
```

Invariant liên bảng được bảo vệ bằng transaction, unique constraint, unlink guard, MySQL integration test và audit query; không thể cưỡng chế hoàn toàn bằng CHECK trên `users`.

### user_auth_providers

- Provider dùng `VARCHAR(16)` với CHECK `GOOGLE`/`FACEBOOK`.
- Unique `(provider, provider_user_id)`.
- Unique `(user_id, provider)`.
- `provider_email` và `provider_email_verified` cùng `NULL` khi provider không trả email.
- Không lưu raw provider token.

## 3. Pending registration

- Active key có dạng `registration_type + ':' + identifier_normalized`.
- Unique nullable active key bảo đảm chỉ một pending active cho mỗi type/email; MySQL cho phép nhiều `NULL` trong unique index.
- OTP có hiệu lực 10 phút; pending 24 giờ; resend cooldown 60 giây.
- Resume rotate flow token, trả `resumed=true`, không thay `password_hash` và không tự resend trong cooldown.
- Muốn đổi mật khẩu phải cancel pending rồi tạo registration mới.
- Resend tăng `otp_version`, đặt `delivery_status=PENDING`, xóa failure code, reset attempts, giữ nguyên flow token và không gia hạn pending.
- Verify dùng pessimistic lock; deadlock retry tối đa một lần và không retry lỗi nghiệp vụ.

Khi terminal:

- `COMPLETED`: giữ `registration_type`, `identifier_normalized`, `completed_user_id`, `status`, `terminal_at` và HMAC flow lookup hash tối đa 7 ngày; xóa password/OTP, active key và delivery failure code ngay.
- `CANCELLED`/`EXPIRED`: xóa email, password/OTP và active key ngay; giữ HMAC flow lookup hash tối đa 7 ngày để status và idempotency.
- Raw flow token không được lưu. Cleanup/anonymization sau retention được phép đặt `flow_token_hash=NULL`.
- `completed_user_id` dùng `ON DELETE SET NULL`.
- Quan hệ giữa `completed_user_id` và `status` không nằm trong CHECK constraint để tương thích MySQL; Service và Integration Test phải bảo đảm giá trị này chỉ được gán theo đúng vòng đời hoàn tất đăng ký.

## 4. Link challenge

- Chỉ dùng cho `LINK_EMAIL` và `LINK_EMAIL`; không tái sử dụng pending registration.
- User đích lấy từ JWT hiện tại.
- TTL 15 phút; OTP 10 phút; resend cooldown 60 giây.
- Resend không gia hạn challenge.
- Unique active key theo `purpose:email` và `userId:purpose`.
- Terminal state xóa email, OTP/flow hash, active keys và failure code ngay.

## 5. Social challenge

- TTL 5 phút; conflict token opaque chỉ lưu SHA-256 hash.
- Provider credential được xác minh ngoài transaction và không lưu vào database.
- Trong lúc PENDING, lưu provider identity đã xác minh để resolve challenge.
- Lưu `provider_identity_fingerprint` bằng HMAC với secret riêng để audit mà không giữ raw provider user ID sau terminal.
- Fingerprint secret không dùng chung với OTP HMAC secret hoặc JWT secret.
- Khi terminal, xóa conflict token hash, raw provider user ID/email và active provider key; giữ fingerprint tối đa 7 ngày.
- `ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER` không tự link. Chỉ hướng dẫn login existing account hoặc account recovery.

## 6. Reauthentication challenge

- TTL 5 phút.
- Một user chỉ có một challenge `ACTIVE` cho cùng `scope`.
- Active key có dạng `userId:scope`; challenge vẫn bind `target_auth_method`.
- Challenge mới cùng user/scope phải hủy challenge active cũ.
- Bất kỳ auth method hợp lệ nào của chính user có thể làm proof; `proof_method` không cần trùng target.
- Token chỉ consume khi thao tác nhạy cảm thành công.
- Khi terminal, xóa token hash và active key ngay.

## 7. Hash và secret

- Password: BCrypt.
- OTP: HMAC-SHA-256 với secret riêng lấy từ environment.
- Registration flow token: HMAC-SHA-256 với secret riêng; các opaque token khác theo contract của challenge tương ứng.
- Provider identity fingerprint: HMAC với secret riêng.
- Không dùng chung các secret trên với JWT Access/Refresh secret.
- Không log password, OTP, flow/conflict/reauth token, recipient đầy đủ hoặc provider payload.

## 8. Delivery OTP trong MVP

```text
Commit challenge
→ gửi email ngoài transaction
→ transaction ngắn cập nhật SENT/FAILED/UNKNOWN
```

- Kết quả thất bại rõ ràng: `FAILED`, cho phép resend sớm bằng cách đưa `resend_available_at` về thời điểm hiện tại; application cache vẫn áp dụng rate limit.
- Kết quả không chắc chắn/timeout: `UNKNOWN`, giữ cooldown 60 giây.
- Update delivery phải kèm `otp_version` để response của OTP cũ không ghi đè trạng thái OTP mới.
- Không dùng outbox trong MVP; outbox mã hóa là hướng P1.

## 9. Rate limit

Challenge lưu attempts, cooldown, resend count, delivery count, OTP version và expiry. Rate limit theo cửa sổ giờ/IP/email/user/provider nằm ở application cache với key đã HMAC, không lưu raw PII.

Redis/distributed rate limit, adaptive blocking và abuse telemetry là P1.

## 10. Cleanup

- Mọi terminal transition phải atomic với việc vô hiệu OTP/password secret, giải phóng active key và đặt `terminal_at`; registration flow lookup hash được giữ tối đa 7 ngày.
- Tất cả terminal rows bị xóa cứng tối đa 7 ngày sau `terminal_at`.
- Scheduler chạy batch ổn định theo `(status, terminal_at, id)`; có thể dùng `FOR UPDATE SKIP LOCKED`.
- Scheduler không xóa `users`, `user_profiles` hoặc `user_auth_providers`.

## 11. Rebuild dev/demo

### Academic Profile & Interests

- SQL canonical tạo `schools`, `faculties`, `majors`, `interest_categories`, `user_interests` và bốn cột nullable trên `user_profiles`.
- `faculties.school_id` và `majors.faculty_id` tạo hierarchy chuẩn hóa; Service kiểm tra lại toàn bộ hierarchy và trạng thái `ACTIVE` trước khi lưu profile.
- `user_interests` dùng primary key `(user_id, interest_id)` và index đảo `(interest_id, user_id)` để chống trùng và chuẩn bị query theo sở thích ở phase sau.
- `school_id`, `faculty_id`, `major_id` có foreign key/index riêng; `entry_year` có check tĩnh `1900..9999`, còn giới hạn không vượt năm hiện tại được Service kiểm tra bằng UTC `Clock`.
- Academic fields không tham gia `chk_user_profiles_completion_consistency`, do đó không làm thay đổi username hoặc `profile_completed_at`.
- Master data trong SQL canonical có STU, một số trường TP.HCM và 16 interest categories; chỉ dùng development/demo, không phải danh mục chính thức toàn quốc.

Chỉ rebuild khi đã xác nhận database đích là local/test, đã xem thống kê dữ liệu hiện có và đã tạo backup khi cần bảo tồn.

File `database/student_social_network.sql` tự drop và tạo lại database `student_social_network`, nạp schema, trigger và dữ liệu demo tối thiểu. Sau đó có thể chạy `database/seeds/seed_1000_website_cases.sql` để thay dữ liệu tối thiểu bằng đúng 1.000 users, 1.000 posts có ảnh cùng các quan hệ phục vụ kiểm thử website. Khi sử dụng phải đặt `BOOTSTRAP_ADMIN_ENABLED=false`.

```bash
mysql --default-character-set=utf8mb4 -u root -p < database/student_social_network.sql
mysql --default-character-set=utf8mb4 -u root -p student_social_network < database/seeds/seed_1000_website_cases.sql
```

Sau rebuild, phải kiểm tra số lượng, foreign key/unique/check constraint, counter của bài viết và chạy integration/concurrency test bằng MySQL nếu có cấu hình test database.

Giai đoạn 0E chỉ cập nhật file nguồn; không import, migrate hoặc rebuild database thật.

## 12. Moderation Case

- `moderation_cases` có quan hệ N-1 với `posts`; `reports.moderation_case_id` tham chiếu case.
- Generated key `open_post_key` cùng unique index bảo đảm tối đa một case `OPEN` mỗi Post.
- `report_count`, `first_reported_at`, `latest_reported_at` được cập nhật trong cùng transaction tạo Report.
- Khi nâng cấp database có dữ liệu, phải tự xây dựng và kiểm tra script chuyển đổi riêng; repo demo chỉ giữ SQL canonical rebuild.
- Dữ liệu Report giữ nguyên reporter, reason, description và snapshot; không gộp dữ liệu thành CSV/JSON.
- Report `PENDING` thuộc case `OPEN`; case không vi phạm chuyển Report sang `REJECTED`, case có hành động chuyển sang `RESOLVED`.

## 13. Báo cáo trang cá nhân

- `profile_reports` tách khỏi `reports` để không làm nullable/polymorphic quan hệ Post và Moderation Case.
- `profile_report_cases` duy nhất theo `reported_user_id`, gom mọi lượt báo cáo của nhiều reporter và lưu `report_count`, `latest_reported_at`.
- `profile_reports.case_id` bắt buộc tham chiếu case; mỗi dòng vẫn giữ reporter, lý do và snapshot riêng.
- Generated `pending_report_key` cùng unique index bảo đảm một reporter chỉ có một `PENDING` cho cùng target.
- Snapshot lưu display name, avatar, bio và ngày sinh tại thời điểm gửi; không lưu email hoặc dữ liệu xác thực.
- Check constraint cấm reporter trùng target và giữ invariant các trường resolution.
- Migration: chạy `V007__add_profile_reports.sql`, sau đó `V008__group_profile_reports_into_cases.sql`; dự án không tự chạy migration.

