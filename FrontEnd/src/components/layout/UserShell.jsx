import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import logo from '../../assets/brand/logo.png';
import Button from '../common/Button.jsx';
import Modal from '../common/Modal.jsx';
import PostComposer from '../../features/post/components/PostComposer.jsx';
import MoreMenu from './MoreMenu.jsx';
import { useState } from 'react';
import { useApp } from '../../contexts/AppContext.jsx';

// SVG Icons cho Sidebar
function HomeIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 3l9 7v11a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V10l9-7z" />
    </svg>
  );
}

function CreateIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <line x1="12" x2="12" y1="8" y2="16" />
      <line x1="8" x2="16" y1="12" y2="12" />
    </svg>
  );
}

function SearchIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8" />
      <line x1="21" x2="16.65" y1="21" y2="16.65" />
    </svg>
  );
}

function ActivityIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
  );
}

function ProfileIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  );
}

function BookmarkIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
    </svg>
  );
}

function MoreIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="4" x2="20" y1="9" y2="9" />
      <line x1="4" x2="20" y1="15" y2="15" />
    </svg>
  );
}

export default function UserShell() {
  const { logout, sessionExpired, setSessionExpired } = useApp();
  const [composerMode, setComposerMode] = useState(null);
  const [moreMenuOpen, setMoreMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const navItems = [
    { to: '/feed/for-you', label: 'Dành cho bạn', icon: <HomeIcon />, active: location.pathname.startsWith('/feed') },
    { to: '#create', label: 'Tạo bài viết', icon: <CreateIcon />, active: false, action: () => setComposerMode('modal') },
    { to: '/search', label: 'Tìm kiếm', icon: <SearchIcon />, active: location.pathname === '/search' },
    { to: '#activity', label: 'Hoạt động', icon: <ActivityIcon />, active: false }, // MVP không có trang hoạt động riêng
    { to: '/profile/me', label: 'Trang cá nhân', icon: <ProfileIcon />, active: location.pathname === '/profile/me' },
    { to: '/saved', label: 'Bài viết đã lưu', icon: <BookmarkIcon />, active: location.pathname === '/saved' },
  ];

  return (
    <div className="min-h-screen bg-[var(--app-bg)] text-[var(--app-text)] lg:grid lg:grid-cols-[var(--sidebar-width)_minmax(0,1fr)]">
      {/* 
        Sửa màu nền sidebar trùng với nền web (xóa bg-white và border), 
        thay đổi cách hiển thị chữ in đậm cho tab đang active.
      */}
      <aside className="fixed left-0 top-0 z-30 hidden h-screen w-[var(--sidebar-width)] bg-[var(--app-bg)] px-4 py-5 lg:flex lg:flex-col">
        {/* Logo */}
        <Link to="/feed/for-you" className="flex items-center gap-3 px-4 mb-8">
          <img src={logo} alt="UniShare" className="h-9 w-9 object-contain" />
          <span className="text-[22px] font-extrabold tracking-tight text-[var(--app-text)]">UniShare</span>
        </Link>
        
        {/* Navigation Items */}
        <nav className="grid gap-1 mt-2">
          {navItems.map((item) => {
            const buttonClass = `flex min-h-[52px] items-center gap-4 rounded-[12px] px-4 transition-colors hover:bg-black/5 ${
              item.active ? 'bg-black/5 font-bold text-[var(--app-text)]' : 'font-normal text-[var(--app-text)]'
            }`;

            if (item.action) {
              return (
                <button key={item.label} onClick={item.action} className={buttonClass}>
                  <span className="flex w-6 justify-center text-[var(--app-text)]">{item.icon}</span>
                  <span className="text-[15px]">{item.label}</span>
                </button>
              );
            }
            return (
              <NavLink key={item.to} to={item.to} className={buttonClass}>
                <span className="flex w-6 justify-center text-[var(--app-text)]">{item.icon}</span>
                <span className="text-[15px]">{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Nút Xem thêm & Menu ở góc dưới trái */}
        <div className="relative mt-auto">
          <MoreMenu open={moreMenuOpen} onClose={() => setMoreMenuOpen(false)} onLogout={logout} />
          <button
            onClick={() => setMoreMenuOpen(!moreMenuOpen)}
            className={`flex min-h-[52px] w-full items-center gap-4 rounded-[12px] px-4 font-normal text-[var(--app-text)] transition-colors hover:bg-black/5 ${
              moreMenuOpen ? 'bg-black/5' : ''
            }`}
          >
            <span className="flex w-6 justify-center"><MoreIcon /></span>
            <span className="text-[15px]">Xem thêm</span>
          </button>
        </div>
      </aside>

      {/* Mobile header */}
      <header className="sticky top-0 z-20 flex h-[var(--header-height)] items-center justify-between border-b border-[var(--app-border)] bg-white px-4 lg:hidden">
        <Link to="/feed/for-you" className="flex items-center gap-2">
          <img src={logo} alt="UniShare" className="h-7 w-7 object-contain" />
          <span className="text-[20px] font-extrabold tracking-tight text-[var(--app-text)]">UniShare</span>
        </Link>
        <div className="flex items-center gap-2">
          <Button size="sm" onClick={() => setComposerMode('modal')}>Đăng bài</Button>
          <button
            className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-red-50 hover:text-red-600"
            onClick={logout}
            aria-label="Đăng xuất"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" x2="9" y1="12" y2="12" />
            </svg>
          </button>
        </div>
      </header>

      <main className="flex flex-col min-h-screen items-center lg:items-start lg:pl-[12%] xl:pl-[18%] px-0 lg:col-start-2">
        <Outlet />
      </main>

      {/* Mobile bottom nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-20 grid grid-cols-4 border-t border-[var(--app-border)] bg-white text-center text-xs font-semibold lg:hidden">
        {navItems.filter(item => item.to.startsWith('/') && item.to !== '#activity').map((item) => (
          <NavLink key={item.label} to={item.to} className={`grid gap-1 py-2 justify-items-center ${item.active ? 'text-zinc-950' : 'text-zinc-500'}`}>
            <span aria-hidden="true" className="h-5 w-5">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <PostComposer mode={composerMode} onClose={() => setComposerMode(null)} />
      
      {/* Nút + nổi ở góc dưới phải */}
      <button 
        className="fixed bottom-6 right-6 lg:bottom-10 lg:right-10 z-50 flex h-[68px] w-[68px] items-center justify-center rounded-[20px] bg-white text-zinc-950 border border-zinc-200 shadow-[0_8px_30px_rgb(0,0,0,0.12)] transition-all duration-300 ease-out hover:-translate-y-1.5 hover:scale-105 hover:bg-zinc-50 hover:shadow-[0_12px_40px_rgb(0,0,0,0.2)] active:scale-95"
        onClick={() => setComposerMode('floating')}
        aria-label="Tạo bài viết mới"
      >
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
      </button>
      
      <Modal
        open={sessionExpired}
        title="Phiên đăng nhập hết hạn"
        onClose={() => setSessionExpired(false)}
        footer={<Button onClick={() => navigate('/login')}>Đăng nhập lại</Button>}
      >
        <p className="text-sm text-zinc-600">Vui lòng đăng nhập lại để tiếp tục sử dụng UniShare.</p>
      </Modal>
    </div>
  );
}
