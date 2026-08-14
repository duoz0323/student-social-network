import { Link, NavLink, Outlet, ScrollRestoration, useLocation, useNavigate } from 'react-router-dom';
import Button from '../common/Button.jsx';
import Badge from '../common/Badge.jsx';
import BrandLockup from '../common/BrandLockup.jsx';
import PostComposer from '../../features/post/components/PostComposer.jsx';
import MoreMenu from './MoreMenu.jsx';
import { useEffect, useState } from 'react';
import { useApp } from '../../contexts/AppContext.jsx';
import { useNotifications } from '../../contexts/NotificationContext.jsx';
import { notificationBadgeLabel } from '../../features/notification/utils/notificationState.js';
import { useMessaging } from '../../features/messaging/hooks/useMessaging.js';
import { messagingBadgeLabel } from '../../features/messaging/utils/messagingState.js';
import StudentRecommendationRail from '../../features/recommendation/components/StudentRecommendationRail.jsx';

// SVG Icons cho Sidebar
function HomeIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinejoin="round">
      <path d="M12 3l9 7v11a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V10l9-7z" />
    </svg>
  );
}

function CreateIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <path d="M12 8v8M8 12h8" stroke={active ? 'var(--app-bg)' : 'currentColor'} />
    </svg>
  );
}

function SearchIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? '3' : '2'} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8" />
      <line x1="21" x2="16.65" y1="21" y2="16.65" />
    </svg>
  );
}

function ActivityIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
  );
}

function MessageIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
    </svg>
  );
}

function ProfileIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" fill={active ? 'currentColor' : 'none'} />
      <circle cx="12" cy="7" r="4" fill={active ? 'currentColor' : 'none'} />
    </svg>
  );
}

