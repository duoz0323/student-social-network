# 🎓 Student Social Network

> Website mạng xã hội tinh gọn hướng đến sinh viên, được phát triển trong khuôn khổ luận văn tốt nghiệp ngành Công nghệ thông tin.

---

## 📖 Giới thiệu

**Student Social Network** là nền tảng mạng xã hội tinh gọn hướng đến cộng đồng sinh viên, cho phép người dùng chia sẻ nội dung ngắn, kết nối, tương tác và khám phá các trải nghiệm thực tế liên quan đến học tập, ăn uống, vui chơi và đời sống sinh viên.

Hệ thống cung cấp các nhóm chức năng chính:

- Đăng ký và đăng nhập bằng email, Google hoặc Facebook.
- Xác minh email bằng mã OTP.
- Quản lý và liên kết nhiều phương thức đăng nhập trên cùng một tài khoản.
- Quản lý hồ sơ cá nhân.
- Theo dõi người dùng khác.
- Đăng tải và quản lý bài viết ngắn.
- Tương tác thông qua lượt thích, bình luận và lưu bài viết.
- Xem bảng tin cá nhân.
- Tìm kiếm người dùng và bài viết.
- Báo cáo nội dung vi phạm.
- Quản trị người dùng, bài viết và báo cáo.

Dự án được xây dựng theo mô hình **Client – Server**. Frontend và Backend được phát triển độc lập, giao tiếp thông qua RESTful API và sử dụng JWT để xác thực các request.

Phiên bản hiện tại tập trung hoàn thiện phạm vi **Minimum Viable Product – MVP**, ưu tiên một luồng sử dụng xuyên suốt, ổn định, có thể kiểm thử và trình bày trong luận văn.

---

## 🎯 Mục tiêu dự án

- Xây dựng một website mạng xã hội tinh gọn dành cho sinh viên.
- Tạo môi trường để sinh viên chia sẻ nội dung và kết nối với nhau.
- Hỗ trợ xác thực đa phương thức bằng email, Google và Facebook.
- Xây dựng đầy đủ luồng JWT Access Token và Refresh Token.
- Triển khai các chức năng cốt lõi của mạng xã hội.
- Áp dụng kiến trúc Frontend và Backend tách biệt.
- Đảm bảo khả năng mở rộng, bảo trì và kiểm thử.
- Áp dụng các nguyên tắc bảo mật trong quá trình xây dựng hệ thống.
- Hoàn thiện sản phẩm phục vụ báo cáo và bảo vệ luận văn tốt nghiệp.

---

## 👨‍🎓 Nhóm thực hiện

### Thành viên 1

- **Họ và tên:** Nguyễn Hoàng Dương
- **MSSV:** DH52200548
- **Vai trò:**
  - Phân tích yêu cầu nghiệp vụ.
  - Thiết kế cơ sở dữ liệu.
  - Phát triển Backend.
  - Xây dựng RESTful API.
  - Tích hợp bảo mật JWT.
  - Kiểm thử API.

### Thành viên 2

- **Họ và tên:** Đậu Quốc Khánh
- **MSSV:** DH52200867
- **Vai trò:**
  - Thiết kế giao diện người dùng.
  - Phát triển Frontend.
  - Tích hợp Frontend với RESTful API.
  - Kiểm thử giao diện và luồng người dùng.

### Giảng viên hướng dẫn

- **Giảng viên:** Hoàng Công Quang Huy

---

## 👥 Đối tượng sử dụng

### 1. Khách chưa đăng nhập

Khách chưa đăng nhập có thể:

- Bắt đầu đăng ký bằng email.
- Xác minh đăng ký bằng OTP.
- Đăng ký hoặc đăng nhập bằng Google/Facebook.
- Đăng nhập bằng email đã xác minh.
- Gửi lại mã xác minh hoặc tiếp tục đăng ký đang chờ.
- Khôi phục mật khẩu bằng OTP đối với tài khoản local đủ điều kiện; tài khoản social-only không dùng luồng này để tạo mật khẩu lần đầu.

Khách chưa đăng nhập không được truy cập các chức năng mạng xã hội.

### 2. Người dùng

Người dùng đã đăng nhập và có tài khoản đang hoạt động có thể:

- Hoàn tất và quản lý hồ sơ cá nhân.
- Quản lý các phương thức đăng nhập đã liên kết.
- Theo dõi người dùng khác.
- Chặn, bỏ chặn, hạn chế và bỏ hạn chế người dùng khác.
- Xem danh sách tài khoản đã chặn và đã hạn chế.
- Đăng và quản lý bài viết.
- Like, bình luận và lưu bài viết.
- Xem Feed.
- Tìm kiếm.
- Báo cáo bài viết hoặc trang cá nhân của người dùng khác vi phạm.

### 3. Quản trị viên

Quản trị viên có thể:

- Quản lý người dùng.
- Xem và chỉnh sửa ảnh đại diện, tên hiển thị, ngày sinh, phần giới thiệu của hồ sơ USER.
- Sau khi chỉnh sửa hồ sơ, gửi thông báo hệ thống cho USER rằng nội dung đã bị điều chỉnh do vi phạm Tiêu chuẩn hệ thống.
- Khóa và mở khóa tài khoản.
- Quản lý bài viết.
- Xem, tìm kiếm, tạo, đổi tên và xóa hashtag; đổi tên giữ nguyên liên kết bài viết, còn xóa khiến bài liên quan không còn hashtag.
- Ẩn và khôi phục bài viết.
- Xem và xử lý báo cáo vi phạm.
- Xem lịch sử thao tác quản trị khi chức năng tương ứng được triển khai.

---

## ✨ Phạm vi chức năng MVP

### 🔐 1. Xác thực, đăng ký và hoàn tất hồ sơ

#### Phương thức xác thực

Hệ thống hỗ trợ ba phương thức:

- Email và mật khẩu, xác minh bằng OTP email.
- Google.
- Facebook.

Hệ thống áp dụng mô hình:

```text
Một tài khoản nội bộ
        +
Nhiều phương thức xác thực
```

Mọi phương thức đã được xác minh và liên kết hợp lệ đều ánh xạ về cùng một `users.id`. Sau khi xác thực thành công, Backend cấp JWT Access Token và Refresh Token của hệ thống.

#### Đăng ký local bằng email

- Người dùng cung cấp một địa chỉ email tại mỗi yêu cầu đăng ký.
- Form đăng ký gồm email, mật khẩu và xác nhận mật khẩu.
- Backend loại bỏ khoảng trắng thừa và chuẩn hóa email về chữ thường trước khi kiểm tra.
- Hệ thống chưa tạo `users` và `user_profiles` ngay khi người dùng gửi form đăng ký.
- Backend tạo bản ghi `pending_registrations`, sinh OTP, lưu OTP và flow token dưới dạng hash rồi gửi mã xác minh.
- Chỉ sau khi OTP hợp lệ, hệ thống mới tạo đồng thời `users` và `user_profiles` trong cùng transaction.
- Nếu tạo `user_profiles` thất bại, toàn bộ transaction phải rollback.
- Sau khi tài khoản được tạo, Backend cấp Access Token và Refresh Token rồi chuyển người dùng đến onboarding.

#### Vòng đời đăng ký tạm

Trạng thái của `pending_registrations`:

- `PENDING`: đang chờ xác minh.
- `COMPLETED`: đã hoàn tất và tạo tài khoản thật.
- `CANCELLED`: người dùng hủy hoặc thay thế phương thức đăng ký.
- `EXPIRED`: đăng ký tạm đã hết thời hạn.

Chính sách mặc định:

- OTP có hiệu lực 10 phút.
- Chỉ cho gửi lại mã sau 60 giây.
- Tối đa 5 lần nhập sai cho mỗi OTP.
- Đăng ký tạm có hiệu lực 24 giờ.
- Dữ liệu `CANCELLED` và `EXPIRED` được lưu tối đa 7 ngày trước khi xóa hoặc ẩn danh.

#### Khôi phục luồng đăng ký

- Khi mất mạng, đóng tab hoặc rời màn hình xác minh, đăng ký tạm vẫn được giữ trong thời hạn.
- Người dùng có thể tiếp tục xác minh hoặc gửi lại mã.
- Nếu Frontend mất flow token, người dùng nhập lại email; Backend phát hiện đăng ký đang chờ và không tạo bản ghi trùng.
- OTP cũ phải mất hiệu lực khi mã mới được phát hành.

#### Đăng ký và đăng nhập bằng Google/Facebook

- Frontend nhận credential/token từ Google hoặc Facebook và gửi về Backend.
- Backend phải xác minh token với nhà cung cấp trước khi tin cậy thông tin người dùng.
- Google/Facebook token chỉ dùng tại endpoint xác thực chuyên biệt.
- Các API nghiệp vụ khác chỉ chấp nhận JWT do Backend phát hành.
- Nếu provider đã được liên kết, Backend đăng nhập đúng tài khoản hiện có.
- Nếu provider chưa được liên kết và thông tin hợp lệ chưa thuộc tài khoản nào, Backend tạo tài khoản nội bộ mới.
- Nếu social email trùng một tài khoản `ACTIVE` nhưng provider chưa được liên kết, hệ thống không tự động gộp chỉ dựa trên email.

#### Chuyển từ đăng ký OTP sang social

- Email đang chờ xác minh và social trả cùng email đã xác minh: được phép hoàn tất đăng ký tạm, giữ phương thức local và liên kết social vào cùng tài khoản.
- Email đang chờ nhưng social trả email khác: người dùng phải chọn tiếp tục OTP hoặc hủy đăng ký tạm để dùng social.
- Khi email social khác email đang chờ, social không được xem là bằng chứng xác minh email đang chờ; người dùng phải xác nhận hủy đăng ký email trước khi tiếp tục.
- Chỉ hủy hoặc hoàn tất đăng ký tạm sau khi Backend xác minh social token thành công.

#### Đăng nhập local

