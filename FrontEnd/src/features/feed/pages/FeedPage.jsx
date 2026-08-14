import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Avatar from '../../../components/common/Avatar.jsx';
import Button from '../../../components/common/Button.jsx';
import ContentShell from '../../../components/layout/ContentShell.jsx';
import { feedApi } from '../../../api/index.js';
import { useApp } from '../../../contexts/AppContext.jsx';
import InfinitePostList from '../../post/components/InfinitePostList.jsx';
import PostComposer from '../../post/components/PostComposer.jsx';
import { useInfinitePosts } from '../../post/hooks/useInfinitePosts.js';
import { toFeedItemView, toPostView } from '../../post/utils/postViewModel.js';
import NearbyDiscovery from '../components/NearbyDiscovery.jsx';
import DiscoveryMap from '../components/DiscoveryMap.jsx';
import DiscoveryModeTabs from '../components/DiscoveryModeTabs.jsx';

const FEED_TYPES = new Set(['for-you', 'following', 'nearby', 'map']);

export default function FeedPage() {
  const { type = 'for-you' } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useApp();
  const [composerOpen, setComposerOpen] = useState(false);
  const isNearby = type === 'nearby';
  const isMap = type === 'map';
  const isDiscovery = isNearby || isMap;
  const feedState = useInfinitePosts({
    cacheKey: `feed:${type}`,
    request: type === 'following' ? feedApi.getFollowing : feedApi.getForYou,
    normalizePost: type === 'following' ? toFeedItemView : toPostView,
    enabled: !isDiscovery,
    active: !isDiscovery,
  });

  const { refresh } = feedState;

  // Tự động cuộn lên đầu và kích hoạt load lại bài viết khi ấn Logo hoặc tab Dành cho bạn/Đang theo dõi
  useEffect(() => {
    if (!FEED_TYPES.has(type)) navigate('/feed/for-you', { replace: true });
  }, [navigate, type]);

  useEffect(() => {
    const handleRefresh = (event) => {
      const targetType = event.detail?.type;
      if (!isDiscovery && (!targetType || targetType === type)) {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        void refresh();
      }
    };
    window.addEventListener('unishare:refresh-feed', handleRefresh);
    return () => window.removeEventListener('unishare:refresh-feed', handleRefresh);
  }, [isDiscovery, refresh, type]);

  function handleTabClick(targetType) {
    if (type === targetType) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      if (!isDiscovery) void refresh();
    } else {
      navigate(`/feed/${targetType}`);
    }
  }

  const feedTabs = (
    <div className="flex h-[var(--header-height)] items-stretch justify-around gap-1 px-1 sm:justify-center sm:gap-6">
      <button
        type="button"
        className={`relative px-4 text-[15px] font-bold transition ${type === 'for-you' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => handleTabClick('for-you')}
        aria-current={type === 'for-you' ? 'page' : undefined}
      >
        Dành cho bạn
        {type === 'for-you' && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>
      <button
        type="button"
        className={`relative px-4 text-[15px] font-bold transition ${type === 'following' ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => handleTabClick('following')}
        aria-current={type === 'following' ? 'page' : undefined}
      >
        Đang theo dõi
        {type === 'following' && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>
      <button
        type="button"
        className={`relative px-3 text-[15px] font-bold transition sm:px-4 ${isDiscovery ? 'text-[var(--app-text)]' : 'text-[var(--app-muted)] hover:text-[var(--app-text)]'}`}
        onClick={() => handleTabClick('nearby')}
        aria-current={isDiscovery ? 'page' : undefined}
      >
        Gần bạn
        {isDiscovery && <span className="feed-tab-indicator absolute inset-x-0 bottom-0 h-[3px] rounded-full bg-[var(--app-text)]" />}
      </button>
    </div>
  );

  function closeComposer() {
    setComposerOpen(false);
    if (!isDiscovery) feedState.reload();
  }

  return (
    <>
      <ContentShell header={feedTabs} wide={isMap}>
        {isDiscovery ? (
          <DiscoveryModeTabs
            mode={isMap ? 'map' : 'nearby'}
            onChange={(mode) => navigate(`/feed/${mode}`)}
          />
        ) : null}
        <div className="composer-trigger flex items-center gap-4 border-b border-[var(--app-border-strong)] px-5 pb-4 pt-4">
          <Avatar src={currentUser.avatarUrl} name={currentUser.displayName} />
          <button className="flex-1 text-left text-[15px] text-[var(--app-muted)]" onClick={() => setComposerOpen(true)}>
            Có gì mới?
          </button>
          <Button variant="secondary" className="!h-[36px] !w-[84px] !p-0 text-[15px] !font-semibold !rounded-[10px] !border-[var(--app-border-strong)]" size="sm" onClick={() => setComposerOpen(true)}>
            Đăng
          </Button>
        </div>

        {/* Spinner quay nhỏ ở giữa kiểu Threads/Instagram khi reload feed */}
        {!isDiscovery && feedState.refreshing && (
          <div className="flex items-center justify-center border-b border-[var(--app-border-strong)] py-4 text-[var(--app-muted)]">
            <svg className="h-5 w-5 animate-spin" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="Đang tải bài viết mới...">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          </div>
        )}

        {/* Giữ hook Nearby được mount để tọa độ chỉ tồn tại trong memory và không phải xin lại khi đổi tab. */}
        <NearbyDiscovery active={isNearby} />
        <DiscoveryMap active={isMap} />
        {!isDiscovery ? (
          <InfinitePostList
            {...feedState}
            errorTitle="Không thể tải Feed"
            emptyTitle="Feed đang trống"
            emptyDescription={type === 'following'
              ? 'Hãy theo dõi thêm bạn bè để thấy bài viết mới.'
              : 'Chưa có bài viết phù hợp để hiển thị.'}
          />
        ) : null}
      </ContentShell>
      <PostComposer mode={composerOpen ? 'modal' : null} onClose={closeComposer} />
    </>
  );
}
