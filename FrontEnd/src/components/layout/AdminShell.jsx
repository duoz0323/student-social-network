import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, FileText, Flag, History, LogOut } from 'lucide-react';
import logo from '../../assets/brand/logo.png';
import Button from '../common/Button.jsx';
import { useApp } from '../../contexts/AppContext.jsx';
import AdminToastProvider from '../../features/admin/components/AdminToastProvider.jsx';

export default function AdminShell() {
  const { currentUser } = useApp();
  const navigate = useNavigate();
  const items = [
    { to: '/admin', label: 'Tổng quan', icon: LayoutDashboard, end: true },
    { to: '/admin/users', label: 'Người dùng', icon: Users },
    { to: '/admin/posts', label: 'Bài viết', icon: FileText },
    { to: '/admin/reports', label: 'Báo cáo', icon: Flag },
    { to: '/admin/actions', label: 'Lịch sử', icon: History },
  ];

  return (
    <div className="admin-theme min-h-screen bg-white text-zinc-950 flex">
      {/* Sidebar */}
      <aside className="fixed left-0 top-0 h-screen w-72 border-r border-zinc-200 bg-zinc-50 flex flex-col z-10">
        {/* Header */}
        <div className="flex items-center gap-3 px-6 py-8 border-b border-zinc-200">
          <img src={logo} alt="UniShare" className="h-10 w-10 rounded-xl object-contain grayscale" />
          <div>
            <p className="text-xl font-semibold brand-text text-zinc-900">UniShare</p>
            <p className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mt-0.5">Admin</p>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
          {items.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                end={item.end}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-4 py-3 font-medium transition-colors duration-150 ${
                    isActive
                      ? 'bg-zinc-200 text-zinc-950 shadow-sm'
                      : 'text-zinc-600 hover:bg-zinc-200/50 hover:text-zinc-950'
                  }`
                }
              >
                <Icon size={18} strokeWidth={2} className="flex-shrink-0" />
                {item.label}
              </NavLink>
            );
          })}
        </nav>

        {/* Footer Actions */}
        <div className="p-6 border-t border-zinc-200 bg-zinc-50 mt-auto">
          <div className="flex items-center gap-3 mb-6 px-2">
            <div className="h-10 w-10 rounded-full bg-zinc-200 text-zinc-700 flex items-center justify-center font-bold border border-zinc-300">
              {currentUser?.displayName?.charAt(0).toUpperCase()}
            </div>
            <div className="flex-1 overflow-hidden">
              <p className="text-sm font-semibold text-zinc-950 truncate">{currentUser?.displayName}</p>
              <p className="text-xs text-zinc-500 truncate">{currentUser?.email || 'System Admin'}</p>
            </div>
          </div>
          <Button className="w-full justify-center gap-2 border-zinc-300 hover:bg-zinc-200 text-zinc-800" variant="secondary" onClick={() => navigate('/feed/for-you')}>
            <LogOut size={16} /> Thoát quản trị
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ml-72 flex-1 min-h-screen bg-white p-8 lg:p-12">
        <AdminToastProvider>
          <div className="mx-auto max-w-5xl">
            <Outlet />
          </div>
        </AdminToastProvider>
      </main>
    </div>
  );
}
