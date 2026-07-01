# Dữ liệu demo

## 1. Mục đích

Dùng để:

- Dựng UI trước Backend.
- Kiểm tra trạng thái giao diện.
- Làm ví dụ request/response.
- Giúp Agent hiểu quan hệ dữ liệu.

## 2. Quy tắc

- Không dùng dữ liệu cá nhân thật.
- Không dùng mật khẩu thật.
- Không dùng token thật.
- ID demo có dạng dễ đọc.
- Frontend không phụ thuộc định dạng ID demo.

## 3. Tài khoản

- User ACTIVE.
- Admin ACTIVE.
- User BLOCKED.
- Dữ liệu đăng nhập nằm trong `demoAccounts`.
- Đăng nhập bằng email hoặc số điện thoại và mật khẩu mô phỏng.
- Mỗi tài khoản demo nên có ít nhất một phương thức định danh; có thể để `email` hoặc `phoneNumber` là `null` để mô phỏng đúng luồng đăng ký MVP.
- Không dùng username trong dữ liệu demo.
- Không render công khai email, số điện thoại hoặc passwordDemo.

### Cấu trúc `demoAccounts`

- `id`
- `email`
- `phoneNumber`
- `passwordDemo`
- `role`
- `status`
- `userId`

Quan hệ: `demoAccounts.userId -> users.id`.

Quy tắc: `email` và `phoneNumber` là dữ liệu riêng tư. Mock data có thể chứa cả hai sau khi mô phỏng người dùng bổ sung phương thức còn thiếu, nhưng form đăng ký MVP chỉ gửi một `identifier`.

### Cấu trúc `users`

- `id`
- `displayName`
- `avatarUrl`
- `bio`
- `birthDate`
- `role`
- `status`
- `followerCount`
- `followingCount`

Thông tin công khai chỉ dùng displayName, avatarUrl, bio, birthDate nếu cần, followerCount và followingCount.

## 4. Post

Có dữ liệu:

- PUBLISHED.
- HIDDEN.
- Có ảnh.
- Không ảnh.
- Đã Like.
- Chưa Like.
- Đã Save.
- Chưa Save.
- `posts.authorId -> users.id`.
- PostCard hiển thị displayName của tác giả, không hiển thị username.

## 4.1 Quan hệ dữ liệu

- `comments.authorId -> users.id`.
- `follows.followerId -> users.id`.
- `follows.followingId -> users.id`.
- `reports.reporterId -> users.id`.
- `reports.postId -> posts.id`.
- `postMentions.mentionedUserId -> users.id`.
- `commentMentions.mentionedUserId -> users.id`.

## 4.2 Mention

Mention thuộc `FUTURE_DEVELOPMENT` nếu chưa triển khai trong MVP.

Quy tắc dữ liệu:

- Hiển thị mention bằng displayName.
- Lưu liên kết bằng `mentionedUserId`.
- Có thể lưu `displayNameSnapshot` nếu cần.
- Không lưu hoặc render `@username`.
- Khi bấm mention, điều hướng đến `/profile/:userId`.

## 5. UI State

Mock data cần hỗ trợ:

- Danh sách có dữ liệu.
- Danh sách rỗng.
- Loading mô phỏng.
- Error mô phỏng.
- User không có quyền.
