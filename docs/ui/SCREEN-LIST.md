# Danh sách màn hình

| Mã | Màn hình | Route | Actor | Ưu tiên |
|---|---|---|---|---|
| UI-01 | Đăng nhập | `/login` | Khách | P0 |
| UI-02 | Đăng ký | `/register` | Khách | P0 |
| UI-03 | Feed For You | `/` | User | P0 |
| UI-04 | Feed Following | `/following` | User | P0 |
| UI-05 | Tạo bài | Modal hoặc `/posts/create` | User | P0 |
| UI-06 | Chi tiết bài | `/posts/:postId` | User | P0 |
| UI-07 | Hồ sơ | `/profile/:username` | User | P0 |
| UI-08 | Chỉnh sửa hồ sơ | `/profile/edit` | User | P0 |
| UI-09 | Follower | `/profile/:username/followers` | User | P0 |
| UI-10 | Following | `/profile/:username/following` | User | P0 |
| UI-11 | Tìm kiếm | `/search` | User | P1 |
| UI-12 | Bài đã lưu | `/saved` | User | P1 |
| UI-13 | Báo cáo bài | Modal | User | P1 |
| UI-14 | Admin User | `/admin/users` | Admin | P0/P1 |
| UI-15 | Admin Post | `/admin/posts` | Admin | P1 |
| UI-16 | Admin Report | `/admin/reports` | Admin | P1 |
| UI-17 | Chi tiết Report | `/admin/reports/:reportId` | Admin | P1 |
| UI-18 | Không có quyền | `/403` | Tất cả | P0 |
| UI-19 | Không tìm thấy | `*` | Tất cả | P0 |
