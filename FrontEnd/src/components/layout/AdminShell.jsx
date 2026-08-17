import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { BarChart3, ChevronDown, ChevronLeft, ChevronRight, Compass, GraduationCap, Hash, LayoutDashboard, Users, FileText, Flag, History, LogOut, ShieldAlert, ShieldCheck, SlidersHorizontal, UserCircle } from 'lucide-react';
import Button from '../common/Button.jsx';
import { useApp } from '../../contexts/AppContext.jsx';
import useThemeLogo from '../../hooks/useThemeLogo.js';
import AdminToastProvider from '../../features/admin/components/AdminToastProvider.jsx';
import { getAdminPageTitle, hasPageOwnedAdminHeader } from '../../features/admin/utils/adminPageTitle.js';
import { useAuth } from '../../features/auth/hooks/useAuth.js';
import { ADMIN_PERMISSIONS, getAdminNavigationScopes, getPrimaryAdminRoleLabel } from '../../features/admin/constants/adminRbac.js';
import AdminNotificationBell from '../../features/admin/notifications/AdminNotificationBell.jsx';
import Avatar from '../common/Avatar.jsx';

export default function AdminShell() {
  const { currentUser, logout } = useApp();
  // Logo quản trị đổi cùng color scheme để nhận diện luôn rõ trên nền sidebar.
  const logo = useThemeLogo();
  const auth = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const pageOwnsHeader = hasPageOwnedAdminHeader(location.pathname);
  // Màn hình nhỏ khởi tạo sidebar ở trạng thái thu gọn để không che khu vực nội dung chính.
  const [isCollapsed, setIsCollapsed] = useState(() => (
    typeof window !== 'undefined' && window.matchMedia('(max-width: 767px)').matches
  ));
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const analyticsPaths = ['/admin/user-analytics', '/admin/post-analytics', '/admin/hashtag-analytics'];
  const isAnalyticsRoute = analyticsPaths.includes(location.pathname);
  const [isAnalyticsOpen, setIsAnalyticsOpen] = useState(isAnalyticsRoute);
  const collaboratorItems = [
    { to: '/admin/collaborator', label: 'Dashboard', icon: LayoutDashboard, end: true, permission: ADMIN_PERMISSIONS.COLLABORATOR_DASHBOARD_VIEW },
    { to: '/admin/collaborator/profile', label: 'Hồ sơ cộng tác viên', icon: UserCircle, permission: ADMIN_PERMISSIONS.COLLABORATOR_DASHBOARD_VIEW },
    { to: '/admin/collaborator/posts', label: 'Nội dung của tôi', icon: FileText, permission: ADMIN_PERMISSIONS.COLLABORATOR_POST_VIEW_OWN },
    { to: '/admin/collaborator/hashtags', label: 'Hashtag', icon: Hash, permission: ADMIN_PERMISSIONS.COLLABORATOR_HASHTAG_VIEW },
    { to: '/admin/collaborator/explore', label: 'Khám phá nội dung', icon: Compass, permission: ADMIN_PERMISSIONS.COLLABORATOR_EXPLORE_VIEW },
    { to: '/admin/collaborator/moderation-suggestions', label: 'Đề xuất của tôi', icon: ShieldAlert, permission: ADMIN_PERMISSIONS.COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN },
  ];
  const regularItems = [
    { to: '/admin/profile', label: 'Hồ sơ của tôi', icon: UserCircle },
    { to: '/admin', label: 'Tổng quan', icon: LayoutDashboard, end: true, permission: ADMIN_PERMISSIONS.DASHBOARD_BASIC_VIEW },
    { to: '/admin/users', label: 'Người dùng', icon: Users, permission: ADMIN_PERMISSIONS.USER_VIEW },
    { to: '/admin/posts', label: 'Bài viết', icon: FileText, permission: ADMIN_PERMISSIONS.POST_VIEW },
    { to: '/admin/hashtags', label: 'Hashtag', icon: Hash, permission: ADMIN_PERMISSIONS.HASHTAG_VIEW },
    { to: '/admin/academic', label: 'Dữ liệu học thuật', icon: GraduationCap },
    { to: '/admin/reports', label: 'Báo cáo', icon: Flag, permission: ADMIN_PERMISSIONS.REPORT_VIEW },
    { to: '/admin/moderation-suggestions', label: 'Đề xuất kiểm duyệt', icon: ShieldAlert, permission: ADMIN_PERMISSIONS.MODERATION_SUGGESTION_VIEW },
    {
      label: 'Thống kê',
      icon: BarChart3,
      children: [
        { to: '/admin/user-analytics', label: 'Người dùng', permission: ADMIN_PERMISSIONS.USER_ANALYTICS_VIEW },
        { to: '/admin/post-analytics', label: 'Bài viết', permission: ADMIN_PERMISSIONS.POST_VIEW },
        { to: '/admin/hashtag-analytics', label: 'Hashtag', permission: ADMIN_PERMISSIONS.HASHTAG_VIEW },
      ],
    },
    { to: '/admin/admins', label: 'Quản trị viên', icon: ShieldCheck, permission: ADMIN_PERMISSIONS.ADMIN_VIEW },
    { to: '/admin/permissions', label: 'Phân quyền', icon: SlidersHorizontal, role: 'SUPER_ADMIN' },
    { to: '/admin/actions', label: 'Lịch sử', icon: History, role: 'SUPER_ADMIN' },
  ];
  const navigationScopes = getAdminNavigationScopes(auth.adminRoles);
  const candidateItems = [
    ...(navigationScopes.showRegularAdmin ? regularItems : []),
    ...(navigationScopes.showCollaborator ? collaboratorItems : []),
  ];
  const canAccessItem = (item) => !item.permission && !item.role
    ? true
    : (item.permission ? auth.hasPermission(item.permission) : auth.hasAdminRole(item.role));
  const items = candidateItems
    .map((item) => item.children
      ? { ...item, children: item.children.filter(canAccessItem) }
      : item)
    .filter((item) => item.children ? item.children.length > 0 : canAccessItem(item));

  useEffect(() => {
    // Đồng bộ tiêu đề tab theo route Admin, tránh giữ lại tiêu đề của màn hình đăng nhập trước đó.
    document.title = `${getAdminPageTitle(location.pathname)} • UniShare`;
  }, [location.pathname]);

  async function handleLogout() {
    if (isLoggingOut) return;

    setIsLoggingOut(true);
    // Dùng luồng logout chung để thu hồi Refresh Token và xóa toàn bộ phiên cục bộ trước khi rời app.
    await logout();
    navigate('/login', { replace: true });
  }

  function handleNavigation() {
    // Sau khi chọn mục trên mobile, đóng drawer để người dùng nhìn thấy ngay nội dung vừa điều hướng.
    if (typeof window !== 'undefined' && window.innerWidth < 768) setIsCollapsed(true);
  }

  return (
    <div className="admin-theme flex min-h-screen overflow-x-hidden bg-white text-zinc-950">
      {!isCollapsed ? (
        <button
          type="button"
          aria-label="Đóng thanh chức năng"
          onClick={() => setIsCollapsed(true)}
          className="fixed inset-0 z-20 bg-black/20 md:hidden"
        />
      ) : null}

      {/* Sidebar */}
      <aside
        className={`fixed left-0 top-0 z-30 flex h-dvh flex-col border-r border-zinc-200 bg-zinc-50 transition-[width] duration-200 ${
          isCollapsed ? 'w-16 md:w-20' : 'w-72'
        }`}
      >
        <button
          type="button"
          aria-label={isCollapsed ? 'Mở rộng thanh chức năng' : 'Thu gọn thanh chức năng'}
          title={isCollapsed ? 'Mở rộng thanh chức năng' : 'Thu gọn thanh chức năng'}
          aria-expanded={!isCollapsed}
          onClick={() => setIsCollapsed((collapsed) => !collapsed)}
          className="absolute -right-4 top-8 z-20 flex h-8 w-8 items-center justify-center rounded-full border border-zinc-300 bg-white text-zinc-600 shadow-sm transition hover:bg-zinc-100 hover:text-zinc-950 focus:outline-none focus:ring-2 focus:ring-zinc-500 focus:ring-offset-2"
        >
          {isCollapsed ? <ChevronRight size={17} /> : <ChevronLeft size={17} />}
        </button>

        {/* Header */}
        <div className={`flex items-center border-b border-zinc-200 py-8 ${isCollapsed ? 'justify-center px-3' : 'gap-3 px-6'}`}>
          <img src={logo} alt="UniShare" className="h-10 w-10 rounded-xl object-cover shadow-sm" />
          <div className={isCollapsed ? 'sr-only' : ''}>
            <p className="text-xl font-semibold brand-text text-zinc-900">UniShare</p>
            <p className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mt-0.5">Admin</p>
          </div>
        </div>

        {/* Navigation */}
        <nav className={`flex-1 space-y-1 overflow-y-auto py-6 ${isCollapsed ? 'px-3' : 'px-4'}`} aria-label="Chức năng quản trị">
          {items.map((item) => {
            const Icon = item.icon;
            if (item.children) {
              return (
                <div key={item.label}>
                  <button
                    type="button"
                    aria-expanded={isAnalyticsOpen}
                    aria-controls="admin-analytics-navigation"
                    title={isCollapsed ? item.label : undefined}
                    onClick={() => {
                      if (isCollapsed) setIsCollapsed(false);
                      setIsAnalyticsOpen((open) => !open);
                    }}
                    className={`flex w-full items-center rounded-xl py-3 font-medium transition-colors duration-150 ${
                      isCollapsed ? 'justify-center px-3' : 'gap-3 px-4'
                    } ${isAnalyticsRoute ? 'bg-zinc-200 text-zinc-950 shadow-sm' : 'text-zinc-600 hover:bg-zinc-200/50 hover:text-zinc-950'}`}
                  >
                    <Icon size={18} strokeWidth={2} className="shrink-0" />
                    <span className={isCollapsed ? 'sr-only' : 'flex-1 text-left'}>{item.label}</span>
                    {!isCollapsed ? <ChevronDown size={16} className={`transition-transform ${isAnalyticsOpen ? 'rotate-180' : ''}`} /> : null}
                  </button>
                  {!isCollapsed && isAnalyticsOpen ? (
                    <div id="admin-analytics-navigation" className="mt-1 space-y-1 pl-7">
                      {item.children.map((child) => (
                        <NavLink
                          key={child.to}
                          to={child.to}
                          className={({ isActive }) => `block rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                            isActive ? 'bg-zinc-200 text-zinc-950' : 'text-zinc-500 hover:bg-zinc-100 hover:text-zinc-900'
                          }`}
                        >
                          {child.label}
                        </NavLink>
                      ))}
                    </div>
                  ) : null}
                </div>
              );
            }
            return (
              <NavLink
                key={item.to}
                end={item.end}
                to={item.to}
                onClick={handleNavigation}
                className={({ isActive }) =>
                  `flex items-center rounded-xl py-3 font-medium transition-colors duration-150 ${
                    isCollapsed ? 'justify-center px-3' : 'gap-3 px-4'
                  } ${
                    isActive
                      ? 'bg-zinc-200 text-zinc-950 shadow-sm'
                      : 'text-zinc-600 hover:bg-zinc-200/50 hover:text-zinc-950'
                  }`
                }
                title={isCollapsed ? item.label : undefined}
              >
                <Icon size={18} strokeWidth={2} className="flex-shrink-0" />
                <span className={isCollapsed ? 'sr-only' : ''}>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Footer Actions */}
        <div className={`mt-auto border-t border-zinc-200 bg-zinc-50 ${isCollapsed ? 'p-3' : 'p-6'}`}>
          <div className={`flex items-center ${isCollapsed ? 'mb-4 justify-center' : 'mb-6 gap-3 px-2'}`}>
            <Avatar
              src={currentUser?.avatarUrl}
              name={currentUser?.displayName}
              size="sm"
              className="!h-10 !w-10 shrink-0 border border-zinc-300"
            />
            <div className={isCollapsed ? 'sr-only' : 'flex-1 overflow-hidden'}>
              <p className="text-sm font-semibold text-zinc-950 truncate">{currentUser?.displayName}</p>
              <p className="truncate text-xs text-zinc-500">{getPrimaryAdminRoleLabel(auth.adminRoles)}</p>
            </div>
          </div>
          {isCollapsed ? (
            <button
              type="button"
              aria-label={isLoggingOut ? 'Đang đăng xuất' : 'Đăng xuất'}
              title={isLoggingOut ? 'Đang đăng xuất' : 'Đăng xuất'}
              onClick={handleLogout}
              disabled={isLoggingOut}
              className="mx-auto flex h-10 w-10 items-center justify-center rounded-full border border-zinc-300 bg-white text-zinc-800 shadow-sm transition hover:bg-zinc-200 focus:outline-none focus:ring-2 focus:ring-zinc-500 focus:ring-offset-2"
            >
              <LogOut size={16} />
            </button>
          ) : (
            <Button className="w-full justify-center gap-2 border-zinc-300 hover:bg-zinc-200 text-zinc-800" variant="secondary" onClick={handleLogout} disabled={isLoggingOut}>
              <LogOut size={16} /> {isLoggingOut ? 'Đang đăng xuất...' : 'Đăng xuất'}
            </Button>
          )}
        </div>
      </aside>

      {/* Main Content */}
      <main className={`min-h-screen min-w-0 flex-1 overflow-x-hidden bg-white p-4 transition-[margin,padding] duration-200 sm:p-6 lg:p-8 2xl:p-12 ${
        isCollapsed ? 'ml-16 md:ml-20' : 'ml-16 md:ml-72'
      }`}>
        <AdminToastProvider>
          <div className="admin-content-container relative mx-auto w-full max-w-[100rem]">
            {pageOwnsHeader ? (
              <div className="absolute right-0 top-0 z-10">
                <AdminNotificationBell />
              </div>
            ) : (
              <header className="mb-6 flex min-h-11 items-center justify-between gap-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wider text-zinc-400">Khu vực quản trị</p>
                  <h1 className="text-2xl font-bold text-zinc-950">{getAdminPageTitle(location.pathname)}</h1>
                </div>
                <AdminNotificationBell />
              </header>
            )}
            {/* Header riêng chừa đúng một khoảng cho chuông, nhờ đó tiêu đề và chuông nằm cùng hàng. */}
            <div className={pageOwnsHeader ? '[&>section>header:first-child]:pr-14 sm:[&>section>header:first-child]:pr-16' : ''}>
              <Outlet />
            </div>
          </div>
        </AdminToastProvider>
      </main>
    </div>
  );
}
