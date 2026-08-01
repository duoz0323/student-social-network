import { useState } from 'react';
import Avatar from '../../../components/common/Avatar.jsx';
import { shortTime } from '../../../utils/formatters.js';
import './CommentSection.css';

function ChevronIcon({ expanded }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 20 20" className="comment-chevron">
      <path d={expanded ? 'm5 12 5-5 5 5' : 'm5 8 5 5 5-5'} />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 20 20" className="comment-trash-icon">
      <path d="M3.5 5.5h13M8 3.5h4M6 5.5l.7 10h6.6l.7-10M8.5 8v4.5M11.5 8v4.5" />
    </svg>
  );
}

function CommentIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="comment-empty-icon">
      <path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5 8.7 8.7 0 0 1-3.2-.6L4 20l1.6-4.3A7.5 7.5 0 1 1 20 11.5Z" />
    </svg>
  );
}

function CommentComposer({
  currentUser,
  value,
  onChange,
  onSubmit,
  submitting,
}) {
  async function handleSubmit(event) {
    event.preventDefault();
    if (!value.trim() || submitting) return;
    await onSubmit(value.trim());
  }

  return (
    <form className="comment-composer" onSubmit={handleSubmit}>
      <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} size="sm" />
      <div className="comment-composer__field">
        <textarea
          value={value}
          onChange={(event) => onChange(event.target.value)}
          maxLength={500}
          rows={1}
          aria-label="Nội dung bình luận"
          placeholder="Viết bình luận của bạn..."
        />
        <button type="submit" disabled={!value.trim() || submitting}>
          {submitting ? 'Đang đăng...' : 'Đăng'}
        </button>
      </div>
    </form>
  );
}

function ReplyComposer({
  currentUser,
  displayName,
  value,
  onChange,
  onCancel,
  onSubmit,
  submitting,
}) {
  async function handleSubmit(event) {
    event.preventDefault();
    if (!value.trim() || submitting) return;
    await onSubmit(value.trim());
  }

  return (
    <form className="reply-composer" onSubmit={handleSubmit}>
      <Avatar
        src={currentUser.avatarUrl}
        name={currentUser.displayName}
        size="sm"
        className="reply-avatar"
      />
      <div className="reply-composer__body">
        <div className="reply-composer__context">
          <span>Đang trả lời {displayName}</span>
          <button type="button" onClick={onCancel}>Hủy</button>
        </div>
        <div className="reply-composer__field">
          <textarea
            autoFocus
            value={value}
            onChange={(event) => onChange(event.target.value)}
            maxLength={500}
            rows={1}
            aria-label={`Trả lời ${displayName}`}
            placeholder="Viết câu trả lời..."
          />
          <button type="submit" disabled={!value.trim() || submitting}>
            {submitting ? 'Đang gửi...' : 'Gửi'}
          </button>
        </div>
      </div>
    </form>
  );
}

function ReplyItem({ reply, currentUser, deleting, onDelete }) {
  const isOwner = String(reply.userId) === String(currentUser.id);

  return (
    <article className="comment-reply">
      <Avatar
        src={reply.avatarUrl}
        name={reply.displayName}
        size="sm"
        className="reply-avatar"
      />
      <div className="comment-reply__body">
        <div className="comment-author-row">
          <div className="comment-author">
            <strong>{reply.displayName}</strong>
            <span>{shortTime(reply.createdAt)}</span>
          </div>
          {isOwner ? (
            <button
              type="button"
              className="comment-delete"
              disabled={deleting}
              onClick={onDelete}
              aria-label="Xóa câu trả lời"
              title="Xóa câu trả lời"
            >
              <TrashIcon />
            </button>
          ) : null}
        </div>
        <p className="comment-content">{reply.content}</p>
      </div>
    </article>
  );
}

