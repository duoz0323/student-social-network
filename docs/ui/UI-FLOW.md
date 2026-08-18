# Luồng giao diện

`README.md` là nguồn sự thật cao nhất. Contract HTTP chi tiết duy nhất nằm tại `docs/data/API-CONTRACT.md`; tài liệu này chỉ mô tả chuyển trạng thái và điều hướng UI. Ảnh trong `docs/ui/screens/` và `UI-DEMO.html` là tham chiếu visual, không được dùng để giữ hành vi Auth cũ.

## 1. Luồng MVP hiện tại

### 1.1 Đăng ký

```text
AUTH-02 Đăng ký
→ Nhập email
→ Nhập mật khẩu và xác nhận mật khẩu
→ Đồng ý điều khoản nếu form yêu cầu
→ Khởi tạo đăng ký
→ Backend tạo hoặc phục hồi pending registration
→ Frontend giữ flow token trong memory/sessionStorage
→ AUTH-OTP-01 nhập OTP email
→ Có thể resend sau cooldown hoặc recovery pending
→ OTP hợp lệ mới tạo user, profile và session JWT hệ thống
→ /onboarding/profile
→ AUTH-03 Onboarding bước lớn 1: giữ luồng cơ bản hiện có gồm username, tên hiển thị, avatar, ngày sinh và bio
→ PUT onboarding kiểm tra lại username/ngày sinh và cập nhật profileCompletedAt
→ AUTH-04 Bước lớn 2 tùy chọn: tìm/chọn School → Faculty → Major và Entry Year hoặc bỏ qua
→ AUTH-05 Bước lớn 3 tùy chọn: chọn tối đa 10 Interests hoặc bỏ qua
→ PUT profile chỉ gửi phần Academic/Interests người dùng chủ động lưu; hai bước này không đổi profileCompletedAt
→ AUTH-06 /onboarding/success
→ Hiển thị tạo tài khoản thành công
→ Người dùng chọn Vào trang chủ
→ FEED-01 Feed người dùng
```

Ghi chú: Không dùng username hoặc displayName trong form đăng ký. Submit đầu tiên không tạo user, profile, Access Token hoặc Refresh Token. Cùng email có pending hợp lệ được resume với flow token mới và không tự resend trong cooldown. Flow token không được lưu `localStorage`, đưa vào URL hoặc dùng ngoài Auth flow; header chi tiết theo `API-CONTRACT.md`.

Google/Facebook:

```text
Bấm nút Google hoặc Facebook
→ Provider SDK trả credential cho Frontend
→ Frontend gửi credential duy nhất đến Auth endpoint tương ứng
→ Backend xác minh với provider
→ Nếu provider đã link: đăng nhập đúng users.id
→ Nếu identity mới hợp lệ: tạo provider-only user khi được phép
→ Nếu có xung đột: AUTH-SOCIAL-01 hiển thị lựa chọn Backend cho phép
→ Auth thành công: nhận JWT hệ thống và đến onboarding/Feed
```

Facebook không trả email vẫn có thể tạo provider-only user; UI không yêu cầu hoặc tạo email giả. Khi email Facebook trùng user `ACTIVE` chưa link provider, UI cho chọn đăng nhập account cũ hoặc tạo Facebook-only `USER` độc lập; không tự link/gộp và không kế thừa quyền. Social conflict token là flow token một lần, TTL 5 phút; UI chỉ hiển thị `allowedActions` từ Backend.

Nếu `profile_completed_at` còn `NULL`, route guard chuyển người dùng về onboarding và API mạng xã hội chính trả `PROFILE_NOT_COMPLETED`. Frontend tải lại dữ liệu onboarding hiện có để legacy user chỉ bổ sung username mà không mất displayName, ngày sinh hoặc bio. Sau khi dữ liệu cơ bản hoàn tất, route onboarding vẫn cho phép tiếp tục hai bước tùy chọn và đọc current profile để prefill khi refresh; login và refresh không trả lỗi `PROFILE_NOT_COMPLETED`.

### 1.2 Đăng nhập

```text
AUTH-01 Đăng nhập
→ Nhập email
→ Nhập mật khẩu
→ Gửi đăng nhập
→ Nhận Access Token và Refresh Token
→ Nếu hồ sơ chưa hoàn tất thì đến onboarding
→ Nếu hồ sơ đã hoàn tất và role USER thì đến FEED-01 Feed người dùng
→ Nếu hồ sơ đã hoàn tất và role ADMIN thì đến Admin
```

