# Danh sách component

`README.md` là nguồn sự thật cao nhất. Contract HTTP chi tiết duy nhất nằm tại `docs/data/API-CONTRACT.md`; component chỉ quản lý trình bày và trạng thái UI, không tự quyết định nghiệp vụ Auth.

Tài liệu này phân rã component dựa trên phần lặp lại thực tế trong ảnh Stitch và phạm vi MVP. Không tách component quá nhỏ cho các đoạn HTML chỉ xuất hiện một lần.

## 1. Layout component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `UserShell` | Khung giao diện người dùng gồm sidebar trái và vùng nội dung chính. | FEED-01, POST-01, POST-07, PROFILE-01, PROFILE-02, SEARCH-01. | `activeNav`, `currentUser`, `children`, `onCreatePost`. | Dùng chung. |
| `UserSidebar` | Hiển thị brand và điều hướng người dùng. | FEED-01, POST-01, POST-07, PROFILE-01, PROFILE-02, SEARCH-01. | `activeItem`, `currentUser`, callbacks điều hướng. `/profile/me` active Trang cá nhân; `/profile/:userId` không active Trang cá nhân. | Dùng chung. |
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
| `Pagination` | Điều hướng trang cho danh sách dạng trang. | Admin tables và search. | `page`, `totalPages`, `onChange`. | Không dùng cho các danh sách bài Infinite Scroll. |

## 3. Authentication component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `LoginForm` | Form đăng nhập bằng email và mật khẩu. | AUTH-01. | `email`, `password`, `errors`, `submitting`, `onSubmit`, `onForgotPassword` cho FUTURE_DEVELOPMENT. Không nhận username hoặc displayName. | Module auth. |
| `RegisterForm` | Khởi tạo hoặc phục hồi pending registration bằng email; không tự tạo user/session. | AUTH-02. | `initialValues` gồm `email`, `password`, `confirmPassword`, `acceptTerms`; `errors`, `submitting`, `onSubmit`. Không nhận username hoặc displayName. | Module auth. |
| `OtpVerificationForm` | Nhập OTP đăng ký hoặc link local, hiển thị cooldown, expiry và resend/recovery state. | AUTH-OTP-01, AUTH-METHOD-02. | `maskedIdentifier`, `code`, `resendAvailableAt`, `expiresAt`, `submitting`, `onVerify`, `onResend`, `onRecover`. | Module auth; nghiệp vụ từng purpose do service/contract quyết định. |
| `SocialAuthButtons` | Khởi động Google/Facebook Auth thật và chỉ chuyển provider credential cho Auth service. | AUTH-01, AUTH-02, AUTH-METHOD-01. | `mode`, `loadingProvider`, `onCredential(provider, credential)`. | Module auth; không lưu provider token hoặc dùng token này cho API nghiệp vụ. |
| `SocialConflictDialog` | Hiển thị đúng các lựa chọn xử lý conflict do Backend trả về. | AUTH-SOCIAL-01. | `conflictType`, `allowedActions`, `expiresAt`, `onResolve`. | Module auth; không tự suy luận merge theo email. |
| `AuthMethodList` | Hiển thị các phương thức đăng nhập và điều phối link/unlink. | AUTH-METHOD-01. | `methods`, `loading`, `onLink`, `onUnlink`. | Module security/auth; UI last-method guard chỉ hỗ trợ UX, Backend quyết định cuối. |
| `LinkLocalMethodForm` | Khởi tạo challenge link email riêng trước khi chuyển sang OTP. | AUTH-METHOD-02. | `methodType`, `email`, `submitting`, `onInitiate`. | Module security/auth; không dùng pending registration. |
| `ReauthenticationDialog` | Thu thập proof cho thao tác bảo mật nhạy cảm như unlink. | AUTH-REAUTH-01. | `availableMethods`, `selectedMethod`, `submitting`, `onReauthenticate`. | Module security/auth; token ngắn hạn không lưu localStorage. |
| `OnboardingProfilePage` | Page quản lý ba bước hoàn tất hồ sơ sau đăng ký. | AUTH-03, AUTH-04, AUTH-05. | State nội bộ gồm `displayName`, `avatarUrl`, `dateOfBirth`, `bio`; tên hiển thị và ngày sinh bắt buộc, ngày sinh phải cho thấy người dùng đủ 18 tuổi; avatar và bio có thể bỏ qua. | Module auth/profile. |
| `OnboardingProgress` | Chỉ báo bước onboarding 1/3, 2/3, 3/3 nếu tách riêng khi cần. | AUTH-03 đến AUTH-05. | `currentStep`, `totalSteps`. | Module auth, tùy chọn. |
| `OnboardingSuccessPage` | Màn hình xác nhận tài khoản và hồ sơ đã sẵn sàng sau khi `profileCompletedAt` được cập nhật. | AUTH-06. | Nút chính giữ phiên đăng nhập và điều hướng `/feed/for-you`. | Module auth/profile. |
| `PasswordResetCodeForm` | Nhập mã xác minh đặt lại mật khẩu. | AUTH-P2-02. | `email`, `codeLength`, `submitting`, `onSubmit`, `onResend`. | Module auth, đã tích hợp với Password Recovery API. |
| `SetPasswordForm` | Nhập mật khẩu mới và xác nhận. | AUTH-P2-03. | `errors`, `submitting`, `onSubmit`. | Module auth, đã tích hợp với Password Recovery API. |
| `RouteGuard` | Phân loại khách, user chưa hoàn tất hồ sơ và user đã hoàn tất hồ sơ. | Router Auth/Onboarding/User/Admin. | `currentUser.status`, `currentUser.profile.profileCompletedAt`, `role`. | Router. |
| `Toast` hoặc inline message | Hiển thị lỗi form và thông báo nghiệp vụ. | AUTH-01, AUTH-02, OTP, Social conflict, Onboarding. | `message`, `type`. | Có thể dùng inline trong MVP. |

