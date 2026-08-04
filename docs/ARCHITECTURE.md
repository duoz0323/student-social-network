# Kiến trúc hệ thống

## 1. Tổng quan

Hệ thống gồm:

- Frontend ReactJS.
- Backend Spring Boot.
- MySQL.
- Cloud Storage cho ảnh.
- JWT Access Token.
- Refresh Token.
- WebSocket/STOMP native cho Notification realtime.

## 2. Luồng tổng thể

```text
Browser
→ ReactJS
→ Axios
→ REST API
→ Spring Security
→ Controller
→ Service
→ Repository
→ MySQL
```

Kênh Notification realtime chạy song song với REST:

```text
Browser
→ NotificationContext
→ STOMP CONNECT /ws
→ JWT ChannelInterceptor
→ Simple Broker
→ /user/queue/notifications

Service nghiệp vụ
→ commit MySQL
→ AFTER_COMMIT listener
→ SimpMessagingTemplate
→ user destination
```

## 3. Frontend

Cấu trúc Frontend chính thức hiện tại:

```text
FrontEnd/
├── public/
├── src/
│   ├── assets/
│   ├── components/
│   ├── config/
│   ├── contexts/
│   ├── features/
│   ├── hooks/
│   ├── router/
│   ├── utils/
│   ├── App.css
│   ├── App.jsx
│   ├── index.css
│   └── main.jsx
├── .env
├── .gitignore
├── eslint.config.js
├── index.html
├── package-lock.json
├── package.json
├── README.md
└── vite.config.js
```

### Quy tắc tổ chức

- `components/`: component dùng chung.
- `config/`: Axios, interceptor và cấu hình.
- `contexts/`: Context toàn ứng dụng.
- `features/`: module nghiệp vụ.
- `hooks/`: hook dùng chung.
- `router/`: cấu hình route.
- `utils/`: hàm tiện ích.
- Không tự ý thêm `app/`, `shared/`, `layouts/`, `routes/` hoặc `store/`.

### Feature-Based Structure

```text
src/features/<feature>/
├── components/
├── pages/
├── services/
├── hooks/
├── utils/
└── validation/
```

Chỉ tạo thư mục con khi thực sự cần.

## 4. Backend

Cấu trúc package theo feature:

```text
backend/src/main/java/com/example/socialnetwork/
├── common/
├── config/
├── security/
├── auth/
├── user/
├── follow/
├── post/
├── interaction/
├── feed/
├── search/
├── report/
└── admin/
```

Trong mỗi feature:

```text
post/
├── controller/
├── service/
├── repository/
├── entity/
├── enums/
├── dto/
├── mapper/
└── exception/
```

Quy tắc enum trong Backend:

- Mỗi feature/module có thể tạo thư mục `enums/` khi entity của module có trạng thái, vai trò, loại, lý do hoặc nhóm giá trị cố định.
- Enum phải nằm trong module sở hữu nghiệp vụ, không đặt chung ở package toàn cục nếu chỉ phù hợp với một module.
- Ví dụ: `user/enums/UserStatus.java`, `user/enums/UserRole.java`, `post/enums/PostStatus.java`, `interaction/enums/CommentStatus.java`, `report/enums/ReportStatus.java`.
- Entity, DTO, mapper, service và repository phải import enum từ package `enums` của module tương ứng.

## 5. Tầng xử lý

### Controller

- Nhận request.
- Validation cơ bản.
- Gọi Service.
- Trả response.

### Service

- Nghiệp vụ.
- Phân quyền.
- Transaction.
- Điều phối dữ liệu.

### Repository

- Truy vấn dữ liệu.

### DTO

- Request/Response.

### Mapper

- Entity ↔ DTO.

### Security

- JWT filter.
- Authentication.
- Authorization.
- Password encoder.
- Refresh token.

## 6. Dữ liệu

MySQL là nguồn dữ liệu chuẩn.

Timestamp do MySQL/API quản lý được quy ước theo UTC. `Clock` của Backend phải dùng UTC để deadline nghiệp vụ, gồm giới hạn sửa Post 15 phút, không phụ thuộc múi giờ máy chạy; Frontend chuyển UTC sang múi giờ hiển thị của người dùng.

Media:

- Lưu trên Cloud Storage.
- Database lưu URL và metadata.
- Không lưu BLOB.

## 7. Feed

### Following

- Truy vấn bài của người đang Follow.
- Chỉ PUBLISHED.
- Sắp xếp ổn định `published_at DESC, id DESC`.
- Dùng Cursor Pagination và keyset query.

### For You

MVP dùng điểm cơ bản:

```text
score = freshnessScore
      + likeCount × likeWeight
      + commentCount × commentWeight
```

Không sử dụng Machine Learning.

### Cursor Pagination cho danh sách bài viết

- Feed For You, Feed Following, bài trên hồ sơ, tab Repost, bài đã lưu và bài đã thích dùng Cursor Pagination.
- Request đầu truyền `limit`; request sau truyền nguyên `nextCursor` opaque do Backend trả về.
- `limit` mặc định 10, tối đa 20; Backend lấy `limit + 1` để xác định `hasNext`.
- Feed For You dùng cursor gồm `score`, `publishedAt`, `postId`; danh sách theo thời gian dùng
  `createdAt`, `postId`.
- Following Feed hợp nhất `ORIGINAL` và `REPOST` bằng `UNION ALL`, dùng cursor
  `activityAt`, `itemRank`, `actorId`, `postId`; dữ liệu PostCard được batch-load để tránh N+1.
- Search, bình luận, Follow và Admin tiếp tục dùng `PageResponse`.

