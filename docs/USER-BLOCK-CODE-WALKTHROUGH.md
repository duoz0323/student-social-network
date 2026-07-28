# Phân tích code chức năng User Block

> Tài liệu này giải thích phần code User Block được bổ sung ngày 26/07/2026.
> Đây là tài liệu kỹ thuật hỗ trợ đọc và bảo trì code, không thay thế `README.md`.
> Trạng thái hiện tại: **production code, 599 backend test và migration đã được xác minh trên MySQL 8.0.36; frontend utility test/lint/build đã hoàn tất, còn manual E2E theo checklist**.

---

## 1. Mục đích của tài liệu

Tài liệu giúp người đọc:

- Hiểu yêu cầu nghiệp vụ của User Block.
- Biết dữ liệu Block được lưu ở đâu.
- Hiểu luồng request từ React đến MySQL.
- Biết trách nhiệm của từng class Backend và từng file Frontend.
- Biết Block đã được tích hợp vào module nào.
- Nhận diện các phần còn thiếu hoặc đang lỗi.
- Có hướng sửa đổi mà không phá API, transaction hoặc Cursor Pagination.

Nguồn ưu tiên khi có mâu thuẫn:

1. `README.md`.
2. Yêu cầu User Block của người dùng.
3. SQL và DBML hiện hành.
4. Source code và test.
5. Tài liệu này.

---

## 2. Phạm vi nghiệp vụ

Giả sử User A chặn User B:

- Quan hệ được lưu có hướng `A -> B`.
- Chỉ A có quyền bỏ quan hệ `A -> B`.
- Hiệu lực xem nội dung và tương tác là hai chiều.
- A và B không được xem hồ sơ, bài viết hoặc tương tác với nhau.
- Follow `A -> B` và `B -> A` bị xóa khi Block.
- Không xóa Like, Comment và Save cũ.
- `like_count` và `comment_count` không giảm vì Block không xóa các hàng tương tác lịch sử.
- Nếu A từng Comment trên bài do B sở hữu rồi một trong hai bên tạo Block, B không còn thấy Comment lịch sử của A trong thời gian Block.
- Chủ bài không phải ngoại lệ: hiệu lực ẩn Comment/Reply luôn áp dụng hai chiều cho API người dùng.
- Nếu Comment cha bị ẩn thì toàn bộ nhánh Reply của cha cũng bị ẩn; một Reply bị Block dưới Comment cha hợp lệ được lọc riêng.
- A không thể đọc comment của bài B hoặc tạo Like/Unlike, Comment/Reply, Save/Unsave mới sau khi Block.
- Unblock không phục hồi Follow.
- Sau Unblock, Like và Comment lịch sử vẫn được giữ nguyên.
- Block và Unblock phải idempotent.
- Người thực hiện lấy từ JWT, không nhận `blockerId` từ request.
- User Block khác hoàn toàn `users.status = BLOCKED`:
  - User Block là quan hệ giữa hai người dùng.
  - `users.status = BLOCKED` là trạng thái tài khoản do Admin quản lý.

Ngoài phạm vi:

- Restrict.
- Private Account.
- Follow Request.
- Message.

---

## 3. Kiến trúc tổng thể

```mermaid
flowchart LR
    UI["React Profile / Blocked Users"] --> API["socialApi"]
    API --> Controller["UserBlockController"]
    Controller --> Service["UserBlockServiceImpl"]
    Service --> CurrentUser["CurrentUserProvider"]
    Service --> BlockRepository["UserBlockRepository"]
    Service --> FollowRepository["FollowRepository"]
    BlockRepository --> DB["MySQL user_blocks"]
    FollowRepository --> FollowDB["MySQL follows"]

    Policy["UserRelationshipPolicyService"] --> BlockRepository
    Profile["Profile Service"] --> Policy
    Follow["Follow Service"] --> Policy
    Post["Post / Like / Save / Comment"] --> Policy
    Notification["Notification Service"] --> Policy
```

Điểm quan trọng:

- `UserBlockServiceImpl` xử lý việc tạo/xóa quan hệ Block.
- `UserRelationshipPolicyService` là nơi kiểm tra Block dùng chung.
- Các module khác không nên tự viết lại cách xác định Block.
- Các danh sách có phân trang phải lọc Block ngay trong SQL, không lọc sau khi đã lấy dữ liệu.