- Người dùng đăng nhập bằng email và mật khẩu.
- Email chỉ được dùng đăng nhập khi `email_verified_at` khác `NULL`.
- Tài khoản chỉ dùng social có thể có `password_hash = NULL` và chưa thể đăng nhập local.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.

#### Liên kết phương thức đăng nhập

Người dùng đã đăng nhập có thể liên kết thêm:

- Email.
- Google.
- Facebook.

Quy tắc:

- Email phải được xác minh OTP trước khi liên kết.
- Google/Facebook phải được xác minh trong phiên đang đăng nhập.
- `user_id` đích được lấy từ JWT hiện tại, không suy ra chỉ bằng email social.
- Email hoặc provider đã thuộc tài khoản khác phải bị từ chối.
- Không tự động gộp hai tài khoản đang hoạt động.
- Không được gỡ phương thức đăng nhập cuối cùng.
- Tài khoản social muốn dùng đăng nhập local phải thiết lập mật khẩu và xác minh email tương ứng.

#### Khôi phục mật khẩu

- Chỉ tài khoản `ACTIVE` đã có `password_hash` và EMAIL tương ứng đã verified mới đủ điều kiện.
- Tài khoản social-only không dùng Forgot Password để tạo mật khẩu lần đầu.
- Identifier không tồn tại, chưa verified, social-only hoặc `BLOCKED` đều nhận response start trung tính và một decoy challenge có lifecycle thật.
- Challenge có TTL 15 phút; OTP 10 phút và không vượt challenge expiry; cooldown 60 giây; tối đa 5 lần sai; reset-authorized token 5 phút.
- OTP và token chỉ lưu dạng HMAC hash. Recovery flow token và reset-authorized token chỉ truyền qua `X-Auth-Flow-Token`.
- OTP delivery chạy bất đồng bộ sau commit, không enqueue cho decoy và không giữ transaction khi gọi Gmail SMTP.
- Complete đổi mật khẩu, đánh dấu token single-use và thu hồi toàn bộ Refresh Token trong cùng transaction; không tự đăng nhập hoặc cấp JWT mới.
- Access Token stateless đã phát hành còn hiệu lực tới expiry vì hệ thống hiện chưa dùng `tokenVersion`.
- `password_reset_tokens` là bảng legacy được giữ nguyên để audit; implementation mới không đọc hoặc ghi bảng này.

#### JWT và quản lý phiên

- Xác thực bằng JWT Access Token.
- Làm mới Access Token bằng Refresh Token.
- Refresh Token được lưu dưới dạng hash.
- Thu hồi Refresh Token khi đăng xuất.
- Phân quyền `USER` và `ADMIN`.

#### Hoàn tất hồ sơ ban đầu

- Sau khi tài khoản thật được tạo, Frontend điều hướng đến onboarding.
- Username, tên hiển thị và ngày sinh là thông tin bắt buộc.
- Username là định danh public duy nhất, dài 3–30 ký tự, chỉ gồm `a-z`, `0-9`, `_`, `.`, được trim và chuẩn hóa lowercase trước khi lưu; ký tự `@` chỉ do Frontend thêm khi hiển thị.
- Người dùng phải đủ 18 tuổi tại thời điểm Backend xử lý.
- Ảnh đại diện và giới thiệu cá nhân là tùy chọn.
- Hồ sơ chỉ được đánh dấu hoàn tất khi username, tên hiển thị và ngày sinh hợp lệ, đồng thời người dùng xác nhận.
- Trạng thái hoàn tất được lưu tại `user_profiles.profile_completed_at`.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép các API xác thực, token, đăng xuất và onboarding.

Trạng thái feature username: Backend và Frontend `IMPLEMENTED`; unit test, lint và build `TESTED`. Manual local đã xác minh availability, validation, race-condition, legacy hydration và hiển thị profile current/other. Chưa đánh dấu `INTEGRATED` cho toàn bộ feature cho đến khi hoàn tất manual E2E đăng ký local qua OTP và tạo user mới bằng Google/Facebook với credential thật.
- Các API mạng xã hội chính trả lỗi `PROFILE_NOT_COMPLETED` nếu hồ sơ chưa hoàn tất.

### 👤 2. Hồ sơ người dùng

- Hoàn tất hồ sơ ban đầu sau khi tạo tài khoản.
- Xem hồ sơ cá nhân.
- Xem hồ sơ người dùng khác.
- Cập nhật tên hiển thị.
- Cập nhật ảnh đại diện.
- Cập nhật phần giới thiệu cá nhân.
- Cập nhật ngày sinh.
- Xem số lượng người theo dõi.
- Xem số lượng người đang theo dõi.
- Xem danh sách bài viết đã đăng.

Tất cả hồ sơ trong MVP đều công khai. Email và dữ liệu xác thực không được trả trong API hồ sơ công khai.
Username được trả không kèm ký tự `@`, không được trùng giữa các hồ sơ và không được dùng thay `users.id` trong quan hệ nội bộ.

### 👥 3. Theo dõi người dùng

- Theo dõi người dùng.
- Bỏ theo dõi người dùng.
- Xem danh sách người theo dõi.
- Xem danh sách đang theo dõi.

Quy tắc:

- Không được theo dõi chính mình.
- Một người chỉ được theo dõi một tài khoản tối đa một lần.
- Quan hệ theo dõi có hiệu lực ngay.
- MVP chưa triển khai hồ sơ riêng tư và Follow Request.

### 🚫 3.1. Chặn người dùng

Trạng thái nghiệp vụ: `INTEGRATED` và `TESTED`. Quy tắc hiển thị bình luận lịch sử đã được triển khai thống nhất ở Backend/Frontend. Manual E2E tiếp tục thực hiện theo checklist trước khi phát hành.

- User Block là quan hệ có hướng: A chặn B được lưu khác với B chặn A.
- Hiệu lực về khả năng nhìn thấy và tương tác là hai chiều: chỉ cần tồn tại Block theo một trong hai hướng thì cả A và B không được xem hồ sơ, bài viết hoặc nội dung do đối phương tạo trong các luồng dành cho người dùng.
- User Block khác hoàn toàn với `users.status = BLOCKED`; trạng thái này do Admin khóa tài khoản ở cấp hệ thống.
- Không cho phép người dùng chặn chính mình; Block và Unblock phải có tính idempotent.
- Block xóa cả Follow A → B và Follow B → A trong cùng transaction.
- Trong thời gian tồn tại Block, hai bên không thể tạo Follow, Like/Unlike, Comment/Reply, Save/Unsave hoặc Notification mới liên quan đến nhau.
- Block không xóa Like hoặc Comment đã tồn tại trước thời điểm chặn khỏi cơ sở dữ liệu và không làm giảm `like_count` hoặc `comment_count`; các bản ghi này được giữ để bảo toàn lịch sử tương tác.
- Trong thời gian tồn tại Block, các bình luận và câu trả lời bình luận do một bên tạo phải bị ẩn khỏi bên còn lại, kể cả khi bình luận được viết trên bài do bên còn lại sở hữu.
- Việc ẩn bình luận do Block là kiểm soát hiển thị động tại query hoặc policy truy cập; không chuyển bình luận sang trạng thái `DELETED`, không xóa mềm và không tạo trạng thái ẩn vĩnh viễn cho bình luận.
- Nếu bình luận cha bị ẩn do Block thì các câu trả lời thuộc nhánh bình luận đó cũng phải bị ẩn để không xuất hiện câu trả lời mồ côi trên giao diện.
- Người thực hiện Block và người bị Block không còn nhìn thấy bài viết, Post Detail, Feed, Search, Liked, Saved, bình luận hoặc Notification của nhau trong các luồng dành cho người dùng.
- Khi Unblock, các Like và Comment lịch sử vẫn được giữ nguyên; bình luận được hiển thị trở lại nếu vẫn còn ở trạng thái hợp lệ và người xem có quyền truy cập bài viết.
- Unblock không tự khôi phục Follow, không tạo Notification và không tự tạo lại bất kỳ quan hệ xã hội nào đã bị xóa khi Block.
- Feed, hồ sơ, Post Detail, Search User/Post, follower/following, Liked, Saved, comment và notification phải áp dụng chính sách Block hai chiều ngay tại Backend; các danh sách phân trang phải lọc tại database thay vì lấy dữ liệu rồi lọc bằng Java.
- Private Account và Follow Request chưa được triển khai.

### 🔕 3.2. Hạn chế người dùng

Trạng thái nghiệp vụ: `INTEGRATED` và `TESTED`. Restrict là quan hệ một chiều; A hạn chế B không đồng nghĩa B hạn chế A.

- A và B vẫn tìm kiếm, xem hồ sơ, bài viết, Feed và tương tác với nhau theo quyền truy cập bình thường. Restrict không được dùng để lọc Feed hoặc Search.
- Restrict không hủy Follow, không thay đổi số follower/following và không suppress thông báo Follow.
- Restrict không xóa Like, Save, Comment, Reply, Follow hoặc Notification đã tồn tại trước thời điểm hạn chế.
- Like/Unlike, Save/Unsave, Comment và Reply vẫn hoạt động bình thường; Comment và Reply tiếp tục có trạng thái `PUBLISHED` và vẫn được tính vào bộ đếm.
- Khi A đã hạn chế B, Backend không tạo Notification `POST_LIKE`, `POST_COMMENT` hoặc `COMMENT_REPLY` do B gửi tới A. Vì không tạo bản ghi nên unread count, badge và WebSocket/event tương ứng cũng không tăng hoặc phát.
- Các thông báo quản trị, báo cáo, bảo mật, hệ thống và Follow không bị suppress bởi Restrict.
- B không được thông báo và API công khai không được trả bất kỳ trường nào cho phép B biết A đã hạn chế mình. Response quan hệ chỉ được phép trả `restrictedByMe` cho chính người đang xem.
- Block có độ ưu tiên cao hơn Restrict. Không được tạo Restrict khi có Block ở một trong hai chiều.
- Khi A Block B, quan hệ Restrict A → B bị xóa nhưng không tự ý xóa Restrict B → A. Unblock không tự khôi phục Restrict A → B.
- Unrestrict chỉ xóa quan hệ do chính current user tạo, không tạo Notification và không tạo bù các Notification đã bị bỏ qua.
- Restrict trong MVP không thay đổi nghiệp vụ nhắn tin và không triển khai Hidden Message Request hoặc cơ chế duyệt bình luận.