Google/Facebook login dùng cùng nguyên tắc provider credential chỉ dành cho Auth endpoint. Sau khi Backend cấp JWT hệ thống, Frontend phải loại bỏ provider credential khỏi state sớm nhất có thể.

### 1.2.1 Quản lý phương thức đăng nhập

```text
AUTH-METHOD-01
→ Xem email, Google, Facebook đang liên kết
→ Link email: AUTH-METHOD-02 initiate challenge riêng → verify OTP → quay lại danh sách
→ Link Google/Facebook: xác minh provider trong phiên JWT hiện tại
→ Unlink: AUTH-REAUTH-01 xác thực lại → xác nhận → Backend kiểm tra phương thức cuối
→ Thành công cập nhật danh sách phương thức
```

User đích của mọi thao tác link lấy từ JWT hiện tại. UI không tự suy ra account theo social email và không cho phép bỏ qua Backend unlink guard. Unlink method không tồn tại phải hiển thị `AUTH_METHOD_NOT_LINKED`. Thu hồi các session khác là P1 và chưa tự động triển khai.

Trường hợp phiên hết hạn:

```text
API trả 401
→ Thử refresh token một lần
→ Refresh thất bại
→ SYS-04 Phiên đăng nhập hết hạn
→ AUTH-01 Đăng nhập
```

### 1.3 Feed For You, Following, Nearby và Discovery Map

```text
FEED-01
→ Các trang Feed tải rail “Có thể bạn biết” cố định bên phải trên desktop rộng bằng Student Recommendation V1
→ Card hiển thị avatar, displayName, @username, tối đa hai lý do và nút Theo dõi
→ Follow thành công loại card; Follow thất bại giữ card và hiển thị lỗi
→ Tab Dành cho bạn tải bài PUBLISHED hợp lệ
→ Tab Đang theo dõi tải bài của người đang follow
→ Tab Gần bạn mới yêu cầu một snapshot Geolocation và tải Post có Location theo radius đã chọn
→ Trong Discovery, người dùng có thể chuyển sang Bản đồ tại `/feed/map` mà không xin Geolocation
→ Bản đồ mở ở tâm mặc định; pan/zoom chỉ đánh dấu viewport mới
→ Người dùng bấm Tìm trong khu vực này để tải/thay marker và reset panel đang chọn
→ Marker được cluster phía client; marker click mở Location side panel/bottom sheet và tải PostCard bằng cursor
→ Nút Vị trí của tôi mới gọi getCurrentPosition, pan/zoom và đặt user marker; người dùng vẫn phải bấm tìm viewport
→ Hiển thị danh sách PostCard
→ Người dùng cuộn đến gần cuối danh sách
→ Frontend gửi lại nextCursor opaque để tải trang tiếp theo
→ Dừng khi hasNext = false
```

Nếu Feed Following rỗng:

```text
Hiển thị empty state
→ Gợi ý người dùng tìm kiếm hoặc follow tài khoản khác
```

Nearby chỉ gọi `getCurrentPosition` khi mở tab `Gần bạn` hoặc bấm `Cập nhật vị trí`; không dùng `watchPosition`, không lưu tọa độ vào storage/URL/analytics. Người dùng chọn radius `1/3/5/10/20 km`, mặc định 5 km. Đổi radius hoặc vị trí reset list/cursor và hủy request cũ; deny, unavailable, timeout, API error, empty, loading-more và end đều có trạng thái riêng. `INVALID_CURSOR` dừng phân trang và không tự retry.

Map không gọi marker API theo mỗi lần pan/zoom và không xin GPS khi mount. Marker request bị hủy khi tìm viewport mới; Location Posts request bị hủy khi đổi marker. Cursor luôn opaque, `postCount` lấy từ marker response, `truncated` hiển thị cảnh báo phóng to và lỗi SDK/API/GPS đều có retry hoặc thông báo riêng mà không làm mất khả năng dùng map.

### 1.4 Tạo bài viết

```text
FEED-01 hoặc sidebar Tạo bài viết
→ POST-02 Modal tạo bài viết
→ Nhập nội dung tối đa 500 ký tự hoặc chọn ảnh/video
→ Có thể gắn hashtag
→ Có thể chọn tối đa một Location từ dữ liệu Google Places phía Frontend
→ Đăng bài
→ Bài xuất hiện trên Feed/Profile nếu PUBLISHED
```

