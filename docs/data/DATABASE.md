# Tài liệu Database MVP

`README.md` là nguồn sự thật cao nhất. Schema vật lý được mô tả chi tiết tại `database/student_social_network_db.sql`; DBML tương ứng nằm tại `database/student_social_network_db.dbml`. SQL và DBML phải được cập nhật đồng thời.

## 1. Trạng thái thiết kế Auth 0E

Auth dùng bốn bảng challenge riêng:

- `pending_registrations`.
- `auth_method_link_challenges`.
- `social_auth_challenges`.
- `reauthentication_challenges`.

Không tạo bảng `auth_challenges` tổng quát. Các type/status Auth trong database dùng `VARCHAR + CHECK`; source Java dự kiến dùng enum với `EnumType.STRING`.

## 2. Users và phương thức đăng nhập

### users

- `email`, `phone_number` và `password_hash` đều có thể `NULL`.
- Social-only user có thể không có email/phone; không tạo placeholder.
- Đã bỏ `chk_users_contact_required`.
- `password_hash` chỉ được phép khác `NULL` khi có email verified hoặc phone verified.
- `email_verified_at` chỉ có giá trị khi `email` khác `NULL`.
- `phone_verified_at` chỉ có giá trị khi `phone_number` khác `NULL`.

Invariant cuối cùng:

```text
Local method hợp lệ
= password_hash tồn tại
  và có email verified hoặc phone verified

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
- Unique nullable active key bảo đảm chỉ một pending active cho mỗi type/identifier; MySQL cho phép nhiều `NULL` trong unique index.
- OTP có hiệu lực 10 phút; pending 24 giờ; resend cooldown 60 giây.
- Resume rotate flow token, trả `resumed=true`, không thay `password_hash` và không tự resend trong cooldown.
- Muốn đổi mật khẩu phải cancel pending rồi tạo registration mới.
- Resend tăng `otp_version`, đặt `delivery_status=PENDING`, xóa failure code, reset attempts, giữ nguyên flow token và không gia hạn pending.
- Verify dùng pessimistic lock; deadlock retry tối đa một lần và không retry lỗi nghiệp vụ.

Khi terminal:

- `COMPLETED`: giữ `registration_type`, `identifier_normalized`, `completed_user_id`, `status`, `terminal_at` và HMAC flow lookup hash tối đa 7 ngày; xóa password/OTP, active key và delivery failure code ngay.
- `CANCELLED`/`EXPIRED`: xóa identifier, password/OTP và active key ngay; giữ HMAC flow lookup hash tối đa 7 ngày để status và idempotency.
- Raw flow token không được lưu. Cleanup/anonymization sau retention được phép đặt `flow_token_hash=NULL`.
- `completed_user_id` dùng `ON DELETE SET NULL`.
- Quan hệ giữa `completed_user_id` và `status` không nằm trong CHECK constraint để tương thích MySQL; Service và Integration Test phải bảo đảm giá trị này chỉ được gán theo đúng vòng đời hoàn tất đăng ký.

## 4. Link challenge

- Chỉ dùng cho `LINK_EMAIL` và `LINK_PHONE`; không tái sử dụng pending registration.
- User đích lấy từ JWT hiện tại.
- TTL 15 phút; OTP 10 phút; resend cooldown 60 giây.
- Resend không gia hạn challenge.
- Unique active key theo `purpose:identifier` và `userId:purpose`.
- Terminal state xóa identifier, OTP/flow hash, active keys và failure code ngay.

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
→ gửi email/SMS ngoài transaction
→ transaction ngắn cập nhật SENT/FAILED/UNKNOWN
```

- Kết quả thất bại rõ ràng: `FAILED`, cho phép resend sớm bằng cách đưa `resend_available_at` về thời điểm hiện tại; application cache vẫn áp dụng rate limit.
- Kết quả không chắc chắn/timeout: `UNKNOWN`, giữ cooldown 60 giây.
- Update delivery phải kèm `otp_version` để response của OTP cũ không ghi đè trạng thái OTP mới.
- Không dùng outbox trong MVP; outbox mã hóa là hướng P1.

## 9. Rate limit

Challenge lưu attempts, cooldown, resend count, delivery count, OTP version và expiry. Rate limit theo cửa sổ giờ/IP/identifier/user/provider nằm ở application cache với key đã HMAC, không lưu raw PII.

Redis/distributed rate limit, adaptive blocking và abuse telemetry là P1.

## 10. Cleanup

- Mọi terminal transition phải atomic với việc vô hiệu OTP/password secret, giải phóng active key và đặt `terminal_at`; registration flow lookup hash được giữ tối đa 7 ngày.
- Tất cả terminal rows bị xóa cứng tối đa 7 ngày sau `terminal_at`.
- Scheduler chạy batch ổn định theo `(status, terminal_at, id)`; có thể dùng `FOR UPDATE SKIP LOCKED`.
- Scheduler không xóa `users`, `user_profiles` hoặc `user_auth_providers`.

## 11. Rebuild dev/demo

Database hiện tại chỉ được giả định là dev/demo. Trước rebuild bắt buộc chạy `database/audit_auth_before_rebuild.sql`, lưu kết quả, backup và xác nhận không có dữ liệu thật cần bảo tồn.

Sau rebuild:

1. Nạp `database/student_social_network_db.sql`.
2. Tắt Admin bootstrap và nạp `database/seed_data.sql`.
3. Chạy `database/audit_auth_after_rebuild.sql`.
4. Chạy integration/concurrency test bằng MySQL/Testcontainers.

Giai đoạn 0E chỉ cập nhật file nguồn; không import, migrate hoặc rebuild database thật.
