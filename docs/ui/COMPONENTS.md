# Danh sách component

Tài liệu này phân rã component dựa trên phần lặp lại thực tế trong ảnh Stitch và phạm vi MVP. Không tách component quá nhỏ cho các đoạn HTML chỉ xuất hiện một lần.

## 1. Layout component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `UserShell` | Khung giao diện người dùng gồm sidebar trái và vùng nội dung chính. | FEED-01, POST-01, POST-07, PROFILE-01, PROFILE-02, SEARCH-01. | `activeNav`, `currentUser`, `children`, `onCreatePost`. | Dùng chung. |
| `UserSidebar` | Hiển thị brand và điều hướng người dùng. | FEED-01, POST-01, POST-07, PROFILE-01, PROFILE-02, SEARCH-01. | `activeItem`, `currentUser`, `unreadCount` CẦN XÁC NHẬN, callbacks điều hướng. | Dùng chung. |
| `AdminShell` | Khung trang quản trị gồm sidebar admin và content rộng. | ADMIN-01 đến ADMIN-06. | `activeNav`, `adminUser`, `children`. | Dùng chung admin. |
| `AdminSidebar` | Điều hướng quản trị. | ADMIN-01 đến ADMIN-06. | `activeItem`, `onBackToApp`. | Dùng chung admin. |
| `AuthLayout` | Canh giữa form auth trên nền pattern giáo dục. | AUTH-01 đến AUTH-06. | `title`, `children`, `statusIllustration` nếu có. | Dùng chung auth. |

## 2. Common component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `Button` | Nút primary, secondary, destructive và trạng thái loading/disabled. | Hầu hết màn hình. | `variant`, `size`, `disabled`, `loading`, `icon`, `children`, `onClick`. | Dùng chung. |
| `TextField` | Input một dòng cho auth, search, form admin. | AUTH-01/02/05, SEARCH-01, ADMIN-02/04/05. | `label`, `placeholder`, `value`, `type`, `error`, `icon`. | Dùng chung. |
| `TextareaField` | Nhập nội dung dài cho bài viết, bio, mô tả báo cáo. | POST-02/03/09, PROFILE-03. | `value`, `maxLength`, `error`, `placeholder`, `showCounter`. | Dùng chung. |
| `Modal` | Container modal có header, body, footer và nút đóng. | POST-02 đến POST-10, PROFILE-03/04, SYS-04. | `open`, `title`, `children`, `footer`, `onClose`, `size`. | Dùng chung. |
| `ConfirmDialog` | Xác nhận thao tác nguy hiểm hoặc quan trọng. | POST-05, admin action nếu cần. | `title`, `message`, `confirmLabel`, `cancelLabel`, `variant`, `onConfirm`. | Dùng chung. |
| `StatusResult` | Màn hình/modal trạng thái thành công hoặc lỗi. | AUTH-03/06, POST-06/10, SYS-01 đến SYS-04. | `type`, `title`, `description`, `primaryAction`, `secondaryAction`. | Dùng chung. |
| `Avatar` | Hiển thị ảnh đại diện hoặc fallback. | Feed, post, profile, search, admin detail. | `src`, `name`, `size`, `verified`. | Dùng chung. |
| `Badge` | Hiển thị trạng thái hoặc nhãn ngắn. | Admin, report detail, PostCard nếu cần. | `tone`, `children`. | Dùng chung. |
| `DataTable` | Bảng dữ liệu có header, row, empty/loading và phân trang. | ADMIN-02, ADMIN-04, ADMIN-05. | `columns`, `rows`, `loading`, `pagination`, `onRowAction`. | Dùng chung admin, có thể tái dùng. |
| `Pagination` | Điều hướng trang cho danh sách. | Admin tables, search/feed nếu dùng phân trang dạng nút. | `page`, `totalPages`, `onChange`. | Dùng chung. |

## 3. Authentication component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `LoginForm` | Form đăng nhập bằng email/username và mật khẩu. | AUTH-01. | `initialValues`, `errors`, `submitting`, `onSubmit`, `onForgotPassword`. | Module auth. |
| `RegisterForm` | Form đăng ký tài khoản MVP. | AUTH-02. | `initialValues`, `errors`, `submitting`, `onSubmit`. | Module auth. |
| `PasswordResetCodeForm` | Nhập mã xác minh đặt lại mật khẩu. | AUTH-04. | `email`, `codeLength`, `submitting`, `onSubmit`, `onResend`. | Module auth, P2/CẦN XÁC NHẬN. |
| `SetPasswordForm` | Nhập mật khẩu mới và xác nhận. | AUTH-05. | `errors`, `submitting`, `onSubmit`. | Module auth, P2/CẦN XÁC NHẬN. |
| `AuthSuccessPanel` | Thông báo đăng ký/đặt lại mật khẩu thành công. | AUTH-03, AUTH-06. | `title`, `description`, `actionLabel`, `onAction`. | Module auth. |

Ghi chú: OAuth button xuất hiện trong ảnh nhưng ngoài MVP; không tách thành component nghiệp vụ trong MVP.

