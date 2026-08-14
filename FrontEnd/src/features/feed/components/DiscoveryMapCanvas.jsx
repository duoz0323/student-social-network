import { MarkerClusterer } from '@googlemaps/markerclusterer';
import { useEffect, useRef, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import { DISCOVERY_MAP_CONFIG } from '../config/discoveryMapConfig.js';
import { loadGoogleMapsLibrary, resetGoogleMapsLoader } from '../../post/locations/googleMapsLoader.js';
import { markerAccessibleTitle, zoomToCluster } from '../utils/discoveryMapMarkers.js';

function markerIcon(google, selected = false) {
  return {
    path: google.maps.SymbolPath.CIRCLE,
    fillColor: selected ? '#dc2626' : '#18181b',
    fillOpacity: 1,
    strokeColor: '#ffffff',
    strokeWeight: 2,
    scale: selected ? 13 : 11,
  };
}

/** Adapter React mỏng quanh Google Maps; marker/cluster được dọn hoàn toàn khi dữ liệu hoặc route đổi. */
export default function DiscoveryMapCanvas({
  locations,
  selectedLocationId,
  userCoordinates,
  onViewportChanged,
  onLocationSelect,
}) {
  const containerRef = useRef(null);
  const mapRef = useRef(null);
  const clustererRef = useRef(null);
  const userMarkerRef = useRef(null);
  const [sdkPhase, setSdkPhase] = useState('loading');
  const [sdkError, setSdkError] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let canceled = false;
    let idleListener = null;

    async function initializeMap() {
      try {
        const { Map } = await loadGoogleMapsLibrary('maps');
        await loadGoogleMapsLibrary('marker');
        if (canceled || !containerRef.current) return;
        const map = new Map(containerRef.current, {
          center: DISCOVERY_MAP_CONFIG.defaultCenter,
          zoom: DISCOVERY_MAP_CONFIG.defaultZoom,
          clickableIcons: false,
          fullscreenControl: false,
          mapTypeControl: false,
          streetViewControl: false,
          gestureHandling: 'greedy',
        });
        mapRef.current = map;
        idleListener = map.addListener('idle', () => {
          const bounds = map.getBounds();
          if (bounds) onViewportChanged(bounds);
        });
        setSdkPhase('ready');
      } catch (error) {
        if (!canceled) {
          setSdkPhase('error');
          setSdkError(error.message || 'Không thể tải Google Maps.');
        }
      }
    }

    void initializeMap();
    return () => {
      canceled = true;
      idleListener?.remove();
      clustererRef.current?.clearMarkers();
      clustererRef.current = null;
      userMarkerRef.current?.setMap(null);
      userMarkerRef.current = null;
      mapRef.current = null;
    };
  }, [onViewportChanged, retryToken]);

  useEffect(() => {
    const map = mapRef.current;
    const google = window.google;
    if (sdkPhase !== 'ready' || !map || !google?.maps?.Marker) return undefined;

    clustererRef.current?.clearMarkers();
    const markers = locations.map((location) => {
      const marker = new google.maps.Marker({
        position: { lat: location.latitude, lng: location.longitude },
        title: markerAccessibleTitle(location),
        icon: markerIcon(google, location.locationId === selectedLocationId),
        label: {
          text: String(location.postCount),
          color: '#ffffff',
          fontSize: '11px',
          fontWeight: '700',
        },
      });
      marker.addListener('click', () => onLocationSelect(location));
      return marker;
    });
    clustererRef.current = new MarkerClusterer({ map, markers, onClusterClick: zoomToCluster });

    return () => {
      clustererRef.current?.clearMarkers();
      markers.forEach((marker) => marker.setMap(null));
    };
  }, [locations, onLocationSelect, sdkPhase, selectedLocationId]);

  useEffect(() => {
    const map = mapRef.current;
    const google = window.google;
    if (sdkPhase !== 'ready' || !map || !google?.maps?.Marker || !userCoordinates) return;

    userMarkerRef.current?.setMap(null);
    userMarkerRef.current = new google.maps.Marker({
      map,
      position: { lat: userCoordinates.latitude, lng: userCoordinates.longitude },
      title: 'Vị trí hiện tại của bạn',
      zIndex: 10_000,
      icon: {
        path: google.maps.SymbolPath.CIRCLE,
        fillColor: '#2563eb',
        fillOpacity: 1,
        strokeColor: '#ffffff',
        strokeWeight: 4,
        scale: 9,
      },
    });
    map.panTo(userMarkerRef.current.getPosition());
    map.setZoom(DISCOVERY_MAP_CONFIG.userLocationZoom);
  }, [sdkPhase, userCoordinates]);

  function retrySdk() {
    resetGoogleMapsLoader();
    setSdkPhase('loading');
    setSdkError('');
    setRetryToken((value) => value + 1);
  }

  return (
    <div className="discovery-map-canvas-wrap">
      <div ref={containerRef} className="discovery-map-canvas" role="application" aria-label="Bản đồ khám phá địa điểm" />
      {sdkPhase === 'loading' ? (
        <div className="discovery-map-sdk-state" role="status" aria-live="polite">Đang tải bản đồ...</div>
      ) : null}
      {sdkPhase === 'error' ? (
        <div className="discovery-map-sdk-state" role="alert">
          <strong>Không thể tải bản đồ</strong>
          <span>{sdkError}</span>
          <Button variant="secondary" size="sm" onClick={retrySdk}>Thử lại</Button>
        </div>
      ) : null}
    </div>
  );
}
