# Phân tích code hiện tại của module Post

> Mục tiêu: mô tả chính xác code hiện tại đã triển khai được gì.  
> Ngày rà soát: 2026-07-24.  
> Đây là báo cáo implementation, không phải bản thiết kế nghiệp vụ đích.

## 1. Kết luận nhanh

Module Post hiện tại không còn ở mức CRUD cơ bản. Backend đã triển khai khá đầy đủ các luồng ghi và tương tác:

- Tạo bài có content, hashtag, ảnh hoặc video.
- Upload media lên Cloudinary.
- Xem chi tiết bài.
- Sửa content, hashtag và media trong 15 phút.
- Xóa mềm bài.
- Like/Unlike.
- Comment, reply một cấp, đọc comment/reply và xóa mềm comment.
- Save/Unsave.
- Gợi ý hashtag.
- Tìm bài theo content hoặc hashtag.
- Report bài và lưu snapshot.
- Admin xem, ẩn/khôi phục bài và xử lý report.
- Trigger MySQL tự đồng bộ số Like, Comment và số bài của hashtag.

Tuy nhiên module chưa hoàn chỉnh end-to-end:

- Chưa có Backend Feed For You/Following.
- Chưa có API Backend lấy danh sách Post theo Profile.
- Frontend Profile vẫn đọc mock data.
- Frontend Post Detail đã gọi API thật nhưng PostCard trong các danh sách vẫn dựa trên state mock/hỗn hợp.
- Chưa tìm thấy DBML.

## 2. Phạm vi code đã rà soát

### Backend

- `post`: CRUD Post, media, hashtag, Like, Save.
- `interaction`: Comment và Reply.
- `search`: Tìm Post.
- `report`: User báo cáo Post.
- `admin`: Quản trị Post và Report.
- `storage`: Upload/xóa media Cloudinary.
- `notification`: Notification phát sinh từ tương tác và quản trị.
- `security`: JWT, CurrentUser và profile-completion guard.

### Frontend

- `src/api/postApi.js`.
- `src/api/apiEndpoints.js`.
- `src/contexts/AppContext.jsx`.
- `src/features/post`.
- `src/features/feed`.
- `src/features/profile`.
- `src/features/search`.
- `src/features/admin`.
- `src/data/mockData.js`.

### Database và test

- `database/student_social_network.sql`.
- Test thuộc các module `post`, `interaction`, `search`, `report`, `admin`, `storage` và `security`.

## 3. Kiến trúc implementation hiện tại

```text
React Component
→ AppContext hoặc gọi postApi trực tiếp
→ Axios httpClient
→ Spring Controller
→ Service
→ Repository
→ MySQL

Riêng media:
PostService
→ CloudinaryStorageService
→ Cloudinary
→ sau đó mới mở transaction ghi MySQL
```

Backend đang tuân theo cấu trúc Controller → Service → Repository. Controller không nhận `userId`; danh tính hiện tại được lấy từ JWT/SecurityContext qua `CurrentUserProvider`.

## 4. Các endpoint Backend hiện đã có

### 4.1. Post

| Method | Endpoint | Đã làm |
|---|---|---|
| POST | `/api/v1/posts` | Tạo Post multipart |
| GET | `/api/v1/posts/{postId}` | Xem chi tiết Post `PUBLISHED` |
| PUT | `/api/v1/posts/{postId}` | Sửa content, hashtag và media |
| DELETE | `/api/v1/posts/{postId}` | Xóa mềm Post |

### 4.2. Like và Save

| Method | Endpoint | Đã làm |
|---|---|---|
| POST | `/api/v1/posts/{postId}/likes` | Like |
| DELETE | `/api/v1/posts/{postId}/likes` | Unlike |
| POST | `/api/v1/posts/{postId}/saves` | Save |
| DELETE | `/api/v1/posts/{postId}/saves` | Unsave |

