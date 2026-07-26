import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { feedApi } from '../../../api/index.js';
import { useApp } from '../../../contexts/AppContext.jsx';
import InfinitePostList from '../../post/components/InfinitePostList.jsx';
import PostComposer from '../../post/components/PostComposer.jsx';
import { useInfinitePosts } from '../../post/hooks/useInfinitePosts.js';
import { toPostView } from '../../post/utils/postViewModel.js';

export default function FeedPage() {
  const { type = 'for-you' } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useApp();
  const [composerOpen, setComposerOpen] = useState(false);
  const feedState = useInfinitePosts({
    cacheKey: `feed:${type}`,
    request: type === 'following' ? feedApi.getFollowing : feedApi.getForYou,
    normalizePost: toPostView,
  });

  const feedTabs = (
    <div className="flex h-[var(--header-height)] justify-center gap-12">
      <button
        className={`relative px-4 text-[15px] font-bold transition ${type === 'for-you' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => navigate('/feed/for-you')}
      >
        Dành cho bạn
        {type === 'for-you' && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>
      <button
        className={`relative px-4 text-[15px] font-bold transition ${type === 'following' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => navigate('/feed/following')}
      >
        Đang theo dõi
        {type === 'following' && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>
    </div>
  );

  function closeComposer() {
    setComposerOpen(false);
    feedState.reload();
  }

  return (
    <>
      <ContentShell header={feedTabs}>
        <div className="composer-trigger flex items-center gap-4 border-b border-[var(--app-border-strong)] px-5 pb-4 pt-4">
          <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} />
          <button className="flex-1 text-left text-[15px] text-[var(--app-muted)]" onClick={() => setComposerOpen(true)}>
            Có gì mới?
          </button>
          <Button variant="secondary" className="!h-[36px] !w-[84px] !p-0 text-[15px] !font-semibold !rounded-[10px] !border-[var(--app-border-strong)]" size="sm" onClick={() => setComposerOpen(true)}>
            Đăng
          </Button>
        </div>
        <InfinitePostList
          {...feedState}
          errorTitle="Không thể tải Feed"
          emptyTitle="Feed đang trống"
          emptyDescription={type === 'following'
            ? 'Hãy theo dõi thêm bạn bè để thấy bài viết mới.'
            : 'Chưa có bài viết phù hợp để hiển thị.'}
        />
      </ContentShell>
      <PostComposer mode={composerOpen ? 'modal' : null} onClose={closeComposer} />
    </>
  );
}
