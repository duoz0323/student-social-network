export default function SelectedLocation({ location, onRemove }) {
  if (!location) return null;
  return (
    <div className="mt-3 flex items-center gap-3 rounded-xl border border-[var(--app-border)] bg-[var(--app-surface-soft)] px-3 py-2.5">
      {/* Tách biểu tượng khỏi nội dung để địa điểm dễ nhận ra nhưng vẫn giữ giao diện gọn gàng. */}
      <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[color-mix(in_srgb,var(--app-brand)_10%,var(--app-surface))] text-[var(--app-brand)]" aria-hidden="true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" />
          <circle cx="12" cy="10" r="2.5" />
        </svg>
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-semibold leading-5 text-[var(--app-text)]">{location.displayName}</p>
        {location.formattedAddress && <p className="mt-0.5 truncate text-xs leading-4 text-[var(--app-muted)]">{location.formattedAddress}</p>}
      </div>
      {onRemove && (
        <button type="button" onClick={onRemove} aria-label="Gỡ địa điểm" title="Gỡ địa điểm"
          className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface)] hover:text-[var(--app-text)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--app-brand)]">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M18 6 6 18" />
            <path d="m6 6 12 12" />
          </svg>
        </button>
      )}
    </div>
  );
}
