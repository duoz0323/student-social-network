import { useNavigate } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import { formatMessagingDateTime } from '../utils/messageTime.js';

function formatTime(value) {
  if (!value) return '';
  const date = formatMessagingDateTime(value);
  const today = formatMessagingDateTime(new Date());
  if (date.dayKey === today.dayKey) {
    return date.time;
  }
  return date.dayMonth;
}

/** Danh sách Inbox chỉ render projection an toàn do REST trả về. */
export default function ConversationList({ conversations, activeId, loading, error, hasMore, loadingMore, onLoadMore }) {
  const navigate = useNavigate();
  if (loading && !conversations.length) return <div className="p-6 text-sm text-[var(--app-muted)]">Đang tải hộp thư...</div>;
  if (error && !conversations.length) return <div role="alert" className="p-6 text-sm text-red-600">{error}</div>;
  if (!conversations.length) return <div className="p-6 text-sm text-[var(--app-muted)]">Không tìm thấy cuộc trò chuyện.</div>;
  return (
    <div>
      {conversations.map((conversation) => (
        <button key={conversation.conversationId} type="button" onClick={() => navigate(`/messages/${conversation.conversationId}`)}
          className={`flex w-full gap-3 px-5 py-4 text-left transition hover:bg-[var(--app-surface-soft)] ${String(activeId) === String(conversation.conversationId) ? 'bg-[var(--app-surface-soft)]' : ''}`}>
          <Avatar src={conversation.otherUser?.avatarUrl} name={conversation.otherUser?.displayName} size="md" className="!h-14 !w-14 text-lg" />
          <span className="min-w-0 flex-1">
            <span className="flex items-center justify-between gap-2"><strong className="truncate text-[15px]">{conversation.otherUser?.displayName}</strong><time className="shrink-0 text-xs text-[var(--app-muted)]">{formatTime(conversation.lastMessage?.createdAt)}</time></span>
            <span className="mt-1 flex items-center gap-2"><span className={`min-w-0 flex-1 truncate text-sm ${conversation.unreadCount > 0 ? 'font-semibold text-[var(--app-text)]' : 'text-[var(--app-muted)]'}`}>{conversation.lastMessage?.contentPreview || 'Đã gửi một ảnh'}</span>{conversation.unreadCount > 0 ? <span aria-label={`${conversation.unreadCount} tin nhắn chưa đọc`} className="min-w-5 rounded-full bg-[var(--app-brand)] px-1.5 text-center text-xs leading-5 text-white">{conversation.unreadCount > 99 ? '99+' : conversation.unreadCount}</span> : null}</span>
          </span>
        </button>
      ))}
      {hasMore ? <button type="button" disabled={loadingMore} onClick={onLoadMore} className="w-full p-3 text-sm font-semibold text-[var(--app-brand)] disabled:opacity-50">{loadingMore ? 'Đang tải...' : 'Tải thêm'}</button> : null}
    </div>
  );
}
