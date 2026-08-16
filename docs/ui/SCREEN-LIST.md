# Danh sách màn hình

`README.md` là nguồn sự thật cao nhất. Contract HTTP chi tiết duy nhất nằm tại `docs/data/API-CONTRACT.md`; tài liệu này chỉ mô tả màn hình và trạng thái UI. Ảnh trong `docs/ui/screens/` là tham chiếu visual, còn các màn hình Auth bắt buộc chưa có ảnh được ghi rõ là `DESIGN_REQUIRED`.

## Quy tắc chung

- Ảnh Stitch là tài liệu tham chiếu và không bị xóa dù có chi tiết ngoài MVP.
- UI MVP dùng username duy nhất tại onboarding và hiển thị `@username` trên hồ sơ; route và quan hệ dữ liệu vẫn dùng `userId`, không dùng handle/userSlug/email công khai thay thế.
- Hồ sơ cá nhân dùng route `/profile/me`.
- Hồ sơ người dùng khác dùng route `/profile/:userId`.
- Khi chọn người dùng từ Feed, Search, follower/following hoặc mention, điều hướng bằng userId.
- Mention nếu xuất hiện trong thiết kế tương lai sẽ hiển thị bằng displayName và liên kết nội bộ bằng userId.
- Sau submit đăng ký local, người dùng phải xác minh OTP; chỉ sau OTP hợp lệ và nhận JWT hệ thống mới được điều hướng đến onboarding.
- Người đã đăng nhập nhưng chưa hoàn tất hồ sơ phải được route guard chuyển về onboarding.
- Feed và các chức năng mạng xã hội chính chỉ dành cho tài khoản đã đăng nhập, `ACTIVE` và có `profile_completed_at` khác `NULL`.
- `MVP_CURRENT`: triển khai trong bản demo HTML và Frontend MVP hiện tại.
- `DESIGN_REQUIRED`: thuộc trạng thái đích MVP nhưng chưa có ảnh/UI implementation tương ứng; cần thiết kế và triển khai ở giai đoạn sau.
- `FUTURE_DEVELOPMENT`: giữ làm tài liệu thiết kế sau MVP, không triển khai trong bản demo MVP hiện tại.

## Phân loại phạm vi màn hình

### MVP_CURRENT

- AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05, AUTH-06 theo phần UI đã có; hành vi Auth cũ phải được thay bằng contract đích.
- FEED-01.
- POST-01, POST-02, POST-03, POST-04, POST-05, POST-06, POST-07, POST-08, POST-09, POST-10.
- PROFILE-01, PROFILE-02, PROFILE-03, PROFILE-04.
- SEARCH-01, SAVED-01, LIKED-01.
- ADMIN-01, ADMIN-02, ADMIN-03, ADMIN-04, ADMIN-05, ADMIN-06, ADMIN-07, ADMIN-08, ADMIN-09, ADMIN-10, ADMIN-11.
- SYS-01, SYS-02, SYS-03, SYS-04.

### DESIGN_REQUIRED

- AUTH-OTP-01, AUTH-SOCIAL-01, AUTH-METHOD-01, AUTH-METHOD-02, AUTH-REAUTH-01.

### FUTURE_DEVELOPMENT

- AUTH-P2-01, AUTH-P2-02, AUTH-P2-03, AUTH-P2-04.
- Các chi tiết ngoài MVP đang xuất hiện trong ảnh như nhắn tin, hoạt động realtime, repost, trích dẫn bài viết, file phương tiện, bài đăng lại, cài đặt admin, dashboard nâng cao, mention và các menu quản trị/nội dung nâng cao. Google/Facebook Auth không còn thuộc nhóm này.

Ghi chú chuẩn cho FUTURE_DEVELOPMENT: Màn hình hoặc chi tiết được giữ lại làm tài liệu thiết kế cho giai đoạn mở rộng sau MVP. Không triển khai trong bản demo và Frontend MVP hiện tại.

