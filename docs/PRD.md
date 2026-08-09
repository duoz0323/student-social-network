# PRODUCT REQUIREMENTS DOCUMENT

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. PRD này tóm tắt yêu cầu sản phẩm; nếu có khác biệt, phải áp dụng `README.md` và cập nhật trực tiếp mục tương ứng trong PRD.

## 1. Thông tin sản phẩm

**Tên dự án:** Mạng xã hội tinh gọn hướng đến sinh viên.

**Loại sản phẩm:** Website mạng xã hội.

**Mục tiêu:** Cho phép sinh viên tạo tài khoản, xây dựng hồ sơ, theo dõi người dùng khác, đăng nội dung ngắn, xem bảng tin và thực hiện các tương tác cơ bản.

## 2. Mục tiêu MVP

MVP phải hoàn thiện một luồng xuyên suốt:

Đăng ký local bằng email hoặc đăng ký bằng Google/Facebook
→ Xác minh OTP nếu đăng ký local
→ Đăng nhập và nhận JWT của hệ thống
→ Hoàn tất hồ sơ ban đầu
→ Quản lý hồ sơ
→ Theo dõi
→ Đăng bài
→ Xem Feed
→ Tương tác
→ Tìm kiếm
→ Báo cáo
→ Quản trị.

## 3. Actor

### 3.1 Khách chưa đăng nhập

Được phép:

- Đăng ký.
- Xác minh hoặc tiếp tục đăng ký local đang chờ.
- Gửi lại OTP theo giới hạn tần suất.
- Đăng ký hoặc đăng nhập bằng Google/Facebook.
- Đăng nhập.
- Khôi phục mật khẩu bằng OTP đối với tài khoản local đủ điều kiện.

Không được sử dụng chức năng mạng xã hội.

### 3.2 Người dùng

Điều kiện:

- Đã đăng nhập.
- Tài khoản ở trạng thái ACTIVE.
- Hồ sơ đã hoàn tất, tức `user_profiles.profile_completed_at` khác `NULL`.

Được phép:

- Xem và cập nhật hồ sơ.
- Xem hồ sơ người khác.
- Follow/Unfollow.
- Tạo, xem, sửa và xóa bài.
- Tùy chọn gắn, thay đổi hoặc gỡ một địa điểm trên bài viết.
- Like/Unlike.
- Xem danh sách bài viết đã thích của chính mình.
- Bình luận và xóa bình luận của mình.
- Lưu/Bỏ lưu bài.
- Xem Feed For You.
- Xem Feed Following.
- Tìm kiếm.
- Báo cáo bài viết hoặc trang cá nhân của người dùng khác.

### 3.3 Quản trị viên

Có role `ADMIN`.

Ngoài quyền người dùng, Admin được phép:

- Xem và tìm kiếm người dùng.
- Xem và chỉnh sửa avatar, tên hiển thị, ngày sinh, phần giới thiệu của hồ sơ USER.
- Sau khi chỉnh sửa thành công, USER nhận thông báo hồ sơ đã bị điều chỉnh vì vi phạm Tiêu chuẩn hệ thống.
- Khóa/Mở khóa tài khoản.
- Xem danh sách bài viết.
- Xem, tìm kiếm, tạo, đổi tên và xóa hashtag; đổi tên giữ liên kết, còn xóa chỉ gỡ hashtag và không xóa bài viết.
- Xem bài bị báo cáo.
- Ẩn/Khôi phục bài viết.
- Xử lý báo cáo.

## 4. Phạm vi chức năng

### 4.1 Xác thực

Hệ thống áp dụng mô hình một tài khoản nội bộ có nhiều phương thức xác thực. Email, Google và Facebook sau khi được xác minh và liên kết hợp lệ đều ánh xạ về cùng một `users.id`.

Đăng ký local:

