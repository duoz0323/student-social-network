import { useEffect, useState } from 'react';
import { ChevronDown, MessageCircle, RefreshCw } from 'lucide-react';
import Avatar from '../../../../components/common/Avatar.jsx';
import PublicIdentityBadge from '../../../../components/common/PublicIdentityBadge.jsx';
import Button from '../../../../components/common/Button.jsx';
import { shortTime } from '../../../../utils/formatters.js';
import { collaboratorApi } from '../services/collaboratorApi.js';

const PAGE_SIZE = 20;

/** Hiển thị hội thoại trên bài của Managed Identity; khu vực này không cấp quyền sửa/xóa bình luận người dùng. */
export default function CollaboratorCommentDiscussion({ postId, commentCount, postStatus }) {
  const [comments, setComments] = useState([]);
  const [pageState, setPageState] = useState({ page: 0, last: true });
  const [replies, setReplies] = useState({});
  const [expandedIds, setExpandedIds] = useState(() => new Set());
  const [loading, setLoading] = useState(postStatus === 'PUBLISHED');
  const [loadingMore, setLoadingMore] = useState(false);
  const [loadingReplyId, setLoadingReplyId] = useState(null);
  const [error, setError] = useState('');

  async function loadComments(page = 0, signal) {
    const response = await collaboratorApi.getComments(postId, { page, size: PAGE_SIZE }, signal);
    setComments((current) => page === 0 ? (response.content ?? []) : [...current, ...(response.content ?? [])]);
    setPageState({ page: response.page ?? page, last: response.last ?? true });
  }

  useEffect(() => {
    if (postStatus !== 'PUBLISHED') return undefined;

    const controller = new AbortController();
    collaboratorApi.getComments(postId, { page: 0, size: PAGE_SIZE }, controller.signal)
      .then((response) => {
        setComments(response.content ?? []);
        setPageState({ page: response.page ?? 0, last: response.last ?? true });
      })
      .catch((requestError) => !controller.signal.aborted && setError(requestError.message))
      .finally(() => !controller.signal.aborted && setLoading(false));
    return () => controller.abort();
  }, [postId, postStatus]);

  async function retry() {
    setLoading(true);
    setError('');
    try {
      await loadComments(0);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadMore() {
    if (loadingMore || pageState.last) return;
    setLoadingMore(true);
    setError('');
    try {
      await loadComments(pageState.page + 1);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoadingMore(false);
    }
  }

  async function toggleReplies(comment) {
    const commentId = comment.commentId;
    if (expandedIds.has(commentId)) {
      setExpandedIds((current) => {
        const next = new Set(current);
        next.delete(commentId);
        return next;
      });
      return;
    }

    if (replies[commentId] === undefined) {
      setLoadingReplyId(commentId);
      setError('');
      try {
        const response = await collaboratorApi.getReplies(
          postId, commentId, { page: 0, size: PAGE_SIZE },
        );
        setReplies((current) => ({
          ...current,
          [commentId]: {
            content: response.content ?? [],
            page: response.page ?? 0,
            last: response.last ?? true,
          },
        }));
      } catch (requestError) {
        setError(requestError.message);
        return;
      } finally {
        setLoadingReplyId(null);
      }
    }
    setExpandedIds((current) => new Set(current).add(commentId));
  }

  async function loadMoreReplies(commentId) {
    const currentPage = replies[commentId];
    if (!currentPage || currentPage.last || loadingReplyId === commentId) return;
    setLoadingReplyId(commentId);
    setError('');
    try {
      const response = await collaboratorApi.getReplies(
        postId, commentId, { page: currentPage.page + 1, size: PAGE_SIZE },
      );
      setReplies((current) => ({
        ...current,
        [commentId]: {
          content: [...(current[commentId]?.content ?? []), ...(response.content ?? [])],
          page: response.page ?? currentPage.page + 1,
          last: response.last ?? true,
        },
      }));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoadingReplyId(null);
    }
  }

  return (
    <section className="overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-sm" aria-labelledby="collaborator-comments-title">
      <header className="flex items-start justify-between gap-4 border-b border-zinc-100 px-5 py-4">
        <div>
          <h2 id="collaborator-comments-title" className="font-bold text-zinc-950">Thảo luận về bài viết</h2>
          <p className="mt-1 text-xs text-zinc-500">Các bình luận công khai mà người dùng trao đổi trên bài viết của bạn.</p>
        </div>
        <span className="rounded-full bg-zinc-100 px-2.5 py-1 text-xs font-bold text-zinc-600">{Number(commentCount) || 0}</span>
      </header>

      {postStatus !== 'PUBLISHED' ? (
        <DiscussionMessage icon={MessageCircle} title="Không thể xem thảo luận" description="Bình luận chỉ hiển thị khi bài viết đang được công khai." />
      ) : loading ? (
        <DiscussionMessage spinning icon={RefreshCw} title="Đang tải bình luận" description="Vui lòng chờ trong giây lát." />
      ) : error && comments.length === 0 ? (
        <DiscussionMessage icon={MessageCircle} title="Không thể tải bình luận" description={error} action={<Button size="sm" variant="secondary" onClick={retry}>Thử lại</Button>} />
      ) : comments.length === 0 ? (
        <DiscussionMessage icon={MessageCircle} title="Chưa có bình luận" description="Khi người dùng thảo luận, bình luận sẽ xuất hiện tại đây." />
      ) : (
        <div>
          <div className="divide-y divide-zinc-100">
            {comments.map((comment) => (
              <CommentThread
                key={comment.commentId}
                comment={comment}
                replyPage={replies[comment.commentId]}
                expanded={expandedIds.has(comment.commentId)}
                loadingReplies={loadingReplyId === comment.commentId}
                onToggleReplies={() => toggleReplies(comment)}
                onLoadMoreReplies={() => loadMoreReplies(comment.commentId)}
              />
            ))}
          </div>
          {error ? <p className="mx-5 mb-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{error}</p> : null}
          {!pageState.last ? (
            <div className="border-t border-zinc-100 p-4 text-center">
              <Button size="sm" variant="secondary" loading={loadingMore} loadingLabel="Đang tải..." onClick={loadMore}>Xem thêm bình luận</Button>
            </div>
          ) : null}
        </div>
      )}
    </section>
  );
}

function CommentThread({ comment, replyPage, expanded, loadingReplies, onToggleReplies, onLoadMoreReplies }) {
  const replyCount = Number(comment.replyCount) || 0;
  const replies = replyPage?.content ?? [];
  return (
    <article className="flex gap-3 px-5 py-4">
      <Avatar src={comment.avatarUrl} name={comment.displayName} size="sm" viewable />
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
          <strong className="inline-flex min-w-0 items-center gap-1 text-sm text-zinc-950">
            <span className="truncate">{comment.displayName || 'Người dùng'}</span>
            <PublicIdentityBadge badges={comment.badges} />
          </strong>
          <span className="text-[11px] text-zinc-500">{shortTime(comment.createdAt)}</span>
        </div>
        <p className="mt-1 whitespace-pre-wrap break-words text-sm leading-6 text-zinc-800">{comment.content}</p>
        {replyCount > 0 ? (
          <button type="button" disabled={loadingReplies} onClick={onToggleReplies} className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-zinc-700 hover:text-zinc-950 disabled:opacity-60" aria-expanded={expanded}>
            {loadingReplies ? 'Đang tải câu trả lời...' : `${expanded ? 'Ẩn' : 'Xem'} ${replyCount} câu trả lời`}
            {!loadingReplies ? <ChevronDown size={14} className={expanded ? 'rotate-180' : ''} /> : null}
          </button>
        ) : null}
        {expanded && replies.length > 0 ? (
          <div className="mt-3 space-y-3 border-l border-zinc-200 pl-4">
            {replies.map((reply) => <ReplyItem key={reply.commentId} reply={reply} />)}
            {!replyPage.last ? (
              <button type="button" disabled={loadingReplies} onClick={onLoadMoreReplies} className="text-xs font-semibold text-zinc-700 hover:text-zinc-950 disabled:opacity-60">
                {loadingReplies ? 'Đang tải...' : 'Xem thêm câu trả lời'}
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </article>
  );
}

function ReplyItem({ reply }) {
  return (
    <article className="flex gap-2.5">
      <Avatar src={reply.avatarUrl} name={reply.displayName} size="sm" viewable />
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
          <strong className="inline-flex min-w-0 items-center gap-1 text-xs text-zinc-950"><span className="truncate">{reply.displayName || 'Người dùng'}</span><PublicIdentityBadge badges={reply.badges} /></strong>
          <span className="text-[10px] text-zinc-500">{shortTime(reply.createdAt)}</span>
        </div>
        <p className="mt-0.5 whitespace-pre-wrap break-words text-sm leading-5 text-zinc-700">{reply.content}</p>
      </div>
    </article>
  );
}

function DiscussionMessage({ icon: Icon, title, description, action, spinning = false }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center px-5 py-8 text-center">
      <Icon size={28} className={`mb-3 text-zinc-400 ${spinning ? 'animate-spin' : ''}`} />
      <h3 className="text-sm font-bold text-zinc-900">{title}</h3>
      <p className="mt-1 max-w-md text-xs leading-5 text-zinc-500">{description}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
