# AI-assisted Content Moderation V1 — Manual E2E Checklist

> Chạy trên môi trường test có MySQL riêng, tài khoản USER đã hoàn tất profile và local AI service đã `/ready`. Không dùng database production.

## 1. Chuẩn bị

- [ ] FastAPI khởi động trước Backend; `/health` và `/ready` trả 200; Backend dùng `AI_MODERATION_LOCAL_BASE_URL=http://127.0.0.1:8001`.
- [ ] Backend khởi động với provider và timeout đúng cấu hình; log của cả hai service không chứa API key hoặc nguyên văn content.
- [ ] Frontend đăng nhập USER `ACTIVE`, profile đã hoàn tất.
- [ ] Ghi lại số row `posts`, `comments`, `notifications` và `posts.comment_count` trước từng case rejected.
- [ ] Có cách tạm mô phỏng provider timeout/unavailable trên môi trường test mà không sửa secret production.

## 2. Post create

- [ ] Text bình thường → tạo Post thành công, đúng một row `posts`.
- [ ] Text borderline → nhận `CONTENT_MODERATION_WARNING`; composer giữ text, media, hashtag và Location; không có row Post mới.
- [ ] Text vi phạm → nhận `CONTENT_POLICY_VIOLATION`; composer giữ toàn bộ draft; không có row Post mới.
- [ ] Provider unavailable/timeout → nhận `CONTENT_MODERATION_UNAVAILABLE`; draft được giữ và có thể retry; không có row Post mới.
- [ ] Media + safe caption → Post được tạo sau moderation.
- [ ] Media + violating caption → không upload/persist Post mới; draft media vẫn còn trên UI.
- [ ] Post chỉ có media, không text → giữ behavior hiện tại và không gửi payload text rỗng sang provider.

## 3. Post update

- [ ] Chỉ đổi media/hashtag/Location, text không đổi → update thành công và không gọi moderation lại.
- [ ] Đổi text an toàn trong 15 phút → update thành công, `updated_at` thay đổi.
- [ ] Đổi text borderline/vi phạm → Post, media, hashtag, Location và `updated_at` giữ nguyên.
- [ ] User không phải tác giả → bị chặn quyền trước moderation.
- [ ] Hết cửa sổ 15 phút → bị chặn deadline trước moderation.

## 4. Comment

- [ ] Safe Comment → insert đúng một row, `comment_count` tăng đúng một, Notification/realtime giữ semantics hiện tại.
- [ ] Warning Comment → input không bị clear; không insert, không tăng `comment_count`, không Notification, không realtime event.
- [ ] Block Comment → input không bị clear; không insert, không tăng `comment_count`, không Notification, không realtime event.
- [ ] Provider unavailable → input không bị clear, hiển thị retry message và không có side effect.
- [ ] Comment trên Post `HIDDEN`/`DELETED` hoặc không có quyền xem → reject trước moderation.
- [ ] Quan hệ Block hai chiều → reject trước moderation.
- [ ] Restrict → vẫn giữ semantics tương tác hiện tại; moderation chỉ quyết định content, không thay đổi Restrict policy.

## 5. Reply

- [ ] Safe Reply → insert đúng một row, reply count và tổng `comment_count` tăng đúng, Notification/realtime giữ semantics hiện tại.
- [ ] Warning Reply → draft giữ nguyên; không insert, không tăng count, không Notification/realtime.
- [ ] Block Reply → draft giữ nguyên; không insert, không tăng count, không Notification/realtime.
- [ ] Provider unavailable → draft giữ nguyên và toàn bộ side effect bằng 0.
- [ ] Reply vào comment bị xóa, reply cấp hai hoặc quan hệ Block → reject trước moderation.

## 6. Report/Admin regression và privacy

- [ ] WARNING/BLOCK không tạo `reports`, `moderation_cases`, `admin_actions` hoặc user penalty.
- [ ] Không có account tự chuyển `BLOCKED` vì kết quả AI.
- [ ] Report/Admin human moderation cũ vẫn xử lý Post đã tồn tại như trước.
- [ ] Response không chứa raw provider response, prompt, API key, model reasoning hoặc stack trace.
- [ ] Log chỉ có metadata kỹ thuật cần thiết; không có nguyên văn content nhạy cảm ở INFO.

## 7. Điều kiện nâng trạng thái

- [ ] Toàn bộ case trên pass trên FastAPI local + Backend + Frontend thật.
- [ ] Xác minh row/counter/Notification/realtime bằng DB và hai phiên trình duyệt khi cần.
- [ ] Chỉ sau đó mới đổi trạng thái feature từ `NOT INTEGRATED` sang `INTEGRATED` trong README và `docs/IMPLEMENTATION-STATUS.md`.