Quy tắc: bài phải có nội dung hoặc ít nhất một media; tổng tối đa 4 media và tối đa một video. Ảnh hỗ trợ JPG/JPEG/PNG/WEBP; video hỗ trợ MP4/WebM, tối đa 100 MB và 3 phút. Location là tùy chọn và không được tính là nội dung bài viết. Frontend gửi `placeId`, `displayName`, `formattedAddress`, `latitude`, `longitude`; Backend dùng lại Location theo Place ID nếu đã tồn tại.

### 1.5 Xem chi tiết bài viết

```text
PostCard trong Feed/Profile/Saved
→ POST-01 Chi tiết bài viết
→ Xem nội dung, ảnh, số liệu tương tác
→ Xem danh sách bình luận
→ Nhập bình luận mới
```

Nếu request danh sách bình luận lỗi, Post Detail hợp lệ vẫn phải hiển thị; lỗi được giới hạn trong
khu vực bình luận và không được chuyển toàn màn hình sang trạng thái không tìm thấy bài viết.

### 1.6 Chỉnh sửa và xóa bài viết của mình

Chỉnh sửa:

```text
PostCard của chính mình
→ POST-04 Menu thao tác bài viết
→ Hiển thị Chỉnh sửa bài viết kèm countdown từ 15:00; tự ẩn hành động khi về 00:00
→ Chỉnh sửa bài viết
→ Frontend gọi GET /api/v1/posts/{postId} và hiển thị trạng thái tải/lỗi
→ Dùng Post Detail mới nhất để khởi tạo nội dung, hashtag, media và Location
→ POST-03 Modal chỉnh sửa bài viết
→ Sửa nội dung; tìm/chọn hashtag bằng cùng bộ gợi ý của form tạo bài
→ Giữ/gỡ media cũ hoặc thêm ảnh/video mới
→ Chọn KEEP, REPLACE hoặc REMOVE cho Location
→ Lưu thay đổi bằng PUT /api/v1/posts/{postId}; khóa form trong lúc gửi
→ Cập nhật PostCard từ response PUT
→ Quay lại bài viết/feed/profile
```

Xóa:

```text
PostCard của chính mình
→ POST-04 Menu thao tác bài viết
→ Xóa
→ POST-05 Modal xác nhận xóa
→ Xóa bài viết
→ POST-06 Xóa thành công
→ Bài không còn hiển thị trong Feed/Profile/Search thông thường
```

Quy tắc: chỉ tác giả được sửa/xóa trong giới hạn chỉnh sửa 15 phút. Có thể giữ/gỡ media cũ hoặc thêm ảnh/video mới nhưng tổng tối đa 4 media và tối đa một video. `KEEP` giữ nguyên Location, `REPLACE` chọn Location khác, `REMOVE` gỡ Location; xóa Post không xóa Location dùng chung.

### 1.7 Like, bình luận và lưu bài

Like/Unlike:

```text
PostCard hoặc POST-01
→ Bấm biểu tượng thích
→ Cập nhật trạng thái liked/unliked và số lượt thích
```

Danh sách bài đã thích:

```text
Menu Xem thêm → Đã thích
→ Route /liked
→ Tải GET /api/v1/posts/liked bằng Cursor Pagination
→ Unlike một bài thì loại bài đó khỏi danh sách hiện tại
```

Bình luận:

```text
POST-01
→ Nhập nội dung bình luận
→ Nhấn Enter hoặc nút Đăng để gửi; Shift+Enter để xuống dòng
→ Bình luận xuất hiện trong danh sách
→ Chủ bình luận có thể xóa bình luận của mình
```

Lưu/Bỏ lưu:

```text
PostCard hoặc menu bài viết
→ Lưu hoặc Bỏ lưu
→ POST-07/SAVED-01 hiển thị danh sách bài đã lưu của chính người dùng
```

### 1.8 Xem hồ sơ

Hồ sơ của mình:

```text
Sidebar Trang cá nhân
→ /profile/me
→ PROFILE-01 Hồ sơ của mình
→ Xem thông tin cá nhân, số follower/following, bài đã đăng
→ Chọn Chỉnh sửa trang cá nhân
→ PROFILE-03 Modal chỉnh sửa hồ sơ
→ Sửa thông tin cơ bản, Academic Profile và tối đa 10 Interests
→ Lưu thay đổi trong một request profile update
```

Hồ sơ người khác:

