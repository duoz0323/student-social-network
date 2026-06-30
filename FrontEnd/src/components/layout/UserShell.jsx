import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import logo from '../../assets/brand/logo.png';
import Button from '../common/Button.jsx';
import Avatar from '../common/Avatar.jsx';
import Modal from '../common/Modal.jsx';
import PostComposer from '../../features/post/components/PostComposer.jsx';
import { useState } from 'react';
import { useApp } from '../../contexts/AppContext.jsx';

export default function UserShell() {
  const { currentUser, logout, sessionExpired, setSessionExpired } = useApp();
  const [composerOpen, setComposerOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const navItems = [
    { to: '/feed/for-you', label: 'Danh cho ban', icon: '■', active: location.pathname.startsWith('/feed') },
    { to: '/search', label: 'Tim kiem', icon: '○', active: location.pathname === '/search' },
    { to: '/profile/me', label: 'Trang ca nhan', icon: '♙', active: location.pathname === '/profile/me' },
    { to: '/saved', label: 'Bai viet da luu', icon: '□', active: location.pathname === '/saved' },
  ];

  return (
    <div className="min-h-screen bg-[var(--app-bg)] text-[var(--app-text)] lg:grid lg:grid-cols-[var(--sidebar-width)_minmax(0,1fr)]">
      <aside className="fixed left-0 top-0 hidden h-screen w-[var(--sidebar-width)] border-r border-[var(--app-border)] bg-white px-4 py-5 lg:flex lg:flex-col">
        <Link to="/feed/for-you" className="flex items-center gap-2 px-2 text-[var(--app-brand)]">
          <img src={logo} alt="UniShare" className="h-7 w-7 object-contain" />
          <span className="text-[17px] font-black leading-none">UniShare</span>
        </Link>
        <nav className="mt-9 grid gap-2">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={`flex min-h-[46px] items-center gap-3 rounded-[10px] px-4 text-[15px] font-semibold transition ${
                item.active ? 'bg-[var(--app-surface-soft)] text-[var(--app-text)]' : 'text-zinc-700 hover:bg-zinc-50'
              }`}
            >
              <span className="icon-mark" aria-hidden="true">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <Button className="mt-6 min-h-[46px] w-full font-black" onClick={() => setComposerOpen(true)}>
          Tao bai viet
        </Button>
        {currentUser?.role === 'ADMIN' ? (
          <Button className="mt-3 w-full" variant="secondary" onClick={() => navigate('/admin')}>
            Trang quan tri
          </Button>
        ) : null}
        <div className="mt-auto grid gap-3 rounded-[10px] bg-[var(--app-surface-soft)] p-3">
          <div className="flex items-center gap-3">
          <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} size="sm" />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-bold">{currentUser.displayName}</p>
            <p className="truncate text-xs text-[var(--app-muted)]">Dang su dung UniShare</p>
          </div>
          </div>
          <button className="text-left text-xs font-bold text-[var(--app-muted)] hover:text-[var(--app-text)]" onClick={logout}>
            Dang xuat
          </button>
        </div>
      </aside>

      <header className="sticky top-0 z-20 flex h-[var(--header-height)] items-center justify-between border-b border-[var(--app-border)] bg-white px-4 lg:hidden">
        <Link to="/feed/for-you" className="font-black text-[var(--app-brand)]">UniShare</Link>
        <Button size="sm" onClick={() => setComposerOpen(true)}>Tao bai</Button>
      </header>

      <main className="flex min-h-screen justify-center px-0 lg:col-start-2">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 left-0 right-0 z-20 grid grid-cols-4 border-t border-[var(--app-border)] bg-white text-center text-xs font-semibold lg:hidden">
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} className={`grid gap-1 py-2 ${item.active ? 'text-zinc-950' : 'text-zinc-500'}`}>
            <span aria-hidden="true">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <PostComposer open={composerOpen} onClose={() => setComposerOpen(false)} />
      <Modal
        open={sessionExpired}
        title="Phien dang nhap het han"
        onClose={() => setSessionExpired(false)}
        footer={<Button onClick={() => navigate('/login')}>Dang nhap lai</Button>}
      >
        <p className="text-sm text-zinc-600">Vui long dang nhap lai de tiep tuc su dung UniShare.</p>
      </Modal>
    </div>
  );
}
