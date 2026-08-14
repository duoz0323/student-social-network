import { useCallback, useRef } from 'react';
import Button from '../../../components/common/Button.jsx';
import PostCard from '../../post/components/PostCard.jsx';
import { formatNearbyDistance } from '../utils/nearbyDiscoveryState.js';

export default function NearbyPostList({ posts, paginationPhase, loadMoreError, loadMore, retryLoadMore }) {
  const observerRef = useRef(null);
  const sentinelRef = useCallback((node) => {
    observerRef.current?.disconnect();
    if (!node || paginationPhase === 'end') return;
    observerRef.current = new IntersectionObserver((entries) => {
      if (entries[0]?.isIntersecting) loadMore();
    }, { rootMargin: '400px 0px' });
    observerRef.current.observe(node);
  }, [loadMore, paginationPhase]);

  return (
    <>
      {posts.map((post) => (
        <PostCard
          key={post.id}
          post={post}
          locationMeta={`Cách bạn ${formatNearbyDistance(post.nearbyDistanceMeters)}`}
        />
      ))}
      {loadMoreError ? (
        <div className="flex flex-col items-center gap-2 px-5 py-5 text-center" role="alert">
          <p className="text-sm text-red-600">{loadMoreError}</p>
          {paginationPhase === 'error' ? (
            <Button variant="secondary" size="sm" onClick={retryLoadMore}>Tải lại</Button>
          ) : null}
        </div>
      ) : null}
      {paginationPhase === 'loading' ? (
        <p className="py-5 text-center text-sm text-[var(--app-muted)]" role="status">Đang tải thêm...</p>
      ) : null}
      {paginationPhase === 'end' && !loadMoreError ? (
        <p className="border-t border-[var(--app-border)] px-5 py-5 text-center text-sm text-[var(--app-muted)]">Bạn đã xem hết bài viết gần đây.</p>
      ) : null}
      <div ref={sentinelRef} className="h-px" aria-hidden="true" />
    </>
  );
}
