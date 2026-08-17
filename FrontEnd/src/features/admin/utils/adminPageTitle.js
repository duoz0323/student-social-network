const ADMIN_ROUTE_TITLES = [
  { matches: (pathname) => pathname === '/admin/notifications', title: 'Thông báo quản trị' },
  { matches: (pathname) => pathname === '/admin/collaborator/profile', title: 'Hồ sơ cộng tác viên' },
  { matches: (pathname) => pathname === '/admin/collaborator/explore', title: 'Khám phá nội dung' },
  { matches: (pathname) => pathname === '/admin/collaborator/moderation-suggestions', title: 'Đề xuất của tôi' },
  { matches: (pathname) => pathname.startsWith('/admin/moderation-suggestions'), title: 'Đề xuất kiểm duyệt' },
  { matches: (pathname) => pathname.startsWith('/admin/profile-reports/'), title: 'Chi tiết báo cáo hồ sơ' },
  { matches: (pathname) => pathname.startsWith('/admin/reports/'), title: 'Chi tiết báo cáo' },
  { matches: (pathname) => pathname.startsWith('/admin/posts/'), title: 'Chi tiết bài viết' },
  { matches: (pathname) => pathname === '/admin/user-analytics', title: 'Thống kê người dùng' },
  { matches: (pathname) => pathname === '/admin/users', title: 'Quản lý người dùng' },
  { matches: (pathname) => pathname === '/admin/posts', title: 'Quản lý bài viết' },
  { matches: (pathname) => pathname === '/admin/hashtags', title: 'Quản lý hashtag' },
  { matches: (pathname) => pathname === '/admin/academic', title: 'Dữ liệu học thuật' },
  { matches: (pathname) => pathname === '/admin/reports', title: 'Quản lý báo cáo' },
  { matches: (pathname) => pathname === '/admin/actions', title: 'Lịch sử quản trị' },
  { matches: (pathname) => pathname === '/admin', title: 'Tổng quan quản trị' },
];

const PAGE_OWNED_HEADER_ROUTES = [
  (pathname) => pathname === '/admin',
  (pathname) => pathname === '/admin/notifications',
  (pathname) => pathname === '/admin/academic',
  (pathname) => pathname === '/admin/hashtags',
  (pathname) => pathname === '/admin/post-analytics',
  (pathname) => pathname === '/admin/hashtag-analytics',
  (pathname) => pathname === '/admin/admins',
  (pathname) => pathname === '/admin/permissions',
  (pathname) => pathname === '/admin/actions',
  (pathname) => pathname.startsWith('/admin/moderation-suggestions'),
  (pathname) => pathname.startsWith('/admin/profile-reports/'),
  (pathname) => pathname.startsWith('/admin/posts/'),
  (pathname) => pathname.startsWith('/admin/reports/'),
  (pathname) => pathname.startsWith('/admin/collaborator'),
];

export function getAdminPageTitle(pathname) {
  // Route chi tiết được đặt trước route danh sách để tiêu đề phản ánh đúng màn hình hiện tại.
  return ADMIN_ROUTE_TITLES.find((route) => route.matches(pathname))?.title ?? 'Trang quản trị';
}

/**
 * Cho biết trang con đã tự hiển thị tiêu đề nội dung hay chưa để AdminShell
 * không dựng thêm một tiêu đề giống hệt ở phía trên.
 */
export function hasPageOwnedAdminHeader(pathname) {
  return PAGE_OWNED_HEADER_ROUTES.some((matches) => matches(pathname));
}