### 💬 3.3. Nhắn tin trực tiếp một-một

Trạng thái nghiệp vụ: Database, REST Core, realtime WebSocket, giao diện text, gửi/hiển thị ảnh và Typing Indicator Giai đoạn 1D `INTEGRATED` và `TESTED`. Migration cùng 7 concurrency test đã chạy thành công trên MySQL 8.4 tạm; smoke test E2E hai trình duyệt chưa triển khai.

- Conversation một-một hỗ trợ `TEXT`, chỉ ảnh hoặc ảnh kèm chú thích. Nội dung/chú thích tối đa 2.000 Unicode code point; mỗi message có tối đa 5 ảnh JPG/JPEG/PNG/WEBP, mỗi ảnh tối đa 10 MB; video và tài liệu chưa hỗ trợ.
- Chỉ tài khoản `USER`, `ACTIVE` và đã hoàn tất hồ sơ được dùng Messaging; `ADMIN` không dùng Messaging như tài khoản xã hội và không được nhắn chính mình.
- Một cặp người dùng chỉ có một conversation logic, chuẩn hóa bằng `participant_low_id` và `participant_high_id`; conversation và đúng hai member được tạo trong cùng transaction.
- A chỉ được bắt đầu conversation với B khi B đang Follow A. Conversation rỗng không xuất hiện trong Inbox và phải kiểm tra lại điều kiện Follow khi gửi tin đầu tiên; sau tin đầu tiên, Unfollow không đóng conversation.
- Database gồm `conversations`, `conversation_members`, `messages`, `message_attachments` và `media_cleanup_tasks`; cặp participant, `(sender_id, client_message_id)`, thứ tự attachment và định danh storage là duy nhất. `last_message_id` và `last_read_message_id` phải thuộc đúng conversation và được Service kiểm soát ngoài foreign key.
- REST Core giữ POST JSON gửi text và bổ sung cùng path POST với `multipart/form-data` gồm `clientMessageId`, `content` tùy chọn, `images` tối đa 5. URL ảnh ngắn hạn chỉ được cấp qua `GET /api/v1/message-attachments/{attachmentId}/access` sau khi kiểm tra lại quyền.
- Danh sách conversation và lịch sử message dùng cursor Base64URL opaque, keyset pagination, không truy vấn tổng số. Page lịch sử được truy vấn mới nhất trước nhưng trả nội dung theo thời gian tăng dần trong từng page.
- Gửi message lấy sender từ JWT, nhận UUID v4 `clientMessageId` và Backend tự xác định `TEXT`/`IMAGE`. Payload ảnh có fingerprint gồm conversation, content, số ảnh, MIME, size và SHA-256 từng file; replay chính xác không upload/phát event lại, còn tái sử dụng key với payload khác trả `IDEMPOTENCY_KEY_REUSED`.
- Mark read chỉ tiến lên, không thay đổi `last_read_at` khi marker cũ được gửi lại. Unread được tính trực tiếp từ message của participant còn lại, không dùng counter denormalized.
- Client gửi message bằng REST API; WebSocket/STOMP chỉ dùng để nhận event realtime theo mô hình best-effort.
- REST API và MySQL là nguồn dữ liệu chuẩn; khi mất kết nối realtime, Frontend phải reconciliation lại bằng REST.
- Notification và Messaging dùng chung đúng một native WebSocket/STOMP connection trên mỗi tab tại endpoint `/ws`.
- Frontend subscribe Notification tại `/user/queue/notifications` và Messaging tại `/user/queue/messaging` trên cùng connection.
- JWT tiếp tục được xác thực tại STOMP `CONNECT`; principal name là chuỗi `users.id`. Client chỉ được `SEND` đúng destination `/app/messaging/typing`; mọi destination khác hoặc frame không có destination đều bị từ chối.
- User Block theo một trong hai chiều phải ẩn conversation, chặn history/send/read, loại khỏi unread và chặn nhận realtime; Unblock làm dữ liệu cũ xuất hiện lại. Open/send và Block khóa cặp user theo thứ tự ID ổn định để tuần tự hóa race; Restrict không ảnh hưởng Messaging.
- Message không tạo bản ghi Notification. `MESSAGE_CREATED` và `MESSAGES_READ` chỉ phát `AFTER_COMMIT`; payload message bổ sung metadata `attachments` nhưng không có URL/storage ID. Broker lỗi không rollback REST/MySQL đã commit và không thay đổi contract Notification.
- Upload ảnh dùng Cloudinary `authenticated` ngoài transaction MySQL dài. Khi transaction ngắn lưu message/attachment thất bại, Backend xóa bù ngay; lỗi xóa được ghi durable cleanup task và scheduler retry.
- Media chat không lưu hoặc trả URL công khai vĩnh viễn. Endpoint access chống IDOR, kiểm tra USER/ACTIVE/onboarding/membership/Block và cấp signed URL với TTL cấu hình, mặc định 5 phút.
- Typing Indicator Giai đoạn 1D đã hoàn thành qua `TYPING_STARTED`/`TYPING_STOPPED`, không lưu database, không replay và không thay đổi unread.
- Tài liệu, video, Message Request, Hidden Message Request, online status, delivered status, recall, xóa conversation phía cá nhân và report message chưa thuộc phạm vi.

### 📝 4. Quản lý bài viết

- Tạo bài viết.
- Xem chi tiết bài viết.
- Chỉnh sửa bài viết trong 15 phút sau khi đăng.
- Frontend hiển thị countdown thời gian sửa còn lại cạnh hành động chỉnh sửa và tự ẩn hành động khi hết 15 phút; Backend tính deadline bằng UTC và vẫn là nơi quyết định cuối cùng.
- Xóa mềm bài viết.
- Đăng nội dung văn bản tối đa 500 ký tự.
- Tải lên tối đa 4 media cho một bài viết, gồm ảnh và video.
- Mỗi bài có tối đa 4 ảnh hoặc tối đa 1 video trong tổng giới hạn 4 media.
- Ảnh hỗ trợ JPG, JPEG, PNG, WEBP và tối đa 10 MB mỗi file.
- Video hỗ trợ MP4, WebM, tối đa 100 MB và dài không quá 3 phút.
- Gắn tối đa một hashtag.
- Tùy chọn gắn tối đa một địa điểm Google Places đã được Frontend chọn.

Bài viết phải có ít nhất một trong hai thành phần:

- Nội dung văn bản.
- Hình ảnh.

Trạng thái bài viết:

- `PUBLISHED`: bài viết đang hoạt động.
- `HIDDEN`: bài viết bị quản trị viên ẩn.
- `DELETED`: bài viết đã được tác giả xóa mềm.

#### Địa điểm gắn với bài viết – P1

- Quan hệ dữ liệu là `Location 1 — N Post`: một địa điểm có thể được nhiều bài viết sử dụng; một bài viết có thể không có hoặc có tối đa một địa điểm.
- Frontend gửi object Location tùy chọn gồm `placeId`, `displayName`, `formattedAddress`, `latitude` và `longitude`.
- Backend validate dữ liệu, chuẩn hóa chuỗi và chỉ dùng `placeId` làm natural unique key. Backend không dùng tên hoặc tọa độ để xác định trùng và chưa gọi Google Places API để xác minh trong giai đoạn này.
- Nếu `google_place_id` đã tồn tại, Backend dùng lại `locations`; nếu chưa tồn tại, Backend tạo một bản ghi mới. Các bài dùng cùng Google Place ID không được tạo Location riêng.
- `posts.location_id` cho phép `NULL`, không có unique constraint và tham chiếu `locations.id` bằng khóa ngoại `ON DELETE SET NULL`.
- Entity `Post` ánh xạ Location bằng `@ManyToOne(fetch = FetchType.LAZY)` và `@JoinColumn(name = "location_id")`; không dùng `@OneToOne`, `CascadeType.REMOVE` hoặc `orphanRemoval` cho quan hệ này. Mapping `authorProfile` hiện tại không thuộc thay đổi này.
- Khi cập nhật bài trong giới hạn 15 phút và đúng quyền tác giả, Location hỗ trợ ba hành động: `KEEP` giữ nguyên, `REPLACE` resolve theo Place ID rồi thay thế, và `REMOVE` gỡ quan hệ bằng cách đặt `location_id = NULL`.
- Khi cập nhật bài trong cùng giới hạn quyền và 15 phút, người dùng có thể giữ, gỡ media cũ hoặc thêm ảnh/video mới; tổng media sau cập nhật vẫn phải tuân thủ các giới hạn media của Post.
- Soft delete hoặc hard delete Post không xóa Location. Không cascade remove, không dùng `orphanRemoval` và không tự động xóa Location không còn được tham chiếu.
- Location hoặc `null` phải xuất hiện nhất quán trong response tạo bài, chi tiết bài, Feed For You, Feed Following, bài trên hồ sơ, bài đã lưu, bài đã thích, kết quả tìm kiếm Post và chi tiết Post dành cho Admin.
- Admin Post Detail hiển thị Location hiện tại. Report snapshot chưa lưu Location trong đợt này.
- Discovery Map, tìm theo bán kính, Feed theo Location, trang Location, địa điểm phổ biến, quản trị Location và đồng bộ định kỳ với Google Places không thuộc phạm vi P1 này.

### ❤️ 5. Tương tác bài viết