## Authentication

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| AUTH-01 | Đăng nhập | `docs/ui/screens/auth/login.jpg` | Khách | Cho phép người dùng vào hệ thống. | Đăng nhập local bằng email và mật khẩu; đăng nhập Google/Facebook; điểm vào Password Recovery. | Sau Auth thành công: hồ sơ chưa hoàn tất đến `/onboarding/profile`, đã hoàn tất đến Feed/Admin. | Backend Password Recovery đã triển khai; giao diện Forgot/Reset Password được tích hợp ở hạng mục Frontend 13J. |
| AUTH-02 | Khởi tạo đăng ký | `docs/ui/screens/auth/register.jpg` | Khách | Tạo hoặc phục hồi đăng ký local đang chờ. | Một email email, mật khẩu, xác nhận mật khẩu; nút Google/Facebook. | Local thành công nhận flow token và đến AUTH-OTP-01; chưa tạo user, profile hoặc session. | Không dùng username/displayName. Flow token chỉ giữ memory/sessionStorage, không localStorage. |
| AUTH-OTP-01 | Xác minh đăng ký OTP | Chưa có ảnh | Khách có pending | Xác minh email trước khi tạo tài khoản thật. | Nhập OTP, cooldown resend, hiển thị attempt/expiry phù hợp, recovery pending. | OTP hợp lệ mới nhận JWT và đến `/onboarding/profile`; resend/recovery rotate flow token. | DESIGN_REQUIRED. Dùng cùng UI cho OTP email; không đưa flow token vào URL. |
| AUTH-SOCIAL-01 | Xử lý social conflict | Chưa có ảnh | Khách | Yêu cầu lựa chọn khi social identity không thể tự động hợp nhất an toàn. | Tiếp tục OTP, hủy pending hoặc hành động được Backend cho phép. | Dùng social conflict token một lần; hết hạn quay lại Auth phù hợp. | DESIGN_REQUIRED. Không tự link/tạo user thứ hai khi email social trùng user ACTIVE chưa link provider. |
| AUTH-METHOD-01 | Quản lý phương thức đăng nhập | Chưa có ảnh | User đã đăng nhập | Xem, link và unlink email, Google, Facebook. | Danh sách method, verified state, link/unlink action và last-method guard. | Social link dùng JWT hiện tại; local link đến AUTH-METHOD-02; unlink đến AUTH-REAUTH-01. | DESIGN_REQUIRED. Thu hồi session khác là P1, chưa tự triển khai. |
| AUTH-METHOD-02 | Xác minh link email | Chưa có ảnh | User đã đăng nhập | Hoàn tất challenge liên kết local riêng. | Initiate, nhập OTP, resend và trạng thái expiry/cooldown. | Thành công quay lại AUTH-METHOD-01. | DESIGN_REQUIRED. Không tái sử dụng pending registration. |
| AUTH-REAUTH-01 | Xác thực lại thao tác nhạy cảm | Chưa có ảnh | User đã đăng nhập | Cấp quyền ngắn hạn trước khi unlink. | Chọn proof hợp lệ và xác thực lại. | Thành công quay về xác nhận unlink; thất bại giữ nguyên method. | DESIGN_REQUIRED. Reauthentication chỉ dùng cho thao tác bảo mật nhạy cảm. |
| AUTH-03 | Onboarding hồ sơ cơ bản | `docs/ui/screens/auth/update-profile-1.jpg`, `update-profile-2.jpg`, `update-profile-3.jpg` | User chưa hoàn tất hồ sơ | Hoàn tất username, tên hiển thị và ngày sinh; avatar/bio tùy chọn. | Giữ ba panel cơ bản hiện có, availability debounce, kiểm tra đủ 18 tuổi; panel cuối cập nhật `profileCompletedAt`. | Route `/onboarding/profile`, bước lớn 1/3. | Phạm vi triển khai: MVP_CURRENT. Academic/Interests không tham gia completion. |
| AUTH-04 | Onboarding thông tin học tập | Chưa có ảnh riêng | User đã hoàn tất dữ liệu cơ bản | Chọn School, Faculty, Major và Entry Year tùy chọn. | Autocomplete master API, hierarchy cascade reset, có loading/empty/error và Skip. | Route `/onboarding/profile?step=2`, bước lớn 2/3. | Phạm vi triển khai: MVP_CURRENT. Chỉ gửi ID; không có School Suggestion. |
| AUTH-05 | Onboarding sở thích | Chưa có ảnh riêng | User đã hoàn tất dữ liệu cơ bản | Chọn Interest Categories tùy chọn. | Chip chọn tối đa 10, không trùng, hiển thị số lượng và có Skip. | Route `/onboarding/profile?step=3`, bước lớn 3/3. | Phạm vi triển khai: MVP_CURRENT. Không chạy Recommendation hoặc AI/ML. |
| AUTH-06 | Tạo tài khoản thành công | `docs/ui/screens/auth/update-profile-success.jpg` | User vừa hoàn tất hồ sơ | Xác nhận tài khoản và hồ sơ đã sẵn sàng. | Thông báo thành công, nút vào trang chủ. | Route `/onboarding/success`; nút chính giữ phiên đăng nhập và đến `/feed/for-you`. | Phạm vi triển khai: MVP_CURRENT. Chỉ truy cập khi đã đăng nhập và `profileCompletedAt` khác `NULL`. |
| AUTH-P2-01 | Quên mật khẩu | `docs/ui/screens/auth/forget-password.jpg` | Khách | Bắt đầu Password Recovery cho tài khoản local đủ điều kiện. | Nhập email và nhận response trung tính chống account enumeration. | Route `/forgot-password`; thành công chuyển bước OTP. | Phạm vi triển khai: MVP_CURRENT; đã tích hợp Backend Password Recovery. |
| AUTH-P2-02 | Nhập mã xác minh đổi mật khẩu | `docs/ui/screens/auth/verified-for-change-pass.jpg` | Khách | Xác minh OTP đặt lại mật khẩu. | Nhập mã, tiếp tục, gửi lại theo cooldown. | Dùng `X-Auth-Flow-Token`; thành công chuyển `/reset-password`. | Phạm vi triển khai: MVP_CURRENT. |
| AUTH-P2-03 | Đổi mật khẩu | `docs/ui/screens/auth/change-password.jpg` | Khách | Đặt mật khẩu mới sau khi OTP hợp lệ. | Nhập mật khẩu mới, xác nhận mật khẩu, lưu. | Route `/reset-password`; complete thành công thu hồi Refresh Token hiện có. | Phạm vi triển khai: MVP_CURRENT. |
| AUTH-P2-04 | Đổi mật khẩu thành công | `docs/ui/screens/auth/change-success.jpg` | Khách | Xác nhận đổi mật khẩu thành công. | Thông báo thành công, nút đăng nhập. | Đi đến AUTH-01; không tự cấp JWT mới. | Phạm vi triển khai: MVP_CURRENT. |

