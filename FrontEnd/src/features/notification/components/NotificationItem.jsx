import {
  CheckCircle2,
  Heart,
  LockKeyhole,
  MessageCircle,
  MessageSquareReply,
  RotateCcw,
  ShieldAlert,
  UnlockKeyhole,
  UserPlus,
} from 'lucide-react';
import Avatar from '../../../components/common/Avatar.jsx';
import { formatDateTime } from '../../../utils/formatters.js';

const TYPE_PRESENTATION = {
  FOLLOW: {
    message: 'đã bắt đầu theo dõi bạn',
    icon: UserPlus,
    iconClass: 'bg-violet-500 text-white',
  },
  POST_LIKE: {
    message: 'đã thích bài viết của bạn',
    icon: Heart,
    iconClass: 'bg-rose-500 text-white',
  },
  POST_REPOST: {
    message: 'đã đăng lại bài viết của bạn',
    icon: RotateCcw,
    iconClass: 'bg-emerald-500 text-white',
  },
  POST_COMMENT: {
    message: 'đã bình luận về bài viết của bạn',
    icon: MessageCircle,
    iconClass: 'bg-sky-500 text-white',
  },
  COMMENT_REPLY: {
    message: 'đã trả lời bình luận của bạn',
    icon: MessageSquareReply,
    iconClass: 'bg-cyan-600 text-white',
  },
  REPORT_RESOLVED: {
    message: 'đã chấp nhận báo cáo của bạn',
    icon: CheckCircle2,
    iconClass: 'bg-emerald-500 text-white',
  },
  REPORT_REJECTED: {
    message: 'đã từ chối báo cáo của bạn',
    icon: ShieldAlert,
    iconClass: 'bg-amber-500 text-white',
  },
  POST_HIDDEN_BY_ADMIN: {
    message: 'đã ẩn bài viết của bạn',
    icon: ShieldAlert,
    iconClass: 'bg-orange-500 text-white',
  },
  POST_RESTORED_BY_ADMIN: {
    message: 'đã khôi phục bài viết của bạn',
    icon: RotateCcw,
    iconClass: 'bg-emerald-500 text-white',
  },
  PROFILE_UPDATED_BY_ADMIN: {
    message: 'đã điều chỉnh hồ sơ của bạn vì nội dung vi phạm Tiêu chuẩn hệ thống',
    icon: ShieldAlert,
    iconClass: 'bg-orange-600 text-white',
  },
  ACCOUNT_BLOCKED: {
    message: 'đã khóa tài khoản của bạn',
    icon: LockKeyhole,
    iconClass: 'bg-red-600 text-white',
  },
  ACCOUNT_UNBLOCKED: {
    message: 'đã mở khóa tài khoản của bạn',
    icon: UnlockKeyhole,
    iconClass: 'bg-emerald-500 text-white',
  },
};

export default function NotificationItem({
  item,
  opening = false,
  deleting = false,
  onOpen,
  onDelete,
}) {
  const presentation = TYPE_PRESENTATION[item.type] ?? {
    message: 'đã gửi cho bạn một thông báo mới',
    icon: CheckCircle2,
    iconClass: 'bg-violet-600 text-white',
  };
  const EventIcon = presentation.icon;
  const actorName = item.actor?.displayName || 'UniShare';
  const unread = !item.readAt;

  return (
    <article
      className={`group relative flex items-stretch border-b border-[var(--app-border)] transition-colors last:border-b-0 ${
        unread
          ? 'bg-[color-mix(in_srgb,var(--app-brand)_3.5%,var(--app-surface))]'
          : 'bg-[var(--app-surface)] hover:bg-[var(--app-surface-soft)]'
      }`}
    >
      <button
        type="button"
        className="flex min-w-0 flex-1 items-center gap-3.5 px-4 py-[15px] text-left outline-none transition focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-[var(--app-brand)] sm:px-6 sm:py-4"
        disabled={opening || deleting}
        onClick={() => onOpen(item)}
        aria-label={`${actorName} ${presentation.message}`}
      >
        <span className="relative shrink-0">
          <Avatar
            src={item.actor?.avatarUrl}
            name={actorName}
            className="!h-11 !w-11"
          />
          <span
            className={`absolute -bottom-1 -right-1 flex h-[21px] w-[21px] items-center justify-center rounded-full border-2 border-[var(--app-surface)] ${presentation.iconClass}`}
            aria-hidden="true"
          >
            <EventIcon size={10.5} strokeWidth={2.5} />
          </span>
        </span>

        <span className="min-w-0 flex-1">
          <span className="block text-[14px] leading-[20px] text-[var(--app-text)] sm:text-[15px] sm:leading-[21px]">
            <strong className={unread ? 'font-bold' : 'font-semibold'}>{actorName}</strong>{' '}
            <span className={unread ? '' : 'text-[var(--app-muted)]'}>
              {presentation.message}
            </span>
            <time className="ml-1 whitespace-nowrap text-[13px] font-normal text-[var(--app-muted)]" dateTime={item.createdAt}>
              · {formatDateTime(item.createdAt)}
            </time>
          </span>
        </span>

        {opening ? (
          <span
            className="mt-1 h-4 w-4 shrink-0 animate-spin rounded-full border-2 border-[var(--app-border-strong)] border-t-[var(--app-brand)]"
            aria-label="Đang mở thông báo"
          />
        ) : unread ? (
          <span
            className="h-2 w-2 shrink-0 rounded-full bg-[var(--app-brand)]"
            aria-hidden="true"
          />
        ) : null}
      </button>

      {deleting ? (
        <span className="mr-4 self-center text-[13px] font-semibold text-[var(--app-muted)] sm:mr-6">
          Đang xóa...
        </span>
      ) : (
        <button
          type="button"
          className="mr-3 self-center rounded-lg px-2.5 py-1.5 text-[13px] font-semibold text-[var(--app-muted)] transition hover:bg-red-500/[0.08] hover:text-red-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500/20 sm:mr-5"
          disabled={opening}
          onClick={() => onDelete(item)}
          aria-label={`Xóa thông báo từ ${actorName}`}
        >
          Xóa
        </button>
      )}
    </article>
  );
}