- Like và Unlike bài viết.
- Xem danh sách bài viết đã thích của chính mình.
- Thêm bình luận.
- Xem danh sách bình luận.
- Xóa bình luận của chính mình.
- Lưu và bỏ lưu bài viết.
- Xem danh sách bài viết đã lưu.
- Repost và Unrepost bài viết của người khác.
- Xem tab bài đã đăng lại trên hồ sơ.

Quy tắc:

- Mỗi người chỉ được Like một bài tối đa một lần.
- Mỗi người chỉ được lưu một bài tối đa một lần.
- Mỗi người chỉ được Repost một bài tối đa một lần; Repost và Unrepost là idempotent.
- Chỉ được Repost bài `PUBLISHED` có quyền xem và không được Repost bài của chính mình.
- Repost chỉ lưu quan hệ tham chiếu đến bài gốc, không sao chép content, media, hashtag hoặc Location.
- Bài gốc `HIDDEN` hoặc `DELETED` không được hiển thị qua Repost.
- Không được tương tác với bài viết đã bị ẩn hoặc xóa.
- Danh sách bài viết đã lưu chỉ hiển thị với chủ tài khoản.
- Danh sách bài viết đã thích chỉ hiển thị với chủ tài khoản.
- Frontend cập nhật ngay trạng thái và bộ đếm Like, Comment, Save, Repost từ response REST; các tab cùng trình duyệt đồng bộ qua `BroadcastChannel` và vô hiệu hóa đúng cache danh sách liên quan.
- Khi tab trở lại foreground, Frontend reconciliation danh sách đang hiển thị bằng REST. Notification WebSocket của Like, Comment, Reply và Repost kích hoạt reconciliation Post cho người nhận; MySQL và REST API vẫn là nguồn dữ liệu chuẩn.

### 📰 6. Bảng tin

#### Feed Following

- Hiển thị activity bài gốc và Repost của những người dùng đang được theo dõi.
- Item `ORIGINAL` chứa bài gốc; item `REPOST` chứa `activityAt`, `repostedAt`, `repostedBy` và projection `post` của bài gốc.
- Sắp xếp giảm dần theo thời điểm activity với khóa tổng ổn định `activityAt`, `itemRank`, `actorId`, `postId`.
- Không hiển thị bài viết bị ẩn hoặc bị xóa.
- Dùng Cursor Pagination để cuộn vô hạn; Frontend chỉ gửi lại nguyên cursor opaque do Backend phát hành.

#### Feed For You

- Hiển thị các bài viết hợp lệ trong hệ thống.
- Ưu tiên bài viết mới.
- Có thể xếp hạng theo số lượt Like và số lượng bình luận.
- Không sử dụng Machine Learning trong MVP.
- Hạn chế hiển thị liên tiếp quá nhiều bài viết của cùng một tác giả.

Công thức xếp hạng tham khảo:

```text
Điểm bài viết =
    Điểm độ mới
    + Số lượt Like × Trọng số Like
    + Số bình luận × Trọng số bình luận
```

Các danh sách bài viết dùng Infinite Scroll gồm Feed For You, Feed Following, bài trên hồ sơ,
bài đã lưu, bài đã thích và kết quả Search bài viết/hashtag. Các API này dùng Cursor Pagination:

- Request đầu: `?limit=10`.
- Request tiếp theo: `?limit=10&cursor=<opaque-cursor>`.
- `limit` mặc định 10, từ 1 đến 20.
- Response dữ liệu: `{ "content": [], "nextCursor": null, "hasNext": false }`.
- Cursor do Backend tạo dưới dạng Base64URL opaque; Client không tự tạo hoặc sửa cursor.
- Feed For You giữ đủ khóa xếp hạng `score`, `publishedAt`, `postId`; các danh sách theo thời gian
  giữ `createdAt`, `postId`.
- Feed Following giữ đủ khóa activity `activityAt`, `itemRank`, `actorId`, `postId`.
- Backend dùng keyset và lấy `limit + 1`, không dùng `page`, `offset` hoặc truy vấn tổng `COUNT(*)`.
- Cursor không hợp lệ trả mã nghiệp vụ `INVALID_CURSOR`.

Các endpoint đã chuyển:

- `GET /api/v1/feeds/for-you`.
- `GET /api/v1/feeds/following`.
- `GET /api/v1/users/{userId}/posts`.
- `GET /api/v1/users/{userId}/reposts`.
- `GET /api/v1/posts/saved`.
- `GET /api/v1/posts/liked`.
- `GET /api/v1/search/posts`.

Search người dùng, bình luận, Follow và Admin vẫn dùng `PageResponse` vì giao diện hiện tại
không dùng Infinite Scroll hoặc cần metadata tổng số/trang.

### #️⃣ 7. Hashtag

- Mỗi bài viết có tối đa một hashtag.
- Xóa toàn bộ ký tự `#` ở đầu.
- Trim khoảng trắng Unicode.
- Gộp nhiều khoảng trắng liên tiếp thành một.
- Chuẩn hóa Unicode NFC.
- Chuyển về chữ thường.
- Hỗ trợ tiếng Việt có dấu và hashtag gồm nhiều từ.
- Giới hạn tối đa 100 code point.
- Hashtag không được tính là nội dung bài viết.
- Có API gợi ý hashtag liên quan, không phân trang.
- Người dùng có thể xem danh sách bài viết theo hashtag.

### 🔍 8. Tìm kiếm

- Tìm kiếm người dùng theo tên hiển thị.
- Tìm kiếm bài viết theo nội dung.
- Tìm kiếm bài viết theo hashtag.
- Search người dùng dùng `PageResponse`; Search bài viết/hashtag dùng Cursor Pagination và Infinite Scroll.
- MVP sử dụng MySQL làm nguồn tìm kiếm.
- Elasticsearch được đưa vào hướng phát triển sau khi hệ thống cốt lõi ổn định.

### 🚩 9. Báo cáo vi phạm

- Mỗi lần báo cáo bài viết tạo một dòng `reports` độc lập, giữ riêng reporter, reason, description và snapshot bằng chứng.
- Mỗi Report thuộc một `Moderation Case`; nhiều Report cùng bài được gắn vào case `OPEN` hiện tại.
- Một bài chỉ có tối đa một case `OPEN` tại một thời điểm. Case mới chỉ được tạo khi không còn case mở.
- Cùng một người không được báo cáo lại bài đang có Report thuộc case `OPEN`; sau khi case cũ được giải quyết có thể báo cáo lại nếu bài vẫn đủ điều kiện.

Báo cáo trang cá nhân:

- Chỉ được báo cáo trang cá nhân của USER khác; không được tự báo cáo hoặc báo cáo khi hai tài khoản đang có quan hệ Block.
- Sáu lý do cố định: nội dung không được phép, giả mạo, chưa đủ tuổi tối thiểu, lừa đảo/gian lận, thông tin sai sự thật, bạo lực/tổ chức nguy hiểm.
- Mỗi reporter chỉ có tối đa một báo cáo `PENDING` cho cùng một trang cá nhân.
- Mỗi trang cá nhân chỉ có một `profile_report_cases`; mọi `profile_reports` từ nhiều reporter được gom vào case này.
- `profile_reports` giữ từng reporter, lý do và snapshot; Admin đồng thời xem hồ sơ hiện tại, danh sách người báo cáo và các bài viết hiện tại của target.
- Báo cáo mới mở lại case đã kết luận; lịch sử kết luận vẫn được giữ trong `admin_actions`.
- Admin kết luận đồng thời toàn bộ lượt `PENDING` trong case thành `RESOLVED` hoặc `REJECTED`.
- Khi xác nhận trang cá nhân vi phạm, Admin có quyền khóa ngay tài khoản target trong cùng transaction; nếu không chọn khóa thì chỉ kết luận case.

Trạng thái báo cáo:

- `PENDING`.
- `RESOLVED`.
- `REJECTED`.

Trạng thái Moderation Case:

- `OPEN`: đang chờ Admin đưa ra quyết định và tiếp tục nhận Report hợp lệ mới.
- `RESOLVED_NO_VIOLATION`: Admin kết luận bài không vi phạm; các Report trong case chuyển `REJECTED`.
- `RESOLVED_ACTION_TAKEN`: Admin kết luận có vi phạm và đã áp dụng hành động; các Report chuyển `RESOLVED`.

Mỗi Moderation Case chuyển sang `RESOLVED_ACTION_TAKEN` được tính đúng một lần vi phạm bài viết cho tác giả,
không phụ thuộc số Report/reporter trong case. Khi tổng số case vi phạm của tác giả đạt 3, Backend tự động khóa
tài khoản bằng lý do `REPEATED_VIOLATION`, thu hồi Refresh Token, ghi lịch sử trạng thái, Admin Action và thông báo.

Admin xử lý trực tiếp từ `OPEN` sang một trong hai kết quả cuối. Không có bước tiếp nhận, trạng thái
`IN_REVIEW`, trạng thái `CLOSED`, `assigned_admin_id` hoặc `closed_at`. Case đã giải quyết không nhận
Report mới và không được xử lý lại.

Gửi báo cáo không tự động làm ẩn bài viết. Quản trị viên là người đưa ra quyết định xử lý cuối cùng.
Luồng tạo Report khóa bản ghi Post bằng `PESSIMISTIC_WRITE`, sau đó tìm/tạo case, tạo Report và cập
nhật `report_count` trong cùng transaction. Unique generated key trên case là lớp bảo vệ database để
không tồn tại hai case `OPEN` cho cùng bài.

### 🛡️ 10. Quản trị hệ thống

#### Quản lý người dùng

- Xem danh sách người dùng.
- Tìm kiếm người dùng.
- Chỉnh sửa ảnh đại diện và nội dung hồ sơ của tài khoản USER.
- Khóa và mở khóa tài khoản.
- Lưu lý do thay đổi trạng thái tài khoản.
- Thu hồi Refresh Token còn hiệu lực khi khóa tài khoản.

#### Quản lý bài viết