## 4. Post component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `PostCard` | Hiển thị bài viết trong feed, profile, saved và search. | FEED-01, PROFILE-01/02, POST-07, SEARCH-01 nếu có kết quả bài. | `post`, `currentUser`, `onLike`, `onComment`, `onSave`, `onOpenMenu`, `onOpenDetail`. | Dùng chung module post/feed/profile. |
| `PostDetail` | Hiển thị bài viết đầy đủ và khu vực bình luận. | POST-01. | `post`, `comments`, `currentUser`, action callbacks. | Module post. |
| `PostComposer` | Tạo bài viết mới từ modal hoặc composer nhanh. | FEED-01, POST-02. | `currentUser`, `draft`, `maxLength`, `maxImages`, `submitting`, `onSubmit`. | Module post. |
| `EditPostForm` | Sửa nội dung và hashtag của bài đã đăng. | POST-03. | `post`, `maxLength`, `submitting`, `onSave`, `onCancel`. | Module post. |
| `PostActionMenu` | Menu hành động theo quyền với bài viết. | POST-04, PostCard. | `post`, `isOwner`, `onEdit`, `onDelete`, `onReport`, `onSave`, `onCopyLink`. | Module post. |
| `DeletePostDialog` | Xác nhận xóa mềm bài viết. | POST-05. | `post`, `submitting`, `onConfirm`, `onCancel`. | Module post. |
| `ReportPostFlow` | Gom các bước chọn lý do, nhập mô tả và gửi báo cáo. | POST-08, POST-09, POST-10. | `post`, `reasons`, `selectedReason`, `description`, `submitting`, `onSubmit`. | Module report/post. |
| `CommentList` | Hiển thị danh sách bình luận và thao tác xóa của chủ bình luận. | POST-01. | `comments`, `currentUser`, `onDeleteComment`. | Module post. |
| `CommentComposer` | Nhập và gửi bình luận mới. | POST-01. | `value`, `submitting`, `onSubmit`. | Module post. |

## 5. Profile component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `ProfileHeader` | Hiển thị thông tin hồ sơ, avatar, thống kê và nút hành động. | PROFILE-01, PROFILE-02. | `profile`, `isSelf`, `isFollowing`, `onFollowToggle`, `onEditProfile`. | Module profile. |
| `EditProfileModal` | Cập nhật avatar, tên hiển thị, username/bio theo API. | PROFILE-03. | `profile`, `errors`, `submitting`, `onSave`. | Module profile. |
| `ProfileTabs` | Chuyển nhóm nội dung trong hồ sơ. | PROFILE-01, PROFILE-02. | `activeTab`, `tabs`, `onChange`. | Module profile. |
| `FollowListModal` | Danh sách follower/following có thao tác theo dõi. | PROFILE-04. | `type`, `users`, `pagination`, `currentUser`, `onFollowToggle`. | Module follow/profile. |
| `FollowButton` | Theo dõi hoặc bỏ theo dõi. | PROFILE-02, PROFILE-04, SEARCH-01. | `isFollowing`, `loading`, `onClick`. | Dùng chung profile/search. |

## 6. Search component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `SearchBox` | Nhập từ khóa tìm kiếm. | SEARCH-01, admin list nếu dùng chung style. | `value`, `placeholder`, `onChange`, `onSubmit`. | Dùng chung có biến thể. |
| `PopularSearchList` | Hiển thị từ khóa/hashtag phổ biến. | SEARCH-01. | `items`, `onSelect`. | Module search. |
| `SuggestedUserList` | Gợi ý người dùng để theo dõi. | SEARCH-01, có thể dùng ở Feed nếu có panel gợi ý. | `users`, `onFollowToggle`, `onOpenProfile`. | Module search/follow. |
| `SearchResults` | Hiển thị kết quả user/post/hashtag khi có truy vấn. | SEARCH-01. | `query`, `users`, `posts`, `hashtags`, `loading`, `pagination`. | Module search. |

## 7. Admin component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `AdminSummaryCards` | Hiển thị chỉ số tổng quan đơn giản. | ADMIN-01. | `totalUsers`, `totalPosts`, `pendingReports`, `blockedUsers`. | Module admin, CẦN XÁC NHẬN vì dashboard nâng cao ngoài MVP. |
| `AdminUserTable` | Danh sách user với trạng thái và thao tác. | ADMIN-02. | `users`, `pagination`, `loading`, `onSearch`, `onToggleStatus`. | Module admin. |
| `AdminUserActionMenu` | Menu thao tác user. | ADMIN-03. | `user`, `onBlock`, `onUnblock`, `onView`. | Module admin. |
| `AdminPostTable` | Danh sách bài viết và trạng thái. | ADMIN-04. | `posts`, `pagination`, `loading`, `onHide`, `onRestore`. | Module admin. |
| `AdminReportTable` | Danh sách báo cáo cần xử lý. | ADMIN-05. | `reports`, `pagination`, `loading`, `onOpenDetail`. | Module admin. |
| `ReportDetailPanel` | Chi tiết báo cáo và bài bị báo cáo. | ADMIN-06. | `report`, `post`, `onReject`, `onResolve`, `onHidePost`. | Module admin/report. |
| `AdminStatusBadge` | Badge trạng thái user, post, report. | ADMIN-01 đến ADMIN-06. | `type`, `status`. | Dùng chung admin. |

## 8. System state component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `ErrorPage` | Trang lỗi 403, 404, 500. | SYS-01, SYS-02, SYS-03. | `code`, `title`, `description`, `primaryAction`. | Dùng chung router/system. |
| `SessionExpiredModal` | Thông báo phiên hết hạn và điều hướng đăng nhập lại. | SYS-04. | `open`, `onLoginAgain`. | Dùng chung auth/system. |
| `LoadingState` | Trạng thái đang tải dữ liệu. | Feed, search, admin tables, profile. | `variant`, `message`. | Dùng chung. |
| `EmptyState` | Trạng thái không có dữ liệu. | Feed Following rỗng, search rỗng, saved rỗng, admin table rỗng. | `title`, `description`, `action`. | Dùng chung. |
