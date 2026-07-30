import { usePlaceAutocomplete } from './usePlaceAutocomplete.js';

export default function LocationPicker({ onSelect, onClose }) {
  const places = usePlaceAutocomplete();
  async function choose(item) {
    onSelect(await places.select(item));
    onClose?.();
  }
  return (
    <div className="mt-3 rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] p-3">
      <div className="flex gap-2">
        <input autoFocus value={places.query} onChange={(event) => places.setQuery(event.target.value)}
          placeholder="Tìm địa điểm" className="app-field min-w-0 flex-1 rounded-lg border px-3 py-2 text-sm" />
        {onClose && <button type="button" onClick={onClose} className="px-2 text-sm text-[var(--app-muted)]">Đóng</button>}
      </div>
      {places.loading && <p className="px-2 py-3 text-sm text-[var(--app-muted)]">Đang tìm địa điểm...</p>}
      {places.error && <p className="app-error mt-2 px-2 py-2 text-sm">{places.error}</p>}
      <div className="mt-1 max-h-52 overflow-y-auto">
        {places.suggestions.map((item) => (
          <button type="button" key={item.placeId} onClick={() => choose(item)}
            className="block w-full border-b border-[var(--app-border)] px-2 py-3 text-left text-sm last:border-0 hover:bg-[var(--app-surface-soft)]">
            {item.label}
          </button>
        ))}
      </div>
    </div>
  );
}