- Request chỉ nhận `email`, `password` và `confirmPassword`; không nhận username hoặc tên hiển thị.
- Backend chuẩn hóa email và băm mật khẩu trước khi lưu.
- Khi nhận form hợp lệ, Backend chỉ tạo `pending_registrations`; chưa tạo `users`, `user_profiles`, Access Token hoặc Refresh Token.
- OTP email có hiệu lực 10 phút, chỉ được gửi lại sau 60 giây và cho phép tối đa 5 lần nhập sai.
- Pending có hiệu lực 24 giờ; OTP mới làm OTP cũ mất hiệu lực; không được có hai pending còn hiệu lực cho cùng email.
- Dữ liệu `CANCELLED` và `EXPIRED` được giữ tối đa 7 ngày trước khi xóa hoặc ẩn danh.
- Chỉ OTP hợp lệ mới tạo `users` và `user_profiles` trong cùng transaction. Nếu tạo profile thất bại, toàn bộ transaction phải rollback.
- Sau khi tài khoản thật được tạo, Backend cấp Access Token và Refresh Token rồi điều hướng tới onboarding.

Google/Facebook và liên kết phương thức:

- Backend phải tự xác minh provider token; không tin provider ID, email hoặc trạng thái verified do Frontend khai báo.
- Provider token chỉ dùng tại endpoint Auth; API nghiệp vụ chỉ chấp nhận JWT của hệ thống.
- Provider đã liên kết phải đăng nhập về đúng `users.id`.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Khi liên kết provider, tài khoản đích phải lấy từ JWT hiện tại.
- Email phải được xác minh bằng OTP trước khi liên kết; không được gỡ phương thức đăng nhập cuối cùng.
- `users.password_hash` được phép `NULL` với social-only account; tài khoản này không đăng nhập local được cho đến khi thiết lập mật khẩu và xác minh email local.

Đăng nhập và phiên:

- Login local dùng email và mật khẩu; chỉ email có `email_verified_at` khác `NULL` mới được dùng.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.
- Access Token có thời hạn ngắn; Refresh Token chỉ lưu dạng hash, được rotate khi refresh và bị thu hồi khi logout hoặc theo nghiệp vụ khóa tài khoản.
- Người chưa hoàn tất hồ sơ chỉ được dùng API Auth cần thiết, Refresh Token, logout, onboarding và API quản lý phương thức xác thực theo contract; API mạng xã hội chính trả `PROFILE_NOT_COMPLETED`.
- Email và dữ liệu xác thực không được trả trong API hồ sơ công khai.

### 4.2 Hồ sơ

Người dùng có thể:

- Xem hồ sơ cá nhân.
- Xem hồ sơ người khác.
- Điều hướng hồ sơ bằng userId, trong đó `/profile/me` dành cho hồ sơ cá nhân và `/profile/:userId` dành cho hồ sơ người khác.
- Cập nhật tên hiển thị.
- Cập nhật avatar.
- Cập nhật ngày sinh.
- Cập nhật bio.
- Xem số follower.
- Xem số following.
- Xem bài đã đăng.

Tất cả hồ sơ công khai trong MVP.

Hoàn tất hồ sơ ban đầu:

- Tên hiển thị và ngày sinh là bắt buộc.
- Người dùng phải đủ 18 tuổi tại ngày hoàn tất hoặc cập nhật hồ sơ.
- Avatar và bio là tùy chọn.
- Hồ sơ chỉ hoàn tất sau khi tên hiển thị hợp lệ và ngày sinh hợp lệ của người dùng đủ 18 tuổi đã được lưu, sau đó người dùng xác nhận hoàn tất.
- Backend cập nhật `user_profiles.profile_completed_at` khi hoàn tất.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không đồng nghĩa hồ sơ đã hoàn tất.

### 4.3 Theo dõi

- Follow có hiệu lực ngay.
- Không có Follow Request.
- Không được Follow chính mình.
- Không được tạo Follow trùng.
- Có thể Unfollow.
- Có thể xem follower/following.

#### User Block

- User Block được lưu có hướng nhưng ngăn truy cập và tương tác mới giữa hai tài khoản theo cả hai chiều.
- Block xóa Follow hai chiều; Unblock không tự khôi phục Follow.
- Block không xóa Like hoặc Comment lịch sử và không làm giảm bộ đếm tương tác của bài.
- Trong thời gian còn Block, hai bên không nhìn thấy Comment/Reply lịch sử của nhau, kể cả khi người xem là chủ bài viết; dữ liệu chỉ bị lọc động và không bị xóa.
- Nếu Comment cha bị ẩn do Block thì toàn bộ nhánh Reply của cha đó cũng bị ẩn; Reply bị Block dưới một Comment cha vẫn hợp lệ được lọc riêng.
- Sau Unblock, Comment/Reply lịch sử hiển thị trở lại nếu vẫn hợp lệ và người xem còn quyền truy cập bài viết.
- Sau Block không phát sinh Follow, Like/Unlike, Comment/Reply, Save/Unsave hoặc Notification mới giữa hai tài khoản.