Backend có thêm endpoint `GET /api/v1/posts/saved?page=0&size=20` trả danh sách Saved Posts của người dùng hiện tại theo thời điểm lưu mới nhất.

### 4.3. Comment

| Method | Endpoint | Đã làm |
|---|---|---|
| POST | `/api/v1/posts/{postId}/comments` | Tạo comment gốc |
| GET | `/api/v1/posts/{postId}/comments` | Phân trang comment gốc |
| POST | `/api/v1/comments/{parentCommentId}/replies` | Tạo reply |
| GET | `/api/v1/comments/{parentCommentId}/replies` | Phân trang reply |
| DELETE | `/api/v1/comments/{commentId}` | Xóa mềm comment/reply |

### 4.4. Hashtag, Search và Report

| Method | Endpoint | Đã làm |
|---|---|---|
| GET | `/api/v1/hashtags/suggestions` | Gợi ý hashtag, không phân trang |
| GET | `/api/v1/search/posts` | Tìm Post theo `CONTENT` hoặc `HASHTAG` |
| POST | `/api/v1/posts/{postId}/reports` | Tạo report và snapshot |

### 4.5. Admin Post và Report

Code hiện có:

- Danh sách Admin Post có lọc keyword, status, author và `reportedOnly`.
- Xem chi tiết Admin Post.
- Ẩn Post.
- Khôi phục Post.
- Danh sách Admin Report có nhiều bộ lọc.
- Xem chi tiết Report.
- Reject Report.
- Resolve Report, tùy chọn ẩn Post.

## 5. Tạo Post hiện tại đã làm được gì

### 5.1. Request

Frontend gửi `multipart/form-data`:

- `content`.
- `hashtag`.
- Nhiều `mediaFiles`.

Backend không nhận `authorId`. Tác giả được lấy từ JWT.

### 5.2. Kiểm tra người dùng

`PostServiceImpl` kiểm tra:

- User tồn tại.
- `users.status = ACTIVE`.
- Có `user_profiles`.
- `profile_completed_at` khác `NULL`.

### 5.3. Validation Post

Code hiện hỗ trợ:

- Content được normalize.
- Bài phải có content hoặc media.
- Tối đa 4 media.
- Ảnh JPEG, PNG, WEBP.
- Video MP4, WebM.
- Tối đa một video.
- Ảnh tối đa 10 MB.
- Video tối đa 100 MB.
- Video tối đa 180 giây.
- Một hashtag sau chuẩn hóa.

Frontend `PostComposer` cũng kiểm tra các giới hạn trên để hỗ trợ UX. Backend vẫn là nơi quyết định cuối cùng.

### 5.4. Upload và transaction

Luồng code thực tế:

```text
Validate request
→ Upload từng media lên Cloudinary
→ Nhận URL, publicId và metadata
→ Mở transaction DB
→ INSERT posts
→ INSERT post_media
→ INSERT/đọc hashtags
→ INSERT post_hashtags
→ COMMIT
```

Điểm đã xử lý tốt:

- Không giữ transaction DB trong lúc upload Cloudinary.
- Nếu upload một file bị lỗi, cleanup các file upload trước đó.
- Nếu ghi DB lỗi, cleanup toàn bộ media vừa upload.
- Database chỉ lưu URL và metadata, không lưu BLOB.

### 5.5. Response

`PostResponse` hiện trả:

- Thông tin Post.
- Content và status.
- `isEdited`.
- `likeCount`, `commentCount`.
- Các mốc thời gian.
- Tác giả gồm ID, display name và avatar.
- Danh sách media.
- Một hashtag.

Không trả `storage_public_id` ra Client.

## 6. Media hiện tại

Backend và SQL hiện hỗ trợ cả `IMAGE` và `VIDEO`.

Metadata đang lưu:

- URL.
- Cloudinary public ID.
- Media type.
- MIME type.
- File size.
- Width/height.
- Video duration.
- Thumbnail URL.
- Display order.