```text
Chọn avatar/tên hiển thị từ Feed/Search/Comment
→ /profile/:userId
→ PROFILE-02 Hồ sơ người khác
→ Xem thông tin công khai và bài viết
→ Follow hoặc Unfollow
→ Menu thêm có Hạn chế, Báo cáo và Chặn
→ Báo cáo mở modal sáu lý do rồi gửi tới Admin
```

Quy tắc: `/profile/me` active mục Trang cá nhân. `/profile/:userId` không active mục Trang cá nhân. Nút Nhắn tin gọi REST mở conversation và chỉ điều hướng khi Backend cho phép.

### 1.9 Follow và Unfollow

```text
PROFILE-02 hoặc PROFILE-04
→ Bấm Theo dõi
→ Trạng thái chuyển thành Đang theo dõi
→ Bài của người đó có thể xuất hiện trong Feed Following
→ Bấm Đang theo dõi/Bỏ theo dõi
→ Hủy quan hệ follow
```

Quy tắc: không follow chính mình, không tạo follow trùng, không có Follow Request.

### 1.10 Tìm kiếm

```text
Sidebar Tìm kiếm
→ SEARCH-01
→ Nhập từ khóa theo displayName, nội dung bài viết hoặc hashtag
→ Xem gợi ý/tìm kiếm phổ biến hoặc kết quả
→ Chọn user để đến /profile/:userId
→ Chọn bài viết/hashtag để xem danh sách bài hoặc POST-01
```

MVP dùng MySQL và phân trang; không dùng username hoặc Elasticsearch.

### 1.11 Báo cáo bài viết

```text
PostCard của người khác hoặc POST-01
→ POST-04 Menu thao tác bài viết
→ Báo cáo
→ POST-08 Chọn lý do báo cáo
→ POST-09 Nhập mô tả bổ sung
→ Gửi báo cáo
→ POST-10 Gửi báo cáo thành công
```

Quy tắc báo cáo bài viết: mỗi lần gửi giữ một Report độc lập và gắn vào Moderation Case `OPEN`
của bài. Một người không có nhiều Report trong cùng case `OPEN`; gửi Report không tự động ẩn bài.

Báo cáo trang cá nhân là luồng riêng từ PROFILE-02. Một người không được tự báo cáo và chỉ có một
Profile Report `PENDING` cho cùng target; modal chỉ hiển thị sáu lý do cố định trong README. Các lượt
báo cáo từ nhiều người được gom vào một Profile Report Case duy nhất theo target.

### 1.12 Admin quản lý người dùng

```text
Admin đăng nhập
→ ADMIN-02 Quản lý người dùng
→ Tìm kiếm/lọc người dùng
→ Click một dòng để mở ADMIN-03 Chi tiết người dùng
→ Mở form sửa hồ sơ để xem trước, thay hoặc xóa avatar và cập nhật thông tin
→ Khóa hoặc mở khóa tài khoản trong chi tiết
→ Danh sách cập nhật trạng thái ACTIVE/BLOCKED
→ Biểu đồ trạng thái tài khoản tự làm mới
```

Tài khoản BLOCKED không được đăng nhập hoặc dùng chức năng hệ thống.

### 1.12.1 Admin quản lý quản trị viên

```text
Admin có ADMIN_VIEW
→ /admin/admins hiển thị danh sách quản trị viên
→ Click một quản trị viên và tải chi tiết theo adminId
→ Xem hồ sơ, trạng thái và các role hiện tại
→ Chỉ tài khoản Bootstrap được tạo Admin hỗ trợ và gán/thu hồi role nghiệp vụ
→ Với Admin hỗ trợ, actor có permission phù hợp vẫn có thể vô hiệu hóa/mở khóa hoặc mở form cấp lại mật khẩu
→ Với Master Admin, UI khóa phân quyền và ẩn thao tác cấp lại mật khẩu/vô hiệu hóa; tài khoản chỉ tự đổi mật khẩu trong Hồ sơ
→ Cấp lại mật khẩu Admin hỗ trợ hợp lệ ghi audit và thu hồi toàn bộ Refresh Token của tài khoản đích
```

Mật khẩu chỉ tồn tại trong form và request qua kết nối bảo mật; UI không lưu, hiển thị lại hoặc ghi log.
Tài khoản `BLOCKED` vẫn giữ nguyên trạng thái sau khi được cấp lại mật khẩu.

### 1.12.2 Admin tạo vai trò

