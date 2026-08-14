import { X } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, ErrorState, LoadingState } from '../../../components/common/StateBlock.jsx';
import PostCard from '../../post/components/PostCard.jsx';

// Panel tái sử dụng PostCard chuẩn và không tự diễn giải postCount/cursor từ Backend.
export default function DiscoveryMapPanel({ mapState }) {
  const location = mapState.selectedLocation;
  if (!location) return null;

  return (
    <aside className="discovery-map-panel" aria-label={`Bài viết tại ${location.displayName}`}>
      <header className="sticky top-0 z-10 border-b border-[var(--app-border)] bg-[var(--app-surface)] px-4 py-4">
        <button type="button" className="float-right rounded-full p-2 text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]" onClick={mapState.closeLocation} aria-label="Đóng danh sách địa điểm">
          <X size={18} aria-hidden="true" />
        </button>
        <h2 className="pr-10 text-base font-bold text-[var(--app-text)]">{location.displayName}</h2>
        {location.formattedAddress ? <p className="mt-1 text-sm text-[var(--app-muted)]">{location.formattedAddress}</p> : null}
        <p className="mt-2 text-xs font-semibold uppercase tracking-wide text-[var(--app-muted)]">{location.postCount} bài viết</p>
      </header>

      <div className="discovery-map-panel-content">
        {mapState.panelPhase === 'loading' ? <LoadingState message="Đang tải bài viết tại địa điểm..." /> : null}
        {mapState.panelPhase === 'error' ? (
          <ErrorState title="Không thể tải bài viết" description={mapState.panelError} onAction={mapState.retryLocationPosts} />
        ) : null}
        {mapState.panelPhase === 'empty' ? (
          <EmptyState title="Chưa có bài viết tại địa điểm này" description="Các bài viết có thể đã thay đổi sau lần tải marker gần nhất." />
        ) : null}
        {mapState.panelPhase === 'success' ? (
          <>
            {mapState.posts.map((post) => <PostCard key={post.id} post={post} />)}
            {mapState.loadMoreError ? (
              <div className="grid justify-items-center gap-2 px-4 py-5 text-center">
                <p className="text-sm text-red-600">{mapState.loadMoreError}</p>
                {mapState.paginationPhase === 'error' ? <Button variant="secondary" size="sm" onClick={mapState.retryLoadMore}>Thử tải lại</Button> : null}
              </div>
            ) : null}
            {mapState.paginationPhase === 'loading' ? <p className="py-5 text-center text-sm text-[var(--app-muted)]">Đang tải thêm...</p> : null}
            {mapState.paginationPhase === 'idle' ? (
              <div className="flex justify-center px-4 py-5"><Button variant="secondary" size="sm" onClick={mapState.loadMore}>Tải thêm</Button></div>
            ) : null}
            {mapState.paginationPhase === 'end' && !mapState.loadMoreError ? <p className="py-5 text-center text-sm text-[var(--app-muted)]">Đã hiển thị hết bài viết.</p> : null}
          </>
        ) : null}
      </div>
    </aside>
  );
}
