import { useEffect, useRef, useState } from 'react';
import { Bell } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAdminNotifications } from '../../../contexts/AdminNotificationContext.jsx';
import { getAdminNotificationPath } from './adminNotificationNavigation.js';
import AdminNotificationItem from './AdminNotificationItem.jsx';

export default function AdminNotificationBell() {
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);
  const navigate = useNavigate();
  const notifications = useAdminNotifications();

  useEffect(() => {
    if (open) notifications.loadFirstPage();
  }, [open]); // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => {
    const close = (event) => { if (!rootRef.current?.contains(event.target)) setOpen(false); };
    document.addEventListener('pointerdown', close);
    return () => document.removeEventListener('pointerdown', close);
  }, []);

  async function openNotification(item) {
    try { if (!item.readAt) await notifications.markRead(item.notificationId); } catch { await notifications.reconcile(); }
    const path = getAdminNotificationPath(item);
    if (path) navigate(path);
    setOpen(false);
  }

  return (
    <div className="relative" ref={rootRef}>
      <button type="button" aria-label="Thông báo quản trị" aria-expanded={open} onClick={() => setOpen((value) => !value)} className="relative flex h-11 w-11 items-center justify-center rounded-full border border-zinc-200 bg-white text-zinc-700 shadow-sm hover:bg-zinc-50">
        <Bell size={20} />
        {notifications.unreadCount > 0 ? <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-red-600 px-1 text-center text-[11px] font-bold leading-5 text-white">{notifications.unreadCount > 99 ? '99+' : notifications.unreadCount}</span> : null}
      </button>
      {open ? (
        <section className="absolute right-0 top-14 z-50 w-[min(24rem,calc(100vw-2rem))] overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-xl" aria-label="Notification Center">
          <header className="flex items-center justify-between border-b border-zinc-100 px-4 py-3"><h2 className="font-semibold">Thông báo</h2><button type="button" onClick={notifications.markAllRead} className="text-xs font-semibold text-zinc-700 hover:text-zinc-950 hover:underline">Đọc tất cả</button></header>
          <div className="max-h-[28rem] overflow-y-auto">
            {notifications.loading ? <p className="p-6 text-center text-sm text-zinc-500">Đang tải thông báo...</p> : null}
            {!notifications.loading && notifications.error ? <div className="p-6 text-center text-sm text-red-600"><p>{notifications.error}</p><button type="button" onClick={() => notifications.loadFirstPage()} className="mt-2 font-semibold underline">Thử lại</button></div> : null}
            {!notifications.loading && !notifications.error && notifications.items.length === 0 ? <p className="p-8 text-center text-sm text-zinc-500">Chưa có thông báo quản trị.</p> : null}
            {notifications.items.slice(0, 5).map((item) => <AdminNotificationItem key={item.notificationId} notification={item} compact onOpen={openNotification} onRead={notifications.markRead} onDelete={notifications.deleteNotification} />)}
          </div>
          <button type="button" onClick={() => { navigate('/admin/notifications'); setOpen(false); }} className="w-full border-t border-zinc-100 px-4 py-3 text-sm font-semibold text-zinc-700 hover:bg-zinc-50 hover:text-zinc-950">Xem tất cả</button>
        </section>
      ) : null}
    </div>
  );
}
