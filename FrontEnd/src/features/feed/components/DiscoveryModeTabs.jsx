// Switch nội bộ giữ route Nearby công khai và tách mode Map khỏi state viewport trên URL.
export default function DiscoveryModeTabs({ mode, onChange }) {
  return (
    <div className="flex items-center gap-1 border-b border-[var(--app-border)] bg-[var(--app-surface)] px-4 py-3" role="tablist" aria-label="Chế độ khám phá">
      <button
        type="button"
        role="tab"
        aria-selected={mode === 'nearby'}
        className={`rounded-full px-4 py-2 text-sm font-semibold transition ${mode === 'nearby' ? 'bg-[var(--app-active)] text-[var(--app-active-contrast)]' : 'text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]'}`}
        onClick={() => onChange('nearby')}
      >
        Gần bạn
      </button>
      <button
        type="button"
        role="tab"
        aria-selected={mode === 'map'}
        className={`rounded-full px-4 py-2 text-sm font-semibold transition ${mode === 'map' ? 'bg-[var(--app-active)] text-[var(--app-active-contrast)]' : 'text-[var(--app-muted)] hover:bg-[var(--app-surface-soft)]'}`}
        onClick={() => onChange('map')}
      >
        Bản đồ
      </button>
    </div>
  );
}
