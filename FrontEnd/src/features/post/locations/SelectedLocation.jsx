export default function SelectedLocation({ location, onRemove }) {
  if (!location) return null;
  return (
    <div className="mt-3 flex items-start justify-between gap-3 rounded-xl border border-[var(--app-border)] px-3 py-2.5">
      <div className="min-w-0">
        <p className="truncate text-sm font-semibold text-[var(--app-text)]">📍 {location.displayName}</p>
        {location.formattedAddress && <p className="truncate text-xs text-[var(--app-muted)]">{location.formattedAddress}</p>}
      </div>
      {onRemove && <button type="button" onClick={onRemove} aria-label="Gỡ địa điểm" className="text-[var(--app-muted)] hover:text-[var(--app-text)]">×</button>}
    </div>
  );
}