### 4.4 Bài viết

Bài viết gồm:

- Nội dung tối đa 500 ký tự.
- Tối đa 4 media, trong đó tối đa 4 ảnh hoặc tối đa một video.
- Ảnh hỗ trợ JPG, JPEG, PNG, WEBP và tối đa 10 MB mỗi file.
- Video hỗ trợ MP4, WebM, tối đa 100 MB và dài không quá 3 phút.
- Tối đa một hashtag.
- Tối đa một Location tùy chọn.

Quy tắc:

- Phải có nội dung hoặc ít nhất một media.
- Chỉ tác giả được sửa/xóa.
- Trong giới hạn 15 phút, tác giả có thể giữ/gỡ media cũ hoặc thêm ảnh/video mới; tổng media sau cập nhật vẫn phải hợp lệ.
- Menu thao tác hiển thị countdown sửa bài từ 15 phút và tự ẩn hành động sửa khi hết hạn; Backend kiểm tra deadline theo UTC.
- Trạng thái: PUBLISHED, HIDDEN, DELETED.
- Xóa bài là xóa mềm.

Location gắn với Post thuộc P1 và có phạm vi độc lập với Discovery Map:

- Quan hệ là `Location 1 — N Post`; một Post có `0..1` Location và một Location có thể được nhiều Post sử dụng.
- Frontend gửi object tùy chọn gồm `placeId`, `displayName`, `formattedAddress`, `latitude` và `longitude`.
- Backend validate dữ liệu, chuẩn hóa chuỗi, tìm bằng `google_place_id`, dùng lại bản ghi nếu đã tồn tại và tạo mới nếu chưa tồn tại.
- Chỉ `google_place_id` được dùng làm natural unique key. Không dùng tên địa điểm hoặc tọa độ để xác định trùng.
- Backend chưa xác minh dữ liệu với Google Places API trong giai đoạn này; xác minh và đồng bộ định kỳ thuộc FUTURE.
- Khi cập nhật Post trong giới hạn 15 phút và đúng quyền tác giả, `KEEP` giữ nguyên Location, `REPLACE` thay bằng Location được resolve theo Place ID, còn `REMOVE` gỡ Location.
- Gỡ Location chỉ đặt quan hệ về `NULL`. Xóa mềm hoặc xóa cứng Post không xóa Location; không cascade remove và không tự động dọn Location không còn tham chiếu.
- Location hiện tại hoặc `null` phải xuất hiện trong response tạo bài, chi tiết bài, hai Feed, bài trên hồ sơ, bài đã lưu, bài đã thích, tìm kiếm Post và Admin Post Detail.
- Location chưa được đưa vào report snapshot và chưa có API quản trị Location.

### 4.5 Tương tác

#### Like

- Like/Unlike.
- Xem danh sách bài viết đã thích của chính mình.
- Một người Like một bài tối đa một lần.
- Không Like bài HIDDEN hoặc DELETED.

#### Bình luận

- Thêm bình luận.
- Xem bình luận.
- Xóa bình luận của mình.
- Reply một cấp thuộc P2.

#### Lưu bài

- Save/Unsave.
- Một người Save một bài tối đa một lần.
- Danh sách Save chỉ chủ tài khoản xem.

#### Repost

- Repost/Unrepost idempotent; một user chỉ có một quan hệ với mỗi Post.
- Chỉ Repost bài `PUBLISHED` của người khác mà user có quyền xem.
- Chỉ lưu tham chiếu `user_id`, `post_id`, `created_at`; không sao chép dữ liệu bài gốc.
- Tab Repost trên Profile và activity Repost trong Following Feed dùng Cursor Pagination.
- Bài gốc `HIDDEN` hoặc `DELETED` không xuất hiện qua Repost.

### 4.6 Feed

#### Following

