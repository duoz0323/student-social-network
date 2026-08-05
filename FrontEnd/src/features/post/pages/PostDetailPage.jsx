import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { postApi } from '../../../api/index.js';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import CommentSection from '../components/CommentSection.jsx';
import PostCard from '../components/PostCard.jsx';
import { toPostView } from '../utils/postViewModel.js';
import { publishPostActivity, subscribePostActivity } from '../utils/postActivitySync.js';

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

  useEffect(() => subscribePostActivity((activity) => {
    if (String(activity?.postId) !== String(postId)) return;
    if (Number.isFinite(activity.commentCount)) {
      setPost((current) => current ? { ...current, commentCount: Math.max(0, activity.commentCount) } : current);
    }
  }), [postId]);

  async function submitComment(content) {
    try {
      const created = await postApi.createComment(postId, content);
      setComments((items) => [...items, created]);
      const nextCommentCount = (Number(post?.commentCount) || 0) + 1;
      setPost((item) => ({ ...item, commentCount: nextCommentCount }));
      publishPostActivity({ postId, commentCount: nextCommentCount });
      setComment('');
      setError('');
      return created;
    } catch (requestError) {
      setError(requestError.message);
      return null;
    }
  }

  async function removeComment(commentId) {
    try {
      await postApi.deleteComment(commentId);
      setComments((items) => items.filter((item) => item.commentId !== commentId));
      // Backend trigger đã giảm posts.comment_count; cập nhật cùng thay đổi lên giao diện.
      const nextCommentCount = Math.max(0, (Number(post?.commentCount) || 0) - 1);
      setPost((item) => ({ ...item, commentCount: nextCommentCount }));
      publishPostActivity({ postId, commentCount: nextCommentCount });
      setError('');
    } catch (requestError) {
      setError(requestError.message);
      return false;
    }
  }

  async function loadReplies(commentId) {
    try {
      const page = await postApi.getReplies(commentId, { page: 0, size: 50 });
      setReplies((current) => ({ ...current, [commentId]: page.content ?? [] }));
      setError('');
      return page.content ?? [];
    } catch (requestError) {
      setError(requestError.message);
      return null;
    }
  }

  async function submitReply(commentId, content) {
    try {
      const created = await postApi.createReply(commentId, content);
      setReplies((current) => ({ ...current, [commentId]: [...(current[commentId] ?? []), created] }));
      setComments((items) => items.map((item) => (
        item.commentId === commentId
          ? { ...item, replyCount: (Number(item.replyCount) || 0) + 1 }
          : item
      )));
      // Reply cũng là một bản ghi comments nên được tính vào bộ đếm bình luận của bài viết.
      const nextCommentCount = (Number(post?.commentCount) || 0) + 1;
      setPost((item) => ({ ...item, commentCount: nextCommentCount }));
      publishPostActivity({ postId, commentCount: nextCommentCount });
      setReplyDrafts((current) => ({ ...current, [commentId]: '' }));
      setError('');
      return created;
    } catch (requestError) {
      setError(requestError.message);
      return null;
    }
  }

  async function removeReply(parentCommentId, replyId) {
    try {
      await postApi.deleteComment(replyId);
      setReplies((current) => ({
        ...current,
        [parentCommentId]: (current[parentCommentId] ?? [])
          .filter((item) => item.commentId !== replyId),
      }));
      setComments((items) => items.map((item) => (
        item.commentId === parentCommentId
          ? { ...item, replyCount: Math.max(0, (Number(item.replyCount) || 0) - 1) }
          : item
      )));
      // Reply đã xóa mềm cũng làm bộ đếm tổng của bài viết giảm theo trigger Backend.
      const nextCommentCount = Math.max(0, (Number(post?.commentCount) || 0) - 1);
      setPost((item) => ({ ...item, commentCount: nextCommentCount }));
      publishPostActivity({ postId, commentCount: nextCommentCount });
      setError('');
    } catch (requestError) {
      setError(requestError.message);
      return false;
    }
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
      <CommentSection
        key={requestKey}
        currentUser={currentUser}
        comments={comments}
        commentCount={post.commentCount}
        commentValue={comment}
        onCommentChange={setComment}
        onSubmitComment={submitComment}
        onDeleteComment={removeComment}
        replies={replies}
        replyDrafts={replyDrafts}
        onReplyDraftChange={(commentId, value) => {
          setReplyDrafts((current) => ({ ...current, [commentId]: value }));
        }}
        onLoadReplies={loadReplies}
        onSubmitReply={submitReply}
        onDeleteReply={removeReply}
        error={error}
      />
    </ContentShell>
  );
}
