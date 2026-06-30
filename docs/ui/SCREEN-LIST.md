# Danh sách màn hình

Tài liệu này chỉ liệt kê các màn hình, modal, menu hoặc biến thể thực sự có ảnh trong `docs/ui/screens/`. Các chi tiết xuất hiện trong ảnh nhưng nằm ngoài phạm vi MVP được ghi chú rõ để không triển khai nhầm.

## Quy tắc chung

- Ảnh Stitch là tài liệu tham chiếu và không bị xóa dù có chi tiết ngoài MVP.
- UI MVP không dùng username, handle, userSlug hoặc định danh công khai tương tự.
- Hồ sơ cá nhân dùng route `/profile/me`.
- Hồ sơ người dùng khác dùng route `/profile/:userId`.
- Khi chọn người dùng từ Feed, Search, follower/following hoặc mention, điều hướng bằng userId.
- Mention nếu xuất hiện trong thiết kế tương lai sẽ hiển thị bằng displayName và liên kết nội bộ bằng userId.
- `MVP_CURRENT`: triển khai trong bản demo HTML và Frontend MVP hiện tại.
- `FUTURE_DEVELOPMENT`: giữ làm tài liệu thiết kế sau MVP, không triển khai trong bản demo MVP hiện tại.

## Phân loại phạm vi màn hình

### MVP_CURRENT

- AUTH-01, AUTH-02, AUTH-03.
- FEED-01.
- POST-01, POST-02, POST-03, POST-04, POST-05, POST-06, POST-07, POST-08, POST-09, POST-10.
- PROFILE-01, PROFILE-02, PROFILE-03, PROFILE-04.
- SEARCH-01, SAVED-01.
- ADMIN-01, ADMIN-02, ADMIN-03, ADMIN-04, ADMIN-05, ADMIN-06.
- SYS-01, SYS-02, SYS-03, SYS-04.

### FUTURE_DEVELOPMENT

- AUTH-04, AUTH-05, AUTH-06.
- Các chi tiết ngoài MVP đang xuất hiện trong ảnh như OAuth, nhắn tin, hoạt động realtime, repost, trích dẫn bài viết, file phương tiện, bài đăng lại, cài đặt admin, dashboard nâng cao, mention và các menu quản trị/nội dung nâng cao.

Ghi chú chuẩn cho FUTURE_DEVELOPMENT: Màn hình hoặc chi tiết được giữ lại làm tài liệu thiết kế cho giai đoạn mở rộng sau MVP. Không triển khai trong bản demo và Frontend MVP hiện tại.

## Authentication

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| AUTH-01 | Đăng nhập | `docs/ui/screens/auth/login.jpg` | Khách | Cho phép người dùng vào hệ thống. | Nhập email hoặc số điện thoại, nhập mật khẩu, chuyển sang đăng ký. | Sau đăng nhập thành công đến Feed; liên kết đến AUTH-02. | Phạm vi triển khai: MVP_CURRENT. Nút Google/Facebook và quên mật khẩu trong ảnh thuộc FUTURE_DEVELOPMENT. |
| AUTH-02 | Đăng ký | `docs/ui/screens/auth/register.jpg` | Khách | Tạo tài khoản mới. | Email, số điện thoại, mật khẩu, xác nhận mật khẩu nếu UI cần, đồng ý điều khoản nếu form yêu cầu, chuyển sang đăng nhập. | Thành công đến AUTH-03 hoặc đăng nhập. | Phạm vi triển khai: MVP_CURRENT. Không triển khai username, Google/Facebook. Tên hiển thị là dữ liệu hồ sơ, cập nhật sau khi tạo tài khoản. |
| AUTH-03 | Đăng ký thành công | `docs/ui/screens/auth/register-success.jpg` | Khách | Xác nhận tạo tài khoản thành công. | Thông báo thành công, nút đăng nhập. | Đi đến AUTH-01. | Phạm vi triển khai: MVP_CURRENT. |
| AUTH-04 | Nhập mã xác minh quên mật khẩu | `docs/ui/screens/auth/verified-for-forget-password.jpg` | Khách | Nhập mã xác minh khi đặt lại mật khẩu. | Các ô nhập mã, tiếp tục, gửi lại mã. | Đến AUTH-05 khi mã hợp lệ. | Phạm vi triển khai: FUTURE_DEVELOPMENT. Giữ ảnh làm tài liệu thiết kế sau MVP. |
| AUTH-05 | Đặt lại mật khẩu | `docs/ui/screens/auth/set-password.jpg` | Khách | Tạo mật khẩu mới. | Nhập mật khẩu mới, xác nhận mật khẩu, lưu mật khẩu. | Thành công đến AUTH-06. | Phạm vi triển khai: FUTURE_DEVELOPMENT. Giữ ảnh làm tài liệu thiết kế sau MVP. |
| AUTH-06 | Đặt lại mật khẩu thành công | `docs/ui/screens/auth/set-password-success.jpg` | Khách | Xác nhận đổi mật khẩu thành công. | Thông báo thành công, nút đăng nhập. | Đi đến AUTH-01. | Phạm vi triển khai: FUTURE_DEVELOPMENT. Giữ ảnh làm tài liệu thiết kế sau MVP. |