---

## 4. Thiết kế Database

### 4.1. Bảng `user_blocks`

Đã thêm vào:

- `database/student_social_network.sql`.
- `database/student_social_network.dbml`.

Schema logic:

```sql
CREATE TABLE user_blocks (
    blocker_id BIGINT UNSIGNED NOT NULL,
    blocked_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (blocker_id, blocked_id),
    KEY idx_user_blocks_blocked_blocker (blocked_id, blocker_id),
    CONSTRAINT fk_user_blocks_blocker
        FOREIGN KEY (blocker_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_user_blocks_blocked
        FOREIGN KEY (blocked_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT chk_user_blocks_not_self
        CHECK (blocker_id <> blocked_id)
);
```

### 4.2. Ý nghĩa từng ràng buộc

| Thành phần | Ý nghĩa |
|---|---|
| `PRIMARY KEY (blocker_id, blocked_id)` | Một chiều Block chỉ có tối đa một bản ghi, đồng thời chống duplicate do request đồng thời. |
| `idx_user_blocks_blocked_blocker` | Hỗ trợ kiểm tra chiều ngược lại `blocked_id -> blocker_id`. |
| `chk_user_blocks_not_self` | Database không cho người dùng chặn chính mình. |
| Hai foreign key | Bảo đảm hai tài khoản tồn tại. |
| `ON DELETE CASCADE` | Nếu tài khoản bị xóa vật lý, quan hệ Block liên quan được dọn theo convention quan hệ người dùng hiện tại. |
| Không có `status` | Tồn tại bản ghi nghĩa là đang Block. |
| Không có `deleted_at` | Unblock là xóa vật lý quan hệ. |

### 4.3. Cách kiểm tra Block hai chiều

Một cặp user bị xem là có Block khi tồn tại một trong hai bản ghi:

```text
(blocker_id = A AND blocked_id = B)
OR
(blocker_id = B AND blocked_id = A)
```

Không được chỉ kiểm tra `A -> B`, vì hiệu lực nghiệp vụ là hai chiều.

### 4.4. Lưu ý migration

Repository không có Flyway/Liquibase. SQL canonical là một file dump đầy đủ và migration không phá hủy dành cho schema đang có dữ liệu nằm tại `database/migrations/20260728_add_user_blocks.sql`.

Do đó:

- Code đã cập nhật schema canonical.
- Chưa chạy schema này trên database MySQL thật ở máy hiện tại.
- Cách chạy và các câu lệnh xác minh nằm tại `docs/database/USER-BLOCK-MIGRATION.md`.
- Khi áp dụng lên database đang có dữ liệu, nên tách riêng câu `CREATE TABLE user_blocks`.
- Không chạy lại toàn bộ dump nếu database chứa dữ liệu cần giữ.
- Trước khi áp dụng production cần backup và kiểm tra MySQL version hỗ trợ `CHECK`.

---

## 5. Các class Backend được thêm

### 5.1. `UserBlockId`

Đường dẫn:

`BackEnd/src/main/java/com/stu/edu/vn/backend/user/entity/UserBlockId.java`

Trách nhiệm:

- Ánh xạ composite primary key.
- Chứa `blockerId` và `blockedId`.
- Cài đặt `equals()` và `hashCode()` để JPA nhận diện Entity đúng.

Khi sửa:

- Tên field phải khớp giá trị dùng trong `@MapsId`.
- Tên cột phải tiếp tục khớp SQL và DBML.
- Không bỏ `Serializable`.

### 5.2. `UserBlock`

Đường dẫn:

`BackEnd/src/main/java/com/stu/edu/vn/backend/user/entity/UserBlock.java`

Trách nhiệm:

- Ánh xạ bảng `user_blocks`.
- Liên kết `blocker` và `blocked` đến `User`.
- Không chứa business logic ngoài việc khởi tạo đúng composite key.

Các association dùng `FetchType.LAZY` để tránh tải toàn bộ User khi không cần.

### 5.3. `UserBlockRepository`

Đường dẫn:

`BackEnd/src/main/java/com/stu/edu/vn/backend/user/repository/UserBlockRepository.java`

Các method chính:

```java
existsByIdBlockerIdAndIdBlockedId(blockerId, blockedId)
```