- Xem danh sách và chi tiết bài viết.
- Ẩn và khôi phục bài viết.
- Không khôi phục bài viết đã bị tác giả xóa mềm.

#### Quản lý hashtag

- Xem và tìm kiếm danh sách hashtag có phân trang.
- Hiển thị tên hashtag, số bài viết đang liên kết, ngày tạo và ngày sử dụng mới nhất.
- Admin có thể tạo hashtag theo cùng quy tắc chuẩn hóa dùng khi tạo bài viết; không cho tạo tên trùng sau chuẩn hóa.
- Admin có thể đổi tên hashtag; toàn bộ bài đang sử dụng tự hiển thị tên mới do quan hệ giữ nguyên `hashtag_id`.
- Khi Admin xóa hashtag, Backend gỡ hashtag khỏi mọi bài liên quan rồi xóa hashtag trong cùng transaction; bài viết, nội dung và media vẫn được giữ nguyên.
- Thao tác tạo/xóa hashtag được ghi vào lịch sử quản trị.

#### Quản lý báo cáo

- Xem danh sách Moderation Case, mỗi case đúng một dòng và phân trang tại database.
- Xem chi tiết toàn bộ Report theo dạng rút gọn gồm reporter, reason, description và thời gian; snapshot vẫn được lưu độc lập làm bằng chứng nhưng không render trong từng Report trên giao diện Admin.
- Kết luận trực tiếp không vi phạm hoặc có vi phạm và ẩn bài.
- Chỉ một Admin được phép giải quyết case `OPEN` khi có thao tác đồng thời.

#### Hoạt động quản trị

- Ghi lại các hành động quản trị quan trọng.
- Hỗ trợ hiển thị lịch sử hoạt động của quản trị viên.

#### Analytics hoạt động người dùng — Backend/Frontend `IMPLEMENTED`, Frontend `TESTED`, test MySQL `CONDITIONAL`

Analytics gồm module quản trị độc lập tại `/admin/user-analytics` và widget Dashboard tại `/admin`. Frontend gọi hai
API monthly/summary qua service tập trung, có bộ lọc khoảng tháng và ngưỡng không hoạt động, bốn KPI, biểu đồ
hai trục cho số người quay lại/tỷ lệ tái kích hoạt và bảng snapshot tháng cuối cùng các trạng thái Loading/Empty/Error. Một hoạt động hợp lệ
là request nghiệp vụ thành công đại diện cho hành vi thực của USER như mở Feed, xem chi tiết bài, tạo hoặc
sửa bài, Like/Save, bình luận hoặc Follow. Refresh Token, Auth/OTP, health check, WebSocket heartbeat,
request Admin và request nền không tạo activity. Ngày hoạt động dùng UTC; mỗi USER có tối đa một dòng
`user_daily_activities` trong một ngày nhờ unique `(user_id, activity_date)` và MySQL UPSERT atomic.

Widget Dashboard gọi `GET /api/v1/admin/analytics/user-engagement/dashboard?days=30` (từ 1 đến 90 ngày) để
hiển thị chuỗi tổng `activity_count` theo từng ngày UTC. Response luôn bù các ngày chưa có dữ liệu bằng `0` và
trả tối đa 5 USER nổi bật của ngày UTC hiện tại. USER nổi bật phải `ACTIVE`, hoàn tất hồ sơ; được xếp theo số bài
`PUBLISHED` tạo trong ngày giảm dần, sau đó theo tổng `activity_count` giảm dần. Dashboard không hiển thị lịch sử
hoạt động chi tiết hoặc khối “Hoạt động gần đây”.

Eligible System User tại `evaluationDate` là tài khoản đã được tạo, có `role = USER`, hồ sơ đã hoàn tất và
ở trạng thái `ACTIVE` tại ngày đánh giá. Trạng thái tháng lịch sử được dựng lại từ
`account_status_histories`. Tại mỗi tháng, toàn bộ tài khoản USER hợp lệ được phân loại vào đúng một trong
sáu nhóm `NEW`, `REGULAR`, `RETURNING`, `RECENTLY_INACTIVE`, `ELIGIBLE_INACTIVE_NOT_RETURNED` hoặc
`NEVER_ACTIVE`. Ba nhóm đầu có hoạt động trong tháng; ba nhóm sau không hoạt động trong tháng. Các nhóm
không chồng lặp và tổng sáu nhóm bằng Eligible System User.

- `NEW`: có activity trong tháng và không có activity trước đầu tháng.
- `REGULAR`: có activity trong tháng, có activity trước tháng và khoảng cách tới activity đầu tháng
  `<= inactiveDays`.
- `RETURNING`: có activity trong tháng, có activity trước tháng và khoảng cách đó `> inactiveDays`.
- `RECENTLY_INACTIVE`: không activity trong tháng, đã từng activity và tại đầu tháng chưa vượt ngưỡng.
- `ELIGIBLE_INACTIVE_NOT_RETURNED`: không activity trong tháng và đã vượt ngưỡng tại đầu tháng.
- `NEVER_ACTIVE`: chưa từng có activity hợp lệ tính đến ngày đánh giá.

Công thức:

```text
activeUserCount = NEW + REGULAR + RETURNING
inactiveUserCount = RECENTLY_INACTIVE + ELIGIBLE_INACTIVE_NOT_RETURNED + NEVER_ACTIVE
eligibleSystemUserCount = activeUserCount + inactiveUserCount
eligibleInactiveUserCount = returningEligibleUserCount + eligibleInactiveNotReturnedUserCount
Monthly Active User Rate = activeUserCount / eligibleSystemUserCount × 100
Regular Active Rate = regularActiveUserCount / activeUserCount × 100
Monthly Reactivation Rate = returningEligibleUserCount / eligibleInactiveUserCount × 100
Never Active Rate = neverActiveUserCount / eligibleSystemUserCount × 100
```

Rate trả `null` khi mẫu số bằng 0. Điều kiện inactive dùng toán tử `>`: đúng 15 ngày là REGULAR/chưa đủ
inactive, từ 16 ngày mới là RETURNING/eligible inactive khi `inactiveDays=15`. Tháng hiện tại dùng ngày UTC
hiện tại làm `evaluationDate`; tháng đã qua dùng ngày cuối tháng.

---

## 📌 Mức độ ưu tiên triển khai

### P0 – Bắt buộc hoàn thành

- Đăng ký email có OTP.
- Đăng ký và đăng nhập Google/Facebook.
- Đăng nhập local.
- Hoàn tất hồ sơ ban đầu.
- Đăng xuất.
- JWT Access Token và Refresh Token.
- Phân quyền `USER` và `ADMIN`.
- Quản lý hồ sơ.
- Follow và Unfollow.
- Block, Unblock, Restrict, Unrestrict và danh sách quan hệ do người dùng thiết lập.
- CRUD bài viết.
- Upload hình ảnh.
- Like, Unlike và xem danh sách bài viết đã thích.
- Bình luận.
- Feed Following.
- Feed For You.

### P1 – Cần hoàn thành

- Liên kết và quản lý nhiều phương thức đăng nhập.
- Save và Unsave.
- Repost, Unrepost, tab Repost trên Profile và activity Repost trong Following Feed.
- Hashtag và gợi ý hashtag.
- Tìm kiếm người dùng.
- Tìm kiếm bài viết.
- Báo cáo bài viết và trang cá nhân.
- Moderation Case và xử lý báo cáo theo nhóm bài viết.
- Gắn, thay đổi và gỡ một Location tùy chọn trên Post.
- Admin khóa và mở khóa tài khoản.
- Admin ẩn và khôi phục bài viết.
- Hiển thị hoạt động quản trị cơ bản.

### P2 – Thực hiện nếu đủ tiến độ

- Trả lời bình luận một cấp.
- Thông báo REST và realtime bằng WebSocket/STOMP.
- Nhắn tin trực tiếp một-một bằng text hoặc ảnh; gửi bằng REST và nhận realtime bằng WebSocket/STOMP dùng chung.

---

## 🛠️ Công nghệ sử dụng

### Frontend

- ReactJS.
- JavaScript ES6+.
- Vite.
- React Router DOM.
- Axios.
- STOMP client trên native WebSocket.
- Tailwind CSS.
- Google Identity Services.
- Facebook SDK for JavaScript.
- Context API hoặc Zustand.
- ESLint.

### Backend

- Java.
- Spring Boot.
- Spring Web.
- Spring WebSocket và STOMP.
- Spring Security.
- Spring Data JPA.
- Hibernate.
- Bean Validation.
- JWT Authentication.
- Google token verification.
- Facebook access-token verification.
- Spring JavaMailSender.
- Gmail SMTP cho toàn bộ email OTP.
- Maven.
- Lombok.
- MapStruct hoặc mapper thủ công.

### Database

- MySQL 8.
- MySQL Workbench.
- Bảng `user_blocks` dùng khóa chính kép `(blocker_id, blocked_id)`, khóa ngoại tới `users`, CHECK chống tự Block và index đảo chiều `(blocked_id, blocker_id)`.
- Bảng `user_restrictions` dùng khóa chính kép `(restrictor_id, restricted_id)`, khóa ngoại tới `users`, CHECK chống tự Restrict và index `(restricted_id, restrictor_id)` phục vụ kiểm tra suppress Notification.

### Lưu trữ media

- Cloudinary.
- Firebase Storage.
- Amazon S3.

Database không lưu dữ liệu BLOB. Media công khai có thể lưu URL; media chat chỉ lưu storage identifier và metadata, URL truy cập được ký ngắn hạn sau khi kiểm tra quyền.

### Công cụ phát triển

- Git.
- GitHub.
- Postman.
- IntelliJ IDEA.
- Visual Studio Code.
- MySQL Workbench.
- Docker tùy chọn.
- Figma tùy chọn.

---

## 🏗️ Kiến trúc tổng thể