- Gồm activity bài gốc và Repost của tài khoản đang Follow.
- Sắp xếp theo khóa ổn định `activityAt`, `itemRank`, `actorId`, `postId` giảm dần.
- Không gồm bài HIDDEN hoặc DELETED.
- Dùng Cursor Pagination, sắp xếp ổn định theo `published_at DESC, id DESC`.

#### For You

- Gồm bài PUBLISHED.
- Xếp hạng cơ bản theo:
  - Độ mới.
  - Số Like.
  - Số bình luận.
- Không dùng Machine Learning.
- Hạn chế lặp liên tiếp cùng tác giả.
- Dùng Cursor Pagination với cursor chứa đủ khóa xếp hạng `score`, `publishedAt`, `postId`.

### 4.7 Hashtag

- Chuẩn hóa chữ thường.
- Một bài có tối đa một hashtag.
- Xem bài theo hashtag.

### 4.8 Tìm kiếm

- Tìm user theo tên hiển thị.
- Tìm post theo nội dung.
- Tìm post theo hashtag.
- Dùng MySQL trong MVP.
- Có phân trang.
- Không hiển thị user BLOCKED.
- Không hiển thị post HIDDEN/DELETED.

### 4.8.1 Mention

- Mention không thuộc phạm vi MVP hiện tại.
- Khi phát triển sau MVP, mention hiển thị bằng tên hiển thị của người được chọn.
- Mention phải liên kết nội bộ bằng userId, không dùng tên hiển thị làm khóa.
- Khi bấm mention, điều hướng đến `/profile/:userId`.

### 4.9 Báo cáo

Báo cáo bài viết giữ nguyên mô hình mỗi Report thuộc một Moderation Case của bài.

Thông tin:

- Post.
- Người báo cáo.
- Lý do.
- Mô tả.
- Thời gian.
- Trạng thái.

Trạng thái:

- PENDING.
- RESOLVED.
- REJECTED.

Một người không được có nhiều report PENDING cho cùng một bài.

Một bài chỉ có tối đa một Moderation Case `OPEN` tại một thời điểm. Case dùng ba trạng thái
`OPEN`, `RESOLVED_NO_VIOLATION`, `RESOLVED_ACTION_TAKEN`; không có bước tiếp nhận hoặc trạng thái
đang xử lý. Case đã giải quyết không nhận Report mới và báo cáo hợp lệ tiếp theo tạo case mới.

Báo cáo trang cá nhân dùng `profile_report_cases` và `profile_reports` riêng, không đưa User vào Moderation Case của Post:

- Không báo cáo chính mình hoặc target đang bị Block hai chiều.
- Chỉ dùng sáu lý do cố định đã chốt trong README.
- Mọi lượt báo cáo cùng target được gom vào một case; Admin thấy số lượng, danh sách reporter và lý do tương ứng.
- Báo cáo mới mở lại case đã kết luận; Admin xử lý đồng thời các lượt đang chờ trong case.
- Một reporter chỉ có một báo cáo `PENDING` cho cùng target.
- Lưu snapshot hồ sơ lúc gửi; Admin xem thêm hồ sơ và danh sách bài viết hiện tại.
- Admin kết luận `RESOLVED`/`REJECTED`; khi xác nhận vi phạm có thể khóa ngay tài khoản target trong cùng transaction.

Mỗi Moderation Case bài viết được kết luận `RESOLVED_ACTION_TAKEN` tính một lần vi phạm cho tác giả, dù case có
nhiều reporter. Lần vi phạm thứ ba tự động khóa tài khoản với lý do `REPEATED_VIOLATION` và thu hồi mọi Refresh Token.

### 4.10 Quản trị

#### Người dùng

- Danh sách.
- Tìm kiếm.
- Xem chi tiết và chỉnh sửa avatar cùng nội dung hồ sơ USER.
- Tạo Notification hệ thống `PROFILE_UPDATED_BY_ADMIN` sau khi transaction chỉnh sửa hồ sơ thành công.
- Khóa.
- Mở khóa.

#### Bài viết

- Danh sách.
- Bài bị báo cáo.
- Ẩn.
- Khôi phục.

#### Báo cáo

