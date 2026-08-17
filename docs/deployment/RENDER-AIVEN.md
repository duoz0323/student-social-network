# Deploy Render + Aiven

> `README.md` là nguồn nghiệp vụ cao nhất. Tài liệu này chỉ hướng dẫn vận hành cấu hình hiện hành và không thay đổi contract fail-closed của AI moderation.

## 1. Kiến trúc deploy

```text
Browser
  -> Render Static Site: student-social-network
  -> Render Web Service: student-social-network-server
  -> Aiven MySQL + Cloudinary + Gmail/Google/Facebook
  -> AI moderation service đủ RAM
```

Frontend phải là **Static Site**, không phải Web Service. Backend là **Docker Web Service**. Không chạy MySQL hoặc lưu media trên filesystem Render vì filesystem của Free Web Service là tạm thời.

## 2. Trước khi redeploy code mới

1. Trong Render, mở deploy đang hoạt động gần nhất và giữ khả năng **Rollback**.
2. Trong Aiven, xác nhận service là `RUNNING`, dung lượng chưa gần 1 GB và ghi lại database đang dùng.
3. Backup trước mọi thay đổi schema; lệnh hỏi password tương tác, không đặt password trực tiếp trong command:

```bash
mysqldump --single-transaction --routines --triggers --set-gtid-purged=OFF --ssl-mode=REQUIRED -h AIVEN_HOST -P AIVEN_PORT -u AIVEN_USER -p AIVEN_DATABASE > aiven-backup.sql
mysqldump --no-data --routines --triggers --set-gtid-purged=OFF --ssl-mode=REQUIRED -h AIVEN_HOST -P AIVEN_PORT -u AIVEN_USER -p AIVEN_DATABASE > aiven-schema-before.sql
```

4. Không import trực tiếp `database/student_social_network.sql` nếu cần giữ dữ liệu. File canonical có `DROP DATABASE`, chỉ dành cho rebuild database test/demo sạch.
5. So sánh `aiven-schema-before.sql` với `database/student_social_network.sql`, tạo migration nâng cấp riêng theo đúng schema nguồn thực tế, review backup/rollback rồi mới chạy trên Aiven.
6. Deploy Backend trước. Chỉ deploy Frontend sau khi `GET https://student-social-network-server.onrender.com/health` trả `200` và `{"status":"UP"}`.

Backend dùng `ddl-auto: validate`. Log kiểu `Schema-validation: missing table/column` chứng minh schema Aiven chưa theo kịp code; không được đổi sang `update` để che lỗi.

## 3. Áp dụng cấu hình Render

Repo đã có `render.yaml` với đúng hai service hiện tại. Trong Render Dashboard:

1. Chọn **New > Blueprint** và kết nối repository/branch đang deploy.
2. Kiểm tra Render nhận diện đúng service đã có theo tên; không xác nhận nếu màn hình chuẩn bị tạo service trùng.
3. Backend dùng `BackEnd/Dockerfile`, health check `/health`, plan Free.
4. Frontend dùng root `FrontEnd`, build `npm ci && npm run build`, publish `dist`.
5. Frontend phải có rewrite `/* -> /index.html`, nếu không reload route như `/feed` sẽ trả 404.

Blueprint không ép `region` vì service hiện tại đã tồn tại và Render không cho đổi region tại chỗ. Nếu phải tạo lại Backend, chọn region gần Aiven nhất để giảm độ trễ giữa Spring và MySQL; sau đó mới cân nhắc khoảng cách tới người dùng.

Nếu tiếp tục quản lý thủ công thay vì Blueprint, sao chép chính xác các giá trị tương ứng từ `render.yaml` vào Dashboard.

## 4. Biến môi trường Backend

Các biến tối thiểu cần kiểm tra trong Render:

| Nhóm | Biến |
|---|---|
| Database | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| JWT/HMAC | `JWT_ACCESS_TOKEN_SECRET`, `AUTH_OTP_HMAC_SECRET`, `AUTH_FLOW_TOKEN_HMAC_SECRET`, `AUTH_SOCIAL_IDENTITY_FINGERPRINT_SECRET` |
| CORS | `FRONTEND_ALLOWED_ORIGINS` |
| Google/Facebook | `GOOGLE_CLIENT_ID`, `FACEBOOK_APP_ID`, `FACEBOOK_APP_SECRET` |
| Gmail | `MAIL_USERNAME`, `MAIL_APP_PASSWORD` |
| Cloudinary | `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` |
| AI | `AI_MODERATION_LOCAL_BASE_URL` |

JDBC URL mẫu:

```text
jdbc:mysql://AIVEN_HOST:AIVEN_PORT/AIVEN_DATABASE?sslMode=REQUIRED&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8
```