```text
SUPER_ADMIN Bootstrap mở /admin/permissions
→ Chọn Tạo vai trò
→ Nhập tên vai trò
→ Backend sinh code và tạo role với quyền Tổng quan
→ UI chọn role vừa tạo
→ SUPER_ADMIN Bootstrap bật thêm permission nghiệp vụ trong ma trận
→ Role mới xuất hiện trong danh mục gán role cho tài khoản Admin
```

Frontend không tự sinh hoặc gửi role code; duplicate và validation cuối cùng do Backend quyết định.
UI không cho chọn `SUPER_ADMIN` và khóa ba permission phân quyền `ADMIN_CREATE`, `ADMIN_ROLE_ASSIGN`,
`ADMIN_ROLE_REVOKE`; Backend vẫn là hàng rào bắt buộc nếu Client bị chỉnh sửa.

### 1.12.3 Hồ sơ quản trị viên

```text
Admin thường hoặc Admin đa vai trò mở /admin/profile từ sidebar
→ GET /api/v1/admin/profile
→ sửa tên hiển thị, ngày sinh hoặc bio của hồ sơ quản trị
→ PUT /api/v1/admin/profile
→ Cộng tác viên thuần mở /admin/collaborator/profile
→ GET /api/v1/admin/collaborator/social-identity
→ xem username ở trạng thái chỉ đọc; username đã tạo không thể thay đổi
→ sửa tên hiển thị, avatar hoặc bio của Managed Public Identity
→ PUT /api/v1/admin/collaborator/social-identity hoặc POST avatar
→ Sidebar và Dashboard dùng ngay cùng Managed Public Identity
→ trên cùng trang sửa riêng tên hiển thị, ngày sinh hoặc bio của hồ sơ Admin
→ PUT /api/v1/admin/profile
→ nhập mật khẩu hiện tại và mật khẩu mới
→ PATCH /api/v1/admin/profile/password
→ Backend thu hồi toàn bộ Refresh Token
→ Frontend xóa phiên và chuyển về Đăng nhập
```

Email, username kỹ thuật, trạng thái, role và permission của Admin chỉ đọc. Managed Public Identity là nguồn
username/tên/avatar/bio duy nhất cho các bề mặt Collaborator. Backend luôn lấy quản trị viên hiện tại từ JWT,
không nhận `adminId` hoặc `socialUserId` do Frontend gửi lên.

### 1.12.4 Cộng tác viên xem thảo luận trên bài viết của mình

```text
Cộng tác viên mở /admin/collaborator/posts/:postId
→ GET /api/v1/admin/collaborator/posts/:postId
→ nếu bài PUBLISHED, GET /api/v1/admin/collaborator/posts/:postId/comments?page=0&size=20
→ hiển thị bình luận gốc, trạng thái tải/rỗng/lỗi và nút xem thêm
→ khi mở một nhánh, GET /api/v1/admin/collaborator/posts/:postId/comments/:commentId/replies
→ hiển thị reply một cấp theo chính sách Block của Managed Social Identity
```

Lỗi tải bình luận chỉ nằm trong khu vực thảo luận và không che nội dung bài đã tải thành công. Client không gửi
`viewerId`, `authorId` hoặc `socialUserId`; Backend resolve Managed Identity từ JWT và kiểm tra bài thuộc danh tính đó.

### 1.13 Admin quản lý bài viết

```text
Admin đăng nhập
→ ADMIN-04 Quản lý bài viết
→ Tìm kiếm/lọc bài viết
→ Xem trạng thái bài
→ Double-click một dòng để mở chi tiết bài viết
→ Ẩn bài PUBLISHED sau khi chọn lý do hoặc khôi phục bài HIDDEN tại trang chi tiết
```

Chỉ xử lý trạng thái phục vụ MVP: PUBLISHED, HIDDEN, DELETED.

### 1.14 Admin xử lý báo cáo