## Feed

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| FEED-01 | Feed người dùng | `docs/ui/screens/feed/feed.jpg` | User | Xem bảng tin chính. | Sidebar, tab Dành cho bạn/Đang theo dõi, composer nhanh, danh sách PostCard, like, comment, Repost, lưu, menu bài viết. | Tạo bài mở POST-02; tab Following hiển thị ORIGINAL/REPOST; vào chi tiết POST-01; tìm kiếm SEARCH-01; hồ sơ cá nhân `/profile/me`; hồ sơ tác giả `/profile/:userId`; saved POST-07. | Phạm vi triển khai: MVP_CURRENT. Quote Post, share nâng cao và dữ liệu dạng @ trong ảnh chỉ là tham chiếu visual. |

## Post

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| POST-01 | Chi tiết bài viết | `docs/ui/screens/post/detail-post.jpg` | User | Xem một bài viết cùng bình luận. | Nội dung bài, Location tùy chọn, ảnh, like, bình luận, lưu/chia sẻ, danh sách bình luận, ô nhập bình luận. | Từ FEED-01, PROFILE-01/02, POST-07; menu mở POST-04/POST-05. | Location P1 hiển thị object hiện tại hoặc không hiển thị khối địa điểm khi response là `null`. |
| POST-02 | Modal tạo bài viết | `docs/ui/screens/post/model-create-post.jpg` | User | Tạo bài viết mới. | Nhập nội dung, gắn ảnh/video, tìm/chọn hashtag, tìm/chọn/gỡ tối đa một Location, thanh công cụ và nút đăng. | Mở từ sidebar/composer FEED-01. | Modal; dùng chung bộ gợi ý hashtag và đã tích hợp Google Places picker. |
| POST-03 | Modal chỉnh sửa bài viết | `docs/ui/screens/post/model-edit-post.jpg` | Tác giả bài viết | Sửa nội dung bài của mình. | Sửa text, tìm/chọn hashtag, giữ/gỡ/thêm ảnh hoặc video, KEEP/REPLACE/REMOVE Location, lưu thay đổi, hủy. | Mở từ menu POST-04; sau lưu quay lại bài/feed/profile. | Media sau cập nhật tối đa 4 và tối đa một video; mọi thay đổi tuân theo giới hạn 15 phút. |
| POST-04 | Menu thao tác bài viết | `docs/ui/screens/post/model-thao-tác-post.jpg` | User, tác giả bài viết | Hiển thị hành động theo quyền trên bài viết. | Menu của chính mình có chỉnh sửa/xóa; menu bài người khác có lưu/báo cáo/sao chép liên kết. | Chỉnh sửa đến POST-03; xóa đến POST-05; báo cáo đến POST-08. | Các mục ghim, lưu trữ, ẩn like, thêm vào feed, không quan tâm, tắt thông báo, hạn chế, chặn nằm ngoài MVP/CẦN XÁC NHẬN. |
| POST-05 | Modal xác nhận xóa bài viết | `docs/ui/screens/post/model-delete-post.jpg` | Tác giả bài viết | Xác nhận trước khi xóa mềm bài viết. | Nội dung cảnh báo, hủy, xóa bài viết. | Thành công đến POST-06. | Modal. |
| POST-06 | Xóa bài viết thành công | `docs/ui/screens/post/model-delete-success.jpg` | Tác giả bài viết | Thông báo xóa bài thành công. | Success state, nút đi tiếp/đóng. | Quay lại Feed hoặc Profile. | Modal trạng thái. |
| POST-07 | Bài viết đã lưu | `docs/ui/screens/post/post-saved.jpg` | User | Xem danh sách bài đã lưu của chính mình. | Sidebar, danh sách PostCard đã lưu, tương tác bài viết. | Từ sidebar "Bài viết đã lưu"; mở POST-01. | Saved list chỉ chủ tài khoản xem. |
| POST-08 | Modal chọn lý do báo cáo | `docs/ui/screens/post/model-report.jpg` | User | Chọn lý do báo cáo bài viết. | Danh sách lý do dạng radio, hủy, tiếp tục. | Sau tiếp tục đến POST-09. | Chỉ report bài viết trong MVP. |
| POST-09 | Chi tiết báo cáo bài viết | `docs/ui/screens/post/report-detail.jpg` | User | Bổ sung mô tả trước khi gửi báo cáo. | Xem bài bị báo cáo, chọn/hiển thị lý do, nhập mô tả, gửi báo cáo. | Thành công đến POST-10. | Modal. |
| POST-10 | Gửi báo cáo thành công | `docs/ui/screens/post/report-success.jpg` | User | Xác nhận báo cáo đã gửi. | Success state, thông báo admin sẽ xem xét, nút xong. | Quay lại bài viết/feed. | Modal trạng thái. |

