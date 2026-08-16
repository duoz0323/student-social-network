import { Heart, MessageCircle, Repeat2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import { formatNumber } from '../../../utils/formatters.js';

/** Card bài viết trong Message chỉ render projection mà Backend đã lọc theo viewer hiện tại. */
export default function SharedPostMessage({ post, unavailable }) {
  const navigate = useNavigate();
  if (unavailable || !post) {
    return (
      <div className="rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface-soft)] px-4 py-5 text-center text-sm font-semibold text-[var(--app-muted)]">
        Bài viết không còn khả dụng
      </div>
    );
  }

  const postId = post.postId ?? post.id;
  const media = post.media ?? [];
  return (
    <button type="button" onClick={() => navigate(`/posts/${postId}`)}
      aria-label={`Mở bài viết của ${post.author?.displayName ?? 'người dùng'}`}
      className="block w-full overflow-hidden rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] text-left shadow-sm transition hover:border-[var(--app-border-strong)] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--app-brand)]">
      <div className="p-4">
        <div className="flex items-center gap-2.5">
          <Avatar src={post.author?.avatarUrl} name={post.author?.displayName} size="sm" />
          <span className="min-w-0">
            <strong className="block truncate text-sm">{post.author?.displayName}</strong>
            {post.author?.username ? <span className="block truncate text-xs text-[var(--app-muted)]">@{post.author.username}</span> : null}
          </span>
        </div>
        {post.content ? <p className="mt-3 line-clamp-4 whitespace-pre-wrap break-words text-sm leading-5">{post.content}</p> : null}
      </div>
      {media[0]?.url ? <img src={media[0].url} alt="Media bài viết được chia sẻ" className="max-h-64 w-full border-y border-[var(--app-border)] object-cover" /> : null}
      <div className="flex items-center gap-5 px-4 py-3 text-xs text-[var(--app-muted)]">
        <span className="inline-flex items-center gap-1.5"><Heart size={15} />{formatNumber(post.likeCount)}</span>
        <span className="inline-flex items-center gap-1.5"><MessageCircle size={15} />{formatNumber(post.commentCount)}</span>
        <span className="inline-flex items-center gap-1.5"><Repeat2 size={15} />{formatNumber(post.repostCount)}</span>
      </div>
    </button>
  );
}