```text
Admin đăng nhập
→ ADMIN-05 Quản lý báo cáo
→ Danh sách một dòng mỗi Moderation Case
→ Mở ADMIN-06 Chi tiết hồ sơ kiểm duyệt
→ Xem bài, thống kê lý do và danh sách Report rút gọn theo người gửi, lý do, mô tả, thời gian
→ Chọn trực tiếp Không vi phạm hoặc Có vi phạm / Ẩn bài

Tab Trang cá nhân
→ Danh sách Profile Report Case, mỗi target chỉ một dòng và có tổng số người báo cáo
→ Mở chi tiết gồm mọi reporter/lý do, snapshot, hồ sơ hiện tại và vùng cuộn bài viết của target
→ Kết luận mọi lượt PENDING là Không vi phạm, Xác nhận vi phạm, hoặc Vi phạm & khóa tài khoản
→ Lựa chọn khóa cập nhật USER, thu hồi phiên và ghi lịch sử trong cùng transaction
→ Nếu có vi phạm, chọn một lý do ẩn bài thuộc enum Backend trong modal
→ Backend cập nhật case, toàn bộ Report, Admin Action và Notification trong một transaction
→ Với báo cáo bài viết, mỗi case có vi phạm tính một lần; lần thứ ba Backend tự động khóa tác giả
```

Không có bước tiếp nhận, trạng thái đang xử lý hoặc trạng thái đóng riêng. Case đã giải quyết chỉ hiển
thị kết luận và không còn nút xử lý.

### 1.15 Admin xem thống kê hoạt động người dùng

```text
Admin đăng nhập
→ Chọn Thống kê người dùng trên sidebar
→ ADMIN-07 tải đồng thời thống kê monthly và summary
→ Chọn khoảng từ tháng/đến tháng tối đa 24 tháng và ngưỡng không hoạt động
→ Xem bốn KPI, xu hướng số người quay lại/tỷ lệ tái kích hoạt và bảng snapshot tháng kết thúc
→ Khi lỗi có thể thử lại; khi không có USER đủ điều kiện hiển thị Empty State
```

Luồng dùng ngày UTC và route `/admin/user-analytics`. Ngoài module riêng, ADMIN-01 Dashboard hiển thị widget
30 ngày tổng tương tác, danh sách tối đa 5 USER nổi bật theo bài viết/tương tác của ngày hiện tại và không có
khối “Hoạt động gần đây”.

### 1.16 Admin quản lý hashtag

```text
Admin đăng nhập
→ Chọn Hashtag trên sidebar
→ ADMIN-09 tải trang đầu của danh sách hashtag
→ Xem tên, số bài viết, ngày tạo và ngày sử dụng mới nhất
→ Nhập từ khóa để tìm theo tên; thay trang hoặc số dòng mỗi trang khi cần
→ Chọn Tạo hashtag, nhập tên và lưu; Backend chuẩn hóa và từ chối tên trùng
→ Hoặc chọn Sửa trên một dòng, nhập tên mới và lưu; các bài liên quan giữ nguyên hashtag_id
→ Hoặc chọn Xóa trên một dòng, đọc số bài bị ảnh hưởng và xác nhận
→ Sau khi xóa, danh sách làm mới; bài liên quan vẫn tồn tại nhưng không có hashtag
```

Luồng tại route `/admin/hashtags`; mọi thao tác ghi yêu cầu ADMIN và được lưu lịch sử quản trị.

### 1.17 Admin quản lý dữ liệu học thuật

```text
Admin đăng nhập
→ Chọn Dữ liệu học thuật trên sidebar
→ ADMIN-10 mở tab Trường / Khoa / Ngành
→ Tìm kiếm, phân trang, tạo, sửa hoặc đổi ACTIVE/INACTIVE cho School
→ Chọn Xem khoa để đi sâu vào Faculty của School
→ Chọn Xem ngành để đi sâu vào Major của Faculty
→ Hoặc chuyển tab Sở thích để quản lý Interest Category độc lập
→ Khi chuyển ACTIVE sang INACTIVE, đọc cảnh báo bảo toàn reference và xác nhận
→ Backend không cascade status xuống child, không hard delete và ghi Admin Action
```

Public Academic API chỉ hiển thị hierarchy có toàn bộ ancestor `ACTIVE`; dữ liệu inactive đã gắn với hồ sơ cũ vẫn được bảo toàn. Luồng dùng route `/admin/academic` và không bao gồm Recommendation, Smart Student Match, School Suggestion hoặc AI/ML.

### 1.18 Trạng thái hệ thống

```text
Không có quyền → SYS-01
Không tìm thấy route/tài nguyên → SYS-02
Lỗi server/API → SYS-03
Phiên đăng nhập hết hạn → SYS-04
```

### 1.16 Thông báo REST và realtime