## Profile

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| PROFILE-01 | Hồ sơ của mình | `docs/ui/screens/profile/profile-for-self.jpg` | User | Xem và quản lý hồ sơ cá nhân. | Thông tin cá nhân công khai, Academic/Interests nếu có, số follower/following, chỉnh sửa trang cá nhân, tab bài viết, danh sách bài. | Route `/profile/me`; chỉnh sửa mở PROFILE-03; follower/following mở PROFILE-04; bài mở POST-01. | Phạm vi triển khai: MVP_CURRENT. Field Academic null không hiển thị. |
| PROFILE-02 | Hồ sơ người khác | `docs/ui/screens/profile/profile-for-other.jpg` | User | Xem hồ sơ công khai và theo dõi/nhắn tin người khác. | Thông tin người dùng, Follow/Unfollow, Nhắn tin, menu thêm và tab nội dung. | Route `/profile/:userId`; Nhắn tin mở `/messages/:conversationId` khi Backend cho phép. | Phạm vi triển khai: MVP_CURRENT. |
| PROFILE-03 | Modal chỉnh sửa hồ sơ | `docs/ui/screens/profile/model-edit-profile.jpg` | User | Cập nhật hồ sơ cá nhân. | Avatar, thông tin cơ bản, Academic Profile, tối đa 10 Interests; username chỉ đọc. | Mở từ PROFILE-01. | Phạm vi triển khai: MVP_CURRENT. Dùng chung component onboarding; chưa triển khai đổi username hoặc hồ sơ riêng tư. |
| PROFILE-04 | Modal danh sách follower/following | `docs/ui/screens/other/model-list-follow.jpg` | User | Xem người theo dõi và đang theo dõi. | Tab Người theo dõi/Đang theo dõi, danh sách user theo displayName, nút theo dõi/trạng thái đang theo dõi. | Mở từ PROFILE-01/02; chọn user điều hướng `/profile/:userId`. | Phạm vi triển khai: MVP_CURRENT. |

