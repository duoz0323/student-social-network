import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { shortTime } from '../../../utils/formatters.js';
import ReportPostFlow from './ReportPostFlow.jsx';

export default function PostCard({ post, detail = false }) {
  const navigate = useNavigate();
  const { currentUserId, getUserById, data, toggleLike, toggleSave, updatePost, deletePost } = useApp();
  const [menuOpen, setMenuOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [reporting, setReporting] = useState(false);
  const [draft, setDraft] = useState(post.content);
  const [tags, setTags] = useState(post.hashtags.join(', '));
  const author = getUserById(post.authorId);
  const isOwner = post.authorId === currentUserId;
  const liked = data.likes.some((like) => like.postId === post.id && like.userId === currentUserId);
  const saved = data.savedPosts.some((item) => item.postId === post.id && item.userId === currentUserId);

  if (!author) return null;

  function saveEdit() {
    // Chi cap nhat noi dung va hashtag vi MVP khong cho sua anh sau khi dang.
    updatePost(post.id, { content: draft, hashtags: tags });
    setEditing(false);
  }

  function confirmDelete() {
    deletePost(post.id);
    setDeleting(false);
    if (detail) navigate('/feed/for-you');
  }

  return (
    <article className="grid grid-cols-[42px_minmax(0,1fr)] gap-3 border-b border-[var(--app-border)] bg-white px-5 py-4">
      <Link to={author.id === currentUserId ? '/profile/me' : `/profile/${author.id}`} className="pt-0.5">
        <Avatar src={author.avatarUrl} name={author.displayName} />
      </Link>

      <div className="min-w-0">
        <header className="flex items-start justify-between gap-3">
          <Link to={author.id === currentUserId ? '/profile/me' : `/profile/${author.id}`} className="min-w-0">
            <p className="truncate text-sm font-black text-[var(--app-text)]">
              {author.displayName}{' '}
              <span className="font-semibold text-[var(--app-muted)]">
                · {shortTime(post.createdAt)}
                {post.edited ? ' · da sua' : ''}
              </span>
            </p>
          </Link>
          <button
            className="rounded-full px-2 text-lg font-bold leading-6 text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]"
            onClick={() => setMenuOpen((value) => !value)}
          >
            ...
          </button>
        </header>

        <button className="mt-1 block w-full text-left" onClick={() => !detail && navigate(`/posts/${post.id}`)}>
          <p className="whitespace-pre-line text-[15px] leading-6 text-[var(--app-text)]">{post.content}</p>
          {post.hashtags.length ? (
            <div className="mt-1 flex flex-wrap gap-x-2 gap-y-1">
              {post.hashtags.map((tag) => (
                <span key={tag} className="text-[15px] font-semibold text-[var(--app-brand)]">#{tag}</span>
              ))}
            </div>
          ) : null}
          {post.imageUrls.length ? (
            <div className="mt-3 aspect-video overflow-hidden rounded-[14px] bg-[var(--app-surface-soft)]">
              <img src={post.imageUrls[0]} alt="Anh bai viet" className="h-full w-full object-cover" />
            </div>
          ) : null}
        </button>

        {menuOpen ? (
          <div className="mt-3 grid gap-1 rounded-[12px] border border-[var(--app-border)] bg-white p-2 text-sm shadow-sm">
            {isOwner ? (
              <>
                <button className="rounded-[10px] px-3 py-2 text-left font-semibold hover:bg-[var(--app-surface-soft)]" onClick={() => setEditing(true)}>
                  Chinh sua bai viet
                </button>
                <button className="rounded-[10px] px-3 py-2 text-left font-semibold text-red-600 hover:bg-red-50" onClick={() => setDeleting(true)}>
                  Xoa bai viet
                </button>
              </>
            ) : (
              <button className="rounded-[10px] px-3 py-2 text-left font-semibold hover:bg-[var(--app-surface-soft)]" onClick={() => setReporting(true)}>
                Bao cao bai viet
              </button>
            )}
            <button className="rounded-[10px] px-3 py-2 text-left font-semibold hover:bg-[var(--app-surface-soft)]" onClick={() => toggleSave(post.id)}>
              {saved ? 'Bo luu bai viet' : 'Luu bai viet'}
            </button>
          </div>
        ) : null}

        <footer className="mt-3 flex items-center gap-5">
          <button className={`post-action hover:text-[var(--app-text)] ${liked ? 'text-red-500' : ''}`} onClick={() => toggleLike(post.id)}>
            <span aria-hidden="true">{liked ? '♥' : '♡'}</span>
            <span>{post.likeCount}</span>
          </button>
          <button className="post-action hover:text-[var(--app-text)]" onClick={() => navigate(`/posts/${post.id}`)}>
            <span aria-hidden="true">□</span>
            <span>{post.commentCount}</span>
          </button>
          <button className={`post-action hover:text-[var(--app-text)] ${saved ? 'text-[var(--app-brand)]' : ''}`} onClick={() => toggleSave(post.id)}>
            <span aria-hidden="true">{saved ? '▣' : '⇧'}</span>
          </button>
        </footer>
      </div>

      <Modal
        open={editing}
        title="Chinh sua bai viet"
        onClose={() => setEditing(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setEditing(false)}>Huy</Button>
            <Button disabled={!draft.trim() || draft.length > 500} onClick={saveEdit}>Luu</Button>
          </>
        }
      >
        <textarea className="min-h-32 w-full rounded-2xl border border-zinc-200 p-3" value={draft} maxLength={500} onChange={(event) => setDraft(event.target.value)} />
        <input className="mt-3 w-full rounded-2xl border border-zinc-200 p-3" value={tags} onChange={(event) => setTags(event.target.value)} />
        <p className="mt-2 text-xs text-zinc-500">Anh da dang khong duoc chinh sua trong MVP.</p>
      </Modal>

      <Modal
        open={deleting}
        title="Xoa bai viet"
        onClose={() => setDeleting(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setDeleting(false)}>Huy</Button>
            <Button variant="danger" onClick={confirmDelete}>Xoa bai</Button>
          </>
        }
      >
        <p className="text-sm text-zinc-600">Bai viet se duoc xoa mem va khong hien thi trong feed, profile, search.</p>
      </Modal>

      <ReportPostFlow open={reporting} post={post} onClose={() => setReporting(false)} />
    </article>
  );
}