## 8. Bảo mật

- Mật khẩu băm.
- Access Token ngắn hạn.
- Refresh Token thu hồi được.
- API Admin yêu cầu ADMIN.
- Backend kiểm tra quyền.
- Không trả Entity trực tiếp.
- Không trả stack trace.

## 9. Phân trang

- Danh sách bài viết phục vụ Infinite Scroll dùng Cursor Pagination, mặc định 10 và tối đa 20.
- Các danh sách cần số trang/tổng số tiếp tục dùng `PageResponse` theo contract từng endpoint.
- Không dùng `page`, `offset` hoặc `COUNT(*)` cho các endpoint Cursor Pagination.

## 10. Notification realtime

- MySQL và REST API là nguồn sự thật. STOMP là kênh best-effort để giảm độ trễ hiển thị.
- Endpoint WebSocket là `/ws`, không dùng SockJS; simple broker phục vụ prefix `/queue`, còn user
  destination prefix là `/user`.
- Client chỉ được subscribe `/user/queue/notifications`. Server gửi bằng
  `convertAndSendToUser(users.id, "/queue/notifications", envelope)`.
- JWT Access Token phải nằm trong STOMP `CONNECT` header `Authorization: Bearer ...`; không truyền
  token qua URL. Interceptor tái sử dụng `JwtService`, dựng principal có `name = users.id`, từ chối
  Refresh Token, tài khoản không `ACTIVE` và hồ sơ chưa hoàn tất.
- Client không được `SEND` message nghiệp vụ lên broker và không được subscribe destination của
  người dùng khác.
- Service chỉ publish event nội bộ nhẹ sau khi Notification đã được lưu. Listener
  `AFTER_COMMIT` đọc lại projection đã lọc `deleted_at`, Block hai chiều và trạng thái recipient,
  rồi mới phát `NOTIFICATION_CREATED`.
- Giai đoạn 1 không phát realtime cho read, read-all, delete hoặc invalidation; không có outbox và
  không dùng broker ngoài.
- Frontend dùng một STOMP client dùng chung trong `NotificationContext`, reconcile danh sách đầu và
  unread count bằng REST khi connect/reconnect. Khi disconnected, tab visible polling unread count
  mỗi 30 giây; logout hoặc user không đủ điều kiện phải deactivate client.
- `eventId` được giữ trong bộ nhớ có giới hạn để chống xử lý trùng trong một phiên; `notification.id`
  tiếp tục là khóa chống trùng khi merge danh sách. Badge dùng unread count authoritative từ Backend
  và hiển thị tối đa `99+`.

## 11. Messaging REST Core và realtime UI

- Module `messaging` theo feature package, không trả Entity và không phụ thuộc module Notification.
- MySQL dùng `conversations`, `conversation_members`, `messages`, `message_attachments` và
  `media_cleanup_tasks`; unique participant pair và unique
  `(sender_id, client_message_id)` là hàng rào chống trùng cuối cùng.
- Inbox và history dùng keyset cursor Base64URL, lấy `limit + 1`, không dùng offset hoặc count tổng.
- Service lấy sender từ SecurityContext, kiểm tra role/status/onboarding, Follow đúng hướng và Block
  hai chiều. Open/send cùng dùng `UserPairLockCoordinator` với Block theo thứ tự low/high user ID.
- Gửi mới lưu message rồi cập nhật last message trong cùng transaction; replay cùng payload trả bản
  ghi cũ. Mark read khóa membership và chỉ cho marker tiến lên.
- Service publish domain event nhẹ trong transaction chỉ khi insert/marker thực sự tiến lên. Listener `AFTER_COMMIT` mở read-only transaction mới, kiểm tra lại account/profile/Block, tính unread riêng và phát cho cả hai user qua `/queue/messaging`; lỗi broker chỉ log warning.
- JSON text và multipart ảnh dùng cùng POST path theo content type. Toàn bộ file được kiểm tra extension,
  MIME khai báo, magic bytes/khả năng giải mã, dimensions, size và SHA-256 trước khi upload.
- Cloudinary lưu ảnh chat dạng `authenticated`. Upload ở ngoài transaction MySQL; transaction ngắn khóa pair/conversation, recheck idempotency rồi lưu message + attachments. Rollback/race xóa bù, lỗi xóa tạo durable cleanup task trong transaction độc lập.
- REST/history/realtime chỉ trả attachment metadata. Endpoint access kiểm tra lại account/profile/membership/Block rồi tạo signed URL TTL cấu hình; storage public ID không đi ra client.
- `MessagingContext` dùng connection manager chung, merge optimistic/REST/WebSocket theo `messageId` hoặc `clientMessageId`, reconcile qua REST khi reconnect/tab visible và polling 30 giây khi disconnected. Không tạo Notification row, Outbox hay STOMP client thứ hai.
- Typing dùng cùng connection manager qua `/app/messaging/typing`; inbound interceptor chỉ allowlist chính xác destination này và vẫn từ chối mọi client `SEND` khác. Controller không nhận identity từ payload; service read-only xác thực principal, account/profile, hai member và Block bằng projection rồi phát riêng recipient.
- `TypingRateLimiter` là fixed window in-memory tối đa 4 frame/user/giây, có cleanup key cũ và chỉ bảo vệ từng application instance. Frontend phát START lần đầu, refresh sau mỗi 3 giây hoạt động, STOP sau 2 giây idle/submit/blank/blur/leave; receiver dedupe `eventId` và tự xóa START sau 5 giây. Typing không dùng transaction after-commit vì không có state bền vững.

