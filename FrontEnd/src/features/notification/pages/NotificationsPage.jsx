import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { socialApi } from '../../../api/index.js';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

const TYPE_LABELS = { FOLLOW: 'đã theo dõi bạn', LIKE: 'đã thích bài viết của bạn', COMMENT: 'đã bình luận bài viết của bạn', REPLY: 'đã trả lời bình luận của bạn', REPORT_RESOLVED: 'đã xử lý báo cáo của bạn' };

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  async function load(signal) {
    setLoading(true);
    try { const page = await socialApi.getNotifications({ page: 0, size: 50 }, signal); setItems(page.content ?? []); setError(''); }
    catch (requestError) { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); }
    finally { setLoading(false); }
  }
  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      socialApi.getNotifications({ page: 0, size: 50 }, controller.signal),
      socialApi.getUnreadCount(controller.signal),
    ]).then(([page, count]) => { setItems(page.content ?? []); setUnreadCount(count.unreadCount); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, []);
  async function openNotification(item) {
    try {
      if (!item.readAt) await socialApi.markNotificationRead(item.notificationId);
      if (item.postId) navigate(`/posts/${item.postId}`); else if (item.actor?.userId) navigate(`/profile/${item.actor.userId}`);
      else await load();
    } catch (requestError) { setError(requestError.message); }
  }
  async function readAll() { try { await socialApi.markAllNotificationsRead(); setItems((current) => current.map((item) => ({ ...item, readAt: item.readAt || new Date().toISOString() }))); setUnreadCount(0); } catch (requestError) { setError(requestError.message); } }
  async function remove(id) { try { await socialApi.deleteNotification(id); setItems((current) => current.filter((item) => item.notificationId !== id)); } catch (requestError) { setError(requestError.message); } }
  const header = <div className="flex h-[var(--header-height)] items-center justify-between px-6"><h1 className="font-bold">Thông báo {unreadCount > 0 && `(${unreadCount})`}</h1><Button size="sm" variant="secondary" onClick={readAll}>Đọc tất cả</Button></div>;
  return <ContentShell header={header}>{error && <p className="m-4 rounded-xl bg-red-50 p-3 text-red-700">{error}</p>}{loading ? <LoadingState /> : items.length === 0 ? <EmptyState title="Chưa có thông báo" description="Hoạt động mới sẽ xuất hiện tại đây." /> : items.map((item) => (
    <article key={item.notificationId} className={`flex items-center gap-3 border-b p-4 ${item.readAt ? '' : 'bg-blue-50/40'}`}>
      <button className="flex min-w-0 flex-1 items-center gap-3 text-left" onClick={() => openNotification(item)}><Avatar src={item.actor?.avatarUrl} name={item.actor?.displayName || 'UniShare'} /><span><strong>{item.actor?.displayName || 'UniShare'}</strong> {TYPE_LABELS[item.type] || item.type}<small className="block text-gray-500">{formatDateTime(item.createdAt)}</small></span></button>
      <button className="text-sm text-red-600" onClick={() => remove(item.notificationId)}>Xóa</button>
    </article>
  ))}</ContentShell>;
}
