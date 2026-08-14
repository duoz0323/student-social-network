import { LocateFixed, Search } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { useDiscoveryMap } from '../hooks/useDiscoveryMap.js';
import DiscoveryMapCanvas from './DiscoveryMapCanvas.jsx';
import DiscoveryMapPanel from './DiscoveryMapPanel.jsx';
import './DiscoveryMap.css';

// Màn hình ghép Map controls, canvas và panel responsive nhưng giữ toàn bộ state trong feature Feed.
export default function DiscoveryMap({ active }) {
  const mapState = useDiscoveryMap({ active });
  if (!active) return null;

  const hasPanel = Boolean(mapState.selectedLocation);
  return (
    <section className={`discovery-map-layout ${hasPanel ? 'discovery-map-layout--with-panel' : ''}`} aria-labelledby="discovery-map-heading">
      <h1 id="discovery-map-heading" className="sr-only">Khám phá bài viết trên bản đồ</h1>
      <div className="discovery-map-stage">
        <DiscoveryMapCanvas
          locations={mapState.locations}
          selectedLocationId={mapState.selectedLocation?.locationId}
          userCoordinates={mapState.userCoordinates}
          onViewportChanged={mapState.updateViewport}
          onLocationSelect={mapState.selectLocation}
        />

        <div className="discovery-map-controls" aria-label="Điều khiển bản đồ">
          <Button
            size="sm"
            onClick={mapState.searchViewport}
            disabled={!mapState.viewport || !mapState.viewportDirty}
          >
            <Search size={16} aria-hidden="true" />
            {mapState.markerPhase === 'loading' && !mapState.viewportDirty ? 'Đang tìm...' : 'Tìm trong khu vực này'}
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={mapState.requestMyLocation}
            disabled={mapState.geolocationPhase === 'loading'}
          >
            <LocateFixed size={16} aria-hidden="true" />
            {mapState.geolocationPhase === 'loading' ? 'Đang định vị...' : 'Vị trí của tôi'}
          </Button>
        </div>

        {mapState.markerPhase === 'empty' ? (
          <p className="discovery-map-notice" role="status">Không có địa điểm nào trong khu vực này.</p>
        ) : null}
        {mapState.markerPhase === 'error' ? (
          <div className="discovery-map-notice discovery-map-notice--error" role="alert">
            <span>{mapState.markerError}</span>
            <button type="button" className="font-bold underline" onClick={mapState.searchViewport}>Thử lại</button>
          </div>
        ) : null}
        {mapState.truncated ? (
          <p className="discovery-map-notice discovery-map-notice--warning" role="status">Khu vực này có nhiều địa điểm. Hãy phóng to để xem chính xác hơn.</p>
        ) : null}
        {mapState.geolocationMessage ? (
          <p className={`discovery-map-geo-message ${mapState.geolocationPhase === 'error' ? 'discovery-map-geo-message--error' : ''}`} role={mapState.geolocationPhase === 'error' ? 'alert' : 'status'}>
            {mapState.geolocationMessage}
          </p>
        ) : null}
      </div>
      <DiscoveryMapPanel mapState={mapState} />
    </section>
  );
}
