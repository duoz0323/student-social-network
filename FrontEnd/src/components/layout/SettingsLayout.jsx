import { Ban, ChevronRight, KeyRound, Shield } from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';

const ACCOUNT_ITEMS = [
  {
    to: '/settings/auth-providers',
    label: 'Phương thức đăng nhập',
    icon: KeyRound,
  },
];

const PRIVACY_ITEMS = [
  {
    to: '/settings/restricted-users',
    label: 'Tài khoản đã hạn chế',
    icon: Shield,
  },
  {
    to: '/settings/blocked-users',
    label: 'Tài khoản đã chặn',
    icon: Ban,
  },
];

/**
 * Nhóm điều hướng cài đặt thiết kế chuẩn kiểu Meta/Threads với thẻ pill bo góc nổi bật.
 */
function SettingsNavGroup({ label, items }) {
  return (
    <div className="contents lg:block">
      {label && (
        <p className="hidden px-3.5 pb-2 pt-3 text-[11px] font-bold uppercase tracking-wider text-[var(--app-muted)] first:pt-1 lg:block">
          {label}
        </p>
      )}
      <div className="flex gap-1 overflow-x-auto pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden lg:block lg:space-y-1 lg:overflow-visible lg:pb-0">
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `group flex min-w-max items-center gap-3.5 rounded-xl px-3.5 py-3 text-[15px] transition-all lg:min-w-0 ${
                isActive
                  ? 'bg-[var(--app-surface-soft)] font-bold text-[var(--app-text)]'
                  : 'font-medium text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)]'
              }`}
            >
              {({ isActive }) => (
                <>
                  <Icon size={20} strokeWidth={isActive ? 2.2 : 1.8} className="shrink-0 text-[var(--app-text)]" aria-hidden="true" />
                  <span className="whitespace-nowrap">{item.label}</span>
                  <ChevronRight
                    size={18}
                    className={`ml-auto hidden transition-transform lg:block ${
                      isActive
                        ? 'translate-x-0 text-[var(--app-text)]'
                        : '-translate-x-1 text-[var(--app-muted)] opacity-0 group-hover:translate-x-0 group-hover:opacity-100'
                    }`}
                    aria-hidden="true"
                  />
                </>
              )}
            </NavLink>
          );
        })}
      </div>
    </div>
  );
}

/**
 * Layout cài đặt mang phong cách Meta/Threads hiện đại; kết hợp Trung tâm tài khoản và sidebar danh mục.
 */
export default function SettingsLayout() {
  return (
    <div className="w-full max-w-[920px] pb-24 lg:-ml-[6%] lg:pb-8 xl:-ml-[10%]">
      <header className="flex h-[72px] items-center bg-[var(--app-bg)] px-5 sm:px-6 lg:h-auto lg:items-start lg:pb-5 lg:pt-[34px] lg:px-1">
        <div>
          <h1 className="text-xl font-extrabold tracking-[-0.02em] text-[var(--app-text)] sm:text-2xl">Cài đặt</h1>
          <p className="mt-0.5 hidden text-sm text-[var(--app-muted)] sm:block">
            Quản lý tài khoản và quyền riêng tư
          </p>
        </div>
      </header>

      <div className="min-h-[580px] overflow-hidden border-y border-[var(--app-border-strong)] bg-[var(--app-surface)] sm:border-x lg:grid lg:grid-cols-[280px_minmax(0,1fr)] lg:rounded-[24px]">
        <aside className="min-w-0 border-b border-[var(--app-border)] p-3 lg:border-b-0 lg:border-r lg:p-4">

          <nav className="space-y-3" aria-label="Danh mục cài đặt">
            <SettingsNavGroup label="Bảo mật & Xác thực" items={ACCOUNT_ITEMS} />
            <SettingsNavGroup label="Quyền riêng tư" items={PRIVACY_ITEMS} />
          </nav>
        </aside>

        <main className="min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
