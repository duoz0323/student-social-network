import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import logo from '../../assets/brand/logo.png';
import Button from '../common/Button.jsx';
import { useApp } from '../../contexts/AppContext.jsx';

export default function AdminShell() {
  const { currentUser } = useApp();
  const navigate = useNavigate();
  const items = [
    { to: '/admin', label: 'Tong quan', end: true },
    { to: '/admin/users', label: 'Nguoi dung' },
    { to: '/admin/posts', label: 'Bai viet' },
    { to: '/admin/reports', label: 'Bao cao' },
  ];

  return (
    <div className="min-h-screen bg-zinc-50 text-zinc-950">
      <aside className="fixed left-0 top-0 h-screen w-72 border-r border-zinc-200 bg-white px-5 py-6">
        <div className="flex items-center gap-3">
          <img src={logo} alt="UniShare" className="h-10 w-10 rounded-xl object-contain" />
          <div>
            <p className="text-xl font-black">UniShare</p>
            <p className="text-xs text-zinc-500">Trang quan tri</p>
          </div>
        </div>
        <nav className="mt-8 space-y-2">
          {items.map((item) => (
            <NavLink
              key={item.to}
              end={item.end}
              to={item.to}
              className={({ isActive }) => `block rounded-2xl px-4 py-3 font-semibold ${isActive ? 'bg-zinc-100' : 'text-zinc-600 hover:bg-zinc-50'}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <Button className="mt-6 w-full" variant="secondary" onClick={() => navigate('/feed/for-you')}>
          Quay lai ung dung
        </Button>
        <p className="absolute bottom-6 left-5 right-5 text-sm text-zinc-500">{currentUser.displayName}</p>
      </aside>
      <main className="ml-72 min-h-screen p-8">
        <Outlet />
      </main>
    </div>
  );
}