## Feed

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| FEED-01 | Feed người dùng | `docs/ui/screens/feed/feed.jpg` | User | Xem bảng tin chính. | Sidebar, tab Dành cho bạn/Đang theo dõi, composer nhanh, danh sách PostCard, like, comment, lưu, menu bài viết. | Tạo bài mở POST-02; tab Following dùng cùng màn hình; vào chi tiết POST-01; tìm kiếm SEARCH-01; hồ sơ cá nhân `/profile/me`; hồ sơ tác giả `/profile/:userId`; saved POST-07. | Phạm vi triển khai: MVP_CURRENT. Mục "Hoạt động", repost/share nâng cao và dữ liệu dạng @ trong ảnh là tham chiếu visual, không triển khai như nghiệp vụ MVP. |

## Post

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| POST-01 | Chi tiết bài viết | `docs/ui/screens/post/detail-post.jpg` | User | Xem một bài viết cùng bình luận. | Nội dung bài, ảnh, like, bình luận, lưu/chia sẻ, danh sách bình luận, ô nhập bình luận. | Từ FEED-01, PROFILE-01/02, POST-07; menu mở POST-04/POST-05. | Một số biểu tượng cần xác nhận ý nghĩa chính xác. |
| POST-02 | Modal tạo bài viết | `docs/ui/screens/post/model-create-post.jpg` | User | Tạo bài viết mới. | Nhập nội dung, gắn ảnh, thanh công cụ, nút đăng, giới hạn nội dung. | Mở từ sidebar/composer FEED-01. | Modal. |
| POST-03 | Modal chỉnh sửa bài viết | `docs/ui/screens/post/model-edit-post.jpg` | Tác giả bài viết | Sửa nội dung bài của mình. | Sửa text/hashtag, xem ảnh cũ, lưu thay đổi, hủy. | Mở từ menu POST-04; sau lưu quay lại bài/feed/profile. | Ảnh không được thay đổi sau khi đăng, đúng MVP. |
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
| PROFILE-03 | Modal chỉnh sửa hồ sơ | `docs/ui/screens/profile/model-edit-profile.jpg` | User | Cập nhật hồ sơ cá nhân. | Avatar, tên hiển thị, bio/thông tin cá nhân, ngày sinh nếu UI cần, lưu. | Mở từ PROFILE-01. | Phạm vi triển khai: MVP_CURRENT. Không triển khai username hoặc hồ sơ riêng tư dù có trong ảnh. |
| PROFILE-04 | Modal danh sách follower/following | `docs/ui/screens/other/model-list-follow.jpg` | User | Xem người theo dõi và đang theo dõi. | Tab Người theo dõi/Đang theo dõi, danh sách user theo displayName, nút theo dõi/trạng thái đang theo dõi. | Mở từ PROFILE-01/02; chọn user điều hướng `/profile/:userId`. | Phạm vi triển khai: MVP_CURRENT. |

## Search và Saved Posts

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| SEARCH-01 | Tìm kiếm và khám phá | `docs/ui/screens/search/model-search-and-discovery.jpg` | User | Tìm kiếm người dùng, bài viết hoặc hashtag. | Thanh tìm kiếm, tìm kiếm phổ biến, gợi ý theo dõi. | Từ sidebar; kết quả user đến `/profile/:userId`; kết quả bài đến POST-01. | Phạm vi triển khai: MVP_CURRENT. Tìm user theo displayName, không theo username. Tên ảnh có "discovery" nhưng Discovery Map thuộc FUTURE_DEVELOPMENT. |
| SAVED-01 | Bài viết đã lưu | `docs/ui/screens/post/post-saved.jpg` | User | Xem các bài đã lưu. | Danh sách bài đã lưu, PostCard. | Trùng ảnh với POST-07. | Phạm vi triển khai: MVP_CURRENT. Biến thể thuộc nhóm Post nhưng liên quan trực tiếp đến Saved Posts. |