## Messaging

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| MSG-01 | Inbox | Chưa có ảnh chuẩn | User | Xem conversation đã có message. | Avatar, displayName, preview, thời gian, unread, cursor load-more. | `/messages`; chọn item đến MSG-02. | Desktop split-view, mobile route riêng. |
| MSG-02 | Conversation detail | Chưa có ảnh chuẩn | User | Đọc và gửi text một-một. | Cursor history, bubble, seen marker, optimistic/failed retry, composer. | `/messages/:conversationId`; mobile quay lại MSG-01. | Content render dạng text; REST/MySQL là nguồn sự thật. |

## Search, Saved Posts và Liked Posts

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| SEARCH-01 | Tìm kiếm và khám phá | `docs/ui/screens/search/model-search-and-discovery.jpg` | User | Tìm kiếm người dùng, bài viết hoặc hashtag. | Thanh tìm kiếm, tìm kiếm phổ biến, gợi ý theo dõi. | Từ sidebar; kết quả user đến `/profile/:userId`; kết quả bài đến POST-01. | Phạm vi triển khai: MVP_CURRENT. Tìm user theo displayName, không theo username. Tên ảnh có "discovery" nhưng Discovery Map thuộc FUTURE_DEVELOPMENT. |
| SAVED-01 | Bài viết đã lưu | `docs/ui/screens/post/post-saved.jpg` | User | Xem các bài đã lưu. | Danh sách bài đã lưu, PostCard. | Trùng ảnh với POST-07. | Phạm vi triển khai: MVP_CURRENT. Biến thể thuộc nhóm Post nhưng liên quan trực tiếp đến Saved Posts. |
| LIKED-01 | Bài viết đã thích | Chưa có ảnh riêng | User | Xem các bài đã Like của chính mình. | Danh sách PostCard đã thích, Unlike loại bài khỏi danh sách ngay. | Từ mục “Đã thích” trong menu “Xem thêm”; route `/liked`; mở POST-01. | Chỉ chủ tài khoản xem; dữ liệu lấy từ API phân trang. |

