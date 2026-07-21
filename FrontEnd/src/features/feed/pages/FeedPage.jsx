import { useNavigate, useParams } from 'react-router-dom';
import { useState } from 'react';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import { EmptyState } from '../../../components/common/StateBlock.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import PostComposer from '../../post/components/PostComposer.jsx';
import PostCard from '../../post/components/PostCard.jsx';

export default function FeedPage() {
  const { type = 'for-you' } = useParams();
  const navigate = useNavigate();
  const [composerOpen, setComposerOpen] = useState(false);
  const { publicPosts, data, currentUserId, currentUser } = useApp();
  const followingIds = data.follows.filter((follow) => follow.followerId === currentUserId).map((follow) => follow.followingId);

  // For You ưu tiên bài mới và tương tác, Following chỉ lấy tác giả đang theo dõi.
  const posts =
    type === 'following'
      ? publicPosts.filter((post) => followingIds.includes(post.authorId)).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      : [...publicPosts].sort((a, b) => b.likeCount + b.commentCount - (a.likeCount + b.commentCount) || new Date(b.createdAt) - new Date(a.createdAt));

  const feedTabs = (
    <div className="flex h-[var(--header-height)] justify-center gap-12">
      <button
        className={`relative px-4 text-[15px] font-bold transition ${type === 'for-you' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => navigate('/feed/for-you')}
      >
        Dành cho bạn
        {type === 'for-you' && <span className="absolute bottom-0 left-1/2 h-[3px] w-full -translate-x-1/2 rounded-full bg-[var(--app-text)]" />}
      </button>
      <button
        className={`relative px-4 text-[15px] font-bold transition ${type === 'following' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => navigate('/feed/following')}
      >
        Đang theo dõi
        {type === 'following' && <span className="absolute bottom-0 left-1/2 h-[3px] w-full -translate-x-1/2 rounded-full bg-[var(--app-text)]" />}
      </button>
    </div>
  );

  return (
    <>
      <ContentShell header={feedTabs}>
        {/* Composer nhanh: avatar + placeholder + nút đăng */}
        <div className="flex items-center gap-4 border-b border-[var(--app-border-strong)] px-5 pb-4 pt-4">
          <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} />
          <button className="flex-1 text-left text-[15px] text-[var(--app-muted)]" onClick={() => setComposerOpen(true)}>
            Có gì mới?
          </button>
          <Button variant="secondary" className="!h-[36px] !w-[84px] !p-0 text-[15px] !font-semibold !rounded-[10px] !border-[var(--app-border-strong)]" size="sm" onClick={() => setComposerOpen(true)}>
            Đăng
          </Button>
        </div>

        {/* Danh sách bài viết */}
        <div className="pb-0">
          {posts.length ? (
            posts.map((post) => <PostCard key={post.id} post={post} />)
          ) : (
            <EmptyState title="Feed đang trống" description="Hãy tìm kiếm và theo dõi bạn bè để thấy bài viết mới." actionLabel="Đi đến tìm kiếm" onAction={() => navigate('/search')} />
          )}
        </div>
      </ContentShell>
      <PostComposer mode={composerOpen ? 'modal' : null} onClose={() => setComposerOpen(false)} />
    </>
  );
}