```text
Người dùng đã đăng nhập và hoàn tất hồ sơ
→ Frontend tải trang đầu Notification và unread count bằng REST
→ STOMP CONNECT /ws với JWT Access Token
→ Subscribe /user/queue/notifications
→ Khi nhận NOTIFICATION_CREATED, merge theo notification.id và cập nhật unread count từ Backend
→ Badge ở desktop/mobile hiển thị số chưa đọc, tối đa 99+
→ Người dùng mở trang thông báo để tải thêm, đánh dấu đã đọc, đánh dấu tất cả hoặc xóa bằng REST
```

Khi socket mất kết nối, Frontend chỉ polling unread count mỗi 30 giây lúc tab đang visible. Sau khi
connect/reconnect, Frontend reconcile lại bằng REST. Logout, tài khoản bị khóa hoặc hồ sơ chưa hoàn
tất phải deactivate socket. Giai đoạn 1 không phát realtime cho read, read-all hoặc delete.

### 1.17 Nhắn tin trực tiếp

```text
Profile người khác → Nhắn tin → PUT open direct → /messages/:conversationId
Sidebar Tin nhắn → /messages → Inbox cursor
Mở conversation → tải page message mới nhất → cuộn lên để prepend page cũ
Gửi text → optimistic UUID v4 → REST POST → merge REST/WebSocket echo
Chia sẻ Post → mở modal → tải recipient từ Backend → chọn một người → bấm Gửi → open/reuse conversation → REST POST `sharedPostId` → bubble `POST_SHARE`
Modal chưa chọn người → Sao chép URL canonical hoặc Facebook Share Dialog/Web Sharer → không tạo message/Notification/Repost
Message nhận đã hiển thị ở cuối → REST mark read → MESSAGES_READ cho các tab hai bên
Gõ nội dung → SEND typing START/refresh → participant còn lại thấy "đang nhập..." → STOP/expiry xóa chỉ báo
Reconnect/tab visible → reconcile unread, Inbox và conversation đang mở bằng REST
```

`MessagingContext` subscribe `/user/queue/messaging` trên connection dùng chung. Badge Messaging tách khỏi
Notification. Khi disconnected và tab visible, polling REST khoảng 30 giây; lỗi Block/403/404 xóa nội dung
conversation khỏi state và điều hướng an toàn về Inbox.

History và event `POST_SHARE` chỉ dùng snapshot Backend hydrate theo quyền viewer hiện tại. Khi bài bị
xóa/ẩn, author không còn hợp lệ hoặc Block thay đổi, bubble giữ vị trí trong hội thoại nhưng hiển thị trạng
thái không còn khả dụng.

Composer chỉ phát START ở lần nhập đầu, refresh tối đa mỗi 3 giây khi tiếp tục hoạt động và STOP sau 2 giây
idle, khi submit, input blank, blur hoặc rời conversation. Khi socket disconnected không gửi frame và reconnect
không tự khôi phục typing cũ. Receiver dedupe theo `eventId`, giữ state theo conversation + user và tự xóa sau
5 giây; vùng chỉ báo dùng `aria-live="polite"` và giữ chiều cao ổn định.

## 2. Luồng phát triển tương lai

Các luồng sau có thể đã có ảnh thiết kế nhưng thuộc `FUTURE_DEVELOPMENT`, không triển khai trong bản demo và Frontend MVP hiện tại.

### 2.1 Quên mật khẩu

```text
AUTH-01 Đăng nhập
→ Chọn Quên mật khẩu
→ AUTH-04 Nhập mã xác minh
→ AUTH-05 Đặt lại mật khẩu
→ AUTH-06 Đặt lại mật khẩu thành công
→ AUTH-01 Đăng nhập
```

### 2.2 Mention

```text
Người dùng nhập @ trong bài viết hoặc bình luận
→ Hệ thống gợi ý người dùng theo displayName
→ Người dùng chọn một tài khoản cụ thể
→ Lưu mentionedUserId
→ Render mention bằng @displayName
→ Bấm mention điều hướng /profile/:userId
```

Quy tắc: Không dùng @username, không dùng displayName làm khóa liên kết, không điều hướng theo tên hiển thị.

### 2.3 Các chức năng tương lai khác

- Discovery Map, Feed tùy chỉnh theo Location và trang Location riêng.
- Feed tùy chỉnh.
- Follow Request.
- Trích dẫn bài viết.
- Video hoặc tài liệu trong bài viết.
- Elasticsearch.
- Các Dashboard nâng cao khác ngoài biểu đồ tương tác và USER nổi bật.