## Admin

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| ADMIN-01 | Tổng quan quản trị | `docs/ui/screens/admin/dashboard-admin.jpg` | Admin | Theo dõi nhanh trạng thái hệ thống. | Chỉ số tổng cơ bản, trạng thái user/post, báo cáo cần xử lý. | Điều hướng đến ADMIN-02/04/05. | Phạm vi triển khai: MVP_CURRENT nếu chỉ là tổng quan cơ bản. Hoạt động gần đây/dashboard nâng cao thuộc FUTURE_DEVELOPMENT. |
| ADMIN-02 | Quản lý người dùng | `docs/ui/screens/admin/user-admin.jpg` | Admin | Xem, tìm kiếm và theo dõi thống kê người dùng. | Bảng user không có nút Khóa/Mở khóa trên từng dòng; click dòng mở chi tiết. Hai biểu đồ bên phải hiển thị trạng thái tài khoản và số người dùng mới từng ngày trong tuần hiện tại. | Chi tiết ADMIN-03. | Khóa/Mở khóa chỉ thực hiện trong chi tiết; thống kê lấy dữ liệu thật từ API Admin và tự làm mới sau thay đổi trạng thái. |
| ADMIN-03 | Chi tiết và thao tác người dùng | `docs/ui/screens/admin/thao-tác-user-admin.jpg` | Admin | Xem và quản lý một USER. | Xem thông tin, sửa avatar/tên hiển thị/ngày sinh/bio, khóa hoặc mở khóa. | Từ ADMIN-02; form sửa hồ sơ mở từ chi tiết. | Avatar cho phép xem trước, chọn JPG/JPEG/PNG/WEBP tối đa 10 MB hoặc xóa ảnh hiện tại. |
| ADMIN-04 | Quản lý bài viết | `docs/ui/screens/admin/post-admin.jpg` | Admin | Xem danh sách, thống kê và chi tiết bài viết. | Bảng post, bộ lọc, phân trang; hai biểu đồ bên phải gồm tổng bài/số bài đã ẩn và số bài tạo từng ngày trong tuần hiện tại. Danh sách không có nút Ẩn/Khôi phục; double-click mở chi tiết. | `/admin/posts/:postId`; có thể đi đến report liên quan. | Thống kê lấy dữ liệu thật từ API Admin. Trang danh sách và chi tiết cố định theo viewport; nội dung dài cuộn nội bộ. |
| ADMIN-05 | Quản lý báo cáo | `docs/ui/screens/admin/report-admin.jpg` | Admin | Xem báo cáo bài viết và trang cá nhân. | Hai tab: Moderation Case của bài viết và Profile Report Case đã gom theo target; có số người báo cáo, trạng thái, filter và phân trang. | Mở ADMIN-06 hoặc ADMIN-08. | Hai workflow giữ độc lập. |
| ADMIN-06 | Chi tiết hồ sơ kiểm duyệt | `docs/ui/screens/admin/detail-report-admin.jpg` | Admin | Xử lý một Moderation Case. | Bài hiện tại; từng Report rút gọn; kết luận case và hiển thị số lần vi phạm của tác giả. | Từ ADMIN-05; khi chọn có vi phạm phải chọn lý do ẩn bài trong modal. | Mỗi case vi phạm tính một lần; lần thứ ba tự động khóa tác giả. Trang cố định theo viewport; nội dung dài cuộn nội bộ. |
| ADMIN-07 | Thống kê hệ thống | Ảnh tham chiếu do người dùng cung cấp | Admin có permission module tương ứng | Theo dõi người dùng, bài viết và mức độ sử dụng hashtag. | Sidebar có nhóm mở rộng “Thống kê” với ba mục con. Người dùng có bộ lọc theo tháng và KPI hoạt động; Bài viết có 6 KPI, xu hướng theo ngày, donut trạng thái, cơ cấu tương tác và top bài; Hashtag có 6 KPI, xu hướng bài có hashtag, top 10, donut phân bố, tăng trưởng, hoạt động gần đây và nhóm ít sử dụng. | `/admin/user-analytics`, `/admin/post-analytics`, `/admin/hashtag-analytics`. | Mỗi mục con chỉ hiển thị và truy cập khi có `USER_ANALYTICS_VIEW`, `POST_VIEW` hoặc `HASHTAG_VIEW` tương ứng; Analytics chỉ đọc, CRUD hashtag vẫn thuộc ADMIN-09. |
| ADMIN-08 | Chi tiết báo cáo trang cá nhân | Chưa có ảnh chuẩn | Admin | Xem xét một Profile Report Case. | Tất cả reporter/lý do, snapshot, hồ sơ hiện tại, bài viết và ba lựa chọn: không vi phạm, vi phạm, vi phạm & khóa. | `/admin/profile-reports/:caseId`. | Khóa ngay thu hồi phiên và ghi đầy đủ lịch sử; không tự sửa hồ sơ hoặc ẩn Post. |
| ADMIN-09 | Quản lý hashtag | Chưa có ảnh chuẩn | Admin | Quản lý hashtag đang có trong hệ thống. | Tìm kiếm, phân trang, tạo mới và bảng gồm tên, số bài viết, ngày tạo, ngày sử dụng mới nhất cùng thao tác sửa/xóa. | Route `/admin/hashtags`, mở từ sidebar Admin. | Sửa tên giữ nguyên liên kết bài; xóa có xác nhận và nêu số bài bị ảnh hưởng. |
| ADMIN-10 | Quản lý quản trị viên và phân quyền | Chưa có ảnh chuẩn | Admin có permission tương ứng; phân quyền chỉ dành cho Bootstrap | Xem và quản lý tài khoản ADMIN cùng role RBAC. | Danh sách phân trang; click mở chi tiết; Bootstrap Admin tạo tài khoản hỗ trợ, gán/thu hồi role nghiệp vụ, tạo role tùy chỉnh và cấu hình permission; các thao tác trạng thái/mật khẩu theo permission riêng. | Route `/admin/admins` và `/admin/permissions`. | Master Admin chỉ đọc tại màn hình này, không thể được phân quyền, cấp lại mật khẩu hoặc vô hiệu hóa; vẫn tự đổi mật khẩu tại `/admin/profile`. |
| ADMIN-11 | Hồ sơ quản trị viên | Chưa có ảnh chuẩn | Admin, bao gồm Cộng tác viên | Xem và tự quản lý thông tin tài khoản quản trị. | Xem email, username, trạng thái, role; sửa tên hiển thị, ngày sinh, bio; đổi mật khẩu bằng mật khẩu hiện tại. | Route `/admin/profile`, mở từ sidebar Admin hoặc sidebar Cộng tác viên. | Không cho tự sửa quyền/trạng thái; đổi mật khẩu thu hồi toàn bộ Refresh Token và yêu cầu đăng nhập lại. |
| ADMIN-12 | Chi tiết nội dung Cộng tác viên | Ảnh tham chiếu do người dùng cung cấp | Cộng tác viên có permission tương ứng | Xem và quản lý bài do Managed Social Identity của mình đăng. | Nội dung, tác giả, media, hashtag, Location, trạng thái, thời gian, tương tác; sửa trong 15 phút và xác nhận xóa mềm. | Route `/admin/collaborator/posts/:postId`, mở từ “Nội dung của tôi”. | Xem được bài `HIDDEN/DELETED` của chính danh tính nhưng chỉ bài `PUBLISHED` mới có thao tác; Backend kiểm tra quyền sở hữu cuối cùng. |

