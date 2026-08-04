import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  BellOff,
  CheckCheck,
  RefreshCw,
} from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { useNotifications } from '../../../contexts/NotificationContext.jsx';
import NotificationItem from '../components/NotificationItem.jsx';

function NotificationListSkeleton() {
  return (
    <div aria-label="Đang tải thông báo" aria-busy="true">
      {[0, 1, 2, 3].map((item) => (
        <div key={item} className="flex animate-pulse items-center gap-4 border-b border-[var(--app-border)] px-5 py-5 sm:px-6">
          <div className="h-12 w-12 shrink-0 rounded-full bg-[var(--app-surface-soft)]" />
          <div className="min-w-0 flex-1 space-y-2.5">
            <div className="h-3.5 w-4/5 rounded-full bg-[var(--app-surface-soft)]" />
            <div className="h-3 w-24 rounded-full bg-[var(--app-surface-soft)]" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState('all');
  const [openingId, setOpeningId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [markingAll, setMarkingAll] = useState(false);
  const {
    notifications,
    unreadCount,
    loading,
    loadingMore,
    hasMore,
    error,
    initializeNotifications,
    loadMore,
    reconcile,
    markAsRead,
    markAllAsRead,
    deleteNotification,
  } = useNotifications();

  useEffect(() => {
    const controller = new AbortController();
    initializeNotifications(controller.signal);
    return () => controller.abort();
  }, [initializeNotifications]);

  async function openNotification(item) {
    if (openingId !== null || deletingId !== null) return;
    setOpeningId(item.notificationId);
    try {
      if (!item.readAt) await markAsRead(item.notificationId);
      if (item.postId) navigate(`/posts/${item.postId}`);
      else if (item.actor?.userId) navigate(`/profile/${item.actor.userId}`);
      else await reconcile();
    } catch {
      // Không điều hướng nếu REST mutation thất bại; Context giữ error state dùng chung.
    } finally {
      setOpeningId(null);
    }
  }

  async function handleDelete(item) {
    if (openingId !== null || deletingId !== null) return;
    setDeletingId(item.notificationId);
    try {
      await deleteNotification(item.notificationId);
    } catch {
      // Context hiển thị lỗi nhất quán ở đầu danh sách.
    } finally {
      setDeletingId(null);
    }
  }

  async function handleMarkAllAsRead() {
    if (markingAll || unreadCount === 0) return;
    setMarkingAll(true);
    try {
      await markAllAsRead();
    } catch {
      // Context hiển thị lỗi nhất quán ở đầu danh sách.
    } finally {
      setMarkingAll(false);
    }
  }

  function handleRetry() {
    initializeNotifications();
  }

  const visibleNotifications = useMemo(
    () => (filter === 'unread'
      ? notifications.filter((notification) => !notification.readAt)
      : notifications),
    [filter, notifications],
  );

  const notificationTabs = (
    <div className="relative flex h-[var(--header-height)] items-center justify-center gap-12 px-4 sm:px-6">
      <button
        type="button"
        className={`relative flex h-full items-center px-4 text-[15px] font-bold transition ${filter === 'all' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => setFilter('all')}
      >
        Tất cả
        {filter === 'all' && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>
      <button
        type="button"
        className={`relative flex h-full items-center gap-1.5 px-4 text-[15px] font-bold transition ${filter === 'unread' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => setFilter('unread')}
      >
        <span>Chưa đọc</span>
        {unreadCount > 0 ? (
          <span className="min-w-5 rounded-full bg-[var(--app-surface-soft)] px-1.5 py-0.5 text-[10px] font-bold text-[var(--app-muted)]">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        ) : null}
        {filter === 'unread' && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>

      <button
        type="button"
        className="absolute right-4 rounded-lg px-2 py-1.5 text-[13px] font-semibold text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)] disabled:cursor-default disabled:opacity-40 sm:right-6"
        disabled={unreadCount === 0 || markingAll}
        onClick={handleMarkAllAsRead}
      >
        {markingAll ? 'Đang xử lý...' : 'Đánh dấu đã đọc'}
      </button>
    </div>
  );

  return (
    <ContentShell header={notificationTabs}>

      {error ? (
        <div className="mx-4 mt-4 flex items-center justify-between gap-3 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-600 sm:mx-6">
          <span className="min-w-0">{error}</span>
          <button
            type="button"
            className="shrink-0 rounded-lg px-2 py-1 font-semibold transition hover:bg-red-500/10"
            onClick={handleRetry}
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {loading ? (
        <NotificationListSkeleton />
      ) : notifications.length === 0 ? (
        <div className="flex min-h-[360px] flex-col items-center justify-center px-6 py-14 text-center">
          <div className="relative flex h-20 w-20 items-center justify-center rounded-3xl bg-[var(--app-surface-soft)] text-[var(--app-muted)]">
            <BellOff size={32} strokeWidth={1.7} />
            <span className="absolute right-2 top-2 h-3 w-3 rounded-full bg-[var(--app-brand)] ring-4 ring-[var(--app-surface)]" />
          </div>
          <h2 className="mt-6 text-lg font-bold text-[var(--app-text)]">Chưa có thông báo</h2>
          <p className="mt-2 max-w-xs text-sm leading-6 text-[var(--app-muted)]">
            Khi có người theo dõi, thích hoặc bình luận bài viết, hoạt động mới sẽ xuất hiện tại đây.
          </p>
        </div>
      ) : visibleNotifications.length === 0 ? (
        <div className="flex min-h-[280px] flex-col items-center justify-center px-6 py-12 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600">
            {hasMore && unreadCount > 0 ? <Bell size={27} /> : <CheckCheck size={28} />}
          </div>
          <h2 className="mt-5 text-base font-bold text-[var(--app-text)]">
            {hasMore && unreadCount > 0 ? 'Thông báo chưa đọc nằm ở trang sau' : 'Bạn đã xem hết rồi'}
          </h2>
          <p className="mt-1.5 text-sm text-[var(--app-muted)]">
            {hasMore && unreadCount > 0
              ? 'Tải thêm để tiếp tục xem các hoạt động bạn chưa đọc.'
              : 'Không còn thông báo chưa đọc.'}
          </p>
          {hasMore && unreadCount > 0 ? (
            <Button
              variant="secondary"
              className="mt-5 !rounded-xl"
              disabled={loadingMore}
              onClick={loadMore}
            >
              {loadingMore ? 'Đang tải...' : 'Tải thêm thông báo'}
            </Button>
          ) : (
            <button
              type="button"
              className="mt-4 text-sm font-semibold text-[var(--app-brand)] hover:underline"
              onClick={() => setFilter('all')}
            >
              Xem tất cả thông báo
            </button>
          )}
        </div>
      ) : (
        <div>
          <div className="px-4 pb-2 pt-4 text-[13px] font-semibold text-[var(--app-muted)] sm:px-6">
            {filter === 'unread'
              ? `${unreadCount} thông báo chưa đọc`
              : (unreadCount > 0 ? `${unreadCount} thông báo mới` : 'Hoạt động gần đây')}
          </div>
          {visibleNotifications.map((item) => (
            <NotificationItem
              key={item.notificationId}
              item={item}
              opening={String(openingId) === String(item.notificationId)}
              deleting={String(deletingId) === String(item.notificationId)}
              onOpen={openNotification}
              onDelete={handleDelete}
            />
          ))}
          {hasMore ? (
            <div className="flex justify-center border-t border-[var(--app-border)] p-5">
              <Button
                variant="secondary"
                className="min-w-36 !rounded-xl"
                disabled={loadingMore}
                onClick={loadMore}
              >
                {loadingMore ? (
                  <>
                    <RefreshCw size={16} className="mr-2 animate-spin" />
                    Đang tải...
                  </>
                ) : 'Xem thêm thông báo'}
              </Button>
            </div>
          ) : null}
        </div>
      )}
    </ContentShell>
  );
}
