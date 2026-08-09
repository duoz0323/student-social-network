# Luồng dữ liệu

`README.md` là nguồn sự thật cao nhất về nghiệp vụ và trạng thái đích. Contract HTTP chi tiết duy nhất nằm tại `docs/data/API-CONTRACT.md`; tài liệu này chỉ mô tả trình tự trao đổi dữ liệu giữa các thành phần.

## 1. Đăng ký local và xác minh OTP

```text
Register Form
→ Khởi tạo đăng ký qua POST /api/v1/auth/registrations
→ Backend chuẩn hóa email, tạo hoặc phục hồi pending_registrations
→ Backend gửi OTP ngoài transaction database
→ Frontend giữ flow token trong memory hoặc sessionStorage
→ Người dùng nhập OTP
→ Frontend gửi flow token bằng X-Auth-Flow-Token đến endpoint verify
→ Backend khóa bi quan bản ghi pending trong transaction ngắn
→ Kiểm tra trạng thái, thời hạn, OTP và số lần thử
→ Tạo users và user_profiles trong cùng transaction
→ Hoàn tất pending, tạo phiên và trả JWT hệ thống
→ Frontend điều hướng đến onboarding
```

Resend giữ nguyên flow token, tạo OTP mới, vô hiệu OTP cũ và đặt lại số lần thử nhưng không kéo dài thời hạn 24 giờ của pending. Recovery khi mất token là luồng riêng chưa thuộc Giai đoạn 4. Chi tiết request, response, header, status và error code theo `API-CONTRACT.md`.

## 2. Đăng ký hoặc đăng nhập Google/Facebook

```text
Google/Facebook SDK
→ Frontend nhận provider credential dùng một lần cho Auth
→ Gửi credential đến endpoint social Auth tương ứng
→ Backend xác minh trực tiếp với provider ngoài transaction database
→ Đối chiếu provider identity và trạng thái tài khoản
→ Đăng nhập user đã liên kết, tạo provider-only user hợp lệ,
  hoàn tất pending phù hợp hoặc trả social conflict
→ Backend phát hành Access Token và Refresh Token của hệ thống
→ Frontend hủy provider credential và đi đến onboarding/ứng dụng
```

Provider token không được lưu lâu dài hoặc dùng cho API nghiệp vụ. Facebook không có email vẫn có thể tạo provider-only user; không tạo email giả. Email trùng một user `ACTIVE` nhưng provider chưa liên kết phải đi qua conflict, không tự động link hay tạo user thứ hai. Social conflict dùng flow token opaque một lần, TTL 5 phút, theo contract chi tiết.

## 3. Quản lý phương thức xác thực

```text
Security Settings
→ Link email: initiate challenge riêng → verify OTP → gắn local method
→ Link Google/Facebook: xác minh provider trong phiên JWT hiện tại
→ Unlink: reauthenticate → kiểm tra không phải phương thức cuối → gỡ liên kết
```

Challenge liên kết email không dùng `pending_registrations`. User đích luôn lấy từ JWT hiện tại. Nếu sau unlink không còn local email hợp lệ nhưng vẫn còn social provider thì `password_hash` được đặt `NULL`. Thu hồi các phiên khác sau thay đổi phương thức xác thực là hạng mục P1, chưa tự động thực hiện ở 0B.

## 4. Hoàn tất hồ sơ

```text
Onboarding Page
→ GET /api/v1/users/me/onboarding
→ Nhập tên hiển thị và ngày sinh bắt buộc, avatar/bio tùy chọn
→ Backend kiểm tra ngày sinh hợp lệ và người dùng đủ 18 tuổi
→ PUT /api/v1/users/me/onboarding
→ Backend cập nhật user_profiles.profile_completed_at
→ Frontend cho phép vào Feed
```

Nếu `profile_completed_at` còn `NULL`, các API mạng xã hội chính trả `PROFILE_NOT_COMPLETED` và Frontend điều hướng về onboarding. Login và refresh vẫn được phép, không trả lỗi này.

## 5. Đăng nhập local

```text
Login Form
→ POST /api/v1/auth/login
→ Chuẩn hóa email và kiểm tra phương thức local đã xác minh
→ Kiểm tra mật khẩu và từ chối user BLOCKED
→ Tạo Access Token + Refresh Token
→ Frontend
```

## 6. Refresh Token

```text
Axios nhận 401 do Access Token hết hạn
→ POST /api/v1/auth/refresh-token
→ Kiểm tra Refresh Token, trạng thái revoked/expiry và user BLOCKED
→ Khóa và thu hồi Refresh Token cũ
→ Cấp Access Token và Refresh Token mới
→ Frontend thay thế token đang lưu
→ Gửi lại request cũ
```

## 7. Tạo bài

```text
Create Post Form
→ Kiểm tra profile_completed_at khác NULL
→ Kiểm tra nội dung, media và Location tùy chọn
→ Frontend gửi Location object gồm placeId, displayName, formattedAddress, latitude, longitude nếu người dùng đã chọn địa điểm
→ Upload ảnh → nhận URL
→ POST /api/v1/posts
→ Backend chuẩn hóa Location và tìm theo google_place_id
→ Dùng lại locations nếu Place ID đã tồn tại, hoặc tạo mới nếu chưa tồn tại
→ PostService lưu posts, location_id nullable, post_media và hashtag/post_hashtags
→ Trả PostResponse có location object hoặc location = null
```