Ràng buộc database:

- `storage_public_id` unique.
- `(post_id, display_order)` unique.
- `display_order <= 3`.
- Video duration từ 1 đến 180 giây.
- File size phải lớn hơn 0.

Điểm cần lưu ý: đây là chức năng code hiện tại đã có, dù README hiện chỉ mô tả ảnh.

## 7. Hashtag hiện tại

### 7.1. Chuẩn hóa

`HashtagNormalizer` hiện chịu trách nhiệm:

- Bỏ ký tự `#` đầu.
- Trim khoảng trắng.
- Gộp khoảng trắng.
- Chuẩn hóa Unicode.
- Chuyển chữ thường.
- Giới hạn độ dài.

### 7.2. Lưu dữ liệu

- `hashtags.normalized_name` unique.
- Service dùng insert-if-absent để xử lý hai request tạo cùng hashtag.
- Post hiện được Service kiểm soát tối đa một quan hệ hashtag.
- `post_hashtags` vẫn là bảng nối nhiều-nhiều về mặt schema.

### 7.3. Counter và suggestion

Trigger:

- Insert `post_hashtags` → tăng `hashtags.post_count`.
- Delete `post_hashtags` → giảm `post_count`.

API suggestion đã có và sắp xếp theo dữ liệu hashtag hiện tại.

Giới hạn hiện tại: `post_count` đếm quan hệ vật lý, chưa tự giảm khi Post đổi sang `HIDDEN` hoặc `DELETED`.

## 8. Xem chi tiết Post

`GET /api/v1/posts/{postId}` hiện:

1. Lấy viewer từ JWT.
2. Kiểm tra viewer ACTIVE và hoàn tất profile.
3. Chỉ query Post `PUBLISHED`.
4. Kiểm tra tác giả Post còn ACTIVE.
5. Đọc profile tác giả.
6. Đọc media theo `display_order`.
7. Đọc tối đa một hashtag.
8. Xác định viewer có phải owner không.
9. Trả `PostDetailResponse`.

Post `HIDDEN`, `DELETED`, không tồn tại hoặc thuộc tác giả không hợp lệ đều được che bằng `POST_NOT_FOUND`.

Response detail hiện mới có viewer state `owner`; chưa thấy `liked` và `saved` được Backend đưa vào `PostDetailResponse`.

## 9. Sửa Post

### 9.1. Quyền và thời gian

Code đã kiểm tra:

- Post phải đang `PUBLISHED`.
- Chỉ tác giả được sửa.
- Tác giả phải ACTIVE.
- Chỉ sửa trong 15 phút kể từ `published_at`.

### 9.2. Dữ liệu có thể sửa

Implementation hiện cho phép:

- Sửa content.
- Đổi hoặc gỡ hashtag.
- Giữ một phần media cũ qua `keepMediaIds`.
- Xóa media cũ.
- Upload media mới.
- Đánh lại `display_order`.

Đây là implementation thực tế, dù tài liệu UI hiện nói không sửa ảnh sau khi đăng.

### 9.3. Cleanup Cloudinary

- Media cũ chỉ bị xóa khỏi Cloudinary sau khi DB commit thành công.
- Nếu transaction rollback, media mới upload được cleanup.
- Nếu cleanup sau commit thất bại, code ghi warning và không rollback DB.

### 9.4. Dữ liệu cập nhật

- `posts.content`.
- `posts.is_edited = true`.
- `posts.updated_at`.
- `post_media`.
- `post_hashtags`.
- Counter hashtag được trigger cập nhật.

## 10. Xóa Post

`DELETE /api/v1/posts/{postId}` hiện là xóa mềm:

```text
status = DELETED
deleted_at = thời gian hiện tại
updated_at = thời gian hiện tại
```

Code đã làm:

- Chỉ owner được xóa.
- Chỉ xóa Post đang `PUBLISHED`.
- Dùng conditional update để tránh race khi trạng thái vừa bị đổi.
- Không xóa media Cloudinary.
- Không xóa Like, Comment, Save, Hashtag relation hoặc Report.

Sau khi xóa, các query public dùng `PUBLISHED` nên không còn trả Post đó.

## 11. Like/Unlike

Code hiện:

- Lấy user từ JWT.
- Kiểm tra Post có thể tương tác.
- Insert/delete `post_likes`.
- PK `(user_id, post_id)` chống Like trùng.
- Trigger tự tăng/giảm `posts.like_count`.
- Service đọc lại counter sau khi trigger chạy.
- Trả `likedByCurrentUser` và `likeCount`.
- Tạo notification Like cho tác giả theo service hiện tại.

Service không tự cộng/trừ counter nên tránh cộng hai lần.

## 12. Comment và Reply

### 12.1. Create

Code đã có:

- Comment gốc.
- Reply một cấp.
- Không cho reply sai Post vì Post được suy ra từ comment cha.
- Validate content.
- Kiểm tra Post `PUBLISHED`.
- Lấy author từ JWT.
- Tạo notification.

### 12.2. Read

- Comment gốc phân trang riêng.
- Reply phân trang riêng.
- Page mặc định 20.
- Size tối đa 100.
- Repository có projection đếm reply.

### 12.3. Delete

- Chỉ tác giả comment được xóa.
- Xóa mềm bằng `status = DELETED`.
- Đặt `deleted_at`.
- Trigger giảm `posts.comment_count`.

### 12.4. Counter

- Insert comment `PUBLISHED` → trigger tăng `comment_count`.
- Đổi `PUBLISHED → DELETED` → trigger giảm.
- Trigger cũng hỗ trợ `DELETED → PUBLISHED`, dù chưa thấy User API khôi phục comment.

## 13. Save/Unsave

Backend đã có thao tác Save và Unsave:

- User lấy từ JWT.
- Kiểm tra Post tương tác được.
- PK `(user_id, post_id)` chống trùng.
- Unsave được xử lý theo hướng idempotent.
- Response trả trạng thái `saved`.

Read-side đã có Controller/API phân trang và Frontend `SavedPostsPage` đọc trực tiếp từ Backend, không còn ghép quan hệ Save với danh sách Post mock trong AppContext.

## 14. Search Post

Backend đã triển khai:

### CONTENT

- MySQL FULLTEXT trên `posts.content`.
- Chỉ Post `PUBLISHED`.
- Chỉ tác giả ACTIVE và đã hoàn tất profile.
- Sắp xếp theo relevance, sau đó thời gian và ID.
- Có phân trang.

### HASHTAG

- Normalize hashtag.
- Match chính xác `hashtags.normalized_name`.
- Join qua `post_hashtags`.
- Chỉ trả Post/tác giả hợp lệ.
- Có phân trang.

Frontend `SearchPage` đã gọi search API và map Post response sang `PostCard`. Đây là một trong các luồng danh sách đã dùng Backend thật.

## 15. Report Post

User Report đã được triển khai:

- Chỉ report Post.
- Reason dùng enum.
- Description có validation.
- User lấy từ JWT.
- Backend tự đọc content và media để tạo snapshot.
- Client không gửi snapshot.
- Chống nhiều report `PENDING` cùng reporter và Post.
- Report mới có status `PENDING`.
- Gửi report không tự động ẩn Post.

Database dùng generated column `pending_report_key` và unique constraint để chống race condition.

Frontend đã có `ReportPostFlow` và `postApi.report`.

## 16. Admin quản lý Post

### 16.1. Danh sách

Backend Admin đã hỗ trợ:

- Phân trang.
- Tìm keyword.
- Lọc status.
- Lọc author.
- Chỉ xem Post có report.
- Thumbnail.
- Like/comment/report count qua projection.

### 16.2. Chi tiết

- Thông tin Post.
- Tác giả.
- Media.
- Hashtag.
- Trạng thái và moderation metadata.