- Danh sách một dòng mỗi Moderation Case.
- Chi tiết toàn bộ Report và snapshot độc lập trong case.
- Kết luận trực tiếp không vi phạm hoặc có vi phạm.
- Ẩn bài khi kết luận có vi phạm.
- Có tab vụ việc trang cá nhân, chi tiết gồm danh sách người báo cáo/lý do, snapshot, hồ sơ hiện tại và danh sách bài viết tải nối tiếp.

## 5. Ưu tiên

### P0

- Đăng ký local bằng email và xác minh OTP.
- Đăng ký, đăng nhập Google/Facebook.
- Đăng nhập local và đăng xuất.
- Khôi phục mật khẩu bằng OTP cho tài khoản local đủ điều kiện; dùng decoy challenge để chống account enumeration và không hỗ trợ social-only tạo mật khẩu lần đầu.
- Hoàn tất hồ sơ ban đầu.
- JWT/Refresh Token.
- Hồ sơ.
- Follow/Unfollow.
- CRUD bài.
- Upload ảnh.
- Like/Unlike.
- Bình luận.
- Feed Following.
- Feed For You.
- USER/ADMIN.

### P1

- Liên kết và quản lý nhiều phương thức đăng nhập.
- Save/Unsave.
- Hashtag.
- Search.
- Report.
- Moderation Case.
- Gắn, thay đổi và gỡ Location tùy chọn trên Post.
- Admin khóa tài khoản.
- Admin ẩn/khôi phục bài.
- User Block hai chiều về hiển thị/tương tác; Block xóa Follow hai chiều và Unblock không khôi phục Follow.

### P2

- Reply bình luận một cấp.
- Thông báo REST và realtime bằng WebSocket/STOMP.
- Nhắn tin trực tiếp một-một text-only: Database, REST Core, realtime best-effort, UI và Typing Indicator đã triển khai đến Giai đoạn 1D.
- Lịch sử thao tác quản trị đơn giản.

Phạm vi Notification realtime Giai đoạn 1:

- MySQL và REST API là nguồn sự thật; WebSocket chỉ là kênh cập nhật best-effort.
- Frontend kết nối STOMP native tại `/ws` bằng JWT Access Token trong header `Authorization` của
  frame `CONNECT`, sau đó subscribe `/user/queue/notifications`.
- Chỉ phát `NOTIFICATION_CREATED` sau khi transaction tạo Notification đã commit thành công.
- Badge unread dùng giá trị authoritative từ Backend, hiển thị tối đa `99+`.
- Khi socket mất kết nối, Frontend polling unread count mỗi 30 giây khi tab đang visible và reconcile
  lại bằng REST sau khi kết nối hoặc tái kết nối.
- Giai đoạn này không realtime hóa read, read-all, delete hoặc invalidation; không dùng SockJS,
  outbox hay message broker ngoài.

Phạm vi Messaging đến Giai đoạn 1D và Backend gửi ảnh:

- Chỉ `USER`, `ACTIVE`, hoàn tất hồ sơ; người nhận phải Follow người gửi để bắt đầu conversation.
- Một cặp user có một conversation; conversation rỗng không vào Inbox và gửi tin đầu phải kiểm tra lại Follow.
- Block hai chiều ẩn list/history, chặn send/read và loại unread; Restrict không ảnh hưởng.
- Hỗ trợ `TEXT`, chỉ ảnh và ảnh kèm chú thích; content tối đa 2.000 Unicode code point. Mỗi message tối đa 5 ảnh JPG/JPEG/PNG/WEBP, mỗi ảnh tối đa 10 MB; không hỗ trợ video hoặc tài liệu.
- JSON text cũ được giữ nguyên; ảnh gửi REST multipart cùng path và idempotent bằng UUID v4 cùng fingerprint SHA-256 payload.
- Media chat lưu ở chế độ authenticated, response/event chỉ có metadata. Signed URL TTL ngắn chỉ cấp cho member hợp lệ sau khi kiểm tra USER/ACTIVE/onboarding và Block.
- Upload nằm ngoài transaction MySQL dài; rollback xóa bù và ghi durable cleanup task để scheduler retry khi cần.
- REST/MySQL là nguồn sự thật; WebSocket là best-effort. `MESSAGE_CREATED` và `MESSAGES_READ` phát after-commit, không tạo Notification. UI text có Inbox, detail, badge, optimistic send, cursor history và REST reconciliation; UI ảnh thuộc giai đoạn Frontend tiếp theo.
- Typing là trạng thái tạm thời: client chỉ `SEND` `{conversationId, typing}` đến `/app/messaging/typing`; Backend lấy user từ STOMP principal, kiểm tra account/membership/Block và chỉ phát `TYPING_STARTED`/`TYPING_STOPPED` cho participant còn lại. Không lưu DB, không unread, không Notification, không replay và Restrict không ảnh hưởng.

