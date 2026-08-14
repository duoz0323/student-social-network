import { EmptyState, ErrorState, LoadingState } from '../../../components/common/StateBlock.jsx';
import { useNearbyDiscovery } from '../hooks/useNearbyDiscovery.js';
import NearbyControls from './NearbyControls.jsx';
import NearbyPostList from './NearbyPostList.jsx';

const LOCATION_ERROR_TITLES = Object.freeze({
  'permission-denied': 'Cần quyền truy cập vị trí',
  unavailable: 'Chưa thể xác định vị trí',
  timeout: 'Xác định vị trí quá lâu',
});

export default function NearbyDiscovery({ active }) {
  const nearby = useNearbyDiscovery({ active });
  if (!active) return null;

  const controls = nearby.coordinates ? (
    <NearbyControls
      radiusKm={nearby.radiusKm}
      requestingLocation={nearby.phase === 'requesting-location'}
      onRadiusChange={nearby.changeRadius}
      onRefreshLocation={nearby.refreshLocation}
    />
  ) : null;

  if (nearby.phase === 'initial' || nearby.phase === 'requesting-location' || nearby.phase === 'location-ready') {
    return <LoadingState message={nearby.phase === 'requesting-location' ? 'Đang xác định vị trí của bạn...' : 'Đang chuẩn bị bài viết gần bạn...'} />;
  }
  if (LOCATION_ERROR_TITLES[nearby.phase]) {
    return (
      <ErrorState
        title={LOCATION_ERROR_TITLES[nearby.phase]}
        description={nearby.error}
        actionLabel="Thử lại"
        onAction={nearby.retry}
      />
    );
  }

  return (
    <>
      {controls}
      {nearby.phase === 'loading' ? <LoadingState message="Đang tìm bài viết gần bạn..." /> : null}
      {nearby.phase === 'api-error' ? (
        <ErrorState title="Không thể tải bài viết gần bạn" description={nearby.error} onAction={nearby.retry} />
      ) : null}
      {nearby.phase === 'empty' ? (
        <EmptyState
          title="Chưa có bài viết trong bán kính này"
          description="Bạn có thể chủ động chọn bán kính lớn hơn để khám phá thêm."
        />
      ) : null}
      {nearby.phase === 'success' ? (
        <NearbyPostList
          posts={nearby.posts}
          paginationPhase={nearby.paginationPhase}
          loadMoreError={nearby.loadMoreError}
          loadMore={nearby.loadMore}
          retryLoadMore={nearby.retryLoadMore}
        />
      ) : null}
    </>
  );
}