### 16.3. Hide

- Chỉ Admin ACTIVE.
- Pessimistic lock Post.
- Không ẩn lại Post đã `HIDDEN`.
- Không ẩn Post `DELETED`.
- Ghi `hidden_by`, `hidden_at`, `hidden_reason`.
- Ghi `admin_actions`.
- Tạo notification cho tác giả.
- Post và audit cùng transaction.

### 16.4. Restore

- Chỉ khôi phục Post `HIDDEN`.
- Không khôi phục Post `DELETED`.
- Xóa moderation fields.
- Ghi audit riêng.
- Tạo notification.

Frontend Admin pages đã dùng `adminApi` cho các thao tác này.

## 17. Admin xử lý Report

Code hiện hỗ trợ:

- List/filter Report.
- Xem detail và snapshot.
- Reject Report.
- Resolve Report.
- Resolve và tùy chọn Hide Post.

Concurrency:

- Lock Report `PENDING`.
- Chỉ lock Post khi cần Hide.

Transaction:

- Report status.
- Post status nếu có.
- Admin action.
- Notification record.

được điều phối trong cùng transaction nghiệp vụ.

Code tránh ghi action `HIDE_POST` giả nếu Post đã `HIDDEN` hoặc `DELETED`.

## 18. Database hiện tại đã hỗ trợ gì

### Bảng

- `posts`.
- `post_media`.
- `hashtags`.
- `post_hashtags`.
- `post_likes`.
- `comments`.
- `saved_posts`.
- `reports`.
- `admin_actions`.
- `notifications`.

### Index đáng chú ý

- Post theo author/status/time.
- Post theo status/time.
- Post theo engagement.
- FULLTEXT content.
- Comment theo Post/parent/status/time.
- Like theo Post.
- Saved theo user/time.
- Report theo status/time và Post/status.

### View

- `v_active_posts`.
- `v_feed_posts`.

Hai view chỉ lọc Post/tác giả hợp lệ; chưa chứa logic Following hoặc ranking For You và chưa thấy Backend Feed sử dụng.

### Trigger

- Like insert/delete → `posts.like_count`.
- Comment insert/update → `posts.comment_count`.
- Post-hashtag insert/delete → `hashtags.post_count`.

### Thiếu

- Không tìm thấy file DBML để đối chiếu SQL.

## 19. Frontend hiện đã tích hợp API thật đến đâu

| Màn hình/thao tác | Trạng thái hiện tại |
|---|---|
| Tạo Post | Gọi API thật |
| Sửa Post | Gọi API thật |
| Xóa Post | Gọi API thật |
| Like/Unlike | Gọi API thật |
| Save/Unsave | Gọi API thật |
| Report | Gọi API thật |
| Hashtag suggestions | Gọi API thật |
| Post Detail | Gọi API thật |
| Load comments trong Detail | Gọi API thật |
| Tạo/xóa comment | Gọi API thật |
| Search Post | Gọi API thật |
| Admin Post/Report | Gọi API thật |
| Feed For You/Following | Dùng mock/AppContext |
| Profile Post list | Dùng mock/AppContext |
| Saved Post list | Gọi API thật |

`AppContext` đang là lớp trung gian hỗn hợp:

- Khởi tạo bằng `initialData`.
- Các mutation gọi Backend thật.
- Sau mutation, cập nhật lại state mock tại Client.
- Các màn hình chưa có GET endpoint tiếp tục đọc state này.

Hệ quả:

- Refresh trang có thể làm mất thay đổi chỉ tồn tại trong state Client ở các list mock.
- Post vừa tạo có thể xuất hiện trong local state, nhưng dữ liệu Feed thật chưa được tải lại từ Backend.
- Viewer liked/saved có thể lệch nếu state mock ban đầu khác database.

## 20. Chi tiết Frontend Post UI

### PostComposer

Đã có:

- Modal tạo Post.
- Preview nhiều media.
- Ảnh và video.
- Xóa media trước khi submit.
- Kiểm tra số lượng, loại file, size và video duration.
- Hashtag autocomplete có debounce 250 ms.
- Submit multipart.
- Hiển thị lỗi.

Còn điểm UI ngoài phạm vi hoặc chưa nối nghiệp vụ:

- Menu “ai có thể trả lời”.
- Đối tượng chia sẻ.
- Mention/trích dẫn.
- Một số nút chỉ mang tính giao diện.

### PostCard

Đã có:

- Hiển thị author, content, hashtag, media và counters.
- Điều hướng Detail/Profile.
- Like, Comment, Save.
- Menu owner và non-owner.
- Edit/Delete.
- Report.
- Copy link.

PostCard phụ thuộc shape dữ liệu đã được `toPostView` chuyển đổi; chưa dùng một response model thống nhất từ mọi endpoint.

### PostDetailPage

Đã có:

- Load Detail từ API.
- Load comments từ API.
- Tạo comment.
- Hiển thị PostCard detail.

Cần tiếp tục kiểm tra/tích hợp sâu hơn viewer state, reply pagination và đồng bộ counter sau mọi thao tác.

## 21. Test hiện có

### Post

- `PostControllerTest`.
- `PostLikeControllerTest`.
- `SavedPostControllerTest`.
- `HashtagControllerTest`.
- `HashtagSecurityTest`.
- `PostServiceImplTest`.
- `PostLikeServiceImplTest`.
- `SavedPostServiceImplTest`.
- `HashtagServiceImplTest`.
- Entity mapping tests.
- Repository contract tests.
- Hashtag suggestion integration test.
- Validation tests cho content, hashtag và media.

### Module liên quan

- Comment controller/service/mapper tests.
- Search controller/service tests.
- Report controller/service tests.
- Admin Post/Report tests.
- Cloudinary storage tests.
- Profile completion/security tests.

Nhận xét:

- Coverage unit/controller/repository của các luồng đã làm khá rộng.
- Chưa thấy test Feed vì chưa có module Feed.
- Đã có controller test cho API Saved list; Profile Post list vẫn chưa có endpoint/test tương ứng.
- Có MySQL integration test ở một số repository, nhưng không phải toàn bộ luồng Post end-to-end.

## 22. Những phần đã hoàn thành tương đối tốt

1. CRUD Post Backend.
2. Upload và cleanup media.
3. Validation media.
4. Hashtag normalization/upsert.
5. Like/Unlike và counter trigger.
6. Comment/Reply và counter trigger.
7. Save/Unsave.
8. Search Post.
9. Report snapshot và chống report PENDING trùng.
10. Admin moderation và audit.
11. Phân quyền owner/Admin.
12. Chặn user BLOCKED/profile chưa hoàn tất.
13. Frontend mutation API cho Post.
14. Frontend Post Detail và Search dùng API thật.

## 23. Những phần mới hoàn thành một phần

| Phần | Đã có | Còn thiếu |
|---|---|---|
| PostCard data model | Mapper ở Client, nhiều response gần nhau | Một contract thống nhất cho Feed/Profile/Search/Saved/Detail |
| Viewer state | Owner, Like/Save response | Detail/list response đồng bộ liked/saved |
| Saved Posts | Save/Unsave, GET list Backend và tích hợp Frontend | Bổ sung kiểm thử tích hợp MySQL end-to-end nếu cần |
| Profile Posts | UI list | GET list Backend và pagination |
| Feed | UI và SQL view nền | Backend service/controller/ranking/pagination |
| Comment Reply | Backend endpoint | UI flow đầy đủ và xác nhận phạm vi P2 |
| Media cleanup | Cleanup theo request/transaction | Retry/job cho cleanup thất bại và Post deleted |
| Hashtag counter | Trigger | Semantics với Post hidden/deleted |