## System States

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| SYS-01 | Không có quyền | `docs/ui/screens/system/error-403.jpg` | Tất cả | Báo người dùng không có quyền truy cập. | Mã 403, thông báo, nút quay lại/điều hướng. | Protected/Admin route. | Error state. |
| SYS-02 | Không tìm thấy | `docs/ui/screens/system/error-404.jpg` | Tất cả | Báo route hoặc tài nguyên không tồn tại. | Mã 404, thông báo, nút quay lại. | Route `*` hoặc tài nguyên bị xóa/ẩn. | Error state. |
| SYS-03 | Lỗi hệ thống | `docs/ui/screens/system/error-500.jpg` | Tất cả | Báo lỗi hệ thống. | Mã 500, thông báo, nút thử lại/quay lại. | Khi API/server lỗi. | Error state. |
| SYS-04 | Phiên đăng nhập hết hạn | `docs/ui/screens/system/model-expired-login.jpg` | User, Admin | Yêu cầu đăng nhập lại khi phiên hết hạn. | Thông báo hết hạn, nút đăng nhập lại. | Refresh token thất bại, quay về AUTH-01. | Modal trạng thái hệ thống. |


- Ghi chú sai lệch: Ảnh profile người dùng khác đang active mục “Trang cá nhân” ở sidebar.
- Khi dựng demo và frontend, chỉ active mục này tại hồ sơ của tài khoản hiện tại.