Khi cập nhật Post trong giới hạn 15 phút, `KEEP` giữ nguyên Location, `REPLACE` resolve Location theo Place ID rồi thay thế, còn `REMOVE` đặt `posts.location_id = NULL`. Xóa hoặc gỡ Location khỏi Post không xóa bản ghi `locations`. Backend chưa gọi Google Places API để xác minh trong P1 này.

## 8. Follow

```text
Profile
→ POST /api/v1/users/{userId}/follow
→ Kiểm tra profile hoàn tất, không follow chính mình và không trùng
→ Lưu follows → trả trạng thái mới
```

## 9. Like và Comment

```text
PostCard/Comment Form
→ Gọi endpoint tương ứng dưới /api/v1/posts
→ Kiểm tra profile hoàn tất và post PUBLISHED
→ Kiểm tra uniqueness/validate content
→ Lưu dữ liệu → trả response
```

## 10. Feed

```text
GET /api/v1/feeds/following hoặc /api/v1/feeds/for-you
→ Kiểm tra profile_completed_at khác NULL
→ Chỉ lấy post PUBLISHED
→ Sắp xếp/xếp hạng theo nghiệp vụ README
→ Decode và kiểm tra cursor opaque nếu có
→ Keyset query lấy limit + 1
→ Trả content, nextCursor và hasNext
```

Cùng cơ chế Cursor Pagination được dùng cho bài trên hồ sơ, tab Repost, bài đã lưu và bài đã thích.
Search, bình luận, Follow và Admin vẫn dùng `PageResponse`.

Following Feed hợp nhất bài gốc và `post_reposts` theo activity bằng `UNION ALL`; Backend batch-load
projection Post cùng trạng thái Like/Save/Repost và phát cursor opaque chứa toàn bộ khóa `ORDER BY`.

```text
PUT /api/v1/posts/{postId}/repost
→ khóa pessimistic Post gốc
→ kiểm tra user/Post và quan hệ hiện có
→ INSERT post_reposts + trigger tăng repost_count
→ tạo POST_REPOST trong cùng transaction
→ AFTER_COMMIT đẩy WebSocket best-effort
```

## 11. Search

```text
GET /api/v1/search/users?q= hoặc /api/v1/search/posts?q=
→ Kiểm tra profile_completed_at khác NULL
→ Tìm bằng MySQL, loại dữ liệu không được hiển thị
→ Phân trang → trả kết quả
```

## 12. Report và Admin xử lý Report

```text
Report Modal → POST /api/v1/posts/{postId}/reports
→ Kiểm tra profile hoàn tất và khóa Post
→ Kiểm tra reporter chưa có Report trong case OPEN
→ Tìm hoặc tạo Moderation Case OPEN
→ Tạo Report độc lập, cập nhật report_count và commit cùng transaction

Admin Case Detail → PATCH /api/v1/admin/moderation-cases/{caseId}/resolve-*
→ Kiểm tra ADMIN
→ Khóa case OPEN và Post khi cần
→ Chuyển case sang kết quả cuối, cập nhật mọi Report, audit/notification và có thể ẩn Post

Profile người khác → POST /api/v1/users/{userId}/profile-reports
→ Lấy reporter từ JWT, kiểm tra onboarding/Block/tự báo cáo
→ Khóa target, lấy hoặc tạo profile_report_cases duy nhất theo target
→ Chụp snapshot hồ sơ, tạo profile_reports PENDING và tăng report_count của case
→ Unique generated key chống hai PENDING cùng reporter/target

Admin Reports / tab Trang cá nhân → GET /api/v1/admin/profile-reports
→ Mỗi dòng là một case, không phải từng reporter
→ Chi tiết trả danh sách toàn bộ lượt báo cáo thuộc case
→ Admin kết luận đồng thời mọi lượt PENDING; báo cáo mới sẽ mở lại case
→ Nếu xác nhận vi phạm và blockUser=true: khóa USER, revoke Refresh Token, ghi lịch sử/Admin Action/Notification

Admin xác nhận Moderation Case bài viết có vi phạm
→ Ẩn Post và chuyển case sang RESOLVED_ACTION_TAKEN
→ Đếm số case RESOLVED_ACTION_TAKEN của tác giả; mỗi case chỉ tính một lần
→ Nếu tổng >= 3 và USER đang ACTIVE: khóa tài khoản với REPEATED_VIOLATION và revoke mọi phiên
→ Mở chi tiết, tải hồ sơ hiện tại và phân trang bài viết theo authorId
→ PATCH resolve/reject để kết luận; không tự khóa, sửa hồ sơ hoặc ẩn bài
```

## 13. Admin chỉnh sửa hồ sơ USER

```text
Admin User Detail → PUT /api/v1/admin/users/{userId}/profile
→ Kiểm tra ADMIN đang hoạt động và target có role USER
→ Khóa users/user_profiles và cập nhật hồ sơ cùng audit
→ Tạo Notification hệ thống PROFILE_UPDATED_BY_ADMIN trong cùng transaction
→ Sau commit phát NOTIFICATION_CREATED best-effort tới USER
```

