# Danh sách màn hình

`README.md` là nguồn sự thật cao nhất. Contract HTTP chi tiết duy nhất nằm tại `docs/data/API-CONTRACT.md`; tài liệu này chỉ mô tả màn hình và trạng thái UI. Ảnh trong `docs/ui/screens/` là tham chiếu visual, còn các màn hình Auth bắt buộc chưa có ảnh được ghi rõ là `DESIGN_REQUIRED`.

## Quy tắc chung

- Ảnh Stitch là tài liệu tham chiếu và không bị xóa dù có chi tiết ngoài MVP.
- UI MVP không dùng username, handle, userSlug hoặc email công khai tương tự.
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
- ADMIN-01, ADMIN-02, ADMIN-03, ADMIN-04, ADMIN-05, ADMIN-06.
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
| AUTH-03 | Onboarding tên hiển thị | `docs/ui/screens/auth/update-profile-1.jpg` | User chưa hoàn tất hồ sơ | Nhập tên hiển thị bắt buộc sau đăng ký. | Trường tên hiển thị, nút tiếp tục. | Route `/onboarding/profile`, bước 1/3. | Phạm vi triển khai: MVP_CURRENT. Không có nút bỏ qua vì tên hiển thị bắt buộc để hoàn tất hồ sơ. |
| AUTH-04 | Onboarding ảnh đại diện | `docs/ui/screens/auth/update-profile-2.jpg` | User chưa hoàn tất hồ sơ | Thêm ảnh đại diện tùy chọn. | Chọn ảnh preview hoặc bỏ qua. | Route `/onboarding/profile`, bước 2/3. | Phạm vi triển khai: MVP_CURRENT. Không upload API thật trong mock Frontend. |
| AUTH-05 | Onboarding ngày sinh và bio | `docs/ui/screens/auth/update-profile-3.jpg` | User chưa hoàn tất hồ sơ | Nhập ngày sinh bắt buộc và bio tùy chọn. | Ngày sinh, bio, hoàn tất hồ sơ; không cho bỏ qua ngày sinh. | Route `/onboarding/profile`, bước 3/3; hoàn tất cập nhật `profileCompletedAt`. | Phạm vi triển khai: MVP_CURRENT. Ngày sinh không được lớn hơn ngày hiện tại và người dùng phải đủ 18 tuổi. |
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
| PROFILE-01 | Hồ sơ của mình | `docs/ui/screens/profile/profile-for-self.jpg` | User | Xem và quản lý hồ sơ cá nhân. | Thông tin cá nhân công khai, số follower/following, chỉnh sửa trang cá nhân, tab bài viết, danh sách bài. | Route `/profile/me`; chỉnh sửa mở PROFILE-03; follower/following mở PROFILE-04; bài mở POST-01. | Phạm vi triển khai: MVP_CURRENT. Sidebar active mục "Trang cá nhân". Tab "Bài đăng lại", "Câu trả lời", "File phương tiện" thuộc FUTURE_DEVELOPMENT. |
| PROFILE-02 | Hồ sơ người khác | `docs/ui/screens/profile/profile-for-other.jpg` | User | Xem hồ sơ công khai và theo dõi người khác. | Thông tin người dùng, Follow/Unfollow, menu thêm, tab nội dung, danh sách bài. | Route `/profile/:userId`; follower/following mở PROFILE-04; bài mở POST-01. | Phạm vi triển khai: MVP_CURRENT. Sidebar không active mục "Trang cá nhân". Nút "Nhắn tin" thuộc FUTURE_DEVELOPMENT. |
| PROFILE-03 | Modal chỉnh sửa hồ sơ | `docs/ui/screens/profile/model-edit-profile.jpg` | User | Cập nhật hồ sơ cá nhân. | Avatar, tên hiển thị, bio/thông tin cá nhân, ngày sinh bắt buộc, lưu. | Mở từ PROFILE-01. | Phạm vi triển khai: MVP_CURRENT. Không cho xóa ngày sinh hoặc lưu khi người dùng chưa đủ 18 tuổi; không triển khai username hoặc hồ sơ riêng tư dù có trong ảnh. |
| PROFILE-04 | Modal danh sách follower/following | `docs/ui/screens/other/model-list-follow.jpg` | User | Xem người theo dõi và đang theo dõi. | Tab Người theo dõi/Đang theo dõi, danh sách user theo displayName, nút theo dõi/trạng thái đang theo dõi. | Mở từ PROFILE-01/02; chọn user điều hướng `/profile/:userId`. | Phạm vi triển khai: MVP_CURRENT. |

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
| ADMIN-03 | Menu thao tác người dùng | `docs/ui/screens/admin/thao-tác-user-admin.jpg` | Admin | Thao tác trên một user. | Xem thông tin, khóa/mở khóa hoặc hành động quản trị liên quan. | Từ ADMIN-02. | Ảnh là biến thể/menu thao tác; chi tiết text nhỏ CẦN XÁC NHẬN. |
| ADMIN-04 | Quản lý bài viết | `docs/ui/screens/admin/post-admin.jpg` | Admin | Xem danh sách, thống kê và chi tiết bài viết. | Bảng post, bộ lọc, phân trang; hai biểu đồ bên phải gồm tổng bài/số bài đã ẩn và số bài tạo từng ngày trong tuần hiện tại. Danh sách không có nút Ẩn/Khôi phục; double-click mở chi tiết. | `/admin/posts/:postId`; có thể đi đến report liên quan. | Thống kê lấy dữ liệu thật từ API Admin. Trang danh sách và chi tiết cố định theo viewport; nội dung dài cuộn nội bộ. |
| ADMIN-05 | Quản lý hồ sơ kiểm duyệt | `docs/ui/screens/admin/report-admin.jpg` | Admin | Xem danh sách Moderation Case. | Một dòng mỗi case, tổng Report/reporter, trạng thái, thời gian, filter và phân trang; không hiển thị hoặc lọc theo lý do tại màn hình danh sách. | Mở ADMIN-06. | Frontend không tự group Report; trang cố định theo viewport và vùng danh sách tự cuộn dọc. |
| ADMIN-06 | Chi tiết hồ sơ kiểm duyệt | `docs/ui/screens/admin/detail-report-admin.jpg` | Admin | Xử lý một Moderation Case. | Bài hiện tại; từng Report rút gọn không render snapshot/media; Admin xử lý; hai hành động trực tiếp khi OPEN. | Từ ADMIN-05; khi chọn có vi phạm phải chọn lý do ẩn bài trong modal. | Không có trường kết luận tự do, bước tiếp nhận hoặc xử lý lại case đã giải quyết. Trang cố định theo viewport; nội dung dài cuộn nội bộ và không hiện thanh cuộn dọc. |

