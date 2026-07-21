import { useEffect } from 'react';

export default function Toast({ message, type = 'success', onClose, duration = 3000 }) {
  useEffect(() => {
    if (duration && onClose) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  const types = {
    success: 'border-[var(--status-active)] bg-[var(--status-active-bg)] text-[var(--status-active)]',
    error: 'border-[var(--status-blocked)] bg-[var(--status-blocked-bg)] text-[var(--status-blocked)]',
    info: 'border-[var(--app-border-strong)] bg-[var(--app-surface-soft)] text-[var(--app-text)]',
  };

  return (
    <div className={`fixed bottom-4 right-4 z-50 flex items-center gap-3 rounded-[var(--radius-card)] border px-4 py-3 shadow-lg ${types[type]}`}>
      <span className="text-sm font-semibold">{message}</span>
      {onClose && (
        <button onClick={onClose} className="ml-2 rounded-full p-1 opacity-70 transition hover:bg-black/5 hover:opacity-100">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
        </button>
      )}
    </div>
  );
}
