import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { postApi } from '../../../api/index.js';
import Avatar from '../../../components/common/Avatar.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { shortTime } from '../../../utils/formatters.js';
import PostCard from '../components/PostCard.jsx';
import { toPostView } from '../utils/postViewModel.js';

export default function PostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { currentUser, userRelationshipRevision } = useApp();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [comment, setComment] = useState('');
  const [replies, setReplies] = useState({});
  const [replyDrafts, setReplyDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [loadedRequestKey, setLoadedRequestKey] = useState(null);
  const [error, setError] = useState('');
  const requestKey = `${postId}:${userRelationshipRevision}`;

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      postApi.getDetail(postId, controller.signal),
      postApi.getComments(postId, { page: 0, size: 50 }, controller.signal),
    ]).then(([postResponse, commentPage]) => {
      setPost(toPostView(postResponse));
      setComments(commentPage.content ?? []);
      setReplies({});
      setError('');
      setLoadedRequestKey(requestKey);
    }).catch((requestError) => {
      if (requestError.code !== 'ERR_CANCELED') {
        setPost(null);
        setComments([]);
        setReplies({});
        setError(requestError.message);
        setLoadedRequestKey(requestKey);
      }
    }).finally(() => {
      if (!controller.signal.aborted) setLoading(false);
    });
    return () => controller.abort();
  }, [postId, requestKey]);

  async function submitComment(event) {
    event.preventDefault();
    if (!comment.trim()) return;
    try {
      const created = await postApi.createComment(postId, comment.trim());
      setComments((items) => [...items, created]);
      setPost((item) => ({ ...item, commentCount: (Number(item.commentCount) || 0) + 1 }));
      setComment('');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function removeComment(commentId) {
    try {
      await postApi.deleteComment(commentId);
      setComments((items) => items.filter((item) => item.commentId !== commentId));
      // Backend trigger đã giảm posts.comment_count; cập nhật cùng thay đổi lên giao diện.
      setPost((item) => ({ ...item, commentCount: Math.max(0, (Number(item.commentCount) || 0) - 1) }));
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function loadReplies(commentId) {
    try {
      const page = await postApi.getReplies(commentId, { page: 0, size: 50 });
      setReplies((current) => ({ ...current, [commentId]: page.content ?? [] }));
    } catch (requestError) { setError(requestError.message); }
  }

  async function submitReply(commentId) {
    const content = replyDrafts[commentId]?.trim();
    if (!content) return;
    try {
      const created = await postApi.createReply(commentId, content);
      setReplies((current) => ({ ...current, [commentId]: [...(current[commentId] ?? []), created] }));
      // Reply cũng là một bản ghi comments nên được tính vào bộ đếm bình luận của bài viết.
      setPost((item) => ({ ...item, commentCount: (Number(item.commentCount) || 0) + 1 }));
      setReplyDrafts((current) => ({ ...current, [commentId]: '' }));
    } catch (requestError) { setError(requestError.message); }
  }

  const header = (
    <div className="flex h-[var(--header-height)] items-center px-6">
      <button onClick={() => navigate(-1)} aria-label="Quay lại" className="mr-4 text-xl">←</button>
      <h1 className="text-base font-bold">Bài viết</h1>
    </div>
  );

  // Khi Block/Unblock đổi revision, che snapshot cũ cho tới khi request mới hoàn tất.
  if (loading || loadedRequestKey !== requestKey) {
    return <ContentShell header={header}><LoadingState /></ContentShell>;
  }
  if (!post) return (
    <ContentShell header={header}>
      <EmptyState title="Không tìm thấy bài viết" description={error || 'Bài viết có thể đã bị ẩn hoặc xóa.'} actionLabel="Về feed" onAction={() => navigate('/feed/for-you')} />
    </ContentShell>
  );

  return (
    <ContentShell header={header}>
      <PostCard post={post} detail />
      <form className="flex items-center gap-3 border-b border-[var(--app-border)] px-6 py-4" onSubmit={submitComment}>
        <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} size="sm" />
        <input value={comment} onChange={(event) => setComment(event.target.value)} maxLength={500}
          className="flex-1 bg-transparent px-2 py-2 outline-none" placeholder="Viết bình luận..." />
        <button type="submit" disabled={!comment.trim()} className="font-bold text-blue-600 disabled:opacity-40">Đăng</button>
      </form>
      {error && <p className="app-error m-4 rounded-xl p-3 text-sm">{error}</p>}
      {comments.length === 0 ? (
        <EmptyState title="Chưa có bình luận" description="Hãy là người đầu tiên bình luận." />
      ) : comments.map((item) => (
        <article key={item.commentId} className="flex gap-3 border-b border-[var(--app-border)] px-6 py-4">
          <Avatar src={item.avatarUrl} name={item.displayName} size="sm" />
          <div className="min-w-0 flex-1">
            <div className="flex items-center justify-between">
              <p className="font-bold">{item.displayName} <span className="font-normal text-[var(--app-muted)]">· {shortTime(item.createdAt)}</span></p>
              {String(item.userId) === String(currentUser.id) && (
                <button onClick={() => removeComment(item.commentId)} className="text-xs text-red-600">Xóa</button>
              )}
            </div>
            <p className="mt-1 whitespace-pre-wrap">{item.content}</p>
            {item.replyCount > 0 && replies[item.commentId] === undefined && <button onClick={() => loadReplies(item.commentId)} className="mt-2 text-xs font-semibold text-blue-600">Xem {item.replyCount} phản hồi</button>}
            {(replies[item.commentId] ?? []).map((reply) => <div key={reply.commentId} className="mt-3 border-l-2 pl-3"><strong>{reply.displayName}</strong><p>{reply.content}</p></div>)}
            <div className="mt-3 flex gap-2"><input value={replyDrafts[item.commentId] ?? ''} onChange={(event) => setReplyDrafts((current) => ({ ...current, [item.commentId]: event.target.value }))} placeholder="Viết phản hồi..." className="app-field min-w-0 flex-1 rounded-lg border px-3 py-1 text-sm outline-none transition" /><button onClick={() => submitReply(item.commentId)} className="text-sm font-semibold text-[var(--app-brand)]">Trả lời</button></div>
          </div>
        </article>
      ))}
    </ContentShell>
  );
}
