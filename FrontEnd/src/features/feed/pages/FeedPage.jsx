import { useNavigate, useParams } from 'react-router-dom';
import { useState } from 'react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import PostComposer from '../../post/components/PostComposer.jsx';
import PostCard from '../../post/components/PostCard.jsx';

export default function FeedPage() {
  const { type = 'for-you' } = useParams();
  const navigate = useNavigate();
  const [composerOpen, setComposerOpen] = useState(false);
  const { publicPosts, data, currentUserId, currentUser } = useApp();
  const followingIds = data.follows.filter((follow) => follow.followerId === currentUserId).map((follow) => follow.followingId);

  // For You uu tien bai moi va tuong tac, Following chi lay tac gia dang theo doi.
  const posts =
    type === 'following'
      ? publicPosts.filter((post) => followingIds.includes(post.authorId)).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      : [...publicPosts].sort((a, b) => b.likeCount + b.commentCount - (a.likeCount + a.commentCount) || new Date(b.createdAt) - new Date(a.createdAt));

  return (
    <section className="feed-shell">
      <div className="sticky top-0 z-10 grid h-[var(--header-height)] grid-cols-2 border-b border-[var(--app-border)] bg-white/95 backdrop-blur">
        <button
          className={`relative text-sm font-black ${type === 'for-you' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)]'}`}
          onClick={() => navigate('/feed/for-you')}
        >
          Danh cho ban
          {type === 'for-you' ? <span className="absolute bottom-0 left-1/2 h-0.5 w-24 -translate-x-1/2 bg-[var(--app-text)]" /> : null}
        </button>
        <button
          className={`relative text-sm font-black ${type === 'following' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)]'}`}
          onClick={() => navigate('/feed/following')}
        >
          Dang theo doi
          {type === 'following' ? <span className="absolute bottom-0 left-1/2 h-0.5 w-24 -translate-x-1/2 bg-[var(--app-text)]" /> : null}
        </button>
      </div>

      <div className="grid grid-cols-[42px_minmax(0,1fr)_auto] items-start gap-3 border-b border-[var(--app-border)] px-5 py-4">
        <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} />
        <button className="min-h-10 text-left text-[15px] text-[var(--app-muted)]" onClick={() => setComposerOpen(true)}>
          Co gi moi?
        </button>
        <Button className="min-h-9 px-5 text-sm font-black" size="sm" onClick={() => setComposerOpen(true)}>
          Dang
        </Button>
        <button
          className="col-start-2 w-fit rounded-md px-1 py-1 text-[15px] text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]"
          onClick={() => setComposerOpen(true)}
          aria-label="Them anh vao bai viet"
        >
          ▣
        </button>
      </div>

      <div className="pb-20">
        {posts.length ? (
          posts.map((post) => <PostCard key={post.id} post={post} />)
        ) : (
          <EmptyState title="Feed dang trong" description="Hay tim kiem va theo doi ban be de thay bai viet moi." actionLabel="Di den tim kiem" onAction={() => navigate('/search')} />
        )}
      </div>
      <PostComposer open={composerOpen} onClose={() => setComposerOpen(false)} />
    </section>
  );
}