```text
┌────────────────────────────┐
│       Client Browser       │
└──────────────┬─────────────┘
               │
               ▼
┌────────────────────────────┐
│      ReactJS Frontend      │
│ Vite + Tailwind + OAuth UI │
└──────────────┬─────────────┘
               │ RESTful API / JSON
               │ JWT Access Token
               ▼
┌────────────────────────────┐
│    Spring Boot Backend     │
│                            │
│ Controller / Service       │
│ Repository / Security      │
│ OTP / OAuth Verification   │
└───────┬──────────┬─────────┘
        │          │
        ▼          ▼
┌──────────────┐  ┌──────────────────┐
│   MySQL 8    │  │ External Services│
│ Main Data    │  │ Email/OAuth  │
└───────┬──────┘  └──────────────────┘
        │
        ▼
┌──────────────────┐
│  Cloud Storage   │
│  Post Media      │
└──────────────────┘
```

---

## 📂 Cấu trúc thư mục dự án

```text
student-social-network/
├── frontend/
│   ├── public/
│   └── src/
│       ├── assets/
│       ├── components/
│       ├── config/
│       ├── features/
│       │   ├── auth/
│       │   │   ├── components/
│       │   │   ├── pages/
│       │   │   ├── services/
│       │   │   └── validation/
│       │   ├── profile/
│       │   ├── follow/
│       │   ├── post/
│       │   ├── feed/
│       │   ├── search/
│       │   ├── report/
│       │   └── admin/
│       ├── hooks/
│       ├── routes/
│       ├── store/
│       ├── utils/
│       ├── App.jsx
│       └── main.jsx
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/stu/edu/vn/backend/
│   │   │   │   ├── common/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── dto/
│   │   │   │   │   ├── entity/
│   │   │   │   │   ├── enums/
│   │   │   │   │   ├── provider/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── service/
│   │   │   │   ├── user/
│   │   │   │   ├── follow/
│   │   │   │   ├── post/
│   │   │   │   ├── comment/
│   │   │   │   ├── feed/
│   │   │   │   ├── search/
│   │   │   │   ├── report/
│   │   │   │   └── admin/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── database/
│   ├── student_social_network.sql
│   ├── student_social_network.dbml
│   ├── migrations/
│   ├── seeds/
│   │   └── seed_1000_website_cases.sql
│   └── triggers/
│
├── docs/
│   ├── api/
│   ├── database/
│   ├── diagrams/
│   ├── ui/
│   └── business/
│
├── .gitignore
├── docker-compose.yml
└── README.md
```

---

## 🧩 Nguyên tắc tổ chức Frontend

Frontend được tổ chức theo hướng **Feature-Based Structure**.

- `components/`: component riêng của module.
- `pages/`: các trang được khai báo trong Router.
- `services/`: các hàm gọi RESTful API.
- `validation/`: quy tắc kiểm tra biểu mẫu.
- Component dùng chung đặt trong `src/components/common/`.
- Axios và Interceptor đặt trong `src/config/`.
- Không gọi Axios trực tiếp trong component giao diện.
- Token được quản lý tập trung.
- Credential của Google/Facebook chỉ gửi đến endpoint Auth tương ứng.
- Không dùng provider token để gọi API Feed, Post hoặc các API nghiệp vụ khác.

---

## 🧩 Nguyên tắc tổ chức Backend

Backend được tổ chức theo module nghiệp vụ kết hợp kiến trúc phân lớp.

- `controller`: tiếp nhận request và trả response.
- `dto/request`: nhận và kiểm tra dữ liệu đầu vào.
- `dto/response`: trả dữ liệu cần thiết cho Frontend.
- `entity`: ánh xạ bảng dữ liệu.
- `enums`: nhóm giá trị cố định của module.
- `repository`: truy cập dữ liệu.
- `service`: khai báo nghiệp vụ.
- `service/impl`: triển khai nghiệp vụ và quản lý transaction.
- `provider`: tích hợp email, Google và Facebook.
- `mapper`: chuyển đổi Entity và DTO.
- `common/security`: JWT và Spring Security.
- `common/exception`: xử lý lỗi tập trung.
- `common/api`: chuẩn hóa response.

Nguyên tắc:

- Controller không trực tiếp truy cập Repository.
- Entity không được trả trực tiếp ra API.
- Mọi kiểm tra quyền phải thực hiện tại Backend.
- Các thao tác nhiều bước phải dùng transaction khi cần.
- Không giữ transaction database trong lúc chờ SMTP, email hoặc provider bên ngoài.
- Không lưu mật khẩu, OTP, flow token hoặc Refresh Token dưới dạng văn bản thuần.

---

## 🗄️ Các bảng dữ liệu chính

### Tài khoản và xác thực

- `users`: tài khoản nội bộ, email, mật khẩu băm, trạng thái xác minh, vai trò và trạng thái tài khoản.
- `user_profiles`: hồ sơ người dùng và trạng thái onboarding.
- `pending_registrations`: đăng ký local đang chờ OTP.
- `user_auth_providers`: liên kết Google/Facebook với cùng một `users.id`.
- `refresh_tokens`: phiên làm mới JWT.
- `password_recovery_challenges`: challenge thật/decoy cho Password Recovery, chỉ lưu OTP và token dạng hash.
- `password_reset_tokens`: bảng đặt lại mật khẩu legacy, được giữ nguyên để audit và không được service mới ghi dữ liệu.

### Quan hệ theo dõi

- `follows`.

### Bài viết

- `posts`.
- `locations`: địa điểm Google Places dùng chung giữa nhiều bài viết, duy nhất theo `google_place_id`.
- `post_media`.
- `hashtags`.
- `post_hashtags`.

Quan hệ Location–Post:

- `locations.id` là khóa chính nội bộ.
- `locations.google_place_id` là natural unique key, `NOT NULL` và duy nhất.
- `locations.display_name`, `locations.latitude` và `locations.longitude` là `NOT NULL`; `locations.formatted_address` cho phép `NULL`; bảng có `created_at` và `updated_at`.
- `posts.location_id` cho phép `NULL`, có index, không duy nhất và tham chiếu `locations.id` bằng `ON DELETE SET NULL`.

### Tương tác

- `post_likes`.
- `comments`.
- `saved_posts`.

### Báo cáo và quản trị

- `reports`.
- `moderation_cases`: hồ sơ xử lý chung theo bài viết; duy nhất một case `OPEN` cho mỗi Post.
- `profile_report_cases`: một vụ việc duy nhất cho mỗi trang cá nhân, gom báo cáo từ nhiều người.
- `profile_reports`: từng lượt báo cáo thuộc case, giữ reporter/lý do/snapshot và chống trùng `PENDING` theo reporter/target.
- `account_status_histories`.
- `admin_actions`.

Ràng buộc Auth quan trọng:

- `users.email` là duy nhất khi có giá trị.
- `users.password_hash` được phép `NULL` với tài khoản chỉ dùng social.
- `user_auth_providers(provider, provider_user_id)` là duy nhất.
- OTP, flow token và Refresh Token chỉ lưu dưới dạng hash.

---

## 🔐 Cơ chế xác thực

### Access Token

- Xác thực request đến Backend.
- Có thời hạn ngắn.
- Gửi trong header:

```http
Authorization: Bearer <access_token>
```

### Refresh Token

- Dùng để cấp Access Token mới.
- Có thời hạn dài hơn.
- Được lưu dưới dạng hash.
- Có thể bị thu hồi khi đăng xuất hoặc khi tài khoản bị khóa.

### Provider Token

- Google ID Token và Facebook Access Token chỉ dùng cho endpoint Auth.
- Backend phải xác minh token trước khi xử lý.
- Provider Token không thay thế JWT của hệ thống.

### Hạ tầng realtime dùng chung

- REST API và MySQL tiếp tục là nguồn dữ liệu chuẩn của Notification.
- WebSocket/STOMP chỉ là kênh phân phối realtime theo mô hình best-effort.
- Handshake endpoint là `/ws`, không bật SockJS và chưa dùng Outbox.
- Mỗi tab chỉ tạo một STOMP client và một WebSocket connection dùng chung cho các module realtime.
- Frontend subscribe Notification tại `/user/queue/notifications` và Messaging tại `/user/queue/messaging` mà không tạo client thứ hai.
- Access Token chỉ được gửi trong native header `Authorization: Bearer <access_token>` của STOMP `CONNECT`, không truyền token trong URL.
- Principal của kết nối được Backend xác định từ JWT và có tên bằng chuỗi `users.id`; Backend không tin `userId` do Frontend gửi.
- Event chỉ được phát sau khi transaction tạo Notification commit thành công.
- Giai đoạn đầu chỉ phát event `NOTIFICATION_CREATED`; read, read-all, delete và invalidation vẫn đồng bộ qua REST.
- Messaging phát `MESSAGE_CREATED` và `MESSAGES_READ` sau commit cho cả hai participant; mỗi envelope có unread count authoritative riêng cho user nhận.
- Client không được gửi mutation bền vững qua STOMP. Chỉ frame typing tạm thời tại `/app/messaging/typing` được phép; Backend tự lấy actor từ principal, kiểm tra membership/account/Block và chỉ phát cho participant còn lại qua `/user/queue/messaging`.
- Typing dùng `TYPING_STARTED`/`TYPING_STOPPED`, giới hạn tối đa 4 frame/user/giây trong bộ nhớ của từng instance, không ghi database, không thay đổi unread và không replay. Frontend gửi START lần đầu, refresh mỗi 3 giây khi còn hoạt động, STOP sau 2 giây idle và tự hết hạn trạng thái nhận sau 5 giây.
- Khi socket gián đoạn, Frontend phải reconcile bằng REST và chỉ polling unread count khi tab đang hiển thị.

Quy trình tổng quát:

```text
Local OTP hoặc Social Provider
            │
            ▼
Backend xác minh danh tính
            │
            ▼
Tìm hoặc tạo users.id
            │
            ▼
Cấp Access Token + Refresh Token
            │
            ▼
Frontend dùng Access Token gọi API
```

---

## 📡 Quy ước RESTful API