Không dùng URI dạng `mysql://...` cho `DB_URL`. Không thêm dấu `/` cuối `FRONTEND_ALLOWED_ORIGINS`; nếu có nhiều origin, phân tách bằng dấu phẩy. Aiven mặc định có thể cho phép `0.0.0.0/0`; nếu đã bật IP allowlist, thêm toàn bộ outbound CIDR của region Render đang dùng.

Các giá trị tối ưu Free đã nằm trong Blueprint: pool tối đa 5, một idle connection, tối đa 30 Tomcat threads và trust proxy headers. Không bật nhiều backend instance vì rate limiter và STOMP broker hiện là in-memory.

## 5. Biến môi trường Frontend

Biến Vite được đóng vào bundle ở **build time**, nên đổi biến phải redeploy Static Site:

```text
VITE_API_BASE_URL=https://student-social-network-server.onrender.com
VITE_API_TIMEOUT_MS=75000
VITE_API_WITH_CREDENTIALS=false
VITE_PUBLIC_APP_URL=https://student-social-network.onrender.com
VITE_GOOGLE_CLIENT_ID=...
VITE_FACEBOOK_APP_ID=...
VITE_GOOGLE_MAPS_API_KEY=...
```

Không đặt Client Secret, password hoặc token bí mật trong biến `VITE_*`.

## 6. AI moderation trên gói miễn phí

Render Free Web Service có 512 MB RAM, trong khi checkpoint AI tải khoảng 540 MB chỉ riêng trọng số và còn cần PyTorch/runtime memory. Vì vậy không ghép AI vào Backend và không kỳ vọng `ai-service` chạy ổn trên Render Free.

Các lựa chọn đúng contract:

1. Chạy AI service trên máy/server có đủ RAM và HTTPS public ổn định, rồi đặt URL vào `AI_MODERATION_LOCAL_BASE_URL`.
2. Dùng một dịch vụ ML có đủ RAM; phải giữ nguyên API `/health`, `/ready`, `/v1/moderation` và model/revision trong `ai-service`.
3. Nâng riêng AI lên instance đủ RAM.

Không bypass moderation trong production. Khi AI ngủ, đang tải model hoặc lỗi, Post/Comment/Reply có text trả `CONTENT_MODERATION_UNAVAILABLE` theo thiết kế.

## 7. Chẩn đoán theo log

| Dấu hiệu | Nguyên nhân ưu tiên | Cách xử lý |
|---|---|---|
| `Schema-validation: missing ...` | Aiven schema cũ | Khôi phục deploy cũ hoặc chạy migration đã review |
| `Communications link failure` | Host/port/IP allowlist sai | Kiểm tra Aiven service, URL và outbound CIDR Render |
| Lỗi SSL/TLS | JDBC URL thiếu/sai SSL mode | Dùng `sslMode=REQUIRED` |
| `JWT secret must contain at least 32 bytes` | Secret thiếu hoặc quá ngắn | Tạo secret ngẫu nhiên đủ mạnh, không tái dùng HMAC secret |
| Exit code `137` / Out of memory | Quá 512 MB | Giữ JVM limits hiện tại; không chạy AI cùng backend |
| Health check fail / 502 | App chưa startup | Đọc log từ lỗi đầu tiên, đặc biệt schema và env |
| Browser báo CORS | Origin không khớp tuyệt đối | Sửa `FRONTEND_ALLOWED_ORIGINS`, redeploy Backend |
| Reload route Frontend trả 404 | Thiếu SPA rewrite | Áp dụng route trong `render.yaml` |
| Request đầu chậm 30–90 giây | Render Free cold start | Chờ request đầu; timeout production đã đặt 75 giây |
| Post/Comment trả moderation unavailable | AI service không ready | Kiểm tra `AI_MODERATION_LOCAL_BASE_URL/ready` và RAM AI |
| Realtime mất rồi tự nối lại | Instance sleep/redeploy | REST/MySQL vẫn là nguồn chuẩn; client tự reconnect/reconcile |

## 8. Smoke test sau deploy

1. Gọi `/health` hai lần: lần đầu có thể cold start, lần hai phải nhanh.
2. Mở trực tiếp `/login`, `/feed`, rồi refresh để xác nhận SPA rewrite.
3. Kiểm tra DevTools Network: API dùng HTTPS backend và socket dùng `wss://.../ws`.
4. Đăng nhập local; refresh trang; kiểm tra refresh token dựng lại phiên.
5. Tạo Post có text để xác nhận AI; upload ảnh để xác nhận Cloudinary.
6. Mở hai browser/account để kiểm tra Notification/Messaging WebSocket.
7. Kiểm tra CORS chỉ cho đúng frontend production và localhost cần thiết.
8. Theo dõi Render Metrics/Logs và Aiven connections/storage trong ít nhất một lượt cold start.
