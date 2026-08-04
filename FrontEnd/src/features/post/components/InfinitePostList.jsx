import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import PostCard from './PostCard.jsx';

/** Trình bày nhất quán trạng thái đầu trang, tải thêm, lỗi tải thêm và hết cursor. */
export default function InfinitePostList({
  posts,
  initialLoading,
  loadingMore,
  initialError,
  loadMoreError,
  sentinelRef,
  reload,
  retryLoadMore,
  emptyTitle,
  emptyDescription,
  errorTitle,
  onLikeChange,
  onSaveChange,
  onRepostChange,
  showRepostAttribution = true,
}) {
  if (initialLoading) {
    return <LoadingState message="Đang tải bài viết..." />;
  }
  if (initialError) {
    return (
      <EmptyState
        title={errorTitle}
        description={initialError}
        actionLabel="Thử lại"
        onAction={reload}
      />
    );
  }
  if (posts.length === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  return (
    <>
      {posts.map((post) => (
        <PostCard
          key={post.feedItemKey ?? post.id}
          post={post}
          onLikeChange={onLikeChange}
          onSaveChange={onSaveChange}
          onRepostChange={onRepostChange}
          showRepostAttribution={showRepostAttribution}
        />
      ))}
      {loadMoreError && (
        <div className="flex flex-col items-center gap-2 px-5 py-5 text-center">
          <p className="text-sm text-red-600">{loadMoreError}</p>
          <Button variant="secondary" size="sm" onClick={retryLoadMore}>Tải lại</Button>
        </div>
      )}
      {loadingMore && <p className="py-5 text-center text-sm text-[var(--app-muted)]">Đang tải thêm...</p>}
      <div ref={sentinelRef} className="h-px" aria-hidden="true" />
    </>
  );
}