## 6. Ngoài phạm vi
- Hồ sơ riêng tư.
- Follow Request.
- Restrict.
- Video/tài liệu.
- Bản nháp.
- Mention.
- Quote Post.
- Chủ đề.
- Discovery Map.
- Tìm bài theo bán kính, Feed theo Location, trang Location riêng và địa điểm phổ biến.
- Quản trị Location, xác minh Backend và đồng bộ định kỳ với Google Places.
- Feed tùy chỉnh.
- Elasticsearch.
- Các chỉ số Dashboard nâng cao khác ngoài biểu đồ tương tác và USER nổi bật theo ngày.
- Audit Log chi tiết.

## 7. Tiêu chí nghiệm thu

- Đăng ký local chỉ tạo `pending_registrations`, chưa tạo `users`.
- OTP email hợp lệ mới tạo `users` và `user_profiles` trong cùng transaction.
- OTP hết hạn, đã dùng, bị hủy hoặc vượt số lần thử không thể sử dụng.
- Không tồn tại hai pending còn hiệu lực cho cùng email.
- Mất mạng hoặc đóng tab vẫn có thể tiếp tục đăng ký trong thời hạn.
- Google/Facebook token được Backend xác minh trước khi dùng.
- Provider đã liên kết luôn đăng nhập về đúng `users.id`.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Liên kết provider lấy tài khoản đích từ JWT hiện tại.
- Không cho gỡ phương thức đăng nhập cuối cùng.
- Social-only account chưa có mật khẩu không đăng nhập local được.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
- Người dùng phải hoàn tất hồ sơ bằng tên hiển thị và ngày sinh hợp lệ, đồng thời đủ 18 tuổi, trước khi dùng Feed và các chức năng mạng xã hội.
- Backend trả `PROFILE_NOT_COMPLETED` khi tài khoản chưa hoàn tất hồ sơ gọi API mạng xã hội chính.
- Token hoạt động đúng.
- Cập nhật hồ sơ.
- Follow/Unfollow không trùng.
- CRUD bài đúng quyền.
- Người dùng có thể tạo Post không có Location hoặc gắn tối đa một Location; nhiều Post cùng Place ID dùng chung một bản ghi Location.
- Update Post hỗ trợ `KEEP`, `REPLACE`, `REMOVE` Location và vẫn tuân theo quyền tác giả cùng giới hạn 15 phút.
- Xóa hoặc gỡ Location khỏi Post không xóa Location dùng chung; các Post response đã chốt trả Location object hoặc `null` nhất quán.
- Upload và cập nhật ảnh/video hợp lệ.
- Like không trùng.
- Bình luận đúng quyền.
- Save không trùng.
- Feed đúng nguồn.
- Search có phân trang.
- Report không trùng PENDING.
- Một bài không có hai Moderation Case `OPEN`; tạo Report và cập nhật case nằm trong cùng transaction.
- Admin chỉ giải quyết case `OPEN` một lần và danh sách quản trị không group dữ liệu tại Frontend.
- Admin quản lý được user, post, report.
- API từ chối khi không có quyền.
- Password không lưu plain text.

- Admin xem và tìm kiếm danh sách hashtag có phân trang với tên, số bài viết, ngày tạo và ngày sử dụng mới nhất.
- Admin được tạo hashtag theo quy tắc chuẩn hóa chung và xóa hashtag sau bước xác nhận.
- Admin được đổi tên hashtag theo quy tắc chuẩn hóa chung; không cho trùng tên và không thay đổi quan hệ bài viết.
- Xóa hashtag gỡ toàn bộ quan hệ `post_hashtags` trong cùng transaction; các Post liên quan trở thành bài không có hashtag và không bị xóa.
