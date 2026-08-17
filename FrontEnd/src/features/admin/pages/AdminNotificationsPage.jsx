import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAdminNotifications } from '../../../contexts/AdminNotificationContext.jsx';
import AdminNotificationItem from '../notifications/AdminNotificationItem.jsx';
import { getAdminNotificationPath } from '../notifications/adminNotificationNavigation.js';

export default function AdminNotificationsPage() {
  const navigate = useNavigate();
  const notifications = useAdminNotifications();
  useEffect(() => { notifications.loadFirstPage(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  async function openItem(item) {
    try { if (!item.readAt) await notifications.markRead(item.notificationId); } catch { await notifications.reconcile(); }
    const path = getAdminNotificationPath(item);
    if (path) navigate(path);
  }

  return (
    <section className="mx-auto max-w-4xl overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-sm">
      <header className="flex items-center justify-between border-b border-zinc-200 p-5"><div><h1 className="text-xl font-bold">Thông báo quản trị</h1><p className="mt-1 text-sm text-zinc-500">Các sự kiện phù hợp với trách nhiệm và quyền hiện tại của bạn.</p></div><button type="button" onClick={notifications.markAllRead} className="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-semibold hover:bg-zinc-50">Đọc tất cả</button></header>
      {notifications.loading ? <p className="p-10 text-center text-zinc-500">Đang tải...</p> : null}
      {!notifications.loading && notifications.error ? <div className="p-10 text-center text-red-600"><p>{notifications.error}</p><button type="button" onClick={() => notifications.loadFirstPage()} className="mt-3 font-semibold underline">Thử lại</button></div> : null}
      {!notifications.loading && !notifications.error && notifications.items.length === 0 ? <p className="p-12 text-center text-zinc-500">Không có thông báo.</p> : null}
      {notifications.items.map((item) => <AdminNotificationItem key={item.notificationId} notification={item} onOpen={openItem} onRead={notifications.markRead} onDelete={notifications.deleteNotification} />)}
      {notifications.hasNext ? <div className="p-5 text-center"><button type="button" disabled={notifications.loadingMore} onClick={notifications.loadMore} className="rounded-xl bg-zinc-900 px-5 py-2.5 text-sm font-semibold text-white disabled:opacity-50">{notifications.loadingMore ? 'Đang tải...' : 'Tải thêm'}</button></div> : null}
    </section>
  );
}