## Admin

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| ADMIN-01 | Tổng quan quản trị | `docs/ui/screens/admin/dashboard-admin.jpg` | Admin | Theo dõi nhanh trạng thái hệ thống. | Chỉ số tổng cơ bản, trạng thái user/post, báo cáo cần xử lý. | Điều hướng đến ADMIN-02/04/05. | Phạm vi triển khai: MVP_CURRENT nếu chỉ là tổng quan cơ bản. Hoạt động gần đây/dashboard nâng cao thuộc FUTURE_DEVELOPMENT. |
| ADMIN-02 | Quản lý người dùng | `docs/ui/screens/admin/user-admin.jpg` | Admin | Xem và tìm kiếm người dùng. | Bảng user, trạng thái, bộ lọc/tìm kiếm, phân trang, thao tác. | Menu thao tác ADMIN-03. | Các cột chính xác CẦN XÁC NHẬN theo API. |
| ADMIN-03 | Menu thao tác người dùng | `docs/ui/screens/admin/thao-tác-user-admin.jpg` | Admin | Thao tác trên một user. | Xem thông tin, khóa/mở khóa hoặc hành động quản trị liên quan. | Từ ADMIN-02. | Ảnh là biến thể/menu thao tác; chi tiết text nhỏ CẦN XÁC NHẬN. |
| ADMIN-04 | Quản lý bài viết | `docs/ui/screens/admin/post-admin.jpg` | Admin | Xem danh sách bài viết và trạng thái xử lý. | Bảng post, trạng thái, bộ lọc/tìm kiếm, phân trang, thao tác ẩn/khôi phục. | Có thể đi đến bài hoặc report liên quan. | Chỉ ẩn/khôi phục post thuộc MVP. |
| ADMIN-05 | Quản lý báo cáo | `docs/ui/screens/admin/report-admin.jpg` | Admin | Xem danh sách báo cáo. | Bảng report, trạng thái, lý do, người báo cáo, thời gian, phân trang. | Mở ADMIN-06. | Tập trung report bài viết. |
| ADMIN-06 | Chi tiết báo cáo | `docs/ui/screens/admin/detail-report-admin.jpg` | Admin | Xử lý một báo cáo cụ thể. | Bài bị báo cáo, thông tin báo cáo, lịch sử xử lý, từ chối báo cáo, xác nhận vi phạm. | Từ ADMIN-05; có thể ẩn bài khi vi phạm. | Lịch sử xử lý đơn giản; audit log chi tiết ngoài MVP. |

## System States

| Mã | Tên màn hình | Ảnh | Actor | Mục đích | Chức năng thể hiện | Liên quan/điều hướng | Ghi chú |
|---|---|---|---|---|---|---|---|
| SYS-01 | Không có quyền | `docs/ui/screens/system/error-403.jpg` | Tất cả | Báo người dùng không có quyền truy cập. | Mã 403, thông báo, nút quay lại/điều hướng. | Protected/Admin route. | Error state. |
| SYS-02 | Không tìm thấy | `docs/ui/screens/system/error-404.jpg` | Tất cả | Báo route hoặc tài nguyên không tồn tại. | Mã 404, thông báo, nút quay lại. | Route `*` hoặc tài nguyên bị xóa/ẩn. | Error state. |
| SYS-03 | Lỗi hệ thống | `docs/ui/screens/system/error-500.jpg` | Tất cả | Báo lỗi hệ thống. | Mã 500, thông báo, nút thử lại/quay lại. | Khi API/server lỗi. | Error state. |
| SYS-04 | Phiên đăng nhập hết hạn | `docs/ui/screens/system/model-expired-login.jpg` | User, Admin | Yêu cầu đăng nhập lại khi phiên hết hạn. | Thông báo hết hạn, nút đăng nhập lại. | Refresh token thất bại, quay về AUTH-01. | Modal trạng thái hệ thống. |


- Ghi chú sai lệch: Ảnh profile người dùng khác đang active mục “Trang cá nhân” ở sidebar.
- Khi dựng demo và frontend, chỉ active mục này tại hồ sơ của tài khoản hiện tại.