`AuthFlowContext`/`useAuthFlow` có thể giữ flow token trong memory và đồng bộ có kiểm soát với `sessionStorage`; tuyệt đối không dùng `localStorage`, query parameter hoặc props xuyên nhiều tầng. Mọi request flow token dùng header thống nhất `X-Auth-Flow-Token`. Response chứa token phải được xử lý theo `Cache-Control: no-store`. Không đặt gọi API trực tiếp trong component trình bày.

## 4. Post component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `PostCard` | Hiển thị bài viết và Location tùy chọn trong feed, profile, saved, liked và search. | FEED-01, PROFILE-01/02, POST-07, LIKED-01, SEARCH-01 nếu có kết quả bài. | `post` gồm `location` object hoặc `null`, `currentUser`, `onLike`, `onComment`, `onSave`, `onOpenMenu`, `onOpenDetail`. | Dùng chung module post/feed/profile. |
| `InfinitePostList` | Tải nối tiếp PostCard bằng cursor và quản lý loading/empty/end state. | Feed, profile, saved, liked. | `loadPage(cursor)`, `initialItems`, `hasNext`, `onItemsChange`. | Dùng chung cho năm danh sách bài Cursor Pagination. |
| `PostAuthor` | Hiển thị tác giả bài viết bằng displayName và avatar. | FEED-01, POST-01, PROFILE-01/02, POST-07. | `authorId` hoặc `user`, `onOpenProfile`. Điều hướng bằng `/profile/:userId`. | Dùng chung module post/profile. |
| `PostDetail` | Hiển thị bài viết đầy đủ và khu vực bình luận. | POST-01. | `post`, `comments`, `currentUser`, action callbacks. | Module post. |
| `PostComposer` | Tạo bài viết mới với media, hashtag gợi ý và một Location tùy chọn. | FEED-01, POST-02. | `currentUser`, `draft`, `media`, `hashtag`, `location`, `submitting`, `onSubmit`. | Dùng chung `PostHashtagPicker`; đã tích hợp Location picker. |
| Form edit trong `PostCard` | Gọi Post Detail trước khi hiển thị; quản lý loading/error/submitting; sửa nội dung, media, hashtag và Location. | POST-03. | Post Detail snapshot, draft, `keepMediaIds`, `newMediaFiles`, Location, `onSave`, `onCancel`, `onRetry`. | Dùng chung `PostHashtagPicker`; cho giữ/gỡ/thêm media hợp lệ; Backend quyết định quyền và thời hạn. |
| `PostHashtagPicker` | Tìm hashtag có sẵn, hiển thị gợi ý bên dưới và cho chọn hoặc tạo hashtag mới. | POST-02, POST-03. | `value`, `disabled`, `onChange`. | Dùng chung cho tạo và sửa Post; debounce API gợi ý 250 ms. |
| `EditPostMedia` | Hiển thị media hiện tại và cho giữ/gỡ/thêm ảnh hoặc video khi sửa Post. | POST-03. | `media`, `disabled`, `onChange`, `onBusyChange`. | Gửi `keepMediaIds` và `newMediaFiles`; validation Frontend chỉ hỗ trợ UX. |
| `PostActionMenu` | Menu hành động theo quyền; hành động sửa hiển thị countdown 15 phút và tự ẩn khi hết hạn. | POST-04, PostCard. | `post`, `isOwner`, `editRemainingSeconds`, `onEdit`, `onDelete`, `onReport`, `onSave`, `onCopyLink`. | Frontend hỗ trợ UX; Backend vẫn kiểm tra deadline bằng UTC. |
| `DeletePostDialog` | Xác nhận xóa mềm bài viết. | POST-05. | `post`, `submitting`, `onConfirm`, `onCancel`. | Module post. |
| `ReportPostFlow` | Gom các bước chọn lý do, nhập mô tả và gửi báo cáo. | POST-08, POST-09, POST-10. | `post`, `reasons`, `selectedReason`, `description`, `submitting`, `onSubmit`. | Module report/post. |
| `CommentList` | Hiển thị danh sách bình luận và thao tác xóa của chủ bình luận. | POST-01. | `comments`, `currentUser`, `onDeleteComment`. | Module post. |
| `CommentComposer` | Nhập và gửi bình luận mới. | POST-01. | `value`, `submitting`, `onSubmit`. | Module post. |