Dùng khi cần kiểm tra đúng một chiều, ví dụ A có trực tiếp chặn B hay không.

```java
existsEitherDirection(userAId, userBId)
```

Dùng cho quyền xem và tương tác. Đây là truy vấn quan trọng nhất của policy.

```java
findBlockedUsers(blockerId, pageable)
```

Chỉ lấy danh sách do current user trực tiếp chặn. Query trả projection tối thiểu và không trả email.

### 5.4. DTO và projection

| File | Mục đích |
|---|---|
| `BlockedUserProjection.java` | Nhận trực tiếp kết quả danh sách từ native query. |
| `BlockedUserResponse.java` | Response gồm `userId`, `displayName`, `avatarUrl`, `blockedAt`. |
| `UserBlockStatusResponse.java` | Response cho Block/Unblock gồm `targetUserId` và `blocked`. |

Không trả `UserBlock` hoặc `User` Entity trực tiếp qua Controller.

### 5.5. `UserRelationshipPolicyService`

Đường dẫn:

- `user/service/UserRelationshipPolicyService.java`.
- `user/service/impl/UserRelationshipPolicyServiceImpl.java`.

API nội bộ:

```java
boolean existsBlockEitherDirection(Long userAId, Long userBId);
boolean hasBlocked(Long blockerId, Long blockedId);
void assertNoBlock(Long userAId, Long userBId);
```

Quy ước sử dụng:

- Dùng `existsBlockEitherDirection` khi cần chuyển lỗi thành 404 riêng của module.
- Dùng `assertNoBlock` khi có thể trả `USER_RELATIONSHIP_BLOCKED`.
- Dùng `hasBlocked` khi chỉ quan tâm đúng chiều.

Không nên inject `UserBlockRepository` trực tiếp vào nhiều service để tự viết lại policy.

### 5.6. `UserBlockServiceImpl`

Đường dẫn:

`BackEnd/src/main/java/com/stu/edu/vn/backend/user/service/impl/UserBlockServiceImpl.java`

#### Luồng Block

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant BlockRepo
    participant FollowRepo
    participant DB

    Client->>Controller: PUT /users/{targetId}/block
    Controller->>Service: block(targetId)
    Service->>Service: Lấy blockerId từ SecurityContext
    Service->>Service: Chặn self-block, kiểm tra hai User ACTIVE
    Service->>BlockRepo: Kiểm tra A -> B đã tồn tại
    alt Chưa tồn tại
        Service->>BlockRepo: saveAndFlush(UserBlock)
        BlockRepo->>DB: INSERT user_blocks
    end
    Service->>FollowRepo: DELETE Follow A -> B
    Service->>FollowRepo: DELETE Follow B -> A
    Service-->>Controller: blocked = true
    Controller-->>Client: ApiResponse 200