## 24. Những phần chưa làm

1. Backend Feed For You.
2. Backend Feed Following.
3. Backend Profile Posts list.
4. Load Feed từ API thật trên Frontend.
5. Load Profile posts từ API thật.
6. DBML.
7. Chính sách xóa vật lý Post/media.
8. Reconciliation job cho counter hoặc file Cloudinary mồ côi.

## 25. Các chênh lệch quan trọng giữa code hiện tại và README

| Nội dung | Code hiện tại | README hiện tại |
|---|---|---|
| Media | Ảnh và video | Tối đa 4 ảnh |
| Edit media | Cho thêm, xóa, giữ và sắp lại | Không mô tả; UI docs nói không sửa ảnh |
| Reply comment | Đã có Backend | P2 |
| Feed | Chưa có Backend | P0 |
| Saved list | Đã có Backend và tích hợp Frontend | P1 |
| Hashtag | Service một hashtag | README một hashtag; một số docs cũ nhiều hashtag |

Báo cáo này chỉ ghi nhận code đã làm. Khi sửa production code vẫn phải ưu tiên quyết định trong README hoặc quyết định mới của người dùng.

## 26. Rủi ro kỹ thuật hiện tại

### Cao

- Feed/Profile chưa tích hợp đầy đủ API nên demo có thể hiển thị dữ liệu khác database.
- Frontend dùng state mock kết hợp mutation thật, dễ lệch sau refresh hoặc đăng nhập user khác.
- Video và edit-media đang vượt contract README, có thể phải rollback/refactor.

### Trung bình

- Post response chưa thống nhất viewer state.
- `post_count` hashtag vẫn tính Post hidden/deleted.
- Cleanup Cloudinary thất bại chỉ warning, chưa có retry.
- Schema `post_hashtags` không tự enforce tối đa một hashtag/Post.
- Soft-deleted Post giữ toàn bộ quan hệ và file vô thời hạn.

### Thấp

- Một số UI option ngoài MVP vẫn xuất hiện nhưng chưa có hành vi Backend.
- API tài liệu cũ dùng path/method khác source hiện tại.

## 27. Thứ tự phát triển tiếp theo hợp lý

Nếu mục tiêu là hoàn thiện module Post hiện tại:

1. Chốt lại ảnh-only hay giữ video.
2. Chốt có được sửa media hay không.
3. Thống nhất PostCard response và viewer state.
4. Tạo API Profile Posts.
5. Tạo Feed Following.
6. Tạo Feed For You.
7. Thay mock data ở Feed/Profile bằng API thật.
8. Bổ sung integration test và pagination test.
9. Đồng bộ README, API contract, SQL và DBML theo quyết định đã chốt.

## 28. Trạng thái tổng hợp

| Nhóm | Mức hoàn thành theo code hiện tại |
|---|---|
| Post write operations | Cao |
| Media upload/cleanup | Cao |
| Hashtag | Cao |
| Like/Comment/Save mutation | Cao |
| Post Detail | Cao |
| Search Post | Cao |
| Report/Admin moderation | Cao |
| Feed | Thấp |
| Profile Post list | Thấp |
| Saved Post list | Cao |
| Frontend end-to-end consistency | Trung bình |
| Database documentation | Thiếu DBML |

## 29. Kết luận

Code hiện tại đã hoàn thành phần lớn write-side của module Post và các luồng phụ như interaction, report, moderation. Saved Posts đã có read-side phân trang và tích hợp Frontend; phần còn thiếu chủ yếu nằm ở các read-side tổng hợp khác như Feed và Profile Posts.

Nói ngắn gọn:

```text
Tạo/sửa/xóa/tương tác/report/admin: đã có Backend và phần lớn đã nối Frontend.

Saved list: đã đọc dữ liệu thật từ Backend.
Feed/Profile list: UI đã có nhưng dữ liệu vẫn chưa đồng bộ hoàn chỉnh.
```
