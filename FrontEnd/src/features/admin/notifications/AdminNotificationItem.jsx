import { Check, Trash2 } from 'lucide-react';

function relativeTime(value) {
  const seconds = Math.round((new Date(value).getTime() - Date.now()) / 1000);
  const formatter = new Intl.RelativeTimeFormat('vi', { numeric: 'auto' });
  if (Math.abs(seconds) < 60) return formatter.format(seconds, 'second');
  const minutes = Math.round(seconds / 60);
  if (Math.abs(minutes) < 60) return formatter.format(minutes, 'minute');
  const hours = Math.round(minutes / 60);
  if (Math.abs(hours) < 24) return formatter.format(hours, 'hour');
  return formatter.format(Math.round(hours / 24), 'day');
}

export default function AdminNotificationItem({ notification, onOpen, onRead, onDelete, compact = false }) {
  return (
    <article className={`group relative border-b border-zinc-100 p-4 ${notification.readAt ? 'bg-white' : 'bg-blue-50/60'} ${compact ? 'text-sm' : ''}`}>
      <button type="button" onClick={() => onOpen(notification)} className="block w-full pr-16 text-left">
        <span className="font-semibold text-zinc-950">{notification.title}</span>
        <span className="mt-1 block text-zinc-600">{notification.message}</span>
        <time className="mt-2 block text-xs text-zinc-400" dateTime={notification.createdAt}>{relativeTime(notification.createdAt)}</time>
      </button>
      <div className="absolute right-3 top-3 flex gap-1">
        {!notification.readAt ? (
          <button type="button" aria-label="Đánh dấu đã đọc" onClick={() => onRead(notification.notificationId)} className="rounded-lg p-2 text-zinc-500 hover:bg-white hover:text-zinc-900"><Check size={15} /></button>
        ) : null}
        <button type="button" aria-label="Xóa thông báo" onClick={() => onDelete(notification.notificationId)} className="rounded-lg p-2 text-zinc-400 hover:bg-white hover:text-red-600"><Trash2 size={15} /></button>
      </div>
    </article>
  );
}