## System States

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| SYS-01 | Không có quyền | `docs/ui/screens/system/error-403.jpg` | Tất cả | Báo người dùng không có quyền truy cập. | Mã 403, thông báo, nút quay lại/điều hướng. | Protected/Admin route. | Error state. |
| SYS-02 | Không tìm thấy | `docs/ui/screens/system/error-404.jpg` | Tất cả | Báo route hoặc tài nguyên không tồn tại. | Mã 404, thông báo, nút quay lại. | Route `*` hoặc tài nguyên bị xóa/ẩn. | Error state. |
| SYS-03 | Lỗi hệ thống | `docs/ui/screens/system/error-500.jpg` | Tất cả | Báo lỗi hệ thống. | Mã 500, thông báo, nút thử lại/quay lại. | Khi API/server lỗi. | Error state. |
| SYS-04 | Phiên đăng nhập hết hạn | `docs/ui/screens/system/model-expired-login.jpg` | User, Admin | Yêu cầu đăng nhập lại khi phiên hết hạn. | Thông báo hết hạn, nút đăng nhập lại. | Refresh token thất bại, quay về AUTH-01. | Modal trạng thái hệ thống. |


- Ghi chú sai lệch: Ảnh profile người dùng khác đang active mục “Trang cá nhân” ở sidebar.
- Khi dựng demo và frontend, chỉ active mục này tại hồ sơ của tài khoản hiện tại.

