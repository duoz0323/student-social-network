import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { formatDateTime, shortTime } from '../../../utils/formatters.js';
import PostCard from '../components/PostCard.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';

export default function PostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { getPostById, data, getUserById, currentUserId, addComment, deleteComment } = useApp();
  const [comment, setComment] = useState('');
  const post = getPostById(postId);

  if (!post) return <EmptyState title="Không tìm thấy bài viết" description="Bài viết có thể đã bị ẩn hoặc đã xóa." actionLabel="Về feed" onAction={() => navigate('/feed/for-you')} />;

  const comments = data.comments.filter((item) => item.postId === post.id);
  const postAuthor = getUserById(post.authorId);
  const postHandle = postAuthor?.email ? `@${postAuthor.email.split('@')[0]}` : `@user${postAuthor?.id?.slice(-4)}`;

  function submitComment(event) {
    event.preventDefault();
    if (!comment.trim()) return;
    addComment(post.id, comment);
    setComment('');
  }

  const headerContent = (
    <div className="flex h-[var(--header-height)] items-center justify-between px-6">
      <div className="flex w-8 items-center justify-start">
        <button
          className="flex h-8 w-8 items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)]"
          onClick={() => navigate(-1)}
          aria-label="Quay lại"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="19" x2="5" y1="12" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
      </div>
      <div className="flex flex-col items-center">
        <h1 className="text-[16px] font-bold text-[var(--app-text)] leading-tight">Bài viết</h1>
        <span className="text-[13px] text-[var(--app-muted)] leading-tight">2,7K lượt xem</span>
      </div>
      <div className="flex w-8 items-center justify-end">
        <button className="flex h-[34px] w-[34px] items-center justify-center rounded-full text-[var(--app-text)] transition hover:bg-[var(--app-surface-soft)] border border-[var(--app-border)]" aria-label="Tùy chọn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="1.5"/>
            <circle cx="19" cy="12" r="1.5"/>
            <circle cx="5" cy="12" r="1.5"/>
          </svg>
        </button>
      </div>
    </div>
  );

  return (
    <ContentShell header={headerContent}>
      <div className="pb-20">
        <PostCard post={post} detail />
        
        <div className="flex items-center justify-between border-y border-[var(--app-border)] px-6 py-3 bg-[var(--app-surface)]">
          <button className="flex items-center gap-1 text-[14px] font-semibold text-[var(--app-muted)] transition hover:text-[var(--app-text)]">
            Hàng đầu
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="m7 15 5 5 5-5"/><path d="m7 9 5-5 5 5"/></svg>
          </button>
          <button className="flex items-center gap-1 text-[14px] font-semibold text-[var(--app-muted)] transition hover:text-[var(--app-text)]">
            Xem hoạt động
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="m9 18 6-6-6-6"/></svg>
          </button>
        </div>
        
        <div className="border-b border-[var(--app-border)] bg-[var(--app-surface)]">
          <form className="flex items-center gap-3 px-6 py-4" onSubmit={submitComment}>
            <Avatar src={getUserById(currentUserId)?.avatarUrl} name={getUserById(currentUserId)?.displayName} size="sm" className="!w-9 !h-9" />
            <div className="flex flex-1 items-center justify-between">
              <input
                value={comment}
                onChange={(event) => setComment(event.target.value)}
                className="w-full bg-transparent px-1 py-2 text-[15px] outline-none placeholder:text-[var(--app-muted)] text-[var(--app-text)]"
                placeholder={`Trả lời ${postHandle}...`}
              />
              <div className="flex items-center gap-3 text-zinc-400">
                <button type="button" className="transition hover:text-zinc-600"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg></button>
                <button type="button" className="transition hover:text-zinc-600"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="M12 8v8"/><path d="M8 8h8"/></svg></button>
                <button type="button" className="transition hover:text-zinc-600"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/></svg></button>
                {comment.trim() && <button type="submit" className="text-blue-500 font-bold ml-1 transition hover:text-blue-600">Đăng</button>}
              </div>
            </div>
          </form>
        </div>

        <div className="flex flex-col">
          {comments.length ? (
            comments.map((item) => {
              const author = getUserById(item.authorId);
              return (
                <div key={item.id} className="flex gap-3 px-6 py-4 border-b border-[var(--app-border)]">
                  <div className="flex flex-col items-center">
                    <Avatar src={author.avatarUrl} name={author.displayName} size="sm" className="!w-9 !h-9 cursor-pointer" onClick={() => navigate(`/profile/${author.id}`)} />
                    <div className="mt-2 w-[2px] grow bg-[var(--app-border)]"></div>
                  </div>
                  <div className="flex flex-1 flex-col pb-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate(`/profile/${author.id}`)}>
                        <span className="text-[15px] font-bold text-[var(--app-text)] hover:underline">{author.displayName}</span>
                        <span className="text-[14px] text-[var(--app-muted)]">{shortTime(item.createdAt)}</span>
                      </div>
                      <button className="text-[var(--app-text)] transition hover:text-zinc-500">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
                      </button>
                    </div>
                    <p className="mt-0.5 text-[15px] leading-[1.4] text-[var(--app-text)]">{item.content}</p>
                    <div className="mt-3 flex items-center gap-4 text-zinc-500">
                      <button className="flex items-center gap-1.5 transition hover:text-[var(--app-text)]"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg></button>
                      <button className="flex items-center gap-1.5 transition hover:text-[var(--app-text)]"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></button>
                      <button className="flex items-center gap-1.5 transition hover:text-[var(--app-text)]"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></button>
                      <button className="flex items-center gap-1.5 transition hover:text-[var(--app-text)]"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="22" x2="11" y1="2" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></button>
                    </div>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="px-5 py-12 text-center text-[15px] text-[var(--app-muted)]">Chưa có bình luận nào. Hãy là người đầu tiên bình luận!</div>
          )}
        </div>
      </div>
    </ContentShell>
  );
}
