import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCheck, Trash2 } from 'lucide-react';
import { socialApi } from '../../../api/index.js';
import { isRequestCanceled } from '../../../api/apiError.js';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import { publishNotificationUnreadCount } from '../utils/notificationEvents.js';
import {
  getNotificationPresentation,
  getNotificationTarget,
  normalizeNotificationPage,
} from '../utils/notificationViewModel.js';

const PAGE_SIZE = 20;

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [pageNumber, setPageNumber] = useState(0);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [pendingIds, setPendingIds] = useState(() => new Set());
  const [markingAll, setMarkingAll] = useState(false);
  const [error, setError] = useState('');

  const updateUnreadCount = useCallback((nextCount) => {
    const normalizedCount = Math.max(0, Number(nextCount) || 0);
    setUnreadCount(normalizedCount);
    publishNotificationUnreadCount(normalizedCount);
  }, []);

  const loadFirstPage = useCallback(async (signal) => {
    try {
      const [pagePayload, countPayload] = await Promise.all([
        socialApi.getNotifications({ page: 0, size: PAGE_SIZE }, signal),
        socialApi.getUnreadCount(signal),
      ]);
      const page = normalizeNotificationPage(pagePayload);
      setItems(page.content);
      setPageNumber(page.page);
      setHasNextPage(!page.last);
      updateUnreadCount(countPayload?.unreadCount);
      setError('');
    } catch (requestError) {
      if (!isRequestCanceled(requestError)) setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }, [updateUnreadCount]);

  useEffect(() => {
    const controller = new AbortController();
    let active = true;

    Promise.all([
      socialApi.getNotifications({ page: 0, size: PAGE_SIZE }, controller.signal),
      socialApi.getUnreadCount(controller.signal),
    ])
      .then(([pagePayload, countPayload]) => {
        if (!active) return;
        const page = normalizeNotificationPage(pagePayload);
        setItems(page.content);
        setPageNumber(page.page);
        setHasNextPage(!page.last);
        updateUnreadCount(countPayload?.unreadCount);
        setError('');
      })
      .catch((requestError) => {
        if (active && !isRequestCanceled(requestError)) setError(requestError.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
      controller.abort();
    };
  }, [updateUnreadCount]);

  function setItemPending(notificationId, pending) {
    setPendingIds((current) => {
      const next = new Set(current);
      if (pending) next.add(notificationId);
      else next.delete(notificationId);
      return next;
    });
  }

  async function loadMore() {
    if (loadingMore || !hasNextPage) return;
    setLoadingMore(true);
    try {
      const page = normalizeNotificationPage(await socialApi.getNotifications({
        page: pageNumber + 1,
        size: PAGE_SIZE,
      }));
      // Loại trùng giúp danh sách ổn định nếu có thông báo mới trong lúc người dùng đang phân trang.
      setItems((current) => {
        const knownIds = new Set(current.map((item) => item.notificationId));
        return [...current, ...page.content.filter((item) => !knownIds.has(item.notificationId))];
      });
      setPageNumber(page.page);
      setHasNextPage(!page.last);
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoadingMore(false);
    }
  }

  async function openNotification(item) {
    if (pendingIds.has(item.notificationId)) return;
    setItemPending(item.notificationId, true);
    try {
      if (!item.readAt) {
        const result = await socialApi.markNotificationRead(item.notificationId);
        setItems((current) => current.map((candidate) => candidate.notificationId === item.notificationId
          ? { ...candidate, readAt: result?.readAt || new Date().toISOString() }
          : candidate));
        updateUnreadCount(unreadCount - 1);
      }

      const target = getNotificationTarget(item);
      if (target) navigate(target);
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setItemPending(item.notificationId, false);
    }
  }

  async function readAll() {
    if (markingAll || unreadCount === 0) return;
    setMarkingAll(true);
    try {
      const result = await socialApi.markAllNotificationsRead();
      if ((result?.updatedCount ?? 0) > 0) {
        const readAt = new Date().toISOString();
        setItems((current) => current.map((item) => ({ ...item, readAt: item.readAt || readAt })));
      }
      updateUnreadCount(0);
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setMarkingAll(false);
    }
  }

  async function remove(item) {
    if (pendingIds.has(item.notificationId)) return;
    setItemPending(item.notificationId, true);
    try {
      await socialApi.deleteNotification(item.notificationId);
      setItems((current) => current.filter((candidate) => candidate.notificationId !== item.notificationId));
      if (!item.readAt) updateUnreadCount(unreadCount - 1);
      setError('');
    } catch (requestError) {
      setError(requestError.message);
      setItemPending(item.notificationId, false);
    }
  }

  const header = (
    <div className="flex h-[var(--header-height)] items-center justify-between gap-3 px-4 sm:px-6">
      <div className="min-w-0">
        <h1 className="truncate text-lg font-bold">Thông báo</h1>
        <p className="text-xs text-[var(--app-muted)]" aria-live="polite">
          {unreadCount > 0 ? `${unreadCount} thông báo chưa đọc` : 'Bạn đã đọc tất cả thông báo'}
        </p>
      </div>
      <Button size="sm" variant="secondary" disabled={markingAll || unreadCount === 0} onClick={readAll}>
        <CheckCheck aria-hidden="true" className="mr-1 h-4 w-4" />
        {markingAll ? 'Đang xử lý...' : 'Đọc tất cả'}
      </Button>
    </div>
  );

  return (
    <ContentShell header={header}>
      {error ? (
        <div className="app-error m-4 flex items-center justify-between gap-3 rounded-xl p-3" role="alert">
          <p className="text-sm">{error}</p>
          <button
            className="shrink-0 text-sm font-semibold underline"
            onClick={() => {
              setLoading(true);
              loadFirstPage();
            }}
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {loading ? (
        <LoadingState message="Đang tải thông báo..." />
      ) : items.length === 0 ? (
        <div className="p-4">
          <EmptyState title="Chưa có thông báo" description="Hoạt động mới sẽ xuất hiện tại đây." />
        </div>
      ) : (
        <div aria-label="Danh sách thông báo">
          {items.map((item) => {
            const presentation = getNotificationPresentation(item);
            const pending = pendingIds.has(item.notificationId);
            return (
              <article
                key={item.notificationId}
                className={`group flex items-center gap-3 border-b border-[var(--app-border)] p-4 transition-colors ${
                  item.readAt ? 'bg-[var(--app-surface)]' : 'bg-[color-mix(in_srgb,var(--app-brand)_7%,var(--app-surface))]'
                }`}
              >
                <button
                  className="flex min-w-0 flex-1 items-center gap-3 rounded-xl text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--app-brand)]"
                  disabled={pending}
                  onClick={() => openNotification(item)}
                >
                  <Avatar src={presentation.avatarUrl} name={presentation.actorName} />
                  <span className="min-w-0 flex-1">
                    <span className={`block text-sm leading-5 ${item.readAt ? '' : 'font-semibold'}`}>
                      {presentation.message}
                    </span>
                    <small className="mt-1 block text-xs text-[var(--app-muted)]">
                      {formatDateTime(item.createdAt)}
                    </small>
                  </span>
                  {!item.readAt ? (
                    <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-[var(--app-brand)]" aria-label="Chưa đọc" />
                  ) : null}
                </button>
                <button
                  className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-red-500/10 hover:text-red-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500"
                  disabled={pending}
                  aria-label={`Xóa thông báo: ${presentation.message}`}
                  onClick={() => remove(item)}
                >
                  <Trash2 aria-hidden="true" className="h-4 w-4" />
                </button>
              </article>
            );
          })}

          {hasNextPage ? (
            <div className="flex justify-center p-4">
              <Button variant="secondary" disabled={loadingMore} onClick={loadMore}>
                {loadingMore ? 'Đang tải...' : 'Xem thêm thông báo'}
              </Button>
            </div>
          ) : null}
        </div>
      )}
    </ContentShell>
  );
}