function BookmarkIcon({ active = false }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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

function NavigationIcon({ item, notificationUnreadCount, messagingUnreadCount }) {
  const badgeLabel = item.to === '/notifications'
    ? notificationBadgeLabel(notificationUnreadCount)
    : item.to === '/messages' ? messagingBadgeLabel(messagingUnreadCount) : '';
  const unreadCount = item.to === '/messages' ? messagingUnreadCount : notificationUnreadCount;
  return (
    <span className="interactive-icon relative flex w-6 justify-center text-[var(--app-text)]">
      {item.icon(item.active)}
      {badgeLabel ? (
        <Badge
          tone="danger"
          className="absolute -right-4 -top-2 min-w-5 px-1.5 py-0.5 text-center text-[10px] leading-4"
          aria-label={`${unreadCount} ${item.to === '/messages' ? 'tin nhắn' : 'thông báo'} chưa đọc`}
        >
          {badgeLabel}
        </Badge>
      ) : null}
    </span>
  );
}

const ROUTE_TITLES = {
  '/feed': 'Trang chủ',
  '/search': 'Tìm kiếm',
  '/notifications': 'Thông báo',
  '/messages': 'Tin nhắn',
  '/profile': 'Trang cá nhân',
  '/saved': 'Bài viết đã lưu',
  '/liked': 'Bài viết đã thích',
  '/settings': 'Cài đặt',
};

function getPageTitle(pathname) {
  for (const [route, title] of Object.entries(ROUTE_TITLES)) {
    if (pathname.startsWith(route)) return title;
  }
  return 'Trang chủ';
}

export default function UserShell() {
  const { logout } = useApp();
  const { unreadCount } = useNotifications();
  const { totalUnreadCount } = useMessaging();
  const [composerMode, setComposerMode] = useState(null);
  const [moreMenuOpen, setMoreMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const isMessagingRoute = location.pathname.startsWith('/messages');
  const isFeedRoute = location.pathname.startsWith('/feed');

  // Cập nhật tiêu đề tab trình duyệt hiển thị số thông báo/tin nhắn chưa đọc (VD: (1) Trang chủ • UniShare)
  useEffect(() => {
    const pageTitle = getPageTitle(location.pathname);
    const totalUnread = (Number(unreadCount) || 0) + (Number(totalUnreadCount) || 0);
    if (totalUnread > 0) {
      const badgeStr = totalUnread > 99 ? '(99+)' : `(${totalUnread})`;
      document.title = `${badgeStr} ${pageTitle} • UniShare`;
    } else {
      document.title = `${pageTitle} • UniShare`;
    }
  }, [location.pathname, totalUnreadCount, unreadCount]);

  const navItems = [
    { to: '/feed/for-you', label: 'Dành cho bạn', icon: (active) => <HomeIcon active={active} />, active: location.pathname.startsWith('/feed') },
    { to: '#create', label: 'Tạo bài viết', icon: (active) => <CreateIcon active={active} />, active: false, action: () => setComposerMode('modal') },
    { to: '/search', label: 'Tìm kiếm', icon: (active) => <SearchIcon active={active} />, active: location.pathname === '/search' },
    { to: '/notifications', label: 'Thông báo', icon: (active) => <ActivityIcon active={active} />, active: location.pathname === '/notifications' },
    { to: '/messages', label: 'Tin nhắn', icon: (active) => <MessageIcon active={active} />, active: location.pathname.startsWith('/messages') },
    { to: '/profile/me', label: 'Trang cá nhân', icon: (active) => <ProfileIcon active={active} />, active: location.pathname === '/profile/me' },
    { to: '/saved', label: 'Bài viết đã lưu', icon: (active) => <BookmarkIcon active={active} />, active: location.pathname === '/saved' },
  ];

  function handleLogoOrFeedClick(e, targetTo = '/feed/for-you') {
    if (location.pathname.startsWith('/feed')) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      window.dispatchEvent(new CustomEvent('unishare:refresh-feed', { detail: { type: targetTo.includes('following') ? 'following' : 'for-you' } }));
    }
  }

  return (
    <div className="min-h-screen bg-[var(--app-bg)] text-[var(--app-text)] lg:grid lg:grid-cols-[var(--sidebar-width)_minmax(0,1fr)]">
      {/* 
        Sửa màu nền sidebar trùng với nền web (xóa bg-white và border), 
        thay đổi cách hiển thị chữ in đậm cho tab đang active.
      */}
      <aside className="fixed left-0 top-0 z-30 hidden h-screen w-[var(--sidebar-width)] bg-[var(--app-bg)] px-4 py-5 lg:flex lg:flex-col">
        {/* Logo */}
        <Link
          to="/feed/for-you"
          onClick={(e) => handleLogoOrFeedClick(e, '/feed/for-you')}
          className="mb-8 rounded-2xl px-3 py-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--app-brand)]"
        >
          <BrandLockup />
        </Link>
        
        {/* Navigation Items */}
        <nav className="grid gap-1 mt-2">
          {navItems.map((item) => {
            const buttonClass = `interactive-row flex min-h-[52px] items-center gap-4 rounded-[12px] px-4 hover:bg-[var(--app-surface-soft)] ${
              item.active ? 'bg-[var(--app-surface-soft)] font-bold text-[var(--app-text)]' : 'font-normal text-[var(--app-text)]'
            }`;

            if (item.action) {
              return (
                <button key={item.label} onClick={item.action} className={buttonClass}>
                  <NavigationIcon item={item} notificationUnreadCount={unreadCount} messagingUnreadCount={totalUnreadCount} />
                  <span className="text-[15px]">{item.label}</span>
                </button>
              );
            }
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={(e) => handleLogoOrFeedClick(e, item.to)}
                className={buttonClass}
              >
                <NavigationIcon item={item} notificationUnreadCount={unreadCount} messagingUnreadCount={totalUnreadCount} />
                <span className="text-[15px]">{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Nút Xem thêm & Menu ở góc dưới trái */}
        <div className="relative mt-auto">
          <MoreMenu
            open={moreMenuOpen}
            onClose={() => setMoreMenuOpen(false)}
            onLogout={logout}
            onSettings={() => navigate('/settings')}
            onLikedPosts={() => navigate('/liked')}
          />
          <button
            onClick={() => setMoreMenuOpen(!moreMenuOpen)}
            className={`flex min-h-[52px] w-full items-center gap-4 rounded-[12px] px-4 font-normal text-[var(--app-text)] transition-colors hover:bg-[var(--app-surface-soft)] ${
              moreMenuOpen ? 'bg-[var(--app-surface-soft)]' : ''
            }`}
          >
            <span className="flex w-6 justify-center"><MoreIcon /></span>
            <span className="text-[15px]">Xem thêm</span>
          </button>
        </div>
      </aside>

      {/* Mobile header */}
      <header className="sticky top-0 z-20 flex h-[var(--header-height)] items-center justify-between border-b border-[var(--app-border)] bg-[var(--app-surface)] px-4 lg:hidden">
        <Link
          to="/feed/for-you"
          onClick={(e) => handleLogoOrFeedClick(e, '/feed/for-you')}
          className="flex items-center gap-2"
        >
          <BrandLockup compact />
        </Link>
        <div className="flex items-center gap-2">
          {!isMessagingRoute ? <Button size="sm" onClick={() => setComposerMode('modal')}>Đăng bài</Button> : null}
          <button
            className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)]"
            onClick={() => navigate('/settings')}
            aria-label="Mở cài đặt"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.12 2.12-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.04 1.56V20.3h-3v-.08a1.7 1.7 0 0 0-1.04-1.56 1.7 1.7 0 0 0-1.88.34l-.06.06-2.12-2.12.06-.06A1.7 1.7 0 0 0 7 15a1.7 1.7 0 0 0-1.56-1.04H5.3v-3h.14A1.7 1.7 0 0 0 7 9.92a1.7 1.7 0 0 0-.34-1.88l-.06-.06 2.12-2.12.06.06a1.7 1.7 0 0 0 1.88.34A1.7 1.7 0 0 0 11.7 4.7V4.6h3v.1a1.7 1.7 0 0 0 1.04 1.56 1.7 1.7 0 0 0 1.88-.34l.06-.06 2.12 2.12-.06.06a1.7 1.7 0 0 0-.34 1.88 1.7 1.7 0 0 0 1.56 1.04h.14v3h-.14A1.7 1.7 0 0 0 19.4 15Z"/></svg>
          </button>
          <button
            className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-red-500/10 hover:text-red-600"
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

      <main className={`flex min-h-screen flex-col px-0 lg:col-start-2 ${
        isMessagingRoute
          ? 'items-stretch lg:pl-0'
          : `items-center lg:items-start lg:pl-[12%] xl:pl-[10%] ${isFeedRoute ? 'xl:pr-[320px] 2xl:pr-[360px]' : ''}`
      }`}>
        <Outlet />
      </main>

      {/* Rail gợi ý tách khỏi feed để luôn hiện diện khi người dùng cuộn bài viết. */}
      {isFeedRoute ? <StudentRecommendationRail /> : null}

      {/* Mobile bottom nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-20 grid grid-cols-6 border-t border-[var(--app-border)] bg-[var(--app-surface)] text-center text-[10px] font-semibold lg:hidden">
        {navItems.filter(item => item.to.startsWith('/')).map((item) => (
          <NavLink key={item.label} to={item.to} className={`grid gap-1 py-2 justify-items-center ${item.active ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)]'}`}>
            <NavigationIcon item={item} notificationUnreadCount={unreadCount} messagingUnreadCount={totalUnreadCount} />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <PostComposer mode={composerMode} onClose={() => setComposerMode(null)} />

      {/* Feed dùng pathname ổn định để quay lại đúng vị trí dù navigation tạo location key mới. */}
      <ScrollRestoration
        getKey={(nextLocation) => nextLocation.pathname.startsWith('/feed/')
          ? nextLocation.pathname
          : nextLocation.key}
      />
      
      {/* Nút + nổi ở góc dưới phải */}
      {!isMessagingRoute ? <button
        className="fixed bottom-6 right-6 lg:bottom-10 lg:right-10 z-50 flex h-[68px] w-[68px] items-center justify-center rounded-[20px] bg-[var(--app-surface)] text-[var(--app-text)] border border-[var(--app-border)] shadow-[0_8px_30px_rgb(0,0,0,0.12)] transition-all duration-300 ease-out hover:-translate-y-1.5 hover:scale-105 hover:bg-[var(--app-surface-soft)] hover:shadow-[0_12px_40px_rgb(0,0,0,0.2)] active:scale-95"
        onClick={() => setComposerMode('floating')}
        aria-label="Tạo bài viết mới"
      >
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
      </button> : null}
      
    </div>
  );
}