Base URL môi trường phát triển:

```text
http://localhost:8080/api/v1
```

### Auth local và OTP

```http
POST /api/v1/auth/registrations
POST /api/v1/auth/registrations/verify
POST /api/v1/auth/registrations/resend
POST /api/v1/auth/registrations/cancel
GET  /api/v1/auth/registrations/status

POST /api/v1/auth/login
POST /api/v1/auth/refresh-token
POST /api/v1/auth/logout
```

### Social authentication

```http
POST /api/v1/auth/oauth/google
POST /api/v1/auth/oauth/facebook
POST /api/v1/auth/registrations/resolve-social-conflict
```

### Quản lý phương thức đăng nhập

```http
GET    /api/v1/users/me/auth-providers
POST   /api/v1/users/me/auth-providers/email
POST   /api/v1/users/me/auth-providers/google
POST   /api/v1/users/me/auth-providers/facebook
DELETE /api/v1/users/me/auth-providers/{provider}
```

### User Block

```http
PUT    /api/v1/users/{targetUserId}/block
DELETE /api/v1/users/{targetUserId}/block
GET    /api/v1/users/me/blocked-users
```

### User Restriction

```http
POST   /api/v1/users/{targetUserId}/restriction
DELETE /api/v1/users/{targetUserId}/restriction
GET    /api/v1/users/me/restricted-users
```

Các API trên yêu cầu JWT, luôn lấy người thực hiện từ Security Context và có tính idempotent. Không có endpoint công khai để xem ai đã hạn chế current user.

### Các nhóm API khác

```text
/api/v1/users
/api/v1/follows
/api/v1/posts
/api/v1/feeds
/api/v1/search
/api/v1/reports
/api/v1/admin
```

API onboarding và kiểm tra username:

```http
GET /api/v1/users/me/onboarding
GET /api/v1/users/me/onboarding/username-availability?username=duoz_03
PUT /api/v1/users/me/onboarding
```

API Repost đã triển khai:

```http
PUT    /api/v1/posts/{postId}/repost
DELETE /api/v1/posts/{postId}/repost
GET    /api/v1/users/{userId}/reposts?limit=10&cursor=<opaque-cursor>
```

Post response công khai có thêm `repostCount` và `repostedByCurrentUser`. Realtime `POST_REPOST` được đẩy
best-effort tới `/user/queue/notifications` sau transaction commit; REST/MySQL vẫn là nguồn sự thật.

API Moderation Case dành cho Admin:

```http
GET   /api/v1/admin/moderation-cases
GET   /api/v1/admin/moderation-cases/{caseId}
PATCH /api/v1/admin/moderation-cases/{caseId}/resolve-no-violation
PATCH /api/v1/admin/moderation-cases/{caseId}/resolve-action
```

Frontend chỉ gửi hành động nghiệp vụ và kết luận; `status` cùng `resolved_by` do Backend xác định từ
state machine và JWT Principal.

API Analytics độc lập dành cho Admin:

```http
GET /api/v1/admin/analytics/user-engagement/monthly?fromMonth=2026-01&toMonth=2026-06&inactiveDays=15
GET /api/v1/admin/analytics/user-engagement/summary?month=2026-06&inactiveDays=15
GET /api/v1/admin/analytics/user-engagement/dashboard?days=30
```

Khoảng monthly tối đa 24 tháng. `summary` mặc định lấy tháng hiện tại. Response monthly gồm từng tháng,
hai peak (`peakReturningMonth`, `peakReturnRateMonth`) và `comparisonOperator = GREATER_THAN`.
Dashboard mặc định dùng 30 ngày, cho phép từ 1 đến 90 ngày và trả `dailyInteractions` cùng `featuredUsers`.

Ví dụ bắt đầu đăng ký:

```json
{
  "email": "student@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

Ví dụ xác minh OTP:

```json
{
  "registrationFlowToken": "<temporary-token>",
  "code": "123456"
}
```

Ví dụ Google authentication khi tham gia luồng registration đang chờ:

```http
X-Auth-Flow-Token: <optional-registration-flow-token>
Content-Type: application/json
```

```json
{
  "idToken": "<google-id-token>"
}
```

Cấu trúc response tham khảo:

```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": {},
  "timestamp": "2026-07-19T10:00:00"
}
```

---

## 🚀 Hướng dẫn cài đặt

### 1. Yêu cầu môi trường

- Node.js 20 trở lên.
- npm 10 trở lên.
- Java Development Kit 21.
- Maven 3.9 trở lên.
- MySQL 8.
- Git.
- IntelliJ IDEA hoặc IDE hỗ trợ Spring Boot.
- Visual Studio Code hoặc IDE hỗ trợ ReactJS.

### 2. Clone Repository

```bash
git clone https://github.com/your-username/student-social-network.git
cd student-social-network
```

### 3. Khởi tạo cơ sở dữ liệu

File `student_social_network.sql` tự động drop database test hiện tại, tạo lại schema và nạp dữ liệu demo tối thiểu. Từ Command Prompt hoặc Git Bash:

```bash
mysql --default-character-set=utf8mb4 -u root -p < database/student_social_network.sql
```

Từ PowerShell:

```powershell
cmd /c "mysql --default-character-set=utf8mb4 -u root -p < database\student_social_network.sql"
```

Để thay dữ liệu demo tối thiểu bằng bộ dữ liệu kiểm thử website gồm đúng 1.000 users và 1.000 posts có từ 1–4 ảnh:

```powershell
cmd /c "mysql --default-character-set=utf8mb4 -u root -p student_social_network < database\seeds\seed_1000_website_cases.sql"
```

### 4. Cấu hình Backend

Ví dụ biến môi trường:

```env
DB_URL=jdbc:mysql://localhost:3306/student_social_network?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

JWT_ACCESS_TOKEN_SECRET=your_access_token_secret
JWT_ACCESS_TOKEN_EXPIRATION_MILLIS=900000
JWT_REFRESH_TOKEN_EXPIRATION_MILLIS=2592000000

AUTH_OTP_HMAC_SECRET=your_independent_otp_hmac_secret
AUTH_FLOW_TOKEN_HMAC_SECRET=your_independent_flow_token_hmac_secret
AUTH_SOCIAL_IDENTITY_FINGERPRINT_SECRET=your_independent_social_fingerprint_secret

GOOGLE_CLIENT_ID=your_google_client_id
FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_APP_SECRET=your_facebook_app_secret

MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_APP_PASSWORD=your_16_character_google_app_password
MAIL_SENDER_NAME=UniShare
```

`MAIL_USERNAME` là tài khoản Gmail gửi OTP. `MAIL_APP_PASSWORD` phải là Google App Password,
không dùng mật khẩu đăng nhập Google thông thường. `MAIL_SENDER_NAME` là tên hiển thị người gửi
và mặc định là `UniShare`. Không đưa thông tin bí mật lên GitHub.

Chạy Backend:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 5. Cấu hình Frontend

```bash
cd frontend
npm install
```

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_GOOGLE_CLIENT_ID=your_google_client_id
VITE_FACEBOOK_APP_ID=your_facebook_app_id
```

```bash
npm run dev
```

---

## 🧪 Kiểm thử

### Backend

```bash
cd backend
mvn test
mvn clean package
```

### Frontend

```bash
cd frontend
npm run lint
npm run build
npm run preview
```

### Luồng Auth cần kiểm thử

- Đăng ký email và xác minh OTP.
- Gửi lại OTP và giới hạn tần suất.
- OTP sai, hết hạn, đã dùng hoặc vượt số lần thử.
- Tiếp tục đăng ký sau khi đóng tab hoặc mất mạng.
- Đăng ký/đăng nhập Google.
- Đăng ký/đăng nhập Facebook.
- Email pending chuyển sang social cùng email.
- Provider đã liên kết đăng nhập đúng `users.id`.
- Liên kết và gỡ phương thức đăng nhập.
- Từ chối gỡ phương thức cuối cùng.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
- Report đầu tiên tạo case `OPEN`; các Report hợp lệ tiếp theo cùng bài dùng lại đúng case đó.
- Hai request báo cáo đồng thời không tạo hai case `OPEN`, không mất Report và không làm sai `report_count`.
- Admin giải quyết case `OPEN` đúng một lần; thao tác cạnh tranh còn lại nhận lỗi conflict.
- Kết luận không vi phạm không ẩn bài và chuyển Report sang `REJECTED`; kết luận có vi phạm ẩn bài và chuyển Report sang `RESOLVED`.
- Danh sách Admin hiển thị một dòng mỗi case; chi tiết giữ riêng reporter, reason, description và snapshot của từng Report.

---

## ✅ Tiêu chí nghiệm thu MVP

### Xác thực và tài khoản

1. Đăng ký local chỉ tạo `pending_registrations`, chưa tạo `users`.
2. OTP hợp lệ mới tạo `users` và `user_profiles`.
3. `users` và `user_profiles` được tạo trong cùng transaction.
4. OTP hết hạn, đã dùng, bị hủy hoặc vượt số lần thử không thể sử dụng.
5. Không tồn tại hai đăng ký tạm còn hiệu lực cho cùng một email.
6. Mất mạng hoặc đóng tab vẫn có thể tiếp tục trong thời hạn.
7. Email pending cùng social email đã xác minh hoàn tất đúng một tài khoản.
9. Provider đã liên kết luôn đăng nhập đúng `users.id`.
10. Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
11. Không cho gỡ phương thức đăng nhập cuối cùng.
12. Tài khoản social chưa có mật khẩu không đăng nhập local được.
13. Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
14. Access Token và Refresh Token hoạt động đúng.
15. Refresh Token bị thu hồi không thể cấp Access Token mới.

### Hồ sơ và chức năng mạng xã hội

