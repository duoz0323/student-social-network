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
 * Nhóm điều hướng cài đặt dùng cùng kiểu hàng phẳng với sidebar chính của website.
 */
function SettingsNavGroup({ label, items }) {
  return (
    <div className="contents lg:block">
      <p className="hidden px-3 pb-2 pt-4 text-xs font-semibold text-[var(--app-muted)] first:pt-1 lg:block">
        {label}
      </p>
      {items.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `group relative flex min-w-max items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm transition-colors lg:min-w-0 lg:gap-3 lg:py-3 ${
              isActive
                ? 'bg-[var(--app-surface-soft)] font-bold text-[var(--app-text)]'
                : 'font-medium text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)]'
            }`}
          >
            {({ isActive }) => (
              <>
                <span
                  className={`absolute bottom-2.5 left-0 top-2.5 hidden w-[3px] rounded-full lg:block ${
                    isActive ? 'bg-[var(--app-text)]' : 'bg-transparent'
                  }`}
                  aria-hidden="true"
                />
                <Icon size={19} strokeWidth={isActive ? 2.35 : 1.9} aria-hidden="true" />
                <span className="whitespace-nowrap">{item.label}</span>
                <ChevronRight
                  size={16}
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
  );
}

/**
 * Layout cài đặt giữ một bề mặt liền mạch; desktop dùng sidebar và mobile dùng thanh mục ngang.
 */
export default function SettingsLayout() {
  return (
    <div className="w-full max-w-[900px] pb-24 lg:pb-8">
      <header className="flex h-[72px] items-center bg-[var(--app-bg)] px-5 sm:px-6 lg:h-[84px] lg:px-1">
        <div>
          <h1 className="text-xl font-extrabold tracking-[-0.02em] text-[var(--app-text)] sm:text-2xl">Cài đặt</h1>
          <p className="mt-0.5 hidden text-sm text-[var(--app-muted)] sm:block">
            Quản lý tài khoản và quyền riêng tư
          </p>
        </div>
      </header>

      <div className="min-h-[560px] overflow-hidden border-y border-[var(--app-border-strong)] bg-[var(--app-surface)] sm:border-x lg:grid lg:grid-cols-[238px_minmax(0,1fr)] lg:rounded-[24px]">
        <aside className="min-w-0 border-b border-[var(--app-border)] px-3 py-2 lg:border-b-0 lg:border-r lg:px-3 lg:py-4">
          <nav
            className="flex gap-1 overflow-x-auto pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden lg:block lg:overflow-visible lg:pb-0"
            aria-label="Danh mục cài đặt"
          >
            <SettingsNavGroup label="Tài khoản" items={ACCOUNT_ITEMS} />
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
