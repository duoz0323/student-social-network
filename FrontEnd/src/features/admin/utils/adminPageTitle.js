const ADMIN_ROUTE_TITLES = [
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

export function getAdminPageTitle(pathname) {
  // Route chi tiết được đặt trước route danh sách để tiêu đề phản ánh đúng màn hình hiện tại.
  return ADMIN_ROUTE_TITLES.find((route) => route.matches(pathname))?.title ?? 'Trang quản trị';
}
