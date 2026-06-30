import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import PostCard from '../components/PostCard.jsx';

export default function PostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { getPostById, data, getUserById, currentUserId, addComment, deleteComment } = useApp();
  const [comment, setComment] = useState('');
  const post = getPostById(postId);

  if (!post) return <EmptyState title="Khong tim thay bai viet" description="Bai viet co the da bi an hoac da xoa." actionLabel="Ve feed" onAction={() => navigate('/feed/for-you')} />;

  const comments = data.comments.filter((item) => item.postId === post.id);

  function submitComment(event) {
    event.preventDefault();
    addComment(post.id, comment);
    setComment('');
  }

  return (
    <section className="space-y-4 pb-20">
      <Button variant="ghost" onClick={() => navigate(-1)}>Quay lai</Button>
      <PostCard post={post} detail />
      <div className="rounded-2xl border border-zinc-200 bg-white p-4">
        <h2 className="text-lg font-black">Binh luan</h2>
        <form className="mt-4 flex gap-2" onSubmit={submitComment}>
          <input value={comment} onChange={(event) => setComment(event.target.value)} className="flex-1 rounded-full border border-zinc-200 px-4 py-2" placeholder="Viet binh luan..." />
          <Button type="submit" disabled={!comment.trim()}>Gui</Button>
        </form>
        <div className="mt-5 space-y-4">
          {comments.length ? (
            comments.map((item) => {
              const author = getUserById(item.authorId);
              return (
                <div key={item.id} className="flex gap-3">
                  <Avatar src={author.avatarUrl} name={author.displayName} size="sm" />
                  <div className="flex-1 rounded-2xl bg-zinc-50 p-3">
                    <div className="flex justify-between gap-3">
                      <p className="font-bold">{author.displayName}</p>
                      <p className="text-xs text-zinc-500">{formatDateTime(item.createdAt)}</p>
                    </div>
                    <p className="mt-1 text-sm text-zinc-700">{item.content}</p>
                    {item.authorId === currentUserId ? (
                      <button className="mt-2 text-xs font-bold text-red-600" onClick={() => deleteComment(item.id)}>Xoa binh luan</button>
                    ) : null}
                  </div>
                </div>
              );
            })
          ) : (
            <p className="text-sm text-zinc-500">Chua co binh luan nao.</p>
          )}
        </div>
      </div>
    </section>
  );
}