## 5. Profile component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `ProfileHeader` | Hiển thị thông tin hồ sơ, avatar, thống kê và nút hành động. | PROFILE-01, PROFILE-02. | `userId` hoặc `profile`, `isSelf`, `isFollowing`, `onFollowToggle`, `onEditProfile`. | Module profile. |
| `EditProfileModal` | Cập nhật avatar, tên hiển thị, bio và ngày sinh bắt buộc. | PROFILE-03. | `profile`, `errors`, `submitting`, `onSave`. Không nhận username; không cho xóa ngày sinh hoặc lưu khi người dùng chưa đủ 18 tuổi. | Module profile. |
| `ProfileTabs` | Chuyển nhóm nội dung trong hồ sơ. | PROFILE-01, PROFILE-02. | `activeTab`, `tabs`, `onChange`. | Module profile. |
| `FollowListModal` | Danh sách follower/following có thao tác theo dõi. | PROFILE-04. | `type`, `users`, `pagination`, `currentUser`, `onFollowToggle`, `onOpenProfile(userId)`. | Module follow/profile. |
| `FollowButton` | Theo dõi hoặc bỏ theo dõi. | PROFILE-02, PROFILE-04, SEARCH-01. | `isFollowing`, `loading`, `onClick`. | Dùng chung profile/search. |
| `UserListItem` | Hiển thị một người dùng trong danh sách follow/search/gợi ý. | PROFILE-04, SEARCH-01. | `userId` hoặc `user`, `onOpenProfile(userId)`, `onFollowToggle`. | Dùng chung profile/search. |

## 6. Search component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `SearchBox` | Nhập từ khóa tìm kiếm. | SEARCH-01, admin list nếu dùng chung style. | `value`, `placeholder`, `onChange`, `onSubmit`. | Dùng chung có biến thể. |
| `PopularSearchList` | Hiển thị từ khóa/hashtag phổ biến. | SEARCH-01. | `items`, `onSelect`. | Module search. |
| `SuggestedUserList` | Gợi ý người dùng để theo dõi. | SEARCH-01, có thể dùng ở Feed nếu có panel gợi ý. | `users`, `onFollowToggle`, `onOpenProfile(userId)`. | Module search/follow. |
| `SearchResultItem` | Hiển thị kết quả user bằng displayName hoặc bài viết/hashtag. | SEARCH-01. | `userId` hoặc `user`, `post`, `hashtag`, `onOpenProfile(userId)`, `onOpenPost`. | Module search. |
| `SearchResults` | Hiển thị kết quả user/post/hashtag khi có truy vấn. | SEARCH-01. | `query`, `users`, `posts`, `hashtags`, `loading`, `pagination`. User tìm theo displayName và điều hướng bằng userId. | Module search. |
| `MentionSuggestionItem` | Gợi ý mention theo displayName khi phát triển sau MVP. | FUTURE_DEVELOPMENT. | `userId` hoặc `user`, `onSelect(mentionedUserId)`. Hiển thị avatar, displayName, bio ngắn nếu cần. | FUTURE_DEVELOPMENT, chưa dùng trong MVP. |

