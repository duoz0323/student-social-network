import { LocateFixed } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { NEARBY_RADII_KM } from '../utils/nearbyDiscoveryState.js';

export default function NearbyControls({ radiusKm, requestingLocation, onRadiusChange, onRefreshLocation }) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--app-border-strong)] bg-[var(--app-surface)] px-4 py-3 sm:px-5">
      <fieldset className="flex min-w-0 items-center gap-2">
        <legend className="sr-only">Bán kính khám phá</legend>
        <span className="hidden text-sm font-medium text-[var(--app-muted)] sm:inline">Bán kính</span>
        <select
          value={radiusKm}
          onChange={(event) => onRadiusChange(event.target.value)}
          aria-label="Bán kính khám phá bài viết gần bạn"
          className="app-field min-h-9 rounded-[var(--radius-control)] border px-3 py-1.5 text-sm font-semibold"
        >
          {NEARBY_RADII_KM.map((radius) => <option key={radius} value={radius}>{radius} km</option>)}
        </select>
      </fieldset>
      <Button
        variant="secondary"
        size="sm"
        loading={requestingLocation}
        loadingLabel="Đang định vị..."
        onClick={onRefreshLocation}
        aria-label="Cập nhật vị trí hiện tại"
      >
        <LocateFixed size={16} aria-hidden="true" />
        Cập nhật vị trí
      </Button>
    </div>
  );
}