export default function CommentSection({
  currentUser,
  comments,
  commentCount,
  commentValue,
  onCommentChange,
  onSubmitComment,
  onDeleteComment,
  replies,
  replyDrafts,
  onReplyDraftChange,
  onLoadReplies,
  onSubmitReply,
  onDeleteReply,
  error,
}) {
  const [activeReplyId, setActiveReplyId] = useState(null);
  const [expandedReplyIds, setExpandedReplyIds] = useState(() => new Set());
  const [loadingReplyId, setLoadingReplyId] = useState(null);
  const [submittingComment, setSubmittingComment] = useState(false);
  const [submittingReplyId, setSubmittingReplyId] = useState(null);
  const [deletingCommentId, setDeletingCommentId] = useState(null);

  async function submitComment(content) {
    setSubmittingComment(true);
    try {
      await onSubmitComment(content);
    } finally {
      setSubmittingComment(false);
    }
  }

  async function toggleReplies(commentId) {
    if (expandedReplyIds.has(commentId)) {
      setExpandedReplyIds((current) => {
        const next = new Set(current);
        next.delete(commentId);
        return next;
      });
      return;
    }

    if (replies[commentId] === undefined) {
      setLoadingReplyId(commentId);
      try {
        const loadedReplies = await onLoadReplies(commentId);
        if (loadedReplies === null) return;
      } finally {
        setLoadingReplyId(null);
      }
    }

    setExpandedReplyIds((current) => new Set(current).add(commentId));
  }

  function openReplyComposer(commentId) {
    setActiveReplyId(commentId);
  }

  async function submitReply(commentId, content) {
    setSubmittingReplyId(commentId);
    try {
      const createdReply = await onSubmitReply(commentId, content);
      if (!createdReply) return;
      setExpandedReplyIds((current) => new Set(current).add(commentId));
      setActiveReplyId(null);
    } finally {
      setSubmittingReplyId(null);
    }
  }

  async function deleteItem(commentId, parentCommentId = null) {
    setDeletingCommentId(commentId);
    try {
      if (parentCommentId) await onDeleteReply(parentCommentId, commentId);
      else await onDeleteComment(commentId);
    } finally {
      setDeletingCommentId(null);
    }
  }

  return (
    <section className="comment-section" aria-labelledby="comment-section-title">
      <header className="comment-section__header">
        <div>
          <h2 id="comment-section-title">Bình luận</h2>
          <p>Chia sẻ suy nghĩ của bạn về bài viết.</p>
        </div>
        <span className="comment-section__count">{Number(commentCount) || 0}</span>
      </header>

      <CommentComposer
        currentUser={currentUser}
        value={commentValue}
        onChange={onCommentChange}
        onSubmit={submitComment}
        submitting={submittingComment}
      />

      {error ? <p className="comment-section__error" role="alert">{error}</p> : null}

      {comments.length === 0 ? (
        <div className="comment-empty">
          <CommentIcon />
          <h3>Chưa có bình luận</h3>
          <p>Hãy là người đầu tiên để lại bình luận.</p>
        </div>
      ) : (
        <div className="comment-list">
          {comments.map((item) => {
            const isOwner = String(item.userId) === String(currentUser.id);
            const itemReplies = replies[item.commentId] ?? [];
            const repliesExpanded = expandedReplyIds.has(item.commentId);
            const replyCount = Number(item.replyCount) || 0;

            return (
              <article key={item.commentId} className="comment-thread">
                <Avatar src={item.avatarUrl} name={item.displayName} size="sm" />
                <div className="comment-thread__body">
                  <div className="comment-author-row">
                    <div className="comment-author">
                      <strong>{item.displayName}</strong>
                      <span>{shortTime(item.createdAt)}</span>
                    </div>
                    {isOwner ? (
                      <button
                        type="button"
                        className="comment-delete"
                        disabled={deletingCommentId === item.commentId}
                        onClick={() => deleteItem(item.commentId)}
                        aria-label="Xóa bình luận"
                        title="Xóa bình luận"
                      >
                        <TrashIcon />
                      </button>
                    ) : null}
                  </div>

                  <p className="comment-content">{item.content}</p>

                  <div className="comment-actions">
                    <button type="button" onClick={() => openReplyComposer(item.commentId)}>
                      Trả lời
                    </button>
                    {replyCount > 0 ? (
                      <button
                        type="button"
                        className="comment-reply-toggle"
                        onClick={() => toggleReplies(item.commentId)}
                        disabled={loadingReplyId === item.commentId}
                        aria-expanded={repliesExpanded}
                      >
                        {loadingReplyId === item.commentId
                          ? 'Đang tải...'
                          : `${repliesExpanded ? 'Ẩn' : 'Xem'} ${replyCount} câu trả lời`}
                        {loadingReplyId !== item.commentId ? <ChevronIcon expanded={repliesExpanded} /> : null}
                      </button>
                    ) : null}
                  </div>

                  {repliesExpanded && itemReplies.length > 0 ? (
                    <div className="comment-replies">
                      {itemReplies.map((reply) => (
                        <ReplyItem
                          key={reply.commentId}
                          reply={reply}
                          currentUser={currentUser}
                          deleting={deletingCommentId === reply.commentId}
                          onDelete={() => deleteItem(reply.commentId, item.commentId)}
                        />
                      ))}
                    </div>
                  ) : null}

                  {activeReplyId === item.commentId ? (
                    <ReplyComposer
                      currentUser={currentUser}
                      displayName={item.displayName}
                      value={replyDrafts[item.commentId] ?? ''}
                      onChange={(value) => onReplyDraftChange(item.commentId, value)}
                      onCancel={() => setActiveReplyId(null)}
                      onSubmit={(content) => submitReply(item.commentId, content)}
                      submitting={submittingReplyId === item.commentId}
                    />
                  ) : null}
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