## 7. Admin component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `AdminSummaryCards` | Hiển thị chỉ số tổng quan đơn giản. | ADMIN-01. | `totalUsers`, `totalPosts`, `pendingReports`, `blockedUsers`. | Module admin, CẦN XÁC NHẬN vì dashboard nâng cao ngoài MVP. |
| `AdminUserTable` | Danh sách user không chứa nút Khóa/Mở khóa trên từng dòng. | ADMIN-02. | `users`, `pagination`, `loading`, `onSearch`, `onOpenDetail`. | Click dòng mở chi tiết; thay đổi trạng thái chỉ thực hiện trong chi tiết. |
| `AdminUserAnalytics` | Biểu đồ vòng trạng thái ACTIVE/BLOCKED và biểu đồ số người dùng mới từng ngày trong tuần hiện tại. | ADMIN-02. | Dữ liệu lấy từ `useAdminUserStatistics`; `refreshKey` làm mới sau Khóa/Mở khóa. | Cột rộng 16rem từ breakpoint 2XL và không làm co bảng dữ liệu. |
| `AdminUserActionMenu` | Menu thao tác user. | ADMIN-03. | `user`, `onBlock`, `onUnblock`, `onView`. | Module admin. |
| `AdminPostTable` | Danh sách bài viết và trạng thái, không chứa nút Ẩn/Khôi phục. | ADMIN-04. | `posts`, `pagination`, `loading`, `onOpenDetail`. | Double-click hoặc Enter/Space trên dòng để mở chi tiết Admin; thay đổi trạng thái chỉ thực hiện ở trang chi tiết. |
| `AdminPostAnalytics` | Hai thẻ biểu đồ gọn theo kích thước nội dung: vòng tổng bài/số bài đã ẩn và cột số bài từng ngày trong tuần hiện tại. | ADMIN-04. | Dữ liệu lấy từ `useAdminPostStatistics`. | Cột rộng 16rem từ breakpoint 2XL; bố cục mở rộng và dịch trái để giữ nguyên chiều rộng bảng dữ liệu. |
| `AdminReportTable` | Danh sách một dòng mỗi Moderation Case, không hiển thị lý do. | ADMIN-05. | `moderationCases`, `filters`, `pagination`, `loading`, `onOpenDetail`. | Module admin; dữ liệu đã aggregate từ Backend và vùng bảng tự cuộn trong viewport. |
| `ReportDetailPanel` | Chi tiết case, bài và danh sách Report rút gọn không render snapshot/media. | ADMIN-06. | `moderationCase`, `post`, `onResolveNoViolation`, `onResolveAction`. | Module admin/report; không có trường kết luận hoặc bước tiếp nhận. |
| `AdminStatusBadge` | Badge trạng thái user, post, report. | ADMIN-01 đến ADMIN-06. | `type`, `status`. | Dùng chung admin. |

## 8. System state component

| Component | Trách nhiệm | Màn hình sử dụng | Dữ liệu/props dự kiến | Phạm vi |
|---|---|---|---|---|
| `ErrorPage` | Trang lỗi 403, 404, 500. | SYS-01, SYS-02, SYS-03. | `code`, `title`, `description`, `primaryAction`. | Dùng chung router/system. |
| `SessionExpiredModal` | Thông báo phiên hết hạn và điều hướng đăng nhập lại. | SYS-04. | `open`, `onLoginAgain`. | Dùng chung auth/system. |
| `LoadingState` | Trạng thái đang tải dữ liệu. | Feed, search, admin tables, profile. | `variant`, `message`. | Dùng chung. |
| `EmptyState` | Trạng thái không có dữ liệu. | Feed Following rỗng, search rỗng, saved/liked rỗng, admin table rỗng. | `title`, `description`, `action`. | Dùng chung. |

