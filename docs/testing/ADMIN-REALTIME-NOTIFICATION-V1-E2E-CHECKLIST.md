# Admin Realtime Notification V1 — Manual E2E Checklist

Trạng thái hiện tại: `IMPLEMENTED`, automated test `TESTED`, manual E2E `NOT INTEGRATED`.

## Chuẩn bị

- Dựng MySQL test cô lập bằng `database/student_social_network.sql`; không chạy file rebuild trên database production hoặc môi trường cần giữ dữ liệu.
- Khởi động Backend, Frontend và đăng nhập các tab riêng: SUPER_ADMIN, USER_MANAGER, MODERATOR, COLLABORATOR, ADS_MANAGER, custom role `REPORT_REVIEWER`, cùng hai USER.
- DevTools xác nhận mỗi tab chỉ có một connection `/ws`; ADMIN subscribe thêm `/user/queue/admin-notifications` trên connection đó.

## Kịch bản

1. USER report Post: SUPER_ADMIN, MODERATOR và REPORT_REVIEWER có `REPORT_VIEW` nhận đúng một event; USER_MANAGER, COLLABORATOR và ADS_MANAGER không nhận. Reload vẫn thấy row qua REST.
2. USER report Profile: audience bám đúng `REPORT_VIEW`; notification mở route Profile Report và resource API vẫn kiểm tra `REPORT_DETAIL_VIEW`.
3. COLLABORATOR gửi Moderation Suggestion: holder `MODERATION_SUGGESTION_VIEW` nhận. Khi Moderator accept/reject, chỉ đúng Collaborator owner nhận direct notification.
4. Admin mang `USER_MANAGER + MODERATOR` và match hai nhánh permission: cùng event chỉ có một row và một item UI.
5. Tạo custom role `REPORT_REVIEWER`, gán `REPORT_VIEW` không restart Backend, phát Report mới và xác nhận admin nhận realtime.
6. Thu hồi `REPORT_VIEW`: row cũ yêu cầu quyền này biến mất khỏi list/unread; event mới không fan-out; resource API trả 403 nếu thiếu quyền chi tiết.
7. Ngắt WebSocket, tạo event, kết nối lại: realtime có thể miss nhưng REST reconciliation khôi phục item và badge đúng.
8. Gây rollback transaction trong môi trường test: không có row `admin_notifications` và không có STOMP event.
9. ADMIN A thử read/delete ID của ADMIN B: nhận 404/403 theo error policy và dữ liệu B không đổi.
10. Read one lặp lại, read-all, delete và thao tác từ tab thứ hai: badge/list nhất quán sau BroadcastChannel hoặc REST reconciliation.
11. Broker tắt sau commit: business action thành công, row vẫn tồn tại; bật lại/reconnect tải row bằng REST.
12. ADS_MANAGER mở center: bell render bình thường và empty state đúng, không có Ads event giả.

## Kết quả cần ghi

- Tài khoản/role/permission thực tế, request ID, notification ID, event key và destination đã quan sát.
- Kết quả REST, STOMP, reload/reconnect, cross-tab và resource authorization.
- Chỉ đổi trạng thái README sang `INTEGRATED` khi toàn bộ kịch bản bắt buộc đã pass trên MySQL test thật.