16. Người dùng hoàn tất onboarding bằng username duy nhất, tên hiển thị và ngày sinh hợp lệ trước khi truy cập chức năng chính.
17. Người dùng cập nhật và xem hồ sơ thành công.
18. Follow và Unfollow hoạt động đúng, không tạo quan hệ trùng.
18a. Block hoạt động idempotent, xóa Follow hai chiều; Unblock không khôi phục Follow.
18b. Các query dành cho người dùng lọc Block hai chiều trực tiếp tại database mà không làm sai phân trang/cursor.
18c. Block không xóa Like hoặc Comment lịch sử và không làm giảm `like_count` hoặc `comment_count`.
18d. Trong thời gian Block, hai bên không nhìn thấy bình luận hoặc câu trả lời bình luận do đối phương tạo; sau Unblock, nội dung lịch sử hiển thị trở lại nếu vẫn hợp lệ và còn quyền truy cập.
18e. Bình luận cha bị ẩn do Block không được để lộ các câu trả lời con dưới dạng nội dung mồ côi.
18f. Restrict/Unrestrict hoạt động một chiều và idempotent; không tạo quan hệ trùng, không cho tự Restrict hoặc Restrict khi có Block hai chiều.
18g. Restrict không thay đổi Follow, Feed, Search, Like, Save, Comment, Reply hoặc các bộ đếm; Comment/Reply vẫn `PUBLISHED`.
18h. Notification Like, Comment và Reply từ người bị hạn chế tới người hạn chế bị chặn trước khi lưu, không tăng unread count và không phát WebSocket/event; Notification Follow vẫn hoạt động.
18i. API chỉ trả `restrictedByMe` cho current user, không tiết lộ chiều ngược lại; danh sách restricted users chỉ chứa quan hệ do current user tạo.
18j. Block sau Restrict xóa đúng quan hệ Restrict cùng chiều; Unblock không tự khôi phục Restrict và Unrestrict không tạo bù Notification.
19. Người dùng tạo bài có nội dung hoặc hình ảnh.
20. Chỉ tác giả được sửa hoặc xóa bài của mình.
21. Bài bị ẩn hoặc xóa không xuất hiện trên Feed.
19. Người dùng tạo bài có nội dung hoặc hình ảnh và có thể tùy chọn gắn tối đa một Location; các bài dùng cùng Google Place ID phải dùng chung một bản ghi `locations`.
20. Chỉ tác giả được sửa hoặc xóa bài của mình; trong giới hạn chỉnh sửa 15 phút, tác giả có thể cập nhật nội dung/hashtag, giữ/gỡ/thêm ảnh hoặc video và giữ/thay đổi/gỡ Location bằng `KEEP`, `REPLACE`, `REMOVE`.
21. Bài bị ẩn hoặc xóa không xuất hiện trên Feed; xóa Post không xóa Location dùng chung.
22. Like, Unlike, danh sách bài viết đã thích, bình luận và Save hoạt động đúng.
23. Repost và Unrepost idempotent; tab Repost và Following Feed chỉ hiển thị quan hệ có bài gốc còn `PUBLISHED`.
24. Feed Following và Feed For You trả đúng dữ liệu hợp lệ, gồm Location object hoặc `null` cho từng Post.
25. Tìm kiếm người dùng và bài viết hoạt động đúng; Search Post và Admin Post Detail trả Location nhất quán với các Post response khác.
26. Người dùng gửi được báo cáo.
27. Admin quản lý được người dùng, chỉnh sửa nội dung hồ sơ USER và USER nhận được thông báo hệ thống sau khi chỉnh sửa, quản lý bài viết và báo cáo.
28. Backend từ chối thao tác không có quyền.
29. Mật khẩu, OTP, flow token và Refresh Token không lưu dạng văn bản thuần.
30. Backend không trả stack trace hoặc thông tin nhạy cảm cho Client.

---

## 🔒 Yêu cầu bảo mật

- Mật khẩu được băm bằng BCrypt.
- OTP, flow token và Refresh Token chỉ lưu dạng hash.
- Access Token có thời hạn ngắn.
- Refresh Token có khả năng thu hồi.
- Rate limit cho đăng nhập, gửi OTP, gửi lại OTP và social authentication.
- Token Google/Facebook phải được xác minh tại Backend.
- Không tin email, provider ID hoặc trạng thái verified do Frontend tự gửi.
- API Admin chỉ dành cho vai trò `ADMIN`.
- Backend kiểm tra quyền cho mọi thao tác.
- Không ghi mật khẩu, OTP hoặc Token vào log.
- Bean Validation cho dữ liệu đầu vào.
- Kiểm tra MIME type, phần mở rộng và dung lượng media.
- Hạn chế XSS, SQL Injection và CSRF theo kiến trúc.
- CORS chỉ cho phép các nguồn cần thiết.
- Bí mật ứng dụng được lưu trong biến môi trường.

---

## ⚡ Yêu cầu hiệu năng

- API thông thường phản hồi trung bình không quá 2 giây trong môi trường kiểm thử.
- Feed trả tối đa 20 bài viết mỗi lần tải.
- API danh sách hỗ trợ phân trang theo contract của từng module.
- Danh sách bài viết Infinite Scroll dùng cursor với `limit` mặc định 10 và tối đa 20; các danh
  sách còn dùng `PageResponse` giữ kích thước trang theo endpoint hiện tại.
- Hệ thống hướng đến 30–50 người dùng đồng thời trong môi trường kiểm thử.
- Tránh truy vấn N+1.
- Không tải toàn bộ Entity Relationship không cần thiết.
- Việc gửi email không giữ transaction database mở quá lâu.
- Các tác vụ dọn đăng ký hết hạn có thể chạy theo lịch.

---

## 🌿 Quy ước Git

### Nhánh

- `main`: phiên bản ổn định.
- `develop`: nhánh tích hợp.
- `feature/*`: chức năng mới.
- `fix/*`: sửa lỗi.
- `refactor/*`: tái cấu trúc.
- `docs/*`: cập nhật tài liệu.

Ví dụ:

```text
feature/auth-registration-otp
feature/auth-google
feature/auth-facebook
feature/auth-provider-linking
docs/update-readme
```

### Commit

```text
feat: thêm đăng ký email bằng OTP
feat: thêm đăng nhập Google
fix: ngăn liên kết provider đã thuộc tài khoản khác
docs: cập nhật README module xác thực
test: thêm kiểm thử luồng đăng ký tạm
```

---

## 📝 Quy ước mã nguồn

### Frontend

- Component dùng PascalCase.
- Hàm và biến dùng camelCase.
- Custom Hook bắt đầu bằng `use`.
- Tên file API kết thúc bằng `Api.js`.
- Không gọi API trực tiếp trong JSX.
- Xử lý đầy đủ trạng thái loading, empty và error.
- Không lưu provider token lâu hơn mức cần thiết.

### Backend

- Class dùng PascalCase.
- Biến và phương thức dùng camelCase.
- Hằng số dùng UPPER_SNAKE_CASE.
- DTO Request và Response tách biệt.
- Không trả trực tiếp Entity.
- Service chịu trách nhiệm nghiệp vụ.
- Repository chỉ truy cập dữ liệu.
- Exception xử lý tập trung.
- Dùng `@Transactional` cho thao tác quan trọng.
- Không đặt logic nghiệp vụ phức tạp trong Controller.

---

## 🚧 Chức năng ngoài phạm vi MVP

- Hồ sơ riêng tư.
- Follow Request.
- Video và tài liệu trong bài viết.
- Bài viết nháp.
- Mention.
- Trích dẫn bài viết.
- Chủ đề nội dung do Admin quản lý.
- Discovery Map và các chức năng khám phá nâng cao theo địa điểm.
- Feed cá nhân hóa do người dùng tạo.
- Feed công khai và lưu Feed.
- Elasticsearch.
- Message Request và Hidden Message Request.
- Tài liệu, video, online status, recall và report message trong Nhắn tin.
- Quản lý trường, khoa và ngành.
- Dashboard thống kê nâng cao.
- Machine Learning cho hệ thống gợi ý.
- Ứng dụng mobile native.
- Group Chat.
- Gọi thoại.
- Gọi video.
- Livestream.

---

## 🔮 Định hướng phát triển

- Hồ sơ riêng tư và Follow Request.
- Trích dẫn bài viết và Quote Post.
- Video và tài liệu học tập.
- Bài viết nháp.
- Mention người dùng.
- Feed cá nhân hóa.
- Elasticsearch.
- Khám phá nội dung theo địa điểm.
- Discovery Map.
- Backend xác minh và đồng bộ định kỳ dữ liệu với Google Places.
- Message Request.
- Tài liệu, video, online status, recall và report message trong Nhắn tin.
- Quản lý trường, khoa và ngành.
- Dashboard thống kê nâng cao.
- Audit Log chi tiết.
- Thuật toán gợi ý nâng cao.
- Ứng dụng di động.

---

## 📚 Tài liệu dự án

Các tài liệu phân tích và thiết kế được lưu trong thư mục `docs/`:

- Đặc tả nghiệp vụ.
- Sơ đồ phân rã chức năng.
- Use Case tổng quát và chi tiết.
- Activity Diagram.
- Sequence Diagram.
- Entity Relationship Diagram.
- Tài liệu thiết kế database.
- Tài liệu API.
- Thiết kế giao diện.
- Kịch bản kiểm thử.
- Hướng dẫn sử dụng hệ thống.
- Ma trận trạng thái triển khai: [`docs/IMPLEMENTATION-STATUS.md`](docs/IMPLEMENTATION-STATUS.md).

---

## 📄 Giấy phép

Dự án được phát triển nhằm phục vụ mục đích học tập, nghiên cứu và thực hiện luận văn tốt nghiệp.

Không sử dụng dự án cho mục đích thương mại khi chưa có sự đồng ý của nhóm phát triển.

---

## © Bản quyền

**© 2026 – Trường Đại học Công nghệ Sài Gòn**

**Khoa Công nghệ Thông tin**

**Đề tài: Xây dựng hệ thống mạng xã hội tinh gọn hướng đến sinh viên**