```

Toàn bộ thao tác chạy trong `@Transactional`.

Repository dùng một native upsert nguyên tử:

```sql
INSERT INTO user_blocks (...)
VALUES (...)
ON DUPLICATE KEY UPDATE created_at = user_blocks.created_at
```

Composite primary key trùng trở thành no-op nên hai request đồng thời vẫn idempotent. Khác với `INSERT IGNORE`, lỗi foreign key hoặc CHECK không bị hạ thành warning mà vẫn làm transaction rollback.

#### Luồng Unblock

- Lấy `blockerId` từ JWT.
- Tạo `UserBlockId(blockerId, targetUserId)`.
- Xóa đúng chiều này.
- Không xóa chiều ngược.
- Không phục hồi Follow.
- Không tạo Notification.
- Trả `blocked = false`.

#### Kết quả xác minh concurrency

Test MySQL gửi hai HTTP `PUT` đồng thời đã xác nhận:

- Cả hai response đúng contract idempotent và không trả 500.
- Chỉ có một hàng `user_blocks`.
- Follow hai chiều cùng bị xóa.
- Không tạo Notification Block.
- Unblock dùng `DELETE` có `@Modifying`; kết quả 0 hàng vẫn là thành công.

### 5.7. `UserBlockController`

Đường dẫn:

`BackEnd/src/main/java/com/stu/edu/vn/backend/user/controller/UserBlockController.java`

API:

| Method | Endpoint | Response |
|---|---|---|
| `PUT` | `/api/v1/users/{targetUserId}/block` | `{targetUserId, blocked: true}` |
| `DELETE` | `/api/v1/users/{targetUserId}/block` | `{targetUserId, blocked: false}` |
| `GET` | `/api/v1/users/me/blocked-users?page=0&size=20` | `PageResponse<BlockedUserResponse>` |

Controller không nhận `blockerId`. Current user được lấy tại service qua `CurrentUserProvider`.

---

## 6. Các Error Code được thêm

File:

`BackEnd/src/main/java/com/stu/edu/vn/backend/common/exception/ErrorCode.java`

| Error code | HTTP | Khi dùng |
|---|---:|---|
| `CANNOT_BLOCK_SELF` | 400 | Current user cố chặn chính mình. |
| `USER_RELATIONSHIP_BLOCKED` | 403 | Hai tài khoản đang có Block và thao tác tương tác bị từ chối. |

Profile và Post Detail cố tình dùng lỗi `PROFILE_NOT_FOUND` hoặc `POST_NOT_FOUND` để tránh tiết lộ tài nguyên của tài khoản có Block.

---

## 7. Những module đã được tích hợp

### 7.1. Profile

File:

`UserProfileServiceImpl.java`

Trước khi tải hồ sơ người khác:

```java
if (!profileUserId.equals(currentUserId)
        && relationshipPolicyService.existsBlockEitherDirection(currentUserId, profileUserId)) {
    throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
}
```

Chủ tài khoản vẫn xem hồ sơ của chính mình.

### 7.2. Follow

File:

`FollowServiceImpl.java`

Trước khi tạo Follow:

- Kiểm tra không Follow chính mình.
- Kiểm tra target hợp lệ.
- Gọi `relationshipPolicyService.assertNoBlock(currentUserId, userId)`.

Khi Block, `UserBlockServiceImpl` gọi `deleteFollow` hai lần để xóa cả hai chiều.

### 7.3. Feed và danh sách bài viết

Files:

- `PostRepository.java`.
- `FeedServiceImpl.java`.
- `UserPostServiceImpl.java`.

Các native query được bổ sung `NOT EXISTS` với `user_blocks`. Điều kiện Block nằm trong query trước khi `ORDER BY` và trước khi giới hạn `limit + 1`.

Mục đích:

- Không lấy đủ limit rồi mới lọc bằng Java.
- Không làm trang trả thiếu item khi vẫn còn dữ liệu hợp lệ.
- Không làm `nextCursor` trỏ theo item đã bị lọc.

`findForYouFeed` và `findProfilePosts` được thêm tham số `viewerId`. Đây là thay đổi signature làm một số test cũ bị lỗi và cần được cập nhật.

### 7.4. Post Detail

File:

`PostServiceImpl.java`

Sau khi tìm thấy Post PUBLISHED, service kiểm tra Block giữa viewer và author. Nếu có Block thì trả `POST_NOT_FOUND`.

### 7.5. Like/Unlike

File:

`PostLikeServiceImpl.java`

Service lấy `authorId` qua `PostInteractionTargetProjection`, sau đó gọi policy trước khi tạo hoặc xóa Like.

Lưu ý nghiệp vụ ban đầu chỉ cấm tương tác mới. Việc đang chặn có cho phép Unlike/Unsave dữ liệu cũ hay không cần được test và thống nhất. Code hiện chặn cả Like và Unlike nếu có Block.

### 7.6. Save/Unsave

File:

`SavedPostServiceImpl.java`

Method `assertPostCanBeAccessed()`:

- Kiểm tra Post tồn tại.
- Kiểm tra Post PUBLISHED.
- Kiểm tra Block với tác giả.

Method này được dùng trước Save và Unsave.

### 7.7. Comment/Reply

File:

`CommentServiceImpl.java`

Policy được dùng trước:

- Tạo comment.
- Tạo reply.
- Đọc comment gốc.
- Đọc reply.

Việc đọc và tạo Comment/Reply trên bài không còn quyền truy cập được chặn qua cùng `UserRelationshipPolicyService`; query bài viết là lớp lọc quyền truy cập đầu tiên.

`CommentRepository` nhận `viewerId` cho query comment gốc, reply và reply count. Cả data query lẫn `countQuery`
dùng cùng điều kiện `NOT EXISTS user_blocks` theo hai chiều, vì vậy:

- `totalElements` và `totalPages` không tính Comment bị ẩn.
- `replyCount` khớp đúng số Reply viewer có thể tải.
- Comment cha bị Block không để lộ nhánh Reply mồ côi.
- Không cần lọc lại Page bằng Java và không phát sinh N+1.

### 7.8. Search User

Files:

- `SearchUserProfileRepository.java`.
- `SearchServiceImpl.java`.

Search User lọc Block trực tiếp trong cả query dữ liệu và `countQuery`. `viewerId` được bind từ current user.

### 7.9. Notification

Files:

- `NotificationServiceImpl.java`.
- `NotificationRepository.java`.

Khi tạo Notification:

- Nếu actor bằng recipient thì bỏ qua như trước.
- Nếu actor và recipient có Block theo bất kỳ chiều nào thì không lưu Notification.

Khi đọc danh sách:

- Native query loại Notification có actor đang Block với recipient.
- Notification hệ thống có `actor_id IS NULL` vẫn được trả.

Điểm cần lưu ý:

- Query unread count dùng cùng điều kiện Block với danh sách Notification.
- Các thao tác đọc/xóa từng Notification cũ xác minh recipient và ẩn item có actor đang Block bằng lỗi `NOTIFICATION_NOT_FOUND`.

---

## 8. Frontend

### 8.1. API endpoint

File:

`FrontEnd/src/api/apiEndpoints.js`

Đã thêm:

```javascript
block: (userId) => `/api/v1/users/${encodeURIComponent(userId)}/block`
blockedUsers: '/api/v1/users/me/blocked-users'
```

### 8.2. API service

File:

`FrontEnd/src/api/socialApi.js`

Đã thêm:

```javascript
blockUser(userId, signal)
unblockUser(userId, signal)
getBlockedUsers(params, signal)
```

React component không gọi Axios trực tiếp.

### 8.3. Block trên trang Profile

File:

`FrontEnd/src/features/profile/pages/ProfilePage.jsx`

Luồng hiện tại:

1. Nút “Chặn” chỉ nằm trong khu vực action của hồ sơ người khác.
2. Nhấn nút mở modal xác nhận.
3. Khi submit, nút bị disable bằng state `blocking`.
4. Gọi `socialApi.blockUser(profile.id)`.
5. Thành công thì đóng modal và điều hướng về `/feed/for-you`.
6. Thất bại thì dùng error state hiện có.

Điểm cần lưu ý:

- Toast thành công được đặt ở `AppContext` để không mất khi điều hướng.
- Cache cursor liên quan được invalidation tập trung qua `userBlockState.js`; snapshot Context đồng thời loại user, post, follow, like, save và comment liên quan.
- UI hiện dùng nút trực tiếp thay vì một menu action hoàn chỉnh.
- Dự án chưa có framework component test; đã bổ sung Node test cho cache invalidation và cập nhật snapshot, không cài thêm thư viện.

### 8.4. Trang tài khoản đã chặn

File:

`FrontEnd/src/features/profile/pages/BlockedUsersPage.jsx`

Route:

`/settings/blocked-users`

Màn hình hỗ trợ:

- Loading state.
- Empty state.
- Error state.
- Danh sách avatar và display name.
- Phân trang.
- Modal xác nhận Unblock.
- Disable trong khi request đang xử lý.
- Xóa item khỏi danh sách hiện tại sau khi Unblock.

Điểm còn thiếu:

- Menu “Xem thêm” trong `UserShell` đã có link “Tài khoản đã chặn”.
- Unblock đã xóa item, giảm `totalElements`, invalidation cache và hiển thị toast.
- Chưa có test accessibility/modal.

### 8.5. Router

Files:

- `FrontEnd/src/router/lazyRoutes.jsx`.
- `FrontEnd/src/router/index.jsx`.

Trang Blocked Users được lazy-load và nằm trong `ProtectedRoute`.

---

## 9. Danh sách file đã thêm

### Backend

- `user/controller/UserBlockController.java`.
- `user/dto/response/BlockedUserResponse.java`.
- `user/dto/response/UserBlockStatusResponse.java`.
- `user/entity/UserBlock.java`.
- `user/entity/UserBlockId.java`.
- `user/repository/BlockedUserProjection.java`.
- `user/repository/UserBlockRepository.java`.
- `user/service/UserBlockService.java`.
- `user/service/UserRelationshipPolicyService.java`.
- `user/service/impl/UserBlockServiceImpl.java`.
- `user/service/impl/UserRelationshipPolicyServiceImpl.java`.

### Frontend

- `features/profile/pages/BlockedUsersPage.jsx`.

### Tài liệu

- `docs/USER-BLOCK-CODE-WALKTHROUGH.md`.

---

## 10. Danh sách file đã sửa

### Database

- `database/student_social_network.sql`.
- `database/student_social_network.dbml`.

### Backend

- `common/exception/ErrorCode.java`.
- `feed/service/impl/FeedServiceImpl.java`.
- `follow/service/impl/FollowServiceImpl.java`.
- `interaction/service/impl/CommentServiceImpl.java`.
- `interaction/repository/CommentRepository.java`.
- `notification/repository/NotificationRepository.java`.
- `notification/service/impl/NotificationServiceImpl.java`.
- `post/repository/PostRepository.java`.
- `post/service/impl/PostLikeServiceImpl.java`.
- `post/service/impl/PostServiceImpl.java`.
- `post/service/impl/SavedPostServiceImpl.java`.
- `post/service/impl/UserPostServiceImpl.java`.
- `search/repository/SearchUserProfileRepository.java`.
- `search/service/impl/SearchServiceImpl.java`.
- `user/service/impl/UserProfileServiceImpl.java`.

### Frontend

- `src/api/apiEndpoints.js`.
- `src/api/socialApi.js`.
- `src/features/profile/pages/ProfilePage.jsx`.
- `src/features/profile/pages/BlockedUsersPage.jsx`.
- `src/features/profile/utils/userBlockState.js`.
- `src/features/post/pages/PostDetailPage.jsx`.
- `src/contexts/AppContext.jsx`.
- `src/router/index.jsx`.
- `src/router/lazyRoutes.jsx`.

---

## 11. Kết quả kiểm tra hiện tại

Các lệnh đã chạy:

```powershell
cd BackEnd
mvn -q -DskipTests compile
```

Kết quả: thành công.

```powershell
cd FrontEnd
npm.cmd run lint
npm.cmd run build
```

Kết quả:

- ESLint thành công.
- Production build thành công.

```powershell
cd BackEnd
mvn test -q
```

Kết quả suite cuối với profile `mysql-test`:

- Tổng test: 599.
- Failure assertion: 0.
- Error runtime/setup: 0.
- Skipped: 0.
- Các lỗi constructor, repository signature và Mockito stubbing cũ đã được sửa tại fixture/test thay vì tạo constructor production dành riêng cho test.
- Đã chạy test Service, Controller, repository contract, notification policy, query cursor và HTTP concurrency trên MySQL 8.0.36.

---

## 12. Các phần còn cần xác minh thủ công

### 12.1. Backend

- Không còn bước MySQL tự động bắt buộc: migration, query native, cursor và HTTP concurrency đều đã chạy thành công.
- Khi chuẩn bị release thật, vẫn phải dùng database staging riêng và không dùng credential/container test trong tài liệu này cho production.

### 12.2. Frontend

- Bổ sung framework component test trong một thay đổi dependency được phê duyệt riêng; hiện đã có Node test cho cache/state.
- Chạy accessibility/modal và toàn bộ luồng nghiệp vụ bằng checklist manual E2E.

### 12.3. Tài liệu

- README, API Block và sự khác nhau với Admin account status `BLOCKED` đã được cập nhật.
- Trạng thái migration đã đổi sang `VERIFIED`; checklist manual E2E vẫn để trống để người kiểm thử ghi bằng chứng.

---

## 13. Thứ tự sửa tiếp theo đề xuất

1. Chạy `docs/testing/USER-BLOCK-E2E-CHECKLIST.md` với ba tài khoản test A, B và C.
2. Ghi kết quả thực tế, PASS/FAIL và ảnh/bằng chứng cho từng bước.
3. Nếu có lỗi E2E, đối chiếu query/policy theo các mục phía trên trước khi sửa.

---

## 14. Hướng dẫn sửa code an toàn

### Thêm một module mới cần kiểm tra Block

Ưu tiên:

```java
relationshipPolicyService.assertNoBlock(currentUserId, targetUserId);
```

Nếu API cần che giấu tài nguyên:

```java
if (relationshipPolicyService.existsBlockEitherDirection(currentUserId, ownerId)) {
    throw new BusinessException(ErrorCode.POST_NOT_FOUND);
}
```

Không làm:

```java
userBlockRepository.findAll().stream().filter(...);
```

Không viết mỗi module một định nghĩa Block khác nhau.

### Sửa query Cursor Pagination

Điều kiện Block phải nằm trong query trước `ORDER BY` và trước khi Spring áp giới hạn:

```sql
AND NOT EXISTS (
    SELECT 1
    FROM user_blocks ub
    WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
       OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
)
```

Sau khi sửa:

- Giữ nguyên thứ tự sort.
- Giữ nguyên các khóa cursor.
- Vẫn lấy `limit + 1`.
- Không lọc lại bằng Java.
- Cập nhật cả method signature, service call và test mock.

### Sửa Search dùng PageResponse

Phải thêm cùng điều kiện vào:

- Query lấy dữ liệu.
- `countQuery`.

Nếu chỉ sửa query dữ liệu, `totalElements` và `totalPages` sẽ sai.

### Thêm API mới

Luôn giữ:

- Current user từ `CurrentUserProvider`.
- DTO response, không trả Entity.
- `ApiResponse` chung.
- `PageResponse` cho danh sách page-based.
- Validation ở Service.
- Controller mỏng.

### Sửa Frontend

Luôn đặt request trong API service. Component chỉ:

- Quản lý state giao diện.
- Gọi service.
- Hiển thị loading/error/success.
- Điều hướng và cập nhật cache.

Không gọi `httpClient` hoặc Axios trực tiếp trong JSX.

---

## 15. Checklist test cần bổ sung

### UserBlockService

- Không chặn chính mình.
- Block user hợp lệ.
- Block lặp lại.
- Unblock hợp lệ.
- Unblock lặp lại.
- Block xóa Follow hai chiều.
- Hai request Block đồng thời.
- Danh sách chỉ thuộc current user.
- Response không có email.

### Tích hợp module

- Follow bị từ chối khi có Block ở mỗi chiều.
- Profile trả 404 ở mỗi chiều.
- Post Detail trả 404 ở mỗi chiều.
- Like, Comment và Save bị từ chối.
- Feed không trả post bị Block.
- Cursor vẫn đủ limit và đúng `nextCursor`.
- Search User/Post không trả dữ liệu bị Block.
- Notification không được tạo.
- Notification cũ không xuất hiện trong list và unread count.

### Frontend

- Không hiện Block cho hồ sơ của chính mình.
- Modal có nội dung đúng.
- Không gửi hai request khi double click.
- API dùng đúng method/path.
- Thành công thì điều hướng và làm mới cache.
- Danh sách Block có loading/empty/error/page.
- Unblock xóa đúng item và không tự Follow.

---

## 16. Trạng thái cuối tại thời điểm viết

| Hạng mục | Trạng thái |
|---|---|
| SQL/DBML/migration | Đã đồng bộ; migration chạy thành công trên schema sạch và schema có dữ liệu của MySQL 8.0.36 |
| Backend compile | Thành công |
| API Block | Đã hoàn tất |
| Policy dùng chung | Đã áp dụng |
| Search Post và follower/following | Đã lọc hai chiều tại database |
| Feed/Profile/Liked/Saved | Query contract và MySQL integration test đã chạy thành công |
| Notification | Đã lọc tạo/list/unread/thao tác item |
| Backend test | 599 test, 0 lỗi, 0 skipped trên MySQL 8.0.36 |
| Frontend lint | Thành công |
| Frontend build | Thành công |
| Frontend test Block | Có 3 Node test utility/cache; `FRONTEND COMPONENT TEST NOT AVAILABLE` do dự án chưa có DOM/component test framework |
| README chính thức | Đã cập nhật |
| Migration MySQL thật | `VERIFIED` trên schema sạch và schema có dữ liệu |
| Sẵn sàng manual test | Có; dùng `docs/testing/USER-BLOCK-E2E-CHECKLIST.md` |

Kết luận: **READY FOR MANUAL E2E TEST**. Đây chưa phải xác nhận release production; checklist E2E thủ công vẫn đang ở trạng thái chưa chạy.
