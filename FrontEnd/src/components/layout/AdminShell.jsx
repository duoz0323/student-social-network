import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { BarChart3, ChevronLeft, ChevronRight, LayoutDashboard, Users, FileText, Flag, History, LogOut } from 'lucide-react';
import logo from '../../assets/brand/logo-light.jpg';
import Button from '../common/Button.jsx';
import { useApp } from '../../contexts/AppContext.jsx';
import AdminToastProvider from '../../features/admin/components/AdminToastProvider.jsx';

export default function AdminShell() {
  const { currentUser } = useApp();
  const navigate = useNavigate();
  // Giữ trạng thái thu gọn tại layout để không bị đặt lại khi chuyển giữa các trang quản trị.
  const [isCollapsed, setIsCollapsed] = useState(false);
  const items = [
    { to: '/admin', label: 'Tổng quan', icon: LayoutDashboard, end: true },
    { to: '/admin/users', label: 'Người dùng', icon: Users },
    { to: '/admin/user-analytics', label: 'Thống kê người dùng', icon: BarChart3 },
    { to: '/admin/posts', label: 'Bài viết', icon: FileText },
    { to: '/admin/reports', label: 'Báo cáo', icon: Flag },
    { to: '/admin/actions', label: 'Lịch sử', icon: History },
  ];

  return (
    <div className="admin-theme min-h-screen bg-white text-zinc-950 flex">
      {/* Sidebar */}
      <aside
        className={`fixed left-0 top-0 z-10 flex h-screen flex-col border-r border-zinc-200 bg-zinc-50 transition-[width] duration-200 ${
          isCollapsed ? 'w-20' : 'w-72'
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
            return (
              <NavLink
                key={item.to}
                end={item.end}
                to={item.to}
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
            <div className="h-10 w-10 rounded-full bg-zinc-200 text-zinc-700 flex items-center justify-center font-bold border border-zinc-300">
              {currentUser?.displayName?.charAt(0).toUpperCase()}
            </div>
            <div className={isCollapsed ? 'sr-only' : 'flex-1 overflow-hidden'}>
              <p className="text-sm font-semibold text-zinc-950 truncate">{currentUser?.displayName}</p>
              <p className="text-xs text-zinc-500 truncate">{currentUser?.email || 'System Admin '}</p>
            </div>
          </div>
          {isCollapsed ? (
            <button
              type="button"
              aria-label="Thoát quản trị"
              title="Thoát quản trị"
              onClick={() => navigate('/feed/for-you')}
              className="mx-auto flex h-10 w-10 items-center justify-center rounded-full border border-zinc-300 bg-white text-zinc-800 shadow-sm transition hover:bg-zinc-200 focus:outline-none focus:ring-2 focus:ring-zinc-500 focus:ring-offset-2"
            >
              <LogOut size={16} />
            </button>
          ) : (
            <Button className="w-full justify-center gap-2 border-zinc-300 hover:bg-zinc-200 text-zinc-800" variant="secondary" onClick={() => navigate('/feed/for-you')}>
              <LogOut size={16} /> Thoát quản trị
            </Button>
          )}
        </div>
      </aside>

      {/* Main Content */}
      <main className={`min-h-screen flex-1 bg-white p-8 transition-[margin] duration-200 lg:p-12 ${isCollapsed ? 'ml-20' : 'ml-72'}`}>
        <AdminToastProvider>
          <div className="mx-auto max-w-5xl">
            <Outlet />
          </div>
        </AdminToastProvider>
      </main>
    </div>
  );
}
